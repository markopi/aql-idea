package com.github.markopi.ideafirstplugin.plugin.settings

import com.intellij.openapi.options.Configurable
import javax.swing.JComponent

class AqlSettingsConfigurable: Configurable {
    private lateinit var settingsComponent: AqlSettingsComponent
    override fun createComponent(): JComponent {
        settingsComponent = AqlSettingsComponent()
        return settingsComponent.mainPanel
    }

    override fun isModified(): Boolean {
        val settings = AqlSettingsState.INSTANCE
        return settings.serverUrl != settingsComponent.serverUrl ||
                settings.loginUsername != settingsComponent.username ||
                settings.loginPassword != settingsComponent.password
    }

    override fun apply() {
        val settings = AqlSettingsState.INSTANCE
        settings.serverUrl = settingsComponent.serverUrl
        settings.loginUsername = settingsComponent.username
        settings.loginPassword = settingsComponent.password
    }

    override fun reset() {
        val settings = AqlSettingsState.INSTANCE
        settingsComponent.serverUrl = settings.serverUrl
        settingsComponent.username = settings.loginUsername
        settingsComponent.password=settings.loginPassword
    }

    override fun getDisplayName(): String = "AQL Settings"
    override fun getPreferredFocusedComponent(): JComponent = settingsComponent.preferredFocusedComponent
}