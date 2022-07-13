package care.better.tools.aqlidea.plugin.toolWindow.query

import care.better.tools.aqlidea.ui.treetable.JTreeTable.Companion.of
import care.better.tools.aqlidea.ui.treetable.TreeTableData
import com.fasterxml.jackson.databind.ObjectMapper
import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.openapi.fileTypes.FileTypes
import com.intellij.openapi.project.Project
import com.intellij.ui.EditorTextField
import javax.swing.JEditorPane
import javax.swing.JScrollPane
import javax.swing.JTabbedPane
import kotlin.reflect.KProperty

class AqlQueryPanel(private val project: Project) : JTabbedPane() {
    private val rawRequestEditorScrollPane: JScrollPane
    private val rawRequestEditor: EditorTextField
    private val rawResponseEditorScrollPane: JScrollPane
    private val rawResponseEditor: EditorTextField
    private val queryResponseTab: JScrollPane

    private val objectMapper = ObjectMapper()

    init {
//        tabbedPane = JTabbedPane()
        val jsonFileType = FileTypeManager.getInstance().getFileTypeByExtension("json")

        rawRequestEditor = EditorTextField(project, jsonFileType).apply {
            isViewer = true
            setOneLineMode(false)
        }
        rawRequestEditorScrollPane = JScrollPane(rawRequestEditor)
        rawResponseEditor = EditorTextField(project, jsonFileType).apply {
            isViewer = true
            setOneLineMode(false)
        }
        rawResponseEditorScrollPane = JScrollPane(rawResponseEditor)
        queryResponseTab = JScrollPane()

        addTab("Request", rawRequestEditorScrollPane)
        addTab("Response", rawResponseEditorScrollPane)
        addTab("Query", queryResponseTab)
    }

    var rawRequest: String by rawRequestEditor
    var rawResponse: String by rawResponseEditor

    fun setResultTableData(treeTableData: TreeTableData?) {
        val treeTableData = treeTableData ?: TreeTableData(emptyList(), emptyList())

        val table = of(treeTableData)
        queryResponseTab.setViewportView(table)

    }

    fun showQueryTab(tab: QueryTab) {
        selectedIndex = tab.index
    }


    operator fun EditorTextField.getValue(thisRef: Any?, property: KProperty<*>): String = text
    operator fun EditorTextField.setValue(thisRef: Any?, property: KProperty<*>, value: String) {
//        text = value
        text = formatJsonText(value)
    }

    private fun formatJsonText(text: String): String {
        if (text.isEmpty()) return text
        return try {
            val tree = objectMapper.readTree(text.toByteArray())
            objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(tree)
        } catch (e: Exception) {
            text
        }
    }

    enum class QueryTab(val index: Int) {
        request(0), response(1), table(2);
    }


}