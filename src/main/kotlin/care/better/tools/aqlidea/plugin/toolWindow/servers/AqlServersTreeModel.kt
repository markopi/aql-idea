package care.better.tools.aqlidea.plugin.toolWindow.servers

import com.intellij.util.ui.tree.AbstractTreeModel
import java.nio.file.Path

class AqlServersTreeModel : AbstractTreeModel() {
    override fun getRoot(): Any {
        TODO("Not yet implemented")
    }

    override fun getChild(parent: Any?, index: Int): Any {
        TODO("Not yet implemented")
    }

    override fun getChildCount(parent: Any?): Int {
        TODO("Not yet implemented")
    }

    override fun isLeaf(node: Any?): Boolean {
        TODO("Not yet implemented")
    }

    override fun getIndexOfChild(parent: Any?, child: Any?): Int {
        TODO("Not yet implemented")
    }

}

private sealed class AqlServersTreeNode {
    abstract fun getChildCount(): Int
    abstract fun getChild(index: Int): AqlServersTreeNode

    class RootTreeNode(val configuration: AqlServersConfiguration) : AqlServersTreeNode() {
        override fun getChildCount(): Int = configuration.servers.size

        override fun getChild(index: Int): AqlServersTreeNode = AqlServerTreeNode(configuration.servers[index])
    }

    class AqlServerTreeNode(val server: AqlServer) : AqlServersTreeNode() {

    }

    class ConsolesTreeNode(val server: AqlServer) : AqlServersTreeNode() {

    }

    class ConsoleTreeNode(val server: AqlServer, val file: Path) : AqlServersTreeNode() {

    }
}


