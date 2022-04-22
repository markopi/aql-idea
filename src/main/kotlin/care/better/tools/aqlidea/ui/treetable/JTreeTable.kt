package care.better.tools.aqlidea.ui.treetable

import java.util.*
import javax.swing.JTable
import javax.swing.table.*

/*
 * Based on: http://www.java2s.com/Code/Java/Swing-Components/GroupableGroupHeaderExample.htm
 */
class JTreeTable private constructor(model: DefaultTableModel) : JTable(model) {
    override fun createDefaultTableHeader(): JTableHeader = GroupableTableHeader(columnModel)

    companion object {
        private fun leafHeaders(target: MutableMap<Int, TreeTableNode>, header: TreeTableNode) {
            if (header.dataIndex>=0) target[header.dataIndex]=header
            for (child in header.children) {
                leafHeaders(target, child)
            }
        }

        private fun leafHeaders(data: TreeTableData): List<TreeTableNode> {
            val target = TreeMap<Int, TreeTableNode>()
            for (header in data.headers) {
                leafHeaders(target, header)
            }
            return target.values.toList()
        }


        private fun getColumn(header: TreeTableNode, columnModel: TableColumnModel): Any {
            if (header.children.isEmpty() && header.dataIndex>=0) {
                return columnModel.getColumn(header.dataIndex)
            }
            val column = ColumnGroup(header.name)
            for (childHeader in header.children) {
                when (val childColumn = getColumn(childHeader, columnModel)) {
                    is TableColumn -> column.add(childColumn)
                    is ColumnGroup -> column.add(childColumn)
                }
            }
            return column
        }
        fun of(data: TreeTableData): JTreeTable {

            val leafHeaders = leafHeaders(data)

            val model = DefaultTableModel()
            val v = data.data.map { it.toTypedArray() }.toTypedArray()
            model.setDataVector(v, leafHeaders.map { it.name }.toTypedArray())

            val table = JTreeTable(model)

            val tableHeader = table.tableHeader as GroupableTableHeader
            for (header in data.headers) {
                val column = getColumn(header, table.columnModel)
                if (column is ColumnGroup) {
                    tableHeader.addColumnGroup(column)
                }
            }

            return table
        }
    }
}