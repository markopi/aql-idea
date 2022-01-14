package com.github.markopi.ideafirstplugin.plugin.runner

import com.intellij.execution.configurations.RunConfigurationOptions
import com.intellij.openapi.components.StoredProperty


class AqlRunConfigurationOptions : RunConfigurationOptions() {
    private val myScriptFile: StoredProperty<String?> = string("").provideDelegate(this, "scriptFile")

    var scriptFile: String
        get() = myScriptFile.getValue(this) ?: ""
        set(scriptName) {
            myScriptFile.setValue(this, scriptName)
        }
}