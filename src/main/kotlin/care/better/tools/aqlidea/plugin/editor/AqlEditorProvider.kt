package care.better.tools.aqlidea.plugin.editor

import com.intellij.openapi.components.NamedComponent
import com.intellij.openapi.fileEditor.*
import com.intellij.openapi.fileEditor.impl.text.TextEditorProvider
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import org.jdom.Element

@Deprecated("Does not work, does not trigger any events")
class AqlEditorProvider : FileEditorProvider, NamedComponent, DumbAware {
    override fun accept(project: Project, file: VirtualFile): Boolean {
        return file.fileType == AqlFileType.INSTANCE
    }

    override fun createEditor(project: Project, file: VirtualFile): FileEditor {
        val defaultTextEditor = TextEditorProvider.getInstance().createEditor(project, file) as TextEditor
//        return defaultTextEditor
        return AqlEditor(project, file, defaultTextEditor)
    }

    override fun getEditorTypeId(): String {
        return "AQL"
    }

    override fun getPolicy(): FileEditorPolicy {
        return FileEditorPolicy.NONE
    }

    override fun disposeEditor(editor: FileEditor) {
        TextEditorProvider.getInstance().disposeEditor(editor)
    }

    override fun readState(sourceElement: Element, project: Project, file: VirtualFile): FileEditorState {
        return TextEditorProvider.getInstance().readState(sourceElement, project, file)
    }

    override fun writeState(state: FileEditorState, project: Project, targetElement: Element) {
        TextEditorProvider.getInstance().writeState(state, project, targetElement)
    }
}