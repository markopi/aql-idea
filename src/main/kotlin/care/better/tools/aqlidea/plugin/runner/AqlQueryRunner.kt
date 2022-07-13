package care.better.tools.aqlidea.plugin.runner

import care.better.tools.aqlidea.plugin.AqlPluginException
import care.better.tools.aqlidea.plugin.AqlUtils
import care.better.tools.aqlidea.plugin.service.ThinkEhrClientService
import care.better.tools.aqlidea.plugin.settings.AqlServer
import care.better.tools.aqlidea.plugin.toolWindow.AqlToolWindowFactory
import com.google.common.base.Stopwatch
import com.intellij.notification.NotificationType
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import java.util.concurrent.TimeUnit

object AqlQueryRunner {
    private val log = Logger.getInstance(AqlQueryRunner::class.java)

    fun run(
        project: Project,
        aqlServer: AqlServer,
        aql: String
    ) {
        try {
            val thinkehr = ThinkEhrClientService.INSTANCE
            val target = thinkehr.toThinkEhrTarget(aqlServer)

            AqlToolWindowFactory.showQueryStart(project)
            try {
//                val t1 = System.currentTimeMillis()
                val r = thinkehr.client.query(target, aql)
//                val t2 = System.currentTimeMillis()
//                log.info("Query retrieved in ${t2 - t1}ms")
                AqlToolWindowFactory.showQueryResult(project, r)
                val t3 = System.currentTimeMillis()
//                log.info("Query displayed in ${t3 - t2}ms")
            } catch (e: Exception) {
                AqlToolWindowFactory.showQueryError(project, e)
            }
        } catch (e: Exception) {
            if (e !is AqlPluginException) {
                log.error("Error calling ThinkEhr Server", e)
            }
            val text = if (e is AqlPluginException) e.message ?: e.toString() else e.toString()
            AqlUtils.notify("ThinkEhr server error", text, NotificationType.ERROR, project)
            return
        }
    }

}