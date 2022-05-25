package care.better.tools.aqlidea.plugin.toolWindow.servers

import care.better.tools.aqlidea.plugin.AqlUtils
import care.better.tools.aqlidea.plugin.console.AqlRootType
import care.better.tools.aqlidea.plugin.settings.AqlServersPersistentState
import care.better.tools.aqlidea.plugin.toolWindow.AqlToolWindowFactory
import com.intellij.execution.console.ConsoleHistoryController
import com.intellij.icons.AllIcons
import com.intellij.ide.scratch.ScratchFileService
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Splitter
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBScrollPane
import java.awt.BorderLayout
import java.awt.Component
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.DefaultListModel
import javax.swing.JList
import javax.swing.JPanel
import javax.swing.ListCellRenderer


@Suppress("UNCHECKED_CAST")
class AqlServersPanel(private val project: Project) : JPanel() {

    init {
        createGui()
    }

    private val stateService = AqlServersPersistentState.getService()

    private lateinit var currentModel: AqlServersConfiguration

    private lateinit var actionToolbar: ActionToolbar
    private lateinit var dataSourcesList: JBList<AqlServer>
    private lateinit var dataSourcesListModel: DefaultListModel<AqlServer>

    private fun createGui() {
        layout = BorderLayout()
        actionToolbar = createActionToolbar()
        add(actionToolbar.component, BorderLayout.WEST)

        dataSourcesList = JBList<AqlServer>()
        dataSourcesListModel = DefaultListModel()
        dataSourcesList.model = dataSourcesListModel
        dataSourcesList.cellRenderer = AqlServerListCellRenderer()

        dataSourcesList.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (e.clickCount == 2) {
//                    val clickIndex = dataSourcesList.locationToIndex(e.point)
                    val listIndex = dataSourcesList.selectedIndex
                    if (listIndex in 0 until currentModel.servers.size) {
                        val aqlServer = currentModel.servers[listIndex]
                        openServerConsole(aqlServer)

//                        populateDataSourcesList(currentModel)
                    }
                }
                super.mouseClicked(e)
            }
        })

        val splitPanel = Splitter()
        splitPanel.firstComponent = JBScrollPane(dataSourcesList)
        splitPanel.secondComponent = JPanel()
        add(splitPanel, BorderLayout.CENTER)
    }

    private fun openServerConsole(server: AqlServer) {

        val contentFile = ConsoleHistoryController.getContentFile(
            AqlRootType.INSTANCE,
            server.id,
            ScratchFileService.Option.create_if_missing
        )!!
        contentFile.putUserData(AqlUtils.KEY_AQL_SERVER_ID, server.id)

        FileEditorManager.getInstance(project).openFile(contentFile, true)

    }

    fun activate() {
        currentModel = stateService.readState()
        populateDataSourcesList(currentModel)
    }

    private fun populateDataSourcesList(model: AqlServersConfiguration) {
        dataSourcesListModel.clear()
        dataSourcesListModel.addAll(model.servers)
    }

    private fun createActionToolbar(): ActionToolbar {
        val action = object : AnAction("Configure", "Configure data sources", AllIcons.General.Settings) {
            override fun actionPerformed(e: AnActionEvent) {
                val stateService = AqlServersPersistentState.getService()
                val model = stateService.readState()

                val dialog = AqlEditDataSourcesDialog(e.project!!, model)
                val ok = dialog.showAndGet()
                if (ok) {
                    model.cleanDefaults()
                    stateService.writeState(model)
                    currentModel = model
                    populateDataSourcesList(model)
                }
            }
        }

        val actionGroup = DefaultActionGroup(action)
        return ActionManager.getInstance().createActionToolbar(AqlToolWindowFactory.TOOL_WINDOW_ID, actionGroup, false)

    }

    private class AqlServerListCellRenderer() : JBLabel(), ListCellRenderer<AqlServer> {

        init {
            isOpaque = true
        }

        override fun getListCellRendererComponent(
            list: JList<out AqlServer>,
            value: AqlServer,
            index: Int,
            isSelected: Boolean,
            cellHasFocus: Boolean
        ): Component {
            text = value.name
//            if (value.default) {
//                icon = AllIcons.Actions.Run_anything
//            } else {
//                icon = null
//            }

            if (isSelected) {
                background = list.selectionBackground
                foreground = list.selectionForeground
            } else {
                background = list.background
                foreground = list.foreground
            }
            return this
        }
    }

}