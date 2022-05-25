package care.better.tools.aqlidea.plugin.service

import care.better.tools.aqlidea.plugin.settings.AqlServersPersistentState
import care.better.tools.aqlidea.plugin.toolWindow.servers.AqlServer
import care.better.tools.aqlidea.thinkehr.CachingThinkEhrClient
import care.better.tools.aqlidea.thinkehr.ThinkEhrClient
import care.better.tools.aqlidea.thinkehr.ThinkEhrClientImpl
import care.better.tools.aqlidea.thinkehr.ThinkEhrTarget
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project

class ThinkEhrClientService {
    val client: ThinkEhrClient

    @Deprecated("Replaced with toThinkEhrTarget")
    fun getTarget(project: Project): ThinkEhrTarget? {
        val settings = AqlServersPersistentState.getService()
        val state = settings.readState()
        val defaultServer = state.defaultServer()
        if (defaultServer == null ) {
            val n = Notification(
                "AQL",
                "No AQL server configured",
                "Please configure aql server in Tool Window AQL / Servers",
                NotificationType.ERROR
            )
            Notifications.Bus.notify(n, project)
            return null
        }
        return toThinkEhrTarget(defaultServer)
    }

    fun toThinkEhrTarget(server: AqlServer): ThinkEhrTarget {
        return ThinkEhrTarget(
            url = server.serverUrl,
            username = server.username,
            password = server.password
        )
    }

    init {
        client = CachingThinkEhrClient(ThinkEhrClientImpl())
    }
    companion object {
        val INSTANCE: ThinkEhrClientService
         get() = ApplicationManager.getApplication().getService(ThinkEhrClientService::class.java)
    }
}