package care.better.tools.aqlidea.aql

import com.intellij.psi.CustomHighlighterTokenType.*
import org.junit.Test
import kotlin.test.assertEquals

class CustomAqlLexerTest {

    @Test
    fun parseBasic() {
        val lexer = CustomAqlLexer()
        val tokens = parseTokenList(lexer, "select c from composition c")

        assertEquals(
            listOf("select", " ", "c", " ", "from", " ", "composition", " ", "c"),
            tokens.map { it.text })
        assertEquals(
            listOf(
                KEYWORD_1, WHITESPACE, IDENTIFIER, WHITESPACE, KEYWORD_1, WHITESPACE,
                KEYWORD_2, WHITESPACE, IDENTIFIER
            ),
            tokens.map { it.type })
    }


}