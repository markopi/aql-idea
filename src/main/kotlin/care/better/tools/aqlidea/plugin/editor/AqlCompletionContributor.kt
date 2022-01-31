package care.better.tools.aqlidea.plugin.editor

import care.better.tools.aqlidea.aql.CustomAqlLexer
import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.CustomHighlighterTokenType
import com.intellij.psi.tree.IElementType
import com.intellij.util.ProcessingContext


class AqlCompletionContributor : CompletionContributor() {
    init {
        extend(
            CompletionType.BASIC, PlatformPatterns.psiElement(AqlTextTokenTypes.AQL_TEXT),
            AqlCompletionProvider
        )
    }

    object AqlCompletionProvider : CompletionProvider<CompletionParameters>() {
        private val keywordsByClause: Map<String?, Set<String>> = mapOf(
            null to keywords("select"),
            "select" to keywords("as from count min max avg distinct"),
            "from" to keywords("contains ehr composition observation evaluation instruction action admin_entry where order+by offset limit fetch version versioned_object cluster top section union+all task_plan work_plan"),
            "where" to keywords("and or xor not exists matches order+by offset limit fetch union+all"),
            "order" to keywords("asc desc ascending descending offset limit fetch"),
            "limit" to keywords("limit fetch offset")
        )

        private fun keywords(keywords: String): Set<String> {
            return keywords.split(" ")
                .map { it.replace('+', ' ') }
                .toSet()
        }

        public override fun addCompletions(
            parameters: CompletionParameters,
            context: ProcessingContext,
            resultSet: CompletionResultSet
        ) {
            val aql = parameters.originalPosition!!.text
            val offset = parameters.offset
            val positionInfo = getPositionInfo(aql, offset)

            val keywords = keywordsByClause[positionInfo.clause] ?: return
            val actualKeywords = keywords
                .filter { it != positionInfo.previousKeyword}
                .sorted()
            actualKeywords.forEach { keyword ->
                resultSet.addElement(LookupElementBuilder.create(keyword))
            }
//
//
//            resultSet.addElement(LookupElementBuilder.create("Hello"))
        }

        fun getPositionInfo(aql: String, offset: Int): PositionInfo {
            var clause: String? = null
            var previousKeyword: Token? = null

            val lexer = CustomAqlLexer()
            lexer.start(aql)
            var tokenType: IElementType? = lexer.tokenType

            while (tokenType != null) {
                val tokenStart = lexer.tokenStart
                val tokenEnd = lexer.tokenEnd

                if (tokenStart > offset || tokenEnd > offset) {
                    return PositionInfo(clause = clause, previousKeyword = previousKeyword?.text)
                }

                if (tokenType == AqlTextTokenTypes.AQL_KEYWORD || tokenType==AqlTextTokenTypes.AQL_RM_TYPE) {
                    val tokenText = aql.substring(tokenStart, tokenEnd)
                    val tokenTextLowercase = tokenText.toLowerCase()
                    previousKeyword = Token(tokenTextLowercase, tokenStart, tokenEnd, tokenType)
                    when (tokenTextLowercase) {
                        "select", "from", "where", "order" ->
                            clause = tokenTextLowercase
                        "limit", "fetch", "offset" ->
                            clause = "limit"
                    }
                }


                lexer.advance()
                tokenType = lexer.tokenType
            }
            lexer.advance()
            return PositionInfo(clause = clause, previousKeyword = previousKeyword?.text)
        }

        data class Token(val text: String, val start: Int, val end: Int, val type: IElementType)
        data class PositionInfo(val clause: String?, val previousKeyword: String?)
    }
}