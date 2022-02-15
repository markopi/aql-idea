package care.better.tools.aqlidea.plugin.editor

import com.intellij.diff.util.FileEditorBase
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import java.awt.BorderLayout
import javax.swing.JComponent

@Deprecated("Does not work, does not trigger any events")
class AqlEditor(private val project: Project, private val file: VirtualFile, private val defaultTextEditor: TextEditor): TextEditor by defaultTextEditor {
    private val form = AqlEditorToolbar()

    init {
        form.editorPanel.add(defaultTextEditor.component, BorderLayout.CENTER)
    }

    override fun getComponent(): JComponent {
        return form.rootPanel
    }

    override fun getName(): String {
        return "AQL Editor Toolbar"
    }

    override fun getFile(): VirtualFile {
        return file
    }
}