package care.better.tools.aqlidea.plugin.runner

import care.better.tools.aqlidea.aql.LexedAqls
import care.better.tools.aqlidea.plugin.AqlPluginException
import care.better.tools.aqlidea.plugin.AqlUtils
import care.better.tools.aqlidea.plugin.editor.AqlTextTokenTypes
import care.better.tools.aqlidea.plugin.service.ThinkEhrClientService
import care.better.tools.aqlidea.plugin.toolWindow.AqlToolWindowFactory
import com.intellij.execution.lineMarker.RunLineMarkerContributor
import com.intellij.icons.AllIcons
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.LeafPsiElement

class AqlRunLineMarkerProvider : RunLineMarkerContributor() {

    override fun getInfo(e: PsiElement): Info? {
        if (e is LeafPsiElement && (e.elementType == AqlTextTokenTypes.AQL_KEYWORD) && e.text.equals(
                "select",
                ignoreCase = true
            )
        ) {
//        if (e.elementType == AqlTextTokenTypes.AQL_TEXT) {
            return Info(RunAqlAction(e.containingFile.text, e.startOffset))
        } else {
            return null
        }
    }

    class RunAqlAction(val fileContents: String, val offset: Int) :
        AnAction("Run AQL Query", "Run AQL query on configured server", AllIcons.RunConfigurations.TestState.Run) {

        override fun actionPerformed(event: AnActionEvent) {
            val project = event.project ?: return
            val virtualFile = event.getRequiredData(CommonDataKeys.VIRTUAL_FILE)
            val aqlServer = AqlUtils.aqlServerForFile(virtualFile) ?: return

            try {
                val lexedAqls = LexedAqls.of(fileContents)
                val partToRun = lexedAqls.parts.first { it.offset >= offset }
                val aql = partToRun.lexed.aql
                val thinkehr = ThinkEhrClientService.INSTANCE
                val target = thinkehr.toThinkEhrTarget(aqlServer)

                AqlToolWindowFactory.showQueryStart(project)
                try {
                    val r = thinkehr.client.query(target, aql)
                    AqlToolWindowFactory.showQueryResult(project, r)
                } catch (e: Exception) {
                    AqlToolWindowFactory.showQueryError(project, e)
                }
            } catch (e: Exception) {
                if (e !is AqlPluginException) {
                    log.error("Error calling ThinkEhr Server", e)
                }
                val text = if (e is AqlPluginException) e.message ?: e.toString() else e.toString()
                AqlUtils.notify("ThinkEhr server error", text, NotificationType.ERROR, event.project)
                return
            }
        }
    }


    companion object {
        private val log: Logger = Logger.getInstance(RunAqlAction::class.java)

    }
}
