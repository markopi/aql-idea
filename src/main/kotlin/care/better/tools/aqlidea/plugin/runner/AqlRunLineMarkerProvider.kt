package care.better.tools.aqlidea.plugin.runner

import care.better.tools.aqlidea.aql.LexedAqls
import care.better.tools.aqlidea.plugin.AqlPluginException
import care.better.tools.aqlidea.plugin.editor.AqlTextTokenTypes
import care.better.tools.aqlidea.plugin.service.ThinkEhrClientService
import care.better.tools.aqlidea.plugin.toolWindow.AqlToolWindowFactory
import com.intellij.execution.lineMarker.RunLineMarkerContributor
import com.intellij.icons.AllIcons
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.util.elementType

class AqlRunLineMarkerProvider : RunLineMarkerContributor() {

    override fun getInfo(e: PsiElement): Info? {
        if ( e is LeafPsiElement && (e.elementType==AqlTextTokenTypes.AQL_KEYWORD) && e.text.equals("select", ignoreCase = true)) {
//        if (e.elementType == AqlTextTokenTypes.AQL_TEXT) {
            return Info(RunAqlAction(e.containingFile.text, e.startOffset))
        } else {
            return null
        }
    }

    class RunAqlAction(val fileContents: String, val offset: Int) : AnAction("Run AQL Query", "Run AQL query on configured server", AllIcons.RunConfigurations.TestState.Run) {

        override fun actionPerformed(event: AnActionEvent) {
            try {
                val lexedAqls = LexedAqls.of(fileContents)
                val partToRun = lexedAqls.parts.first { it.offset>=offset }
                val aql = partToRun.lexed.aql
                val thinkehr = ApplicationManager.getApplication().getService(ThinkEhrClientService::class.java)
                val target = thinkehr.getTarget(event.project) ?: return

                val r = thinkehr.client.query(target, aql)
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