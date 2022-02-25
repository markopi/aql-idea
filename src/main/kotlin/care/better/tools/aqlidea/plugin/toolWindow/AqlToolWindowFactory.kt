package care.better.tools.aqlidea.plugin.toolWindow

import care.better.tools.aqlidea.thinkehr.ThinkEhrClient
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.ui.content.ContentFactory
import javax.swing.table.DefaultTableModel

class AqlToolWindowFactory: ToolWindowFactory {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        aqlToolWindow = AqlToolWindow()

        val contentFactory = ContentFactory.SERVICE.getInstance()
        val serversPanel = AqlServersPanel(project)
        val serversContent = contentFactory.createContent(serversPanel, "Servers", false)
        val queryResultsContent = contentFactory.createContent(aqlToolWindow.content, "Query Results", false)
        toolWindow.contentManager.addContent(serversContent)
        toolWindow.contentManager.addContent(queryResultsContent)
        toolWindow.contentManager.setSelectedContent(serversContent)
        toolWindow.activate {
            serversPanel.activate()
        }

    }

    companion object {
        const val TOOL_WINDOW_ID = "AQL"

        // todo remove from companion object
        private lateinit var aqlToolWindow: AqlToolWindow

        fun showQueryResult(project: Project, response: ThinkEhrClient.QueryResponse) {
            val toolWindow = ToolWindowManager.getInstance(project).getToolWindow(TOOL_WINDOW_ID)!!
            toolWindow.contentManager.setSelectedContent(toolWindow.contentManager.contents[1])
            toolWindow.activate {
                updateTableValues(response)
            }
        }

        fun updateTableValues(response: ThinkEhrClient.QueryResponse) {
            val model = DefaultTableModel()
            if (response.response != null) {
                response.response.resultSet.firstOrNull()
                    ?.keys
                    ?.forEach {
                        model.addColumn(it)
                    }

                for (row in response.response.resultSet) {
                    model.addRow(row.values.toTypedArray())
                }

                aqlToolWindow.showQueryTab(AqlToolWindow.QueryTab.table)
            } else {
                aqlToolWindow.showQueryTab(AqlToolWindow.QueryTab.response)

            }

            aqlToolWindow.setRawRequest(response.rawRequest)
            aqlToolWindow.setRawResponse(response.rawResponse)
            aqlToolWindow.setTableModel(model)
        }
    }
}