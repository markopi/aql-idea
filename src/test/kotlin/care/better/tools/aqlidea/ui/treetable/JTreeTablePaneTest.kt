package care.better.tools.aqlidea.ui.treetable

import care.better.tools.aqlidea.plugin.toolWindow.query.AqlQueryResultHeaderBuilder
import care.better.tools.aqlidea.plugin.toolWindow.query.AqlQueryResultHeaderBuilderTest
import care.better.tools.aqlidea.thinkehr.ThinkEhrQueryResponse
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.junit.Assert.*
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.JFrame
import javax.swing.JScrollPane
import javax.swing.JTable
import javax.swing.table.DefaultTableModel
import javax.swing.table.JTableHeader

object JTreeTablePaneTest {
    val objectMapper = ObjectMapper().apply {
        disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        registerModule(KotlinModule())
    }

    private fun loadResponse(name: String): ThinkEhrQueryResponse {
        return javaClass.classLoader.getResourceAsStream("care/better/tools/aqlidea/plugin/toolWindow/query/response/$name.json").use {
            objectMapper.readValue(it, ThinkEhrQueryResponse::class.java)
        }
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val frame = JFrame("Groupable Header Example").apply {
//            val resp = loadResponse("simple-scalar")
//            val resp = loadResponse("ehr_id_reordered")
            val resp = loadResponse("big")
            val ttd = AqlQueryResultHeaderBuilder().build(resp)

            val table = JTreeTable.of(ttd)
//            table.autoResizeMode = JTable.AUTO_RESIZE_OFF
//            for (columnIndex in 0 until table.columnModel.columnCount) {
//                table.columnModel.getColumn(columnIndex).preferredWidth = 100 + columnIndex * 50
////                table.columnModel.getColumn(columnIndex).minWidth = 50 + columnIndex * 20
//            }

            val scroll = JScrollPane(table)
            contentPane.add(scroll)
            setSize(800, 220)

            addWindowListener(object : WindowAdapter() {
                override fun windowClosing(e: WindowEvent) {
                    System.exit(0)
                }
            })
        }
        frame.isVisible = true;

    }
}