package care.better.tools.aqlidea.plugin.toolWindow

import care.better.tools.aqlidea.thinkehr.ThinkEhrClient
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import javax.swing.JTable
import javax.swing.table.DefaultTableModel

class AqlToolWindowFactory: ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        aqlToolWindow = AqlToolWindow()

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
        private lateinit var aqlToolWindow: AqlToolWindow

        fun updateTableValues(response: ThinkEhrClient.QueryResponse) {
            val model = DefaultTableModel()

            response.response.resultSet.firstOrNull()
                ?.keys
                ?.forEach {
                    model.addColumn(it)
                }

            for (row in response.response.resultSet) {
                model.addRow(row.values.toTypedArray())
            }

            aqlToolWindow.setRawRequest(response.rawRequest)
            aqlToolWindow.setRawResponse(response.rawResponse)
            aqlToolWindow.setTableModel(model)
            aqlToolWindow.showTableTab()
        }
    }
}