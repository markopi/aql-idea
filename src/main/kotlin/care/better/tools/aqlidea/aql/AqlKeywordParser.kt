package care.better.tools.aqlidea.aql

import com.intellij.ide.highlighter.custom.tokens.TokenInfo
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.CustomHighlighterTokenType
import com.intellij.psi.tree.IElementType
import com.intellij.util.containers.CharTrie
import it.unimi.dsi.fastutil.ints.IntOpenHashSet
import it.unimi.dsi.fastutil.ints.IntSet

class AqlKeywordParser(keywordSets: List<Pair<IElementType, Set<String>>>, private val myIgnoreCase: Boolean) {
    private val myKeywordSets: MutableList<Pair<IElementType, Set<String>>> = ArrayList()
    private val myTrie = CharTrie()
    private val myHashCodes: IntSet = IntOpenHashSet()

    init {
        LOG.assertTrue(keywordSets.isNotEmpty())
        for (keywordSet in keywordSets) {
            val normalized = normalizeKeywordSet(keywordSet.second)
            myKeywordSets.add(keywordSet.first to normalized)
            for (s in normalized) {
                myHashCodes.add(myTrie.getHashCode(s))
            }
        }
    }

    private fun normalizeKeywordSet(keywordSet: Set<String>): Set<String> {
        if (!myIgnoreCase) {
            return HashSet(keywordSet)
        }
        val result: MutableSet<String> = HashSet()
        for (s in keywordSet) {
            result.add(StringUtil.toUpperCase(s))
        }
        return result
    }

    fun hasToken(position: Int, myBuffer: CharSequence, tokenInfo: TokenInfo?): Boolean {
        // avoid detecting keywords as part of path or archetypeId
        if (position>0 && !myBuffer[position-1].isWhitespace()) return false

        var index = 0
        var offset = position
        var longestKeyword: String? = null
        var longestKeywordType: IElementType? = null
        while (offset < myBuffer.length) {
            val c = myBuffer[offset++]
            val nextIndex = myTrie.findSubNode(index, if (myIgnoreCase) c.toUpperCase() else c)
            if (nextIndex == 0) {
                break
            }
            index = nextIndex
            if (myHashCodes.contains(index) && isWordEnd(offset, myBuffer)) {
                val keyword = myBuffer.subSequence(position, offset).toString()
                val testKeyword = if (myIgnoreCase) StringUtil.toUpperCase(keyword) else keyword
                for (i in 0 until CustomHighlighterTokenType.KEYWORD_TYPE_COUNT) {
                    if (myKeywordSets[i].second.contains(testKeyword)) {
                        longestKeyword = testKeyword
                        longestKeywordType = myKeywordSets[i].first
                        break
                    }
                }
            }
        }
        if (longestKeyword != null && tokenInfo != null) {
            tokenInfo.updateData(position, position + longestKeyword.length, longestKeywordType)
        }
        return longestKeyword != null
    }

    companion object {
        private val LOG = Logger.getInstance(
            AqlKeywordParser::class.java
        )

        private fun isWordEnd(offset: Int, sequence: CharSequence): Boolean {
            return if (offset == sequence.length) {
                true
            } else !Character.isJavaIdentifierPart(sequence[offset - 1]) || !Character.isJavaIdentifierPart(
                sequence[offset]
            )
        }
    }
}