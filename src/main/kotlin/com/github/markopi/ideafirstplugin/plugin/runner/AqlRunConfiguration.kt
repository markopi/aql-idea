package com.github.markopi.ideafirstplugin.plugin.runner

import com.intellij.execution.ExecutionException
import com.intellij.execution.Executor
import com.intellij.execution.configurations.*
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessHandlerFactory
import com.intellij.execution.process.ProcessTerminatedListener
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import java.nio.file.Paths


class AqlRunConfiguration(project: Project, factory: ConfigurationFactory?, name: String?) :
    RunConfigurationBase<AqlRunConfigurationOptions>(project, factory, name) {

    override fun getOptions(): AqlRunConfigurationOptions {
        return super.getOptions() as AqlRunConfigurationOptions
    }

    var scriptFile: String?
        get() = options.scriptFile
        set(scriptFile) {
            options.scriptFile = scriptFile ?: ""
        }

    override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration?> {
        return AqlSettingsEditor()
    }

    override fun checkConfiguration() {}

    override fun getState(executor: Executor, executionEnvironment: ExecutionEnvironment): RunProfileState {
        return object : CommandLineState(executionEnvironment) {
            @Throws(ExecutionException::class)
            override fun startProcess(): ProcessHandler {
                val commandLine = GeneralCommandLine(options.scriptFile)
                val processHandler = ProcessHandlerFactory.getInstance().createColoredProcessHandler(commandLine)
                ProcessTerminatedListener.attach(processHandler)
                return processHandler
            }
        }
    }

    fun suggestName(): String {
        val n = scriptFile ?: ""
        val filename = Paths.get(n).fileName.toString()
        val dot = filename.lastIndexOf('.')
        return if (dot >= 0)
            filename.substring(dot + 1, filename.length)
        else
            filename
    }
}