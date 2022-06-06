package care.better.tools.aqlidea.plugin.editor.history

import care.better.tools.aqlidea.plugin.AqlUtils
import care.better.tools.aqlidea.plugin.editor.AqlFileType
import care.better.tools.aqlidea.plugin.runner.AqlQueryRunner
import care.better.tools.aqlidea.plugin.settings.AqlPluginHomeDir
import care.better.tools.aqlidea.plugin.settings.AqlServerConsoleHistory
import care.better.tools.aqlidea.plugin.toolWindow.AqlToolWindowFactory
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.ui.EditorTextField
import com.intellij.ui.JBSplitter
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.components.BorderLayoutPanel
import java.awt.Component
import java.awt.Font
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.nio.file.Path
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.swing.*

class AqlHistoryComponent(private val project: Project, private val consoleFile: Path) : BorderLayoutPanel() {

    val historyList: JBList<AqlServerConsoleHistory.Item>
    private val historyListModel: DefaultListModel<AqlServerConsoleHistory.Item>
    private val aqlTextField: EditorTextField

    init {

        historyListModel = DefaultListModel()
        historyList = JBList(historyListModel)
        historyList.cellRenderer = HistoryListCellRenderer()

        aqlTextField = EditorTextField("", project, AqlFileType.INSTANCE).apply {
            this.isViewer = true
        }
        historyList.addListSelectionListener {
            val index = historyList.selectedIndex
            if (index>=0 && index<historyListModel.size()) {
                aqlTextField.text = historyListModel[index].aql
            } else {
                aqlTextField.text=""
            }
        }
        historyList.addKeyListener(object: KeyAdapter() {
            override fun keyPressed(e: KeyEvent) {
                if (e.keyCode == KeyEvent.VK_ENTER) {
                    executeSelectedHistoryItem()
                }
                super.keyPressed(e)
            }
        })
        historyList.addMouseListener(object: MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (SwingUtilities.isLeftMouseButton(e) && e.clickCount == 2) {
                    executeSelectedHistoryItem()
                }
                super.mouseClicked(e)
            }
        })

        val splitter = JBSplitter(false, AqlHistoryComponent::class.java.name + ".splitProportion", 0.5f)
        splitter.firstComponent = JBScrollPane(historyList)
        splitter.secondComponent = aqlTextField
        addToCenter(splitter)
        addToTop(createToolbar())
    }

    private fun executeSelectedHistoryItem() {
        val index = historyList.selectedIndex
        if (index >= 0 && index < historyListModel.size()) {
            runQuery(historyListModel[index])
        }
    }

    private fun createToolbar(): JComponent {
        val action = object : AnAction("Run", "Run aql from history", AllIcons.RunConfigurations.TestState.Run) {
            override fun actionPerformed(e: AnActionEvent) {
                executeSelectedHistoryItem()
            }
        }

        val actionGroup = DefaultActionGroup(action)
        val actionToolbar= ActionManager.getInstance().createActionToolbar(AqlToolWindowFactory.TOOL_WINDOW_ID, actionGroup, true)
        return actionToolbar.component
    }

    private fun runQuery(item: AqlServerConsoleHistory.Item) {
        val vConsoleFile = VfsUtil.findFile(consoleFile, true) ?: return
        val server = AqlUtils.aqlServerForFile(vConsoleFile) ?: return
        AqlQueryRunner.run(project, server, item.aql)
    }

    fun populate() {
        val configuration = AqlPluginHomeDir.readAqlServerConsoleHistoryHistory(consoleFile)
        populate(configuration)
    }

    private fun populate(configuration: AqlServerConsoleHistory) {
        historyListModel.clear()
        historyListModel.addAll(configuration.history.sortedByDescending { it.timestamp })
        historyList.invalidate()
        historyList.selectedIndex = -1
        aqlTextField.text=""
    }

    private class HistoryListCellRenderer: DefaultListCellRenderer() {
        val timestampFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

        val timestampLabel: JBLabel = JBLabel()
            .withFont(JBUI.Fonts.create(Font.MONOSPACED, JBUI.Fonts.label().size))

        val aqlLabel: JBLabel = JBLabel()
            .withFont(JBUI.Fonts.create(Font.MONOSPACED, JBUI.Fonts.smallFont().size))
            .apply {
                border = BorderFactory.createEmptyBorder(0, 15, 0, 0)
            }

        val component: JPanel = BorderLayoutPanel()
            .addToTop(timestampLabel)
            .addToCenter(aqlLabel)
            .apply {
                border = BorderFactory.createEmptyBorder(2, 0, 2, 0)
            }

        private val aqlWhitespace = Regex("""\s+""")

        override fun getListCellRendererComponent(
            list: JList<*>,
            value: Any?,
            index: Int,
            isSelected: Boolean,
            cellHasFocus: Boolean
        ): Component {
            val item = value as? AqlServerConsoleHistory.Item ?: return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus)
            timestampLabel.text = LocalDateTime.ofInstant(Instant.ofEpochMilli(item.timestamp), ZoneId.systemDefault()).format(timestampFormat)
            aqlLabel.text = item.aql.replace(aqlWhitespace, " ")

            if (isSelected) {
                component.background = list.selectionBackground
                component.foreground = list.selectionForeground
            } else {
                component.background = list.background
                component.foreground = list.foreground
            }
            return component
        }
    }
}