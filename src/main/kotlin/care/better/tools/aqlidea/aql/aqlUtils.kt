package care.better.tools.aqlidea.aql

import com.intellij.lexer.Lexer
import com.intellij.psi.tree.IElementType

data class LexerToken(val type: IElementType, val text: String, val start: Int, val end: Int) {
    var prev: LexerToken? = null
    var next: LexerToken? = null
}

fun parseTokenList(lexer: Lexer, text: CharSequence): List<LexerToken> {
    lexer.start(text)
    val result = mutableListOf<LexerToken>()
    var tokenType: IElementType? = lexer.tokenType
    var lastToken: LexerToken? = null
    while (tokenType != null) {
        val tokenStart = lexer.tokenStart
        val tokenEnd = lexer.tokenEnd
        val tokenText = text.substring(tokenStart, tokenEnd)
        val token = LexerToken(tokenType, tokenText, tokenStart, tokenEnd)
        token.prev = lastToken
        lastToken?.let { it.next = token }
        result += token

        lastToken = token
        lexer.advance()
        tokenType = lexer.tokenType
    }
    return result
}