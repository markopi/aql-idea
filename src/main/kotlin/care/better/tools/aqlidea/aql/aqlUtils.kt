package care.better.tools.aqlidea.aql

import care.better.tools.aqlidea.plugin.editor.AqlTextTokenTypes
import care.better.tools.aqlidea.plugin.editor.AqlTextTokenTypes.AQL_KEYWORD
import care.better.tools.aqlidea.plugin.editor.AqlTextTokenTypes.AQL_RM_TYPE
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
        return when (val keyword = text.toLowerCase()) {
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