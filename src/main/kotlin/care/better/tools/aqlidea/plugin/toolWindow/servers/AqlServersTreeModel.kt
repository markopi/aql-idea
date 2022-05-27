package care.better.tools.aqlidea.plugin.toolWindow.servers

import care.better.tools.aqlidea.plugin.icons.AqlPluginIcons
import care.better.tools.aqlidea.plugin.settings.AqlPluginHomeDir
import com.intellij.icons.AllIcons
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.util.loadElementAndKeepBoundaryWhitespace
import com.intellij.util.ui.tree.AbstractTreeModel
import org.apache.commons.io.FilenameUtils
import java.nio.file.Files
import java.nio.file.Path
import javax.swing.Icon
import javax.swing.tree.DefaultMutableTreeNode

fun buildTreeNodeModel(configuration: AqlServersConfiguration): DefaultMutableTreeNode {
    fun addChildren(parent: AqlServersTreeNode) {
        for (readChild in parent.readChildren()) {
            parent.add(readChild)
            addChildren(readChild)
        }
    }

    val root = AqlServersTreeNode.RootTreeNode(configuration)
    addChildren(root)
    return root
}

sealed class AqlServersTreeNode() : DefaultMutableTreeNode() {
    abstract val icon: Icon?
    abstract val label: String
    abstract fun readChildren(): List<AqlServersTreeNode>
    class RootTreeNode(val configuration: AqlServersConfiguration) : AqlServersTreeNode() {
        override val icon: Icon = AllIcons.General.Tree
        override val label: String = "Servers"
        override fun readChildren(): List<AqlServerTreeNode> = configuration.servers.map { AqlServerTreeNode(it) }
    }

    class AqlServerTreeNode(val server: AqlServer) : AqlServersTreeNode() {
        override val icon: Icon? = null
        override val label: String = server.name
        override fun readChildren(): List<AqlServersTreeNode> = listOf(ConsolesTreeNode(server))
    }

    class ConsolesTreeNode(val server: AqlServer) : AqlServersTreeNode() {
        override val icon: Icon = AllIcons.Nodes.Folder
        override val label: String = "Consoles"
        override fun readChildren(): List<ConsoleTreeNode> = loadConsoles()

        fun createNewConsole(): ConsoleTreeNode {
            val existingFiles = AqlPluginHomeDir.listConsoleFiles(server)
            val existingNames =
                existingFiles.map { FilenameUtils.removeExtension(it.fileName.toString()).toLowerCase() }.toSet()

            val nameBase = "Console"
            var index = 1
            var name = "$nameBase $index"
            while (name.toLowerCase() in existingNames) {
                index++
                name = "$nameBase $index"
            }
            val newFile = AqlPluginHomeDir.createConsoleFile(server, name)
            val newNode = ConsoleTreeNode(server, newFile)
            return newNode
        }


        private fun loadConsoles() = AqlPluginHomeDir.listConsoleFiles(server).map { ConsoleTreeNode(server, it) }
    }

    class ConsoleTreeNode(val server: AqlServer, var file: Path) : AqlServersTreeNode() {
        override val icon: Icon = AqlPluginIcons.AqlFileType
        override val label: String get() = file.fileName.toString()
        override fun readChildren(): List<AqlServersTreeNode> = listOf()

        fun renameConsole(project: Project?): Boolean {
            val oldFileName = file.fileName.toString()
            val oldExtension = FilenameUtils.getExtension(oldFileName)
            val oldName = FilenameUtils.removeExtension(oldFileName)
            val renamedValue =
                Messages.showInputDialog(project, "New name for console '$oldName'", "Rename Console", null)
            if (renamedValue == null || renamedValue.isBlank()) return false

            val newFileName = "$renamedValue.$oldExtension"
            val virtualFile = VfsUtil.findFile(file, false)!!
            val newFile = file.parent.resolve(newFileName)
            if (Files.exists(newFile)) return false

            ApplicationManager.getApplication().runWriteAction {
                virtualFile.rename(ApplicationManager.getApplication(), newFileName)
            }

            if (!Files.exists(newFile)) return false

            file = newFile
            return true
        }

        init {
            allowsChildren = false
        }
    }
}


