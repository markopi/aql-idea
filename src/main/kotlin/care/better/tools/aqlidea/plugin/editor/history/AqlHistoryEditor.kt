package care.better.tools.aqlidea.plugin.editor.history

import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorLocation
import com.intellij.openapi.fileEditor.FileEditorState
import com.intellij.openapi.fileEditor.FileEditorStateLevel
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.UserDataHolder
import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.openapi.vfs.VirtualFile
import java.beans.PropertyChangeListener
import java.beans.PropertyChangeSupport
import javax.swing.JComponent

class AqlHistoryEditor(private val project: Project, private val file: VirtualFile) : FileEditor, FileEditorLocation {
    private val changeSupport = PropertyChangeSupport(this)
    private val userDataHolder: UserDataHolder = UserDataHolderBase()
    private val aqlHistoryComponent: AqlHistoryComponent

    init {
        aqlHistoryComponent = AqlHistoryComponent(project, file.toNioPath())
    }

    override fun getName(): String = "History"

    override fun getFile(): VirtualFile = file

    override fun dispose() {
        deselectNotify()
    }

    override fun getComponent(): JComponent = aqlHistoryComponent

    override fun getPreferredFocusedComponent(): JComponent? = aqlHistoryComponent.historyList

    override fun setState(state: FileEditorState, exactState: Boolean) {
    }

    override fun getState(level: FileEditorStateLevel): FileEditorState {
        return FileEditorState.INSTANCE
    }

    override fun setState(state: FileEditorState) {
    }


    override fun isModified(): Boolean = false

    override fun isValid(): Boolean = true
    override fun selectNotify() {
        aqlHistoryComponent.populate()
    }

    override fun addPropertyChangeListener(listener: PropertyChangeListener) {
        changeSupport.addPropertyChangeListener(listener)
    }

    override fun removePropertyChangeListener(listener: PropertyChangeListener) {
        changeSupport.removePropertyChangeListener(listener)
    }

    override fun getCurrentLocation(): FileEditorLocation = this

    override fun compareTo(other: FileEditorLocation?): Int = 1
    override fun getEditor(): FileEditor = this

    override fun <T : Any?> getUserData(key: Key<T>): T? = userDataHolder.getUserData(key)

    override fun <T : Any?> putUserData(key: Key<T>, value: T?) = userDataHolder.putUserData(key, value)
}