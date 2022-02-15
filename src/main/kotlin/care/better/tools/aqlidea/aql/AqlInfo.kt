package care.better.tools.aqlidea.aql

enum class AqlClause {
    select, from, where, order, limit
}

class AqlInfo(val columns: List<Column>, val vars: Map<String, Var>, val varPaths: List<VarPath>) {

    data class Column(val varPath: VarPath, val alias: String?)

    sealed class Var {
        abstract val name: String
    }
    data class SelectVar(override val name: String, val column: Column): Var()
    data class FromVar(override val name: String, val rmType: String, val predicate: String?, val start: Int, val end: Int): Var()

    data class VarPath(val varName: String, val path: String?, val start: Int, val end: Int, val clause: AqlClause)
}