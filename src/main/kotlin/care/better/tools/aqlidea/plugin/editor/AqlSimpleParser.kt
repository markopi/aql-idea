package care.better.tools.aqlidea.plugin.editor

import care.better.tools.aqlidea.plugin.editor.AqlTextTokenTypes.AQL_TEXT
import com.intellij.lang.ASTNode
import com.intellij.lang.PsiBuilder
import com.intellij.lang.PsiParser
import com.intellij.psi.tree.IElementType

class AqlSimpleParser : PsiParser {
    override fun parse(root: IElementType, builder: PsiBuilder): ASTNode {
        val rootMarker = builder.mark()
        while (builder.tokenType != null) {
            val tokenType = builder.tokenType!!
            val marker = builder.mark()
            builder.advanceLexer()
            marker.done(tokenType)
        }
        rootMarker.done(AQL_TEXT)
        return builder.treeBuilt
    }
}