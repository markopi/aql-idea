package care.better.tools.aqlidea.plugin.service

import care.better.tools.aqlidea.plugin.settings.AqlSettingsState
import care.better.tools.aqlidea.thinkehr.CachingThinkEhrClient
import care.better.tools.aqlidea.thinkehr.ThinkEhrClient
import care.better.tools.aqlidea.thinkehr.ThinkEhrClientImpl
import care.better.tools.aqlidea.thinkehr.ThinkEhrTarget
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.project.Project

class ThinkEhrClientService {
    val client: ThinkEhrClient

    fun getTarget(project: Project?): ThinkEhrTarget? {
        val settings = AqlSettingsState.INSTANCE
        if (settings.serverUrl.isBlank()) {
            val n = Notification(
                "AQL",
                "AQL server not configured",
                "Please configure aql server in Settings / Tools / AQL",
                NotificationType.ERROR
            )
            Notifications.Bus.notify(n, project)
            return null
        }
        return ThinkEhrTarget(
            url = settings.serverUrl,
            username = settings.loginUsername,
            password = settings.loginPassword
        )
    }

    init {
        client = CachingThinkEhrClient(ThinkEhrClientImpl())
    }
}