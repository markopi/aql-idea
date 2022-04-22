package care.better.tools.aqlidea.ui.treetable

import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.JFrame
import javax.swing.JScrollPane
import javax.swing.JTable
import javax.swing.table.DefaultTableModel
import javax.swing.table.JTableHeader


object GroupableTableHeaderTest {
    @JvmStatic
    fun main(args: Array<String>) {
        val frame = JFrame("Groupable Header Example").apply {
            val dm = DefaultTableModel()
            dm.setDataVector(
                arrayOf(
                    arrayOf<Any>("119", "foo", "bar", "ja", "ko", "zh"),
                    arrayOf<Any>("911", "bar", "foo", "en", "fr", "pt")
                ), arrayOf<Any>("SNo.", "1", "2", "Native", "2", "3")
            )

            val table: JTable = object : JTable(dm) {
                override fun createDefaultTableHeader(): JTableHeader = GroupableTableHeader(columnModel)
            }

            val cm = table.columnModel
            val g_name = ColumnGroup("Name")
            g_name.add(cm.getColumn(1))
            g_name.add(cm.getColumn(2))
            val g_lang = ColumnGroup("Language")
            g_lang.add(cm.getColumn(3))
            val g_other = ColumnGroup("Others")
            g_other.add(cm.getColumn(4))
            g_other.add(cm.getColumn(5))
            g_lang.add(g_other)

            val header = table.tableHeader as GroupableTableHeader
            header.addColumnGroup(g_name)
            header.addColumnGroup(g_lang)
            val scroll = JScrollPane(table)
            contentPane.add(scroll)
            setSize(400, 120)

            addWindowListener(object : WindowAdapter() {
                override fun windowClosing(e: WindowEvent) {
                    System.exit(0)
                }
            })
        }
        frame.isVisible = true;

    }
}
