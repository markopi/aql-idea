package care.better.tools.aqlidea.ui.treetable

import com.intellij.ui.SideBorder
import java.awt.*
import java.util.*
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JTable
import javax.swing.UIManager
import javax.swing.border.BevelBorder
import javax.swing.border.LineBorder
import javax.swing.plaf.basic.BasicTableHeaderUI
import javax.swing.plaf.metal.MetalBorders
import javax.swing.table.DefaultTableCellRenderer
import javax.swing.table.TableColumn

/*
 * (swing1.1beta3)
 *
 */ /*
 * http://www.java2s.com/Code/Java/Swing-Components/GroupableGroupHeaderExample.htm
 *
 */
class GroupableTableHeaderUI : BasicTableHeaderUI() {
    override fun paint(g: Graphics, c: JComponent) {
        val clipBounds = g.clipBounds
        if (header.columnModel == null) return
        (header as GroupableTableHeader).setColumnMargin()
        var column = 0
        val size = header.size
        val cellRect = Rectangle(0, 0, size.width, size.height)
        val h = Hashtable<Any?, Any?>()
        val columnMargin = header.columnModel.columnMargin
        val enumeration: Enumeration<*> = header.columnModel.columns
        while (enumeration.hasMoreElements()) {
            cellRect.height = size.height
            cellRect.y = 0
            val aColumn = enumeration.nextElement() as TableColumn
            val cGroups = (header as GroupableTableHeader).getColumnGroups(aColumn)
                var groupHeight = 0
                for (cGroup in cGroups) {
                    var groupRect = h[cGroup] as Rectangle?
                    if (groupRect == null) {
                        groupRect = Rectangle(cellRect)
                        val d = cGroup.getSize(header.table)
                        groupRect.width = d.width
                        groupRect.height = d.height
                        h[cGroup] = groupRect
                    }
                    paintCell(g, groupRect, cGroup)
                    groupHeight += groupRect.height
                    cellRect.height = size.height - groupHeight
                    cellRect.y = groupHeight
                }
            cellRect.width = aColumn.width + columnMargin - columnMargin
            if (cellRect.intersects(clipBounds)) {
                paintCell(g, cellRect, column)
            }
            cellRect.x += cellRect.width
            column++
        }
    }

    private fun paintCell(g: Graphics, cellRect: Rectangle, columnIndex: Int) {
        val aColumn = header.columnModel.getColumn(columnIndex)
        var renderer = aColumn.headerRenderer
        //revised by Java2s.com
        renderer = object : DefaultTableCellRenderer() {
            override fun getTableCellRendererComponent(
                table: JTable,
                value: Any,
                isSelected: Boolean,
                hasFocus: Boolean,
                row: Int,
                column: Int
            ): Component {
                val header = JLabel()
                header.font = table.tableHeader.font
                header.horizontalAlignment = CENTER
                header.verticalAlignment = BOTTOM
                header.text = value.toString()
                header.foreground = table.tableHeader.foreground
                header.background = table.tableHeader.background
//                header.border = UIManager.getBorder("TableHeader.cellBorder")
//                header.border = LineBorder.createBlackLineBorder()
//                header.border = BevelBorder(BevelBorder.RAISED, Color.BLACK, Color.DARK_GRAY)
//                header.border = SideBorder(Color.BLACK, SideBorder.TOP + SideBorder.LEFT)
                header.border = MetalBorders.TableHeaderBorder()

                return header
            }
        }
        val c = renderer.getTableCellRendererComponent(
            header.table, aColumn.headerValue, false, false, -1, columnIndex
        )
        c.background = UIManager.getColor("control")
        rendererPane.add(c)
        rendererPane.paintComponent(
            g, c, header, cellRect.x, cellRect.y,
            cellRect.width, cellRect.height, true
        )
    }

    private fun paintCell(g: Graphics, cellRect: Rectangle, cGroup: ColumnGroup) {
        val renderer = cGroup.headerRenderer
        //revised by Java2s.com
        // if(renderer == null){
//      return ;
        //    }
        val component = renderer!!.getTableCellRendererComponent(
            header.table, cGroup.headerValue, false, false, -1, -1
        )
        rendererPane.add(component)
        rendererPane.paintComponent(
            g, component, header, cellRect.x, cellRect.y,
            cellRect.width, cellRect.height, true
        )
    }

    //revised by Java2s.com
    private val headerHeight: Int
        get() {
            var height = 0
            val columnModel = header.columnModel
            for (column in 0 until columnModel.columnCount) {
                val aColumn = columnModel.getColumn(column)
                val renderer = aColumn.headerRenderer ?: header.defaultRenderer
                val component = renderer.getTableCellRendererComponent(
                    header.table, aColumn.headerValue, false, false, -1, column)
                var cHeight = component.height

//                var cHeight = aColumn.headerRenderer
//                    ?.getTableCellRendererComponent(
//                        header.table, aColumn.headerValue, false, false, -1, column)
//                    ?.preferredSize?.height
//                    ?: 120

//                val renderer = aColumn.headerRenderer ?: return 60
//                //revised by Java2s.com
//                val comp = renderer.getTableCellRendererComponent(
//                    header.table, aColumn.headerValue, false, false, -1, column
//                )
//                var cHeight = comp.preferredSize.height
                val cGroups = (header as GroupableTableHeader).getColumnGroups(aColumn)
                for (cGroup in cGroups) {
                    cHeight += cGroup.getSize(header.table).height
                }
                height = Math.max(height, cHeight)
            }
            return height
        }

    private fun createHeaderSize(width: Long): Dimension {
        var width = width
        val columnModel = header.columnModel
        width += (columnModel.columnMargin * columnModel.columnCount).toLong()
        if (width > Int.MAX_VALUE) {
            width = Int.MAX_VALUE.toLong()
        }
        return Dimension(width.toInt(), headerHeight)
    }

    override fun getPreferredSize(c: JComponent): Dimension {
        var width: Long = 0
        val enumeration: Enumeration<*> = header.columnModel.columns
        while (enumeration.hasMoreElements()) {
            val aColumn = enumeration.nextElement() as TableColumn
            width = width + aColumn.preferredWidth
        }
        return createHeaderSize(width)
    }
}