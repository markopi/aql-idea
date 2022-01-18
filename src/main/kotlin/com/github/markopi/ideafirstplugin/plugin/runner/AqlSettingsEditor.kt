package com.github.markopi.ideafirstplugin.plugin.runner

import com.intellij.openapi.options.SettingsEditor
import javax.swing.JComponent

class AqlSettingsEditor : SettingsEditor<AqlRunConfiguration>() {
    private lateinit var panel: AqlSettingsEditorPanel

    override fun resetEditorFrom(aqlRunConfiguration: AqlRunConfiguration) {
        panel.scriptFileField.text = aqlRunConfiguration.scriptFile ?: ""
    }

    override fun applyEditorTo(aqlRunConfiguration: AqlRunConfiguration) {
        aqlRunConfiguration.scriptFile = panel.scriptFileField.text
    }

    override fun createEditor(): JComponent {
        panel = AqlSettingsEditorPanel()
        return panel.rootPanel
    }

}