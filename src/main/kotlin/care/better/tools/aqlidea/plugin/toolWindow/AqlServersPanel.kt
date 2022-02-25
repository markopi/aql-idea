package care.better.tools.aqlidea.plugin.toolWindow

import care.better.tools.aqlidea.plugin.settings.AqlServersPersistentState
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.*
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

    private val stateService = AqlServersPersistentState.getService(project)

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
                    if (listIndex >= 0) {
                        currentModel.servers.forEachIndexed { modelIndex, aqlServer ->
                            aqlServer.default = modelIndex == listIndex
                        }
                        stateService.writeState(currentModel)
                        dataSourcesList.repaint()
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
                val stateService = AqlServersPersistentState.getService(e.project!!)
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
            if (value.default) {
                icon = AllIcons.Actions.Run_anything
            } else {
                icon = null
            }

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