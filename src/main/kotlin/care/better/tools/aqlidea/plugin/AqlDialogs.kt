package care.better.tools.aqlidea.plugin

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import java.awt.Dimension
import javax.swing.JComponent

object AqlDialogs {
    fun rename(project: Project, title: String, message: String, oldValue: String=""): String? {
        val dialog = RenameDialog(project, title, message)
        dialog.textField.text=oldValue

        return if (dialog.showAndGet()) {
            dialog.textField.text
        } else {
            null
        }
    }

    private class RenameDialog(project: Project, title: String, private val message: String): DialogWrapper(project) {
        val textField: JBTextField = JBTextField()
        init {
            this.title = title
            init()
        }
        override fun createCenterPanel(): JComponent {
            val panel= FormBuilder.createFormBuilder()
                .addLabeledComponent(message, textField)
                .panel
            panel.minimumSize = Dimension(300, panel.minimumSize.height)
            return panel
        }
    }

}