package care.better.tools.aqlidea.ui.treetable

import com.intellij.ui.SideBorder
import java.awt.Color
import java.awt.Component
import java.awt.Dimension
import java.util.*
import javax.swing.JTable
import javax.swing.UIManager
import javax.swing.border.BevelBorder
import javax.swing.border.LineBorder
import javax.swing.plaf.metal.MetalBorders
import javax.swing.table.DefaultTableCellRenderer
import javax.swing.table.TableCellRenderer
import javax.swing.table.TableColumn

/**
 * http://www.java2s.com/Code/Java/Swing-Components/GroupableGroupHeaderExample.htm
 *
 * @version 1.0 10/20/98
 * @author Nobuo Tamemasa
 */
class ColumnGroup(renderer: TableCellRenderer?, text: String?) {
    private var renderer: TableCellRenderer
    private val columns: MutableList<Any> = mutableListOf()
    private var text: String?
    private var margin = 0

    constructor(text: String?) : this(null, text) {}

    init {
        if (renderer == null) {
            this.renderer = object : DefaultTableCellRenderer() {
                override fun getTableCellRendererComponent(
                    table: JTable, value: Any?,
                    isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int
                ): Component {
                    val header = table.tableHeader
                    if (header != null) {
                        foreground = header.foreground
                        background = header.background
                        font = header.font
                    }
                    horizontalAlignment = CENTER
                    setText(value?.toString() ?: "")
//                    border = UIManager.getBorder("TableHeader.cellBorder")
//                    border = LineBorder.createBlackLineBorder()
//                    border = SideBorder(Color.BLACK, SideBorder.TOP + SideBorder.LEFT)
                    this.border = MetalBorders.TableHeaderBorder()
                    return this
                }
            }
        } else {
            this.renderer = renderer
        }
        this.text = text
    }

    /**
     * @param obj    TableColumn or ColumnGroup
     */
    fun add(column: TableColumn) = columns.add(column)
    fun add(column: ColumnGroup) = columns.add(column)

    /**
     * @param c    TableColumn
     * @param g    ColumnGroups
     */
    fun getColumnGroups(c: TableColumn?, g: MutableList<Any>): MutableList<Any>? {
        g.add(this)
        if (c != null && c in columns) return g
        for (obj in columns) {
            if (obj is ColumnGroup) {
                val groups = obj.getColumnGroups(c, g.toMutableList())
                if (groups != null) return groups
            }
        }
        return null
    }

    var headerRenderer: TableCellRenderer?
        get() = renderer
        set(renderer) {
            if (renderer != null) {
                this.renderer = renderer
            }
        }
    val headerValue: Any?
        get() = text

    fun getSize(table: JTable?): Dimension {
        val comp = renderer.getTableCellRendererComponent(
            table, headerValue, false, false, -1, -1
        )
        val height = comp.preferredSize.height
        var width = 0
        for (obj in columns) {
            if (obj is TableColumn) {
                width += obj.width
                width += margin
            } else {
                width += (obj as ColumnGroup).getSize(table).width
            }
        }

        return Dimension(width, height)
    }

    fun setColumnMargin(margin: Int) {
        this.margin = margin
        for (obj in columns) {
            if (obj is ColumnGroup) {
                obj.setColumnMargin(margin)
            }
        }
    }
}