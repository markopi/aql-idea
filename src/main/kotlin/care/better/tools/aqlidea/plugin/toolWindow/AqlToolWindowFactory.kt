package care.better.tools.aqlidea.plugin.toolWindow

import care.better.tools.aqlidea.thinkehr.ThinkEhrClient
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.ui.content.ContentFactory
import javax.swing.table.DefaultTableModel

class AqlToolWindowFactory: ToolWindowFactory {


    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val aqlToolWindow = AqlToolWindow()

        val contentFactory = ContentFactory.SERVICE.getInstance()
        val serversPanel = AqlServersPanel(project)
        val serversContent = contentFactory.createContent(serversPanel, "Servers", false)
        val queryResultsContent = contentFactory.createContent(aqlToolWindow, "Query Results", false)

        toolWindow.contentManager.addContent(serversContent)
        toolWindow.contentManager.addContent(queryResultsContent)
        toolWindow.contentManager.setSelectedContent(serversContent)
        toolWindow.activate {
            serversPanel.activate()
        }

    }

    companion object {
        const val TOOL_WINDOW_ID = "AQL"

        private val log: Logger = Logger.getInstance(AqlToolWindowFactory::class.java)

        fun showQueryStart(project: Project) {
            val toolWindow = getToolWindow(project)
            val aqlToolWindow = toolWindow.contentManager.contents[1].component as AqlToolWindow
            toolWindow.contentManager.setSelectedContent(toolWindow.contentManager.contents[1])
            aqlToolWindow.showQueryTab(AqlToolWindow.QueryTab.table)
            aqlToolWindow.setRawRequest(null)
            aqlToolWindow.setRawResponse("<pending>")
            aqlToolWindow.setTableModel(DefaultTableModel())
            toolWindow.activate { }
        }

        fun showQueryError(project: Project, e: Exception) {
            val toolWindow = getToolWindow(project)
            val aqlToolWindow = toolWindow.contentManager.contents[1].component as AqlToolWindow
            toolWindow.contentManager.setSelectedContent(toolWindow.contentManager.contents[1])
            toolWindow.activate {
                if (e is ThinkEhrClient.ThinkEhrAqlException) {
                    aqlToolWindow.setRawRequest(e.request.body)
                    aqlToolWindow.setRawResponse(e.response?.body)
                    aqlToolWindow.setTableModel(DefaultTableModel())
                    aqlToolWindow.showQueryTab(AqlToolWindow.QueryTab.response)
                }
                else {
                    log.error("Error calling ThinkEhr server", e)
                    aqlToolWindow.setRawRequest("")
                    aqlToolWindow.setRawResponse(e.toString())
                    aqlToolWindow.setTableModel(DefaultTableModel())
                    aqlToolWindow.showQueryTab(AqlToolWindow.QueryTab.response)
                }
            }
        }

        fun showQueryResult(project: Project, response: ThinkEhrClient.QueryResponse) {
            val toolWindow = getToolWindow(project)
            val aqlToolWindow = toolWindow.contentManager.contents[1].component as AqlToolWindow
            toolWindow.contentManager.setSelectedContent(toolWindow.contentManager.contents[1])
            toolWindow.activate {
                aqlToolWindow.setRawRequest(response.rawRequest.body)
                aqlToolWindow.setRawResponse(response.rawResponse.body)
                updateTableValues(aqlToolWindow, response)
            }
        }

        private fun getToolWindow(project: Project) =
            ToolWindowManager.getInstance(project).getToolWindow(TOOL_WINDOW_ID)!!

        private fun updateTableValues(aqlToolWindow: AqlToolWindow, response: ThinkEhrClient.QueryResponse) {

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

            aqlToolWindow.setTableModel(model)
        }
    }
}