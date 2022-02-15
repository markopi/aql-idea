package care.better.tools.aqlidea.aql

import care.better.tools.aqlidea.plugin.editor.AqlTextTokenTypes.AQL_IDENTIFIER
import care.better.tools.aqlidea.plugin.editor.AqlTextTokenTypes.AQL_KEYWORD
import care.better.tools.aqlidea.plugin.editor.AqlTextTokenTypes.AQL_SYMBOL
import care.better.tools.aqlidea.plugin.editor.AqlTextTokenTypes.AQL_SYMBOL_BRACKET


class AqlInfoReader private constructor(private val lexedAql: LexedAql) {
    val iterator: LexerTokenIterator

    init {
        iterator = LexerTokenIterator(lexedAql.tokens.firstOrNull())
    }

    fun getInfo(): AqlInfo {
        val variables = mutableListOf<AqlInfo.Var>()
        val columns = mutableListOf<AqlInfo.Column>()
        val varPaths = mutableListOf<AqlInfo.VarPath>()

        var clause: AqlClause? = null
        while (iterator.hasNext()) {
            val token = iterator.next()
            clause = token.updateClause(clause)
            if (clause == AqlClause.select) {
                if (isVarPathStart(token)) {
                    val column = readSelectColumn(token)
                    column.alias?.let { name ->
                        variables += AqlInfo.SelectVar(name, column)
                    }
                    varPaths += column.varPath
                    columns += column
                }
            } else if (clause == AqlClause.from) {
                if (token.type == AQL_IDENTIFIER) {
                    val pp = token.prev?.prev
                    if (pp?.type == AQL_KEYWORD && pp.text.toLowerCase() in containsKeywords) {
                        variables += readFromVariable(token)
                    }
                }
            } else if (clause == AqlClause.where || clause == AqlClause.order) {
                if (isVarPathStart(token)) {
                    val varPath = readVarPath(token, clause)
                    varPaths += varPath
                }
            }
        }
        return AqlInfo(columns, variables.associateBy { it.name }, varPaths)
    }

    private fun readFromVariable(token: LexerToken): AqlInfo.Var {
        val varName = token.text
        val rmType = token.prev!!.text
        val archetypeId = if (iterator.peek()?.matches(AQL_SYMBOL_BRACKET, "[") == true) {
            iterator.next()
            readArchetypeId()
        } else null

        return AqlInfo.FromVar(varName, rmType, archetypeId, token.start, iterator.peekLast().end)

    }

    private fun readArchetypeId(): String? {
        var next = iterator.peek()
        if ((next?.type != AQL_IDENTIFIER) || !next.text.equals("openEHR", ignoreCase = true)) {
            return null
        }
        val start = iterator.next()
        var last = start
        next = iterator.peek()
        while (next != null && isArchetypeIdPart(next)) {
            last = iterator.next()
            next = iterator.peek()
        }
        return lexedAql.aql.substring(start.start, last.end)
    }

    private fun isArchetypeIdPart(token: LexerToken): Boolean {
        if (token.type == AQL_IDENTIFIER) return true
        if (token.type == AQL_SYMBOL) {
            return token.text in archetypeIdSymbols
        }
        return false
    }

    private fun readSelectColumn(token: LexerToken): AqlInfo.Column {
        val varPath = readVarPath(token, AqlClause.select)

        val next = iterator.peek()
        val alias = if (next?.matches(AQL_KEYWORD, "as") == true) {
            iterator.next()
            if (iterator.peek()?.type == AQL_IDENTIFIER) {
                iterator.next().text
            } else null
        } else null

        return AqlInfo.Column(varPath, alias)
    }

    private fun isVarPathStart(token: LexerToken): Boolean {
        if (token.type != AQL_IDENTIFIER) return false
        val prev = token.prev ?: return true
        if (prev.end == token.start) {
            if (prev.type == AQL_SYMBOL && prev.text in aqlPathSymbols) return false
        }
        return true
    }

    private fun readVarPath(token: LexerToken, clause: AqlClause): AqlInfo.VarPath {
        val varName = token.text
        var next = iterator.peek()

        var varEnd: Int = token.end
        var aqlPath: String? = null
        if (next?.matches(AQL_SYMBOL, "/") == true) {
            var depth = 0
            val start = next
            var last: LexerToken = start
            while (next != null && (depth > 0 || isAqlPathPart(next))) {
                if (next.type == AQL_SYMBOL_BRACKET) {
                    depth += if (next.text == "[") 1 else -1
                }
                // whitespace outside brackets
                if (depth == 0 && token.start > last.end) {
                    break
                }

                last = iterator.next()
                next = iterator.peek()
            }
            varEnd = last.end
            aqlPath = lexedAql.aql.substring(start.start, last.end)
        }
        val varPath = AqlInfo.VarPath(varName, aqlPath, token.start, varEnd, clause)
        return varPath
    }

    private fun isAqlPathPart(token: LexerToken): Boolean {
        if (token.type == AQL_KEYWORD) return false
        if (token.type == AQL_SYMBOL) {
            if (token.text !in aqlPathSymbols) return false
        }
        if (token.matches(AQL_SYMBOL, ",")) return false
        return true
    }


    companion object {
        private val archetypeIdSymbols = setOf(".", "-", "_")
        private val aqlPathSymbols = setOf("/", "_")
        private val containsKeywords = setOf("contains", "from", "and", "or")

        fun readInfo(lexedAql: LexedAql): AqlInfo = AqlInfoReader(lexedAql).getInfo()
        fun readInfo(aql: String): AqlInfo = readInfo(LexedAql.of(aql, whitespaceAware = false))
    }
}


