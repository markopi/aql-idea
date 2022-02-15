package care.better.tools.aqlidea.aql.autocomplete

import care.better.tools.aqlidea.aql.AqlClause
import care.better.tools.aqlidea.aql.CustomAqlLexer

sealed class AqlAutocompletion() {
    abstract val completion: String
    abstract val start: Int
    abstract val end: Int

    data class Keyword(val keyword: String, override val start: Int, override val end: Int) : AqlAutocompletion() {
        override val completion: String get() = keyword
    }

    data class Archetype(
        val archetypeId: String,
        val name: String,
        val varName: String,
        val rmType: String,
        override val start: Int,
        override val end: Int
    ) : AqlAutocompletion() {
        override val completion: String get() = "[$archetypeId]"
    }

    data class Path(
        val path: String,
        val type: String,
        val name: String?,
        override val start: Int,
        override val end: Int,
        val clause: AqlClause
    ) : AqlAutocompletion() {
        override val completion: String
            get() = if (name != null && clause == AqlClause.select) {
                val n = escapeName(name)
                "$path as $n"
            } else {
                path
            }

        private fun escapeName(name: String): String {
            var n = name.replace(Regex("[^\\w]"), "_")
            n = n.replace(Regex("__+"), "_")
            if (n.toLowerCase() in CustomAqlLexer.aqlKeywords) {
                n = "_$n"
            }
            return n
        }
    }
}
