package care.better.tools.aqlidea.ui.treetable

class TreeTableNode (
    val name: String,
    // column index of data, only for leaf nodes
    val className: String?,
    val dataIndex: Int,
    val children: List<TreeTableNode>
) {
    val isLeaf: Boolean get() = children.isEmpty()

    override fun toString(): String {
        val r = StringBuilder()
        r.append(name)
        if (className!=null) {
            r.append(":").append(className)
        }
        if (dataIndex>=0) {
            r.append("=@").append(dataIndex)
        }
        if (children.isNotEmpty()) {
            r.append("=").append(children.map { it.toString() })
        }
        return r.toString()
    }
}

