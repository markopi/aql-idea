package care.better.tools.aqlidea.plugin.runner

import com.intellij.execution.configurations.RunProfile
import com.intellij.execution.configurations.RunnerSettings
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.runners.ProgramRunner

class AqlQueryRunner : ProgramRunner<RunnerSettings> {
    override fun getRunnerId(): String = "AqlQueryRunner"

    override fun canRun(executorId: String, profile: RunProfile): Boolean {
        return profile is AqlRunConfiguration
    }

    override fun execute(environment: ExecutionEnvironment) {
//        println("This is weird")
//        println("Why doesn't it stop here?")

    }

}