package care.better.tools.aqlidea.plugin.editor.history

import care.better.tools.aqlidea.plugin.editor.AqlFileType
import com.intellij.openapi.components.NamedComponent
import com.intellij.openapi.fileEditor.*
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.vfs.VirtualFile
import org.jdom.Element
import org.slf4j.LoggerFactory

class AqlHistoryEditorProvider : FileEditorProvider, NamedComponent, DumbAware {
    private val log = LoggerFactory.getLogger(AqlHistoryEditorProvider::class.java)

    override fun accept(project: Project, file: VirtualFile): Boolean {
        return file.fileType == AqlFileType.INSTANCE
    }

    override fun createEditor(project: Project, file: VirtualFile): FileEditor {
        return AqlHistoryEditor(project, file)
    }

    override fun getEditorTypeId(): String {
        return "AQL.history"
    }

    override fun getPolicy(): FileEditorPolicy {
        return FileEditorPolicy.PLACE_AFTER_DEFAULT_EDITOR
    }

    override fun disposeEditor(editor: FileEditor) {
        Disposer.dispose(editor)
    }

    override fun readState(sourceElement: Element, project: Project, file: VirtualFile): FileEditorState {
        return FileEditorState.INSTANCE
    }

    override fun writeState(state: FileEditorState, project: Project, targetElement: Element) {
    }
}