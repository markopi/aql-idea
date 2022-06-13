package care.better.tools.aqlidea.plugin.toolWindow.servers

import care.better.tools.aqlidea.plugin.service.ThinkEhrClientService
import care.better.tools.aqlidea.plugin.settings.AqlServer
import care.better.tools.aqlidea.plugin.settings.AqlServersConfiguration
import care.better.tools.aqlidea.plugin.toolWindow.AqlToolWindowFactory
import care.better.tools.aqlidea.thinkehr.ThinkEhrTarget
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.Splitter
import com.intellij.ui.components.*
import com.intellij.util.ui.FormBuilder
import com.intellij.util.ui.UIUtil
import org.apache.commons.lang.StringEscapeUtils
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Component
import java.awt.Dimension
import java.awt.event.ActionEvent
import java.util.*
import java.util.concurrent.atomic.AtomicLong
import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.text.Document

class AqlEditDataSourcesDialog(project: Project, val model: AqlServersConfiguration) : DialogWrapper(project) {

    init {
        title = "Aql Servers"
        init()

    }

    override fun createCenterPanel(): JComponent {
        return AqlEditDataSourcesDialogPanel(model)
    }

}

class AqlEditDataSourcesDialogPanel(val model: AqlServersConfiguration) : JPanel() {


    private var currentServer: AqlServer? = null
    private var lastServerId: AtomicLong = AtomicLong(readLastServerId())


    private lateinit var actionToolbar: ActionToolbar
    private lateinit var dataSourcesList: JBList<AqlServer>
    private lateinit var dataSourcesListModel: DefaultListModel<AqlServer>

    // right panel
    private val serverNameField = JBTextField()
    private val serverUrlField = JBTextField()
    private val usernameField = JBTextField()
    private val passwordField = JBPasswordField()
    private val testResult = JBLabel()
    private val testButton = JButton()

    init {
        createGui()
        populateGui()
    }

    private fun createGui() {
        layout = BorderLayout()
        minimumSize = Dimension(600, 300)

        val leftPanel = JPanel(BorderLayout())
        actionToolbar = createActionToolbar()
        leftPanel.add(actionToolbar.component, BorderLayout.NORTH)
        dataSourcesList = JBList<AqlServer>()
        dataSourcesList.cellRenderer = AqlServerListCellRenderer()
        dataSourcesListModel = DefaultListModel()
        dataSourcesList.model = dataSourcesListModel
        dataSourcesList.addListSelectionListener {
//            currentServer?.let { saveServerForm(it) }
            if (dataSourcesList.selectedIndex < 0) {
                currentServer = null
            } else {
                currentServer = dataSourcesListModel.get(dataSourcesList.selectedIndex)
            }
            readServerForm(currentServer)
        }
        leftPanel.add(JBScrollPane(dataSourcesList), BorderLayout.CENTER)

        val splitPanel = Splitter()
        splitPanel.firstComponent = leftPanel
        splitPanel.secondComponent = createEditPanel()
        splitPanel.proportion = 0.25f

        add(splitPanel, BorderLayout.CENTER)
    }

    private fun createEditPanel(): JPanel {
        serverNameField.document.addDocumentListener(SimpleDocumentListener {
            currentServer?.let { current ->
                current.name = it.document.text
                deduplicateServerName(model.servers, current)
            }
        })
        serverUrlField.document.addDocumentListener(SimpleDocumentListener {
            currentServer?.serverUrl = it.document.text
        })
        usernameField.document.addDocumentListener(SimpleDocumentListener {
            currentServer?.username = it.document.text
        })
        passwordField.document.addDocumentListener(SimpleDocumentListener {
            currentServer?.password = it.document.text
        })


        return FormBuilder.createFormBuilder()
            .addLabeledComponent(JBLabel("Name:"), serverNameField)
            .addLabeledComponent(JBLabel("Server URL:"), serverUrlField)
            .addComponentToRightColumn(
                JBLabel(
                    "<html>Server URL must refer to a ThinkEHR server web address," +
                            "<br>for example: http://localhost:8082",
                    UIUtil.ComponentStyle.SMALL, UIUtil.FontColor.BRIGHTER
                )
            )
            .addLabeledComponent(JBLabel("Username:"), usernameField)
            .addLabeledComponent(JBLabel("Password:"), passwordField)
            .addComponentFillVertically(JPanel(), 0)
            .addComponent(createTestPanel())
            .panel
    }


    private fun createTestPanel(): JPanel {
        val testPanel = JPanel(BorderLayout())
        testPanel.add(testResult, BorderLayout.CENTER)

        testPanel.add(JPanel(BorderLayout()).apply {
            testButton.action = TestAction()
            add(testButton, BorderLayout.EAST)
        }, BorderLayout.SOUTH)
        return testPanel
    }

