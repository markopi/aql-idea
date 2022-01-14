package com.github.markopi.ideafirstplugin.plugin.runner

import com.github.markopi.ideafirstplugin.plugin.editor.AqlFileType
import com.intellij.execution.actions.ConfigurationContext
import com.intellij.execution.actions.LazyRunConfigurationProducer
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.openapi.util.Ref
import com.intellij.psi.PsiElement
import java.io.File

class AqlRunConfigurationProducer: LazyRunConfigurationProducer<AqlRunConfiguration>() {
    override fun getConfigurationFactory(): ConfigurationFactory =
        AqlRunConfigurationType.INSTANCE.configurationFactories.first()

    override fun isConfigurationFromContext(
        configuration: AqlRunConfiguration,
        context: ConfigurationContext
    ): Boolean {
        val contextPath = File(context.psiLocation!!.containingFile.virtualFile.path).path
        val configPath = File(configuration.scriptFile?:"").path
        return configPath==contextPath
    }

    override fun setupConfigurationFromContext(
        configuration: AqlRunConfiguration,
        context: ConfigurationContext,
        sourceElement: Ref<PsiElement>
    ): Boolean {
        try {
            val contextFile = context.psiLocation!!.containingFile
            if (contextFile == null || !AqlFileType.DEFAULT_ASSOCIATED_EXTENSIONS
                    .contains(contextFile.virtualFile.extension)
            ) {
                return false
            }
            val scriptParent = File(contextFile.virtualFile.path)
            configuration.scriptFile = scriptParent.name
        } catch (e: RuntimeException) {
            return false
        }
        configuration.name = configuration.suggestName()
        return true
    }
}