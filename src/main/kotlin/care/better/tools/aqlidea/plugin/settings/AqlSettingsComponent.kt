package care.better.tools.aqlidea.plugin.settings

import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPasswordField
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import com.intellij.util.ui.UIUtil
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JTextField
import kotlin.reflect.KProperty


class AqlSettingsComponent {
    val mainPanel: JPanel
    private val serverUrlField = JBTextField()
    private val usernameField = JBTextField()
    private val passwordField = JBPasswordField()

    init {
        mainPanel = FormBuilder.createFormBuilder()
            .addLabeledComponent(JBLabel("Server URL:"), serverUrlField)
            .addComponentToRightColumn(
                JBLabel("<html>Server URL must refer to a ThinkEHR server web address," +
                        "<br>for example: http://localhost:8082",
                UIUtil.ComponentStyle.SMALL, UIUtil.FontColor.BRIGHTER))
            .addLabeledComponent(JBLabel("Username:"), usernameField)
            .addLabeledComponent(JBLabel("Password:"), passwordField)
            .addComponentFillVertically(JPanel(), 0)
            .panel
    }

    var serverUrl: String by serverUrlField
    var username: String by usernameField
    var password: String by passwordField

    val preferredFocusedComponent: JComponent get() = serverUrlField

    private operator fun JTextField.getValue(thisRef: Any?, property: KProperty<*>): String = this.text ?: ""
    private operator fun JTextField.setValue(thisRef: Any?, property: KProperty<*>, value: String) {
        this.text = value
    }
}