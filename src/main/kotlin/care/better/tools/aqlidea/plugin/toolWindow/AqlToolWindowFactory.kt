package care.better.tools.aqlidea.plugin.toolWindow

import care.better.tools.aqlidea.thinkehr.ThinkEhrQueryResponse
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import javax.swing.JTable
import javax.swing.table.DefaultTableModel

class AqlToolWindowFactory: ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val aqlToolWindow = AqlToolWindow()
        table = aqlToolWindow.table1
        val contentFactory = ContentFactory.SERVICE.getInstance()
        val content = contentFactory.createContent(aqlToolWindow.content, "Query Results", false)
        toolWindow.contentManager.addContent(content)
        toolWindow.activate {

        }
//            var contentFactory: ContentFactory = ContentFactory.SERVICE.getInstance()!!
//        var content: com.intellij.ui.content.Content? = contentFactory.createContent(myToolWindow.getContent(), "", false)
//        toolWindow.getContentManager().addContent(content)

    }

    companion object {
        private lateinit var table: JTable

        fun updateTableValues(response: ThinkEhrQueryResponse) {
            val model = DefaultTableModel()

            response.resultSet.firstOrNull()
                ?.keys
                ?.forEach {
                    model.addColumn(it)
                }

            for (row in response.resultSet) {
                model.addRow(row.values.toTypedArray())
            }
            table.model = model
        }
    }
}