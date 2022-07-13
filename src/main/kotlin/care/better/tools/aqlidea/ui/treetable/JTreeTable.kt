package care.better.tools.aqlidea.ui.treetable

import java.awt.FontMetrics
import javax.swing.JComponent
import javax.swing.JTable
import javax.swing.table.*
import kotlin.math.max

/*
 * Based on: http://www.java2s.com/Code/Java/Swing-Components/GroupableGroupHeaderExample.htm
 */
class JTreeTable private constructor(model: DefaultTableModel) : JTable(model) {
    override fun createDefaultTableHeader(): JTableHeader = GroupableTableHeader(columnModel)

    companion object {
        private fun leafHeaders(target: MutableList<TreeTableNode>, header: TreeTableNode) {
            if (header.dataIndex >= 0) target += header
            for (child in header.children) {
                leafHeaders(target, child)
            }
        }

        private fun leafHeaders(data: TreeTableData): List<TreeTableNode> {
            val target = mutableListOf<TreeTableNode>()
            for (header in data.headers) {
                leafHeaders(target, header)
            }
            return target
        }


        private fun getColumn(
            dataIndexToColumnMappings: IntArray,
            header: TreeTableNode,
            columnModel: TableColumnModel
        ): Any {
            if (header.children.isEmpty() && header.dataIndex >= 0) {
                val columnIndex = dataIndexToColumnMappings[header.dataIndex]
                return columnModel.getColumn(columnIndex)
            }
            val column = ColumnGroup(header.name)
            for (childHeader in header.children) {
                when (val childColumn = getColumn(dataIndexToColumnMappings, childHeader, columnModel)) {
                    is TableColumn -> column.add(childColumn)
                    is ColumnGroup -> column.add(childColumn)
                }
            }
            return column
        }

        fun of(data: TreeTableData): JTable {

            val leafHeaders = leafHeaders(data)
            val dataIndexToColumnMappings = IntArray(leafHeaders.size)
            leafHeaders.forEachIndexed { index, node ->
                dataIndexToColumnMappings[node.dataIndex] = index
            }

            val model = DefaultTableModel()
            val reorderedData = data.data.map { reorderDataRow(dataIndexToColumnMappings, it.toTypedArray()) }.toTypedArray()
            model.setDataVector(reorderedData, leafHeaders.map { it.name }.toTypedArray())

            val table = JTreeTable(model)
//            val table = JTable(model)

            val tableHeader = table.tableHeader as GroupableTableHeader
            for (header in data.headers) {
                val column = getColumn(dataIndexToColumnMappings, header, table.columnModel)
                if (column is ColumnGroup) {
                    tableHeader.addColumnGroup(column)
                }
            }
            ColumnSizer.setColumnSizes(table, leafHeaders, reorderedData)

            return table
        }


        private fun reorderDataRow(dataIndexToColumnMappings: IntArray, dataRow: Array<Any?>): Array<Any?> {
            val r = Array<Any?>(dataIndexToColumnMappings.size) { null }
            dataRow.forEachIndexed { index, data ->
                val column = dataIndexToColumnMappings[index]
                r[column] = data
            }
            return r
        }
    }


    private object ColumnSizer {
        private val maxPreferredWidth: Int = 600
        private val padding: Int = 10

        private fun toColumnString(data: Any?): String? {
            return data?.toString()
        }
        private fun JComponent.textWidth(text: String?): Int {
            if (text==null) return 0
            return getFontMetrics(font).stringWidth(text)
        }

        private fun columnWidths(fontMetrics: FontMetrics, data: Array<Array<Any?>>, columnIndex: Int, percentile: Int): ColumnWidths {
            if (data.isEmpty()) return ColumnWidths(0, 0, 0)

            require(percentile in 0..100)
            val columnWidths = data
                .map { it[columnIndex] }
                .map { fontMetrics.stringWidth(toColumnString(it)?:"") }
                .sorted()

            val pctIndex = (columnWidths.size * percentile / 100).coerceIn(columnWidths.indices)
            val min = columnWidths.first()
            val max = columnWidths.last()
            val preferred = columnWidths[pctIndex]
            return ColumnWidths(min, max, preferred)
        }
        fun setColumnSizes(table: JTable, leafHeaders: List<TreeTableNode>, data: Array<Array<Any?>>) {
            val dataFontMetrics = table.getFontMetrics(table.font)
            table.autoResizeMode = JTable.AUTO_RESIZE_OFF
            for (columnIndex in 0 until table.columnModel.columnCount) {
                val column = table.columnModel.getColumn(columnIndex)
                val header = leafHeaders[columnIndex]
//                column.minWidth = table.tableHeader.textWidth(header.name) + padding
                val dataWidths = columnWidths(dataFontMetrics, data, columnIndex, 100)
//                column.maxWidth = dataWidths.max.coerceAtLeast(column.minWidth)
                column.preferredWidth = (dataWidths.preferred + padding).coerceAtLeast(column.minWidth)
                    .coerceAtMost(maxPreferredWidth)
            }
        }

        private data class ColumnWidths(val min: Int, val max: Int, val preferred: Int)
    }
}