    private fun readLastServerId(): Long {
        val maxId = model.servers.maxOfOrNull { it.id.toLongOrNull(Character.MAX_RADIX) ?: 0 } ?: -1
        return maxId
    }
    private fun newServerId(): String {
        val nextId = lastServerId.incrementAndGet()
        return nextId.toString(Character.MAX_RADIX).toLowerCase()
    }
    private fun createActionToolbar(): ActionToolbar {
        val addServer = object : AnAction("Add", "Add server", AllIcons.General.Add) {
            override fun actionPerformed(e: AnActionEvent) {
                val newServer = AqlServer(id = newServerId(), name = "New Server", "", "", "", false)
                model.servers.add(newServer)
                dataSourcesListModel.addElement(newServer)
                dataSourcesList.selectedIndex = dataSourcesListModel.size() - 1
            }
        }
        val removeServer = object : AnAction("Remove", "Remove server", AllIcons.General.Remove) {
            override fun actionPerformed(e: AnActionEvent) {
                val oldIndex = dataSourcesList.selectedIndex
                if (oldIndex >= 0) {
                    model.servers.removeAt(oldIndex)
                    dataSourcesListModel.remove(oldIndex)
                    dataSourcesList.selectedIndex =
                        oldIndex.coerceAtMost(dataSourcesListModel.size() - 1)
                }
            }
        }

        val actionGroup = DefaultActionGroup(addServer, removeServer)
        return ActionManager.getInstance().createActionToolbar(AqlToolWindowFactory.TOOL_WINDOW_ID, actionGroup, true)

    }

    private fun populateGui() {
        dataSourcesListModel.clear()
        dataSourcesListModel.addAll(model.servers)
        if (dataSourcesListModel.isEmpty) {
            dataSourcesList.selectedIndex = -1
            readServerForm(null)
        } else {
            dataSourcesList.selectedIndex = 0
            readServerForm(dataSourcesListModel.get(0))
        }
    }

    private fun readServerForm(server: AqlServer?) {
        serverNameField.isEnabled = server != null
        serverUrlField.isEnabled = server != null
        usernameField.isEnabled = server != null
        passwordField.isEnabled = server != null
        testResult.text = ""
        testButton.isEnabled = server != null

        if (server == null) {
            serverNameField.text = ""
            serverUrlField.text = ""
            usernameField.text = ""
            passwordField.text = ""
            return
        }
        serverNameField.text = server.name
        serverUrlField.text = server.serverUrl
        usernameField.text = server.username
        passwordField.text = server.password
    }

    private fun saveServerForm(server: AqlServer) {
        server.name = serverNameField.text
        server.serverUrl = serverUrlField.text
        server.username = usernameField.text
        server.password = String(passwordField.password)
    }

    private val Document.text get() = getText(0, length)

    companion object {
        fun extractBaseNameAndIndex(name: String): Pair<String, Int> {
            val regex = Regex("""^(.*)\((\d+)\)$""")
            val match = regex.matchEntire(name) ?: return name.trim() to 0
            val baseName = match.groupValues[1].trim()
            val index = match.groupValues[2].toInt()
            return baseName to index
        }

        private fun deduplicateServerName(allServers: MutableList<AqlServer>, current: AqlServer) {
            val otherNames = allServers.filter { it !== current }.map { it.name }.toSet()
            if (current.name !in otherNames) return

            var (baseName, index) = extractBaseNameAndIndex(current.name)
            while (true) {
                index++
                val candidate = "$baseName ($index)"
                if (candidate !in otherNames) {
                    current.name = candidate
                    return
                }
            }
        }

    }

    private class SimpleDocumentListener(private val update: (e: DocumentEvent) -> Unit) : DocumentListener {
        override fun insertUpdate(e: DocumentEvent) = update(e)

        override fun removeUpdate(e: DocumentEvent) = update(e)

        override fun changedUpdate(e: DocumentEvent) = update(e)
    }

    private inner class TestAction : AbstractAction("Test") {

        override fun actionPerformed(e: ActionEvent) {
            val server = currentServer
            testResult.text = ""
            if (server == null) {
                return
            }
            val thinkEhrTarget = ThinkEhrTarget(server.serverUrl, server.username, server.password)
            val thinkEhr = ThinkEhrClientService.INSTANCE

            try {
                thinkEhr.client.query(thinkEhrTarget, "select c/uid/value from composition c limit 1")
                testResult.text = "<html>Successful</html>"
                testResult.foreground = Color.GREEN
            } catch (e: Exception) {
                testResult.text = "<html>${StringEscapeUtils.escapeHtml(e.toString())}</html>"
                testResult.foreground = Color.RED
            }
        }
    }

    private class AqlServerListCellRenderer : JBLabel(), ListCellRenderer<AqlServer> {

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