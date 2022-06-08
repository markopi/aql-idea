package care.better.tools.aqlidea.plugin.toolWindow.servers

import care.better.tools.aqlidea.plugin.AqlUtils
import care.better.tools.aqlidea.plugin.settings.*
import care.better.tools.aqlidea.plugin.toolWindow.AqlToolWindowFactory
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.JBMenuItem
import com.intellij.openapi.ui.JBPopupMenu
import com.intellij.openapi.ui.Splitter
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import java.awt.BorderLayout
import java.awt.Component
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.nio.file.Path
import javax.swing.JPanel
import javax.swing.JTree
import javax.swing.SwingUtilities
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeCellRenderer
import javax.swing.tree.DefaultTreeModel


@Suppress("UNCHECKED_CAST")
class AqlServersPanel(private val project: Project) : JPanel() {

    init {
        createGui()
    }

    private val stateService = AqlServersConfigurationService.INSTANCE

    private lateinit var currentConfiguration: AqlServersConfiguration

    private lateinit var actionToolbar: ActionToolbar
    private lateinit var dataSourcesTree: JTree
    private lateinit var dataSourcesTreeModel: DefaultTreeModel

    private fun createGui() {
        layout = BorderLayout()
        actionToolbar = createActionToolbar()
        add(actionToolbar.component, BorderLayout.WEST)

        dataSourcesTree = JTree()
        dataSourcesTree.cellRenderer = AqlServersTreeCellRenderer()
        dataSourcesTree.toggleClickCount = 0

        dataSourcesTree.addKeyListener(object : KeyAdapter() {
            override fun keyPressed(e: KeyEvent) {
                if (e.keyCode == KeyEvent.VK_ENTER || e.keyCode == KeyEvent.VK_F4) {
                    executeDefaultAction()
                    e.consume()
                }
            }
        })
        dataSourcesTree.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (SwingUtilities.isLeftMouseButton(e) && e.clickCount == 2) {
                    executeDefaultAction()
                }

//                if (e.isPopupTrigger) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    // select tree node with right click as well as left click
                    val n = dataSourcesTree.selectTreeRowOnPosition(e)
                    val node = n as? AqlServersTreeNode ?: return
                    val popup = JBPopupMenu()
                    when (node) {
                        is AqlServersTreeNode.ConsolesTreeNode -> {
                            popup.add(JBMenuItem("Add console", AllIcons.General.Add).apply {
                                addActionListener { createConsoleFile(node) }
                            })
                        }
                        is AqlServersTreeNode.ConsoleTreeNode -> {
                            popup.add(JBMenuItem("Rename console", null).apply {
                                addActionListener { renameConsoleFile(node) }
                            })
                            popup.add(JBMenuItem("Remove console", AllIcons.General.Remove).apply {
                                addActionListener { removeConsoleFile(node) }
                            })
                        }
                        else -> {}
                    }
                    if (popup.componentCount > 0) {
                        popup.show(e.component, e.x, e.y)
                    }
                }
                super.mouseClicked(e)
            }
        })

        val splitPanel = Splitter()
        splitPanel.firstComponent = JBScrollPane(dataSourcesTree)
        splitPanel.secondComponent = JPanel()
        add(splitPanel, BorderLayout.CENTER)
    }


    private fun executeDefaultAction() {
        val path = dataSourcesTree.selectionPath ?: return
        val node = path.lastPathComponent as? AqlServersTreeNode ?: return
        if (node is AqlServersTreeNode.AqlServerTreeNode) {
            openServerConsole(node)
        } else if (node is AqlServersTreeNode.ConsoleTreeNode) {
            openConsole(node)
        }
    }


    private fun JTree.selectTreeRowOnPosition(e: MouseEvent): DefaultMutableTreeNode? {
        val selRow = getRowForLocation(e.x, e.y)
        val selPath = getPathForLocation(e.x, e.y)
        selectionPath = selPath
        if (selRow > -1) {
            setSelectionRow(selRow)
        }
        return selectionPath?.lastPathComponent as DefaultMutableTreeNode?
    }

    private fun removeConsoleFile(console: AqlServersTreeNode.ConsoleTreeNode) {
        AqlPluginConfigurationService.deleteConsoleFile(console.server, console.file)
        dataSourcesTreeModel.removeNodeFromParent(console)
    }

    private fun renameConsoleFile(node: AqlServersTreeNode.ConsoleTreeNode) {
        node.renameConsole(project)
        dataSourcesTreeModel.nodeChanged(node)
    }

    private fun createConsoleFile(consoles: AqlServersTreeNode.ConsolesTreeNode) {
        val newNode = consoles.createNewConsole(project) ?: return
        dataSourcesTreeModel.insertNodeInto(newNode, consoles, consoles.childCount)
    }

    private fun openServerConsole(node: AqlServersTreeNode.AqlServerTreeNode) {
        openConsole(node.server, AqlPluginConfigurationService.getMainConsoleFile(node.server))
    }

    private fun openConsole(node: AqlServersTreeNode.ConsoleTreeNode) = openConsole(node.server, node.file)
    private fun openConsole(server: AqlServer, file: Path) {
        val contentFile = VfsUtil.findFile(file, true) ?: return
        contentFile.putUserData(AqlUtils.KEY_AQL_SERVER_ID, server.id)
        FileEditorManager.getInstance(project).openFile(contentFile, true)
    }

    fun activate() {
        currentConfiguration = stateService.load()
        populateDataSourcesList(currentConfiguration)
    }

    private fun populateDataSourcesList(model: AqlServersConfiguration) {
        dataSourcesTreeModel = DefaultTreeModel(buildTreeNodeModel(model))
        dataSourcesTree.model = dataSourcesTreeModel
    }

    private fun createActionToolbar(): ActionToolbar {
        val action = object : AnAction("Configure", "Configure data sources", AllIcons.General.Settings) {
            override fun actionPerformed(e: AnActionEvent) {
                val stateService = AqlServersConfigurationService.INSTANCE
                val model = stateService.load()

                val dialog = AqlEditDataSourcesDialog(e.project!!, model)
                val ok = dialog.showAndGet()
                if (ok) {
                    model.cleanDefaults()
                    stateService.save(model)
                    currentConfiguration = model
                    populateDataSourcesList(model)
                }
            }
        }

        val actionGroup = DefaultActionGroup(action)
        return ActionManager.getInstance().createActionToolbar(AqlToolWindowFactory.TOOL_WINDOW_ID, actionGroup, false)

    }

    private class AqlServersTreeCellRenderer : DefaultTreeCellRenderer() {
        val renderer = JBLabel()
        override fun getTreeCellRendererComponent(
            tree: JTree,
            value: Any,
            sel: Boolean,
            expanded: Boolean,
            leaf: Boolean,
            row: Int,
            hasFocus: Boolean
        ): Component {
            val node = value as? AqlServersTreeNode
                ?: return super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus)

            renderer.text = node.label
            renderer.icon = node.icon
            return renderer
        }
    }
}