package care.better.tools.aqlidea.aql

import care.better.tools.aqlidea.plugin.editor.AqlTextTokenTypes.AQL_IDENTIFIER
import care.better.tools.aqlidea.plugin.editor.AqlTextTokenTypes.AQL_KEYWORD
import care.better.tools.aqlidea.plugin.editor.AqlTextTokenTypes.AQL_RM_TYPE
import care.better.tools.aqlidea.plugin.editor.AqlTextTokenTypes.AQL_WHITESPACE
import org.junit.Test
import kotlin.test.assertEquals

class CustomAqlLexerTest {

    @Test
    fun parseBasic() {
        val lexer = CustomAqlLexer()
        val tokens = parseTokenList(lexer, "select c from composition c", true)

        assertEquals(
            listOf("select", " ", "c", " ", "from", " ", "composition", " ", "c"),
            tokens.map { it.text })
        assertEquals(
            listOf(
                AQL_KEYWORD, AQL_WHITESPACE, AQL_IDENTIFIER, AQL_WHITESPACE, AQL_KEYWORD, AQL_WHITESPACE,
                AQL_RM_TYPE, AQL_WHITESPACE, AQL_IDENTIFIER
            ),
            tokens.map { it.type })
    }

    @Test
    fun parseSkipWhitespace() {
        val lexer = CustomAqlLexer()
        val tokens = parseTokenList(lexer, "select c from composition c", false)

        assertEquals(
            listOf("select", "c", "from", "composition", "c"),
            tokens.map { it.text })
        assertEquals(
            listOf(AQL_KEYWORD, AQL_IDENTIFIER, AQL_KEYWORD, AQL_RM_TYPE, AQL_IDENTIFIER),
            tokens.map { it.type })
    }


}