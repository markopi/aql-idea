package com.github.markopi.ideafirstplugin.plugin.runner

import com.github.markopi.ideafirstplugin.plugin.AqlPluginException
import com.github.markopi.ideafirstplugin.plugin.editor.AqlTextTokenTypes
import com.github.markopi.ideafirstplugin.plugin.settings.AqlSettingsState
import com.github.markopi.ideafirstplugin.plugin.toolWindow.AqlToolWindowFactory
import com.github.markopi.ideafirstplugin.thinkehr.ThinkEhrClient
import com.github.markopi.ideafirstplugin.thinkehr.ThinkEhrTarget
import com.intellij.execution.lineMarker.RunLineMarkerContributor
import com.intellij.icons.AllIcons
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType

class AqlRunLineMarkerProvider : RunLineMarkerContributor() {

    override fun getInfo(e: PsiElement): Info? {
        if (e.elementType == AqlTextTokenTypes.AQL_TEXT) {
            return Info(RunAqlAction(e.text))
        } else {
            return null
        }
    }

    class RunAqlAction(val aql: String) : AnAction({ "Run aql query" }, AllIcons.RunConfigurations.TestState.Run) {

        private fun getThinkEhrTarget(): ThinkEhrTarget? {
            val settings = AqlSettingsState.INSTANCE
            if (settings.serverUrl.isBlank()) return null
            return ThinkEhrTarget(url = settings.serverUrl, username = settings.loginUsername, password = settings.loginPassword)
        }

        override fun actionPerformed(event: AnActionEvent) {
//            val n = Notification("AQL", "Started aql", NotificationType.INFORMATION)
//            Notifications.Bus.notify(n, event.project)
            val target = getThinkEhrTarget()
            if (target == null) {
                val n = Notification(
                    "AQL",
                    "AQL server not configured",
                    "Please configure aql server in project settings",
                    NotificationType.ERROR
                )
                Notifications.Bus.notify(n, event.project)
                return
            }

            try {
                val r = ThinkEhrClient.query(target, aql)
                val toolWindow = ToolWindowManager.getInstance(event.project!!).getToolWindow("AQL")!!
                toolWindow.activate {
                    AqlToolWindowFactory.updateTableValues(r)
                }

            } catch (e: Exception) {
                if (e !is AqlPluginException) {
                    log.error("Error calling ThinkEhr Server", e)
                }
                val n = Notification(
                    "AQL",
                    "ThinkEhr server error",
                    if (e is AqlPluginException) e.message ?: e.toString() else e.toString(),
                    NotificationType.ERROR
                )
                Notifications.Bus.notify(n, event.project)
                return
            }

        }


        companion object {
            private val log: Logger = Logger.getInstance(RunAqlAction::class.java)

        }
    }
}