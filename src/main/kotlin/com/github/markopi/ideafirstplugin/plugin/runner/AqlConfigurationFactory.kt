package com.github.markopi.ideafirstplugin.plugin.runner

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationType
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.openapi.components.BaseState
import com.intellij.openapi.project.Project
import org.jetbrains.annotations.Nullable


class AqlConfigurationFactory(type: ConfigurationType) : ConfigurationFactory(type) {
    override fun getId(): String {
        return AqlRunConfigurationType.ID
    }

    override fun createTemplateConfiguration(project: Project): RunConfiguration {
        return AqlRunConfiguration(project, this, "AQL")
    }

    @Nullable
    override fun getOptionsClass(): Class<out BaseState>? {
        return AqlRunConfigurationOptions::class.java
    }
}