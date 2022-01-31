package care.better.tools.aqlidea.aql

class AqlInfo(val columns: List<Column>, val vars: Map<String, Var>) {

    data class Column(val name: String, val alias: String?)
    data class Var(val name: String, val archetypeId: String?)
}