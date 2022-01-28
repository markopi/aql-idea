package care.better.tools.aqlidea.aql

import com.intellij.ide.highlighter.custom.AbstractCustomLexer
import com.intellij.ide.highlighter.custom.tokens.*
import com.intellij.psi.CustomHighlighterTokenType

class CustomAqlLexer : AbstractCustomLexer(buildTokenParsers()) {

    companion object {
        fun buildTokenParsers(): List<TokenParser> {
            val result = mutableListOf<TokenParser>()
            result += WhitespaceParser()
            result += LineCommentParser("--", false)
            result += AqlKeywordTokenParser()
            result += QuotedStringParser("'", CustomHighlighterTokenType.STRING, true)
            result += PunctuationParser()
            result += NumberParser("", true)
            result += AqlIdentifierTokenParser()
            result += BraceTokenParser.getBraces()
            result += BraceTokenParser.getParens()
            result += BraceTokenParser.getBrackets()
            return result
        }
    }

    private class AqlKeywordTokenParser : TokenParser() {
        private val keywordParser = KeywordParser(
            listOf(
                setOf(
                    "all_versions", "and", "as", "asc", "ascending", "contains", "distinct", "desc", "descending",
                    "exists", "from", "group", "by", "having", "like", "not", "null", "offset", "limit", "or", "order",
                    "select", "top", "union", "where", "xor"
                ), setOf(
                    "ehr", "version", "versioned_object",
                    "composition", "observation", "evaluation", "section", "cluster", "admin_entry",
                    "element", "instruction", "action"
                ), setOf(), setOf()
            ),
            true
        )

        override fun hasToken(position: Int): Boolean {
            return keywordParser.hasToken(position, myBuffer, myTokenInfo)
        }
    }

    private class AqlIdentifierTokenParser : TokenParser() {
        override fun hasToken(position: Int): Boolean {
            if (!Character.isJavaIdentifierStart(myBuffer[position])) return false
            var currentPos = position
            while (++currentPos < myEndOffset) {
                if (!Character.isJavaIdentifierPart(myBuffer[currentPos])) break
            }
            val tokenType = CustomHighlighterTokenType.IDENTIFIER
            myTokenInfo.updateData(position, currentPos, tokenType)
            return true
        }
    }

}