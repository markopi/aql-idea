package care.better.tools.aqlidea.aql.autocomplete

import care.better.tools.aqlidea.aql.CustomAqlLexer
import care.better.tools.aqlidea.aql.LexedAql
import care.better.tools.aqlidea.aql.parseTokenList
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue


class AqlKeywordAutocompletionProviderTest {
    private fun autocompleteKeywords(aql: String, pos: Int): List<AqlAutocompletion.Keyword> {
        val lexedAql = LexedAql.of(aql, false)
        return AqlKeywordAutocompletionProvider.getAutocompletions(lexedAql, pos)
    }

    @Test
    fun autocompleteEmpty() {
        val completions = autocompleteKeywords("", 0)
        assertEquals(
            listOf(AqlAutocompletion.Keyword("select", 0, 0)),
            completions
        )
    }

    @Test
    fun autocompleteSelect() {
        val completions = autocompleteKeywords("select c from composition c", 8)
        val keywords = completions.map { it.keyword }.toSet()
        assertTrue { "as" in keywords }
        assertTrue { "from" in keywords }
        assertTrue { "select" !in keywords }
        assertTrue { "where" !in keywords }
    }

    @Test
    fun autocompleteFrom() {
        val completions = autocompleteKeywords("select c from composition c", 14)
        val keywords = completions.map { it.keyword }.toSet()
        assertTrue { "as" !in keywords }
        assertTrue { "from" !in keywords }
        assertTrue { "select" !in keywords }
        assertTrue { "composition" in keywords }
        assertTrue { "observation" in keywords }
    }
    @Test
    fun autocompleteWhere() {
        val completions = autocompleteKeywords("select c from composition c where 1=2", 35)
        val keywords = completions.map { it.keyword }.toSet()
        assertTrue { "as" !in keywords }
        assertTrue { "from" !in keywords }
        assertTrue { "select" !in keywords }
        assertTrue { "and" in keywords }
        assertTrue { "order by" in keywords }
        assertTrue { "union all" in keywords }
    }
}