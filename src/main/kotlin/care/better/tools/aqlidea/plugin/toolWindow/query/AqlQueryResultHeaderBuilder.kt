package care.better.tools.aqlidea.plugin.toolWindow.query

import care.better.tools.aqlidea.thinkehr.ThinkEhrQueryResponse
import care.better.tools.aqlidea.ui.treetable.TreeTableData
import care.better.tools.aqlidea.ui.treetable.TreeTableNode

class AqlQueryResultHeaderBuilder {
    fun build(response: ThinkEhrQueryResponse): TreeTableData {

        return Builder(response).build()
    }


    private class Builder(val response: ThinkEhrQueryResponse) {
        val rootHeader = BuildTreeTableNode("ROOT", -1)
        val data: MutableList<MutableList<Any?>> = mutableListOf()
        var curLeafColumns = 0

        fun build(): TreeTableData {
            for (row in response.resultSet) {
                addRow(row)
            }
            return TreeTableData(
                headers = rootHeader.children.values.map { toHeader(it) },
                data = data
            )
        }

        private fun toHeader(from: BuildTreeTableNode): TreeTableNode {
            val children = from.children.values.map { toHeader(it) }
            return TreeTableNode(from.name, from.className, from.dataColumnIndex, children)
        }

        fun addRow(row: LinkedHashMap<String, Any>) {
            val rowIndex = data.size
            for ((key, value) in row) {
                add(rootHeader, key, value, rowIndex)
            }
        }

        private fun add(
            parent: BuildTreeTableNode,
            name: String,
            data: Any?,
            rowIndex: Int
        ): Int {
            when {
                data == null -> return rowIndex
                name == "@class" -> {
                    parent.className = data as? String
                    return rowIndex
                }
                data is Map<*, *> -> {
                    val d = data as Map<String, Any?>
                    val node = parent.children.getOrPut(name) { BuildTreeTableNode(name, -1) }
                    var maxRowIndex = rowIndex
                    for ((key, value) in d) {
                        val subRowIndex = add(node, key, value, rowIndex)
                        maxRowIndex = maxRowIndex.coerceAtLeast(subRowIndex)
                    }
                    return maxRowIndex
                }
                data is List<*> -> {
                    val d = data as List<Any?>
//                    val node = parent.children.getOrPut(name) { BuildTreeTableNode(name, -1) }
                    var curRowIndex = rowIndex
                    d.forEachIndexed { index, value ->
                        curRowIndex = add(parent, name, value, curRowIndex)
                    }
                    return curRowIndex
                }
                else -> {
                    val node = parent.children.getOrPut(name) { BuildTreeTableNode(name, curLeafColumns++) }
                    val rowData = getRowData(rowIndex)
                    setColumnData(rowData, node.dataColumnIndex, data)
                    return rowIndex
                }
            }
        }

        private fun setColumnData(row: MutableList<Any?>, colIndex: Int, data: Any) {
            var size = row.size
            while (size <= colIndex) {
                row.add(null)
                size++
            }
            row[colIndex] = data
        }

        private fun getRowData(rowIndex: Int): MutableList<Any?> {
            var size = data.size
            while (size <= rowIndex) {
                data.add(mutableListOf())
                size++
            }
            return data[rowIndex]
        }
    }

    private class BuildTreeTableNode(
        val name: String,
        val dataColumnIndex: Int,
    ) {
        var className: String? = null
        val children = linkedMapOf<String, BuildTreeTableNode>()
    }

}