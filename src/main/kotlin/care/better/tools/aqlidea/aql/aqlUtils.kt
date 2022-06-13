package care.better.tools.aqlidea.aql

import care.better.tools.aqlidea.plugin.editor.AqlTextTokenTypes
import care.better.tools.aqlidea.plugin.editor.AqlTextTokenTypes.AQL_KEYWORD
import care.better.tools.aqlidea.plugin.editor.AqlTextTokenTypes.AQL_RM_TYPE
import care.better.tools.aqlidea.plugin.editor.AqlTextTokenTypes.AQL_SYMBOL
import com.intellij.lexer.Lexer
import com.intellij.psi.tree.IElementType

data class LexerToken(val type: IElementType, val text: String, val start: Int, val end: Int) {
    var prev: LexerToken? = null
    var next: LexerToken? = null

    fun matches(type: IElementType, text: String): Boolean {
        if (this.type !== type) return false
        if (
            !this.text.equals(text, ignoreCase = this.type == AQL_KEYWORD || this.type == AQL_RM_TYPE)) return false
        return true
    }

    fun updateClause(currentClause: AqlClause?): AqlClause? {
        if (type !== AQL_KEYWORD) return currentClause
        return when (text.toLowerCase()) {
            "select" -> AqlClause.select
            "from" -> AqlClause.from
            "where" -> AqlClause.where
            "order" -> AqlClause.order
            "limit", "fetch", "offset" -> AqlClause.limit
            else -> currentClause
        }
    }
}

fun parseTokenList(lexer: Lexer, text: CharSequence, whitespaceAware: Boolean): List<LexerToken> {
    lexer.start(text)
    val result = mutableListOf<LexerToken>()
    var tokenType: IElementType? = lexer.tokenType
    var lastToken: LexerToken? = null
    while (tokenType != null) {
        if (!whitespaceAware && (tokenType == AqlTextTokenTypes.AQL_WHITESPACE || tokenType == AqlTextTokenTypes.AQL_COMMENT)) {
            // do not add whitespaces
        } else {
            val tokenStart = lexer.tokenStart
            val tokenEnd = lexer.tokenEnd
            val tokenText = text.substring(tokenStart, tokenEnd)
            val token = LexerToken(tokenType, tokenText, tokenStart, tokenEnd)
            token.prev = lastToken
            lastToken?.let { it.next = token }
            result += token
            lastToken = token
        }
        lexer.advance()
        tokenType = lexer.tokenType
    }
    return result
}

class LexedAql private constructor(val aql: String, val tokens: List<LexerToken>, val whitespaceAware: Boolean) {
    companion object {
        fun of(aql: String, whitespaceAware: Boolean): LexedAql {
            val tokens = parseTokenList(CustomAqlLexer(), aql, whitespaceAware)
            return LexedAql(aql, tokens, whitespaceAware)
        }
    }
}

class LexerTokenIterator(firstToken: LexerToken?) : Iterator<LexerToken> {
    private lateinit var lastToken: LexerToken
    private var nextToken = firstToken

    override fun hasNext(): Boolean = nextToken != null

    override fun next(): LexerToken = nextToken!!.also {
        lastToken = it
        nextToken = it.next
    }

    fun peekLast(): LexerToken = lastToken
    fun peek(): LexerToken? = nextToken
}

class LexedAqls private constructor(val parts: List<SingleLexedAql>) {
    class SingleLexedAql(val lexed: LexedAql, val offset: Int)

    companion object {
        fun of(aql: String): LexedAqls {
            val parts = mutableListOf<SingleLexedAql>()

            val main = LexedAql.of(aql, false)
            var lastStartSelect: LexerToken? = null
            for (token in main.tokens) {
                if (isStartSelectToken(token)) {
                    if (lastStartSelect!=null) {
                        val part = newPart(main, lastStartSelect, token.prev!!)
                        parts+=part
                    }
                    lastStartSelect=token
                }
            }
            if (lastStartSelect!=null) {
                val str = main.aql.substring(lastStartSelect.start, main.tokens.last().end)
                parts += SingleLexedAql(LexedAql.of(str, false), lastStartSelect.start)
            }
            return LexedAqls(parts)

        }

        private fun newPart(main: LexedAql, select: LexerToken,
            last: LexerToken
        ): SingleLexedAql {
            var actualLast=last
            if (actualLast.matches(AQL_SYMBOL, ";")) actualLast=actualLast.prev!!

            val str = main.aql.substring(select.start, actualLast.end)
            val part = SingleLexedAql(LexedAql.of(str, false), select.start)
            return part
        }

        private fun isStartSelectToken(token: LexerToken): Boolean {
            if (!token.matches(AQL_KEYWORD, "select")) return false
            val prev = token.prev ?: return true
            val prevPrev = prev.prev ?: return true
            if (prevPrev.matches(AQL_KEYWORD, "union") && prev.matches(AQL_KEYWORD, "all")) return false
            return true
        }

    }
}