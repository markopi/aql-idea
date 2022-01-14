package com.github.markopi.ideafirstplugin.plugin.runner

import com.intellij.openapi.options.SettingsEditor
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.panel
import javax.swing.JComponent
import javax.swing.JPanel

class AqlSettingsEditor : SettingsEditor<AqlRunConfiguration>() {
    private lateinit var panel: JPanel
    private lateinit var scriptFileField: JBTextField

    override fun resetEditorFrom(aqlRunConfiguration: AqlRunConfiguration) {
        scriptFileField.text = aqlRunConfiguration.scriptFile
    }

    override fun applyEditorTo(aqlRunConfiguration: AqlRunConfiguration) {
        aqlRunConfiguration.scriptFile = scriptFileField.text
    }

    override fun createEditor(): JComponent {
        panel = panel {
            row("Script file:") {
                scriptFileField = textField().component
            }
        }
        return panel
    }

}