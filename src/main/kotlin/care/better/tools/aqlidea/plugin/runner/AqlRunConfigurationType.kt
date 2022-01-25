package care.better.tools.aqlidea.plugin.runner

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationType
import com.intellij.icons.AllIcons
import javax.swing.Icon


class AqlRunConfigurationType : ConfigurationType {
    override fun getDisplayName(): String {
        return "AQL"
    }

    override fun getConfigurationTypeDescription(): String {
        return "AQL run configuration type"
    }

    override fun getIcon(): Icon {
        return AllIcons.General.Information
    }

    override fun getId(): String {
        return ID
    }

    override fun getConfigurationFactories(): Array<ConfigurationFactory> {
        return arrayOf(AqlConfigurationFactory(this))
    }

    companion object {
        const val ID = "AqlRunConfiguration"
        val INSTANCE = AqlRunConfigurationType()
    }
}