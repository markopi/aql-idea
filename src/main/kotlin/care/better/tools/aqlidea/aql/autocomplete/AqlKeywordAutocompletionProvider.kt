package care.better.tools.aqlidea.aql.autocomplete

import care.better.tools.aqlidea.aql.AqlClause
import care.better.tools.aqlidea.aql.LexedAql
import care.better.tools.aqlidea.aql.LexerToken
import care.better.tools.aqlidea.plugin.editor.AqlTextTokenTypes.AQL_KEYWORD
import care.better.tools.aqlidea.plugin.editor.AqlTextTokenTypes.AQL_RM_TYPE

object AqlKeywordAutocompletionProvider {
    private val keywordsByClause: Map<AqlClause?, Set<String>> = mapOf(
        null to keywords("select"),
        AqlClause.select to keywords("as from count min max avg distinct"),
        AqlClause.from to keywords("contains ehr composition observation evaluation instruction action admin_entry where order+by offset limit fetch version versioned_object cluster top section union+all task_plan work_plan"),
        AqlClause.where to keywords("and or xor not exists matches order+by offset limit fetch union+all"),
        AqlClause.order to keywords("asc desc ascending descending offset limit fetch"),
        AqlClause.limit to keywords("limit fetch offset")
    )

    private fun keywords(keywords: String): Set<String> {
        return keywords.split(" ")
            .map { it.replace('+', ' ') }
            .toSortedSet()
    }

    fun getAutocompletions(lexedAql: LexedAql, cursorOffset: Int): List<AqlAutocompletion.Keyword> {
        val pos = getPositionInfo(lexedAql.tokens, cursorOffset)
        val keywords = keywordsByClause[pos.clause] ?: return listOf()

        val actualKeywords = keywords
            .filter { it != pos.previousKeyword?.text }

        val result = actualKeywords.map { keyword ->
            AqlAutocompletion.Keyword(
                keyword,
                pos.start,
                pos.end
            )
        }
        return result
    }

    private fun getPositionInfo(tokens: List<LexerToken>, cursorOffset: Int): PositionInfo {
        var previousKeyword: LexerToken? = null
        var token = tokens.firstOrNull()
        var clause: AqlClause? = null
        while (token != null) {
            if (token.start > cursorOffset) {
                return PositionInfo(clause, previousKeyword, null, cursorOffset, cursorOffset)
            }
            if (token.end > cursorOffset) {
                return PositionInfo(clause, previousKeyword, token, token.start, token.end)
            }
            if (token.type == AQL_KEYWORD || token.type == AQL_RM_TYPE) {
                previousKeyword = token
            }
            clause = token.updateClause(clause)
            token = token.next
        }
        return PositionInfo(clause, previousKeyword, null, cursorOffset, cursorOffset)
    }

    private data class PositionInfo(val clause: AqlClause?, val previousKeyword: LexerToken?, val current: LexerToken?, val start: Int, val end: Int)

}