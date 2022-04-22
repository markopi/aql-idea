package care.better.tools.aqlidea.ui.treetable

import javax.swing.table.JTableHeader
import javax.swing.table.TableColumn
import javax.swing.table.TableColumnModel

/**
 * GroupableTableHeader
 *
 * http://www.java2s.com/Code/Java/Swing-Components/GroupableGroupHeaderExample.htm
 *
 * @version 1.0 10/20/98
 * @author Nobuo Tamemasa
 */
class GroupableTableHeader(model: TableColumnModel?) : JTableHeader(model) {
    private val columnGroups: MutableList<ColumnGroup> = mutableListOf()

    init {
        setUI(GroupableTableHeaderUI())
        setReorderingAllowed(false)
    }

    override fun updateUI() {
        setUI(GroupableTableHeaderUI())
    }

    override fun setReorderingAllowed(b: Boolean) {
        reorderingAllowed = false
    }

    fun addColumnGroup(g: ColumnGroup) {
        columnGroups.add(g)
    }

    fun getColumnGroups(col: TableColumn?): List<ColumnGroup> {
        for (columnGroup in columnGroups) {
            val v_ret = columnGroup.getColumnGroups(col, mutableListOf())
            if (v_ret != null) {
                return v_ret as List<ColumnGroup>
            }

        }
        return emptyList()
    }

    fun setColumnMargin() {
        val columnMargin = getColumnModel().columnMargin
        for (columnGroup in columnGroups) {
            columnGroup.setColumnMargin(0)
//            columnGroup.setColumnMargin(columnMargin)
        }
    }

    companion object {
        private const val uiClassID = "GroupableTableHeaderUI"
    }
}