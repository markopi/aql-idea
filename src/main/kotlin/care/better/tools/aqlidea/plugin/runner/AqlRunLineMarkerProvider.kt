package care.better.tools.aqlidea.plugin.runner

import care.better.tools.aqlidea.aql.LexedAqls
import care.better.tools.aqlidea.plugin.AqlUtils
import care.better.tools.aqlidea.plugin.editor.AqlTextTokenTypes
import care.better.tools.aqlidea.plugin.settings.AqlPluginHomeDir
import care.better.tools.aqlidea.plugin.settings.AqlServerConsoleHistory
import com.intellij.execution.lineMarker.RunLineMarkerContributor
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.vfs.VirtualFile
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
            return Info(RunAqlAction(e.containingFile.virtualFile, e.containingFile.text, e.startOffset))
        } else {
            return null
        }
    }

    class RunAqlAction(val virtualFile: VirtualFile?, val fileContents: String, val offset: Int) :
        AnAction("Run AQL Query", "Run AQL query on configured server", AllIcons.RunConfigurations.TestState.Run) {

        override fun actionPerformed(event: AnActionEvent) {
            val project = event.project ?: return
            val virtualFile = event.getRequiredData(CommonDataKeys.VIRTUAL_FILE)
            val aqlServer = AqlUtils.aqlServerForFile(virtualFile) ?: return

            val lexedAqls = LexedAqls.of(fileContents)
            val partToRun = lexedAqls.parts.first { it.offset >= offset }
            val aql = partToRun.lexed.aql
            addToHistory(aql)
            AqlQueryRunner.run(project, aqlServer, aql)
        }
        fun addToHistory(aql: String) {
            if (virtualFile==null) return
            val configuration = AqlPluginHomeDir.readAqlServerConsoleHistoryHistory(virtualFile.toNioPath())
            // remove old calls of this same aql
            configuration.history.removeAll { it.aql==aql }
            configuration.history.addFirst(AqlServerConsoleHistory.Item(System.currentTimeMillis(), aql))
            while (configuration.history.size>20) {
                configuration.history.removeLast()
            }
            AqlPluginHomeDir.writeAqlServerConsoleHistory(virtualFile.toNioPath(), configuration)
        }


    }
}
