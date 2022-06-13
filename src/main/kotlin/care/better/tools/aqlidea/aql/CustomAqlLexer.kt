package care.better.tools.aqlidea.aql

import care.better.tools.aqlidea.plugin.editor.AqlTextTokenTypes
import com.intellij.ide.highlighter.custom.AbstractCustomLexer
import com.intellij.ide.highlighter.custom.tokens.PrefixedTokenParser
import com.intellij.ide.highlighter.custom.tokens.QuotedStringParser
import com.intellij.ide.highlighter.custom.tokens.TokenParser
import com.intellij.psi.tree.IElementType

class CustomAqlLexer : AbstractCustomLexer(buildTokenParsers()) {

    companion object {
        private fun buildTokenParsers(): List<TokenParser> {
            val result = mutableListOf<TokenParser>()
            result += WhitespaceParser()
            result += LineCommentParser("--", false)
            result += AqlKeywordTokenParser()
            result += QuotedStringParser("'", AqlTextTokenTypes.AQL_STRING, true)
            result += SymbolParser()
            result += NumberParser()
            result += AqlIdentifierTokenParser()
            result += BraceTokenParser.getBraces()
            result += BraceTokenParser.getParens()
            result += BraceTokenParser.getBrackets()
            return result
        }

        val aqlKeywords = setOf(
            "all_versions", "and", "as", "asc", "ascending", "contains", "distinct", "desc", "descending",
            "exists", "from", "group", "by", "having", "like", "not", "null", "offset", "limit", "or", "order",
            "select", "top", "union", "where", "xor"
        )
        val aqlRmTypes = setOf(
            "ehr", "version", "versioned_object",
            "composition", "observation", "evaluation", "section", "cluster", "admin_entry",
            "element", "instruction", "action"
        )
    }

    private class AqlKeywordTokenParser : TokenParser() {
        private val keywordParser = AqlKeywordParser(keywordsByType, true)

        override fun hasToken(position: Int): Boolean {
            return keywordParser.hasToken(position, myBuffer, myTokenInfo)
        }

        companion object {
            private val keywordsByType = listOf(
                AqlTextTokenTypes.AQL_KEYWORD to aqlKeywords,
                AqlTextTokenTypes.AQL_RM_TYPE to aqlRmTypes
            )
        }
    }

    private class AqlIdentifierTokenParser : TokenParser() {
        override fun hasToken(position: Int): Boolean {
            if (!Character.isJavaIdentifierStart(myBuffer[position])) return false
            var currentPos = position
            while (++currentPos < myEndOffset) {
                if (!Character.isJavaIdentifierPart(myBuffer[currentPos])) break
            }
            val tokenType = AqlTextTokenTypes.AQL_IDENTIFIER
            myTokenInfo.updateData(position, currentPos, tokenType)
            return true
        }
    }

    private class WhitespaceParser : TokenParser() {
        override fun hasToken(position: Int): Boolean {
            var pos = position
            if (!Character.isWhitespace(myBuffer[pos])) return false
            val start = pos
            pos++
            while (pos < myEndOffset && Character.isWhitespace(myBuffer[pos])) {
                pos++
            }
            myTokenInfo.updateData(start, pos, AqlTextTokenTypes.AQL_WHITESPACE)
            return true
        }
    }

    private class LineCommentParser(prefix: String, private val myAtStartOnly: Boolean) :
        PrefixedTokenParser(prefix, AqlTextTokenTypes.AQL_COMMENT) {

        override fun hasToken(position: Int): Boolean {
            return if (myAtStartOnly && position > 0 && myBuffer[position - 1] != '\n') {
                false
            } else super.hasToken(position)
        }

        override fun getTokenEnd(position: Int): Int {
            var p = position
            while (p < myEndOffset) {
                if (myBuffer[p] == '\n') break
                p++
            }
            return p
        }
    }

    private class SymbolParser : TokenParser() {
        private val symbols = ".,:;/-_+=<>!"

        override fun hasToken(position: Int): Boolean {
            val c = myBuffer[position]
            if (symbols.indexOf(c) >= 0) {
                myTokenInfo.updateData(position, position + 1, AqlTextTokenTypes.AQL_SYMBOL)
                return true
            }
            return false
        }
    }

    private class NumberParser() : TokenParser() {

        override fun hasToken(position: Int): Boolean {
            var pos = position
            val start = pos
            val startChar = myBuffer[start]
            if (!isDigit(startChar)) return false
            pos++
            while (pos < myEndOffset) {
                if (!isDigit(myBuffer[pos])) break
                pos++
            }
            if (pos < myEndOffset && myBuffer[pos] == '.') {
                val dotPosition = pos
                pos++
                if (pos < myEndOffset && !isDigit(myBuffer[pos])) {
                    pos = dotPosition
                } else {
                    // after decimal point
                    while (pos < myEndOffset) {
                        if (!isDigit(myBuffer[pos])) break
                        pos++
                    }
                }
            }
            myTokenInfo.updateData(start, pos, AqlTextTokenTypes.AQL_NUMBER)
            return true
        }


        companion object {
            fun isDigit(c: Char): Boolean {
                return c in '0'..'9'
            }
        }
    }

    private class BraceTokenParser(prefix: String?, tokenType: IElementType?) :
        PrefixedTokenParser(prefix, tokenType) {
        override fun getTokenEnd(position: Int): Int {
            return position
        }

        companion object {
            fun getBraces(): List<BraceTokenParser> = listOf(
                BraceTokenParser("{", AqlTextTokenTypes.AQL_SYMBOL_BRACE),
                BraceTokenParser("}", AqlTextTokenTypes.AQL_SYMBOL_BRACE)
            )

            fun getParens(): List<BraceTokenParser> = listOf(
                BraceTokenParser("(", AqlTextTokenTypes.AQL_SYMBOL_PAREN),
                BraceTokenParser(")", AqlTextTokenTypes.AQL_SYMBOL_PAREN)
            )

            fun getBrackets(): List<BraceTokenParser> = listOf(
                BraceTokenParser("[", AqlTextTokenTypes.AQL_SYMBOL_BRACKET),
                BraceTokenParser("]", AqlTextTokenTypes.AQL_SYMBOL_BRACKET)
            )
        }
    }
}