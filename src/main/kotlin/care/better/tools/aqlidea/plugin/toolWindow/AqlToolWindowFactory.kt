package care.better.tools.aqlidea.plugin.toolWindow

import care.better.tools.aqlidea.plugin.toolWindow.query.AqlQueryResultHeaderBuilder
import care.better.tools.aqlidea.plugin.toolWindow.query.AqlQueryPanel
import care.better.tools.aqlidea.plugin.toolWindow.servers.AqlServersPanel
import care.better.tools.aqlidea.thinkehr.ThinkEhrClient
import care.better.tools.aqlidea.ui.treetable.TreeTableData
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.ui.content.ContentFactory

class AqlToolWindowFactory: ToolWindowFactory {


    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val aqlToolWindow = AqlQueryPanel(project)

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
            val aqlToolWindow = toolWindow.contentManager.contents[1].component as AqlQueryPanel
            toolWindow.contentManager.setSelectedContent(toolWindow.contentManager.contents[1])
            aqlToolWindow.showQueryTab(AqlQueryPanel.QueryTab.table)
            aqlToolWindow.rawRequest = ""
            aqlToolWindow.rawResponse = "<pending>"
            aqlToolWindow.setResultTableData(null)
            toolWindow.activate { }
        }

        fun showQueryError(project: Project, e: Exception) {
            val toolWindow = getToolWindow(project)
            val aqlToolWindow = toolWindow.contentManager.contents[1].component as AqlQueryPanel
            toolWindow.contentManager.setSelectedContent(toolWindow.contentManager.contents[1])
            toolWindow.activate {
                if (e is ThinkEhrClient.ThinkEhrAqlException) {
                    aqlToolWindow.rawRequest = e.request.body ?: ""
                    aqlToolWindow.rawResponse = e.response?.body ?: ""
                    aqlToolWindow.setResultTableData(null)
                    aqlToolWindow.showQueryTab(AqlQueryPanel.QueryTab.response)
                }
                else {
                    log.error("Error calling ThinkEhr server", e)
                    aqlToolWindow.rawRequest = ""
                    aqlToolWindow. rawResponse = e.toString()
                    aqlToolWindow.setResultTableData(null)
                    aqlToolWindow.showQueryTab(AqlQueryPanel.QueryTab.response)
                }
            }
        }

        fun showQueryResult(project: Project, response: ThinkEhrClient.QueryResponse) {
            val toolWindow = getToolWindow(project)
            val aqlToolWindow = toolWindow.contentManager.contents[1].component as AqlQueryPanel
            toolWindow.contentManager.setSelectedContent(toolWindow.contentManager.contents[1])
            toolWindow.activate {
                aqlToolWindow.rawRequest = response.rawRequest.body ?: ""
                aqlToolWindow.rawResponse = response.rawResponse.body ?: ""

                updateTableValues(aqlToolWindow, response)
            }
        }

        private fun getToolWindow(project: Project) =
            ToolWindowManager.getInstance(project).getToolWindow(TOOL_WINDOW_ID)!!

        private fun updateTableValues(aqlToolWindow: AqlQueryPanel, response: ThinkEhrClient.QueryResponse) {
            val parsedResponse = response.response

            if (parsedResponse != null) {
                val treeTableData = AqlQueryResultHeaderBuilder().build(parsedResponse)
                aqlToolWindow.setResultTableData(treeTableData)

                aqlToolWindow.showQueryTab(AqlQueryPanel.QueryTab.table)
            } else {
                aqlToolWindow.setResultTableData(TreeTableData(listOf(), listOf()))
                aqlToolWindow.showQueryTab(AqlQueryPanel.QueryTab.response)

            }
        }
    }
}