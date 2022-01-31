package care.better.tools.aqlidea.plugin.editor

import com.intellij.lang.ASTFactory
import com.intellij.lang.ASTNode
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.IFileElementType
import org.joni.constants.internal.TokenType

object AqlTextTokenTypes {
    val AQL_FILE: IElementType = object : IFileElementType("AQL_FILE", AqlLanguage) {
        override fun parseContents(chameleon: ASTNode): ASTNode {
            return ASTFactory.leaf(AQL_TEXT, chameleon.chars)
        }
    }
    val AQL_TEXT = AqlElementType("AQL_TEXT")

    val AQL_KEYWORD = AqlElementType("AQL.KEYWORD")
    val AQL_RM_TYPE = AqlElementType("AQL.RM_TYPE")
    val AQL_IDENTIFIER = AqlElementType("AQL.IDENTIFIER")
    val AQL_WHITESPACE = AqlElementType("AQL.WHITESPACE")
    val AQL_COMMENT = AqlElementType("AQL.COMMENT")
    val AQL_STRING = AqlElementType("AQL.STRING")
    val AQL_SYMBOL = AqlElementType("AQL.SYMBOL")
    val AQL_SYMBOL_BRACE = AqlElementType("AQL.SYMBOL.BRACE")
    val AQL_SYMBOL_PAREN = AqlElementType("AQL.SYMBOL.PAREN")
    val AQL_SYMBOL_BRACKET = AqlElementType("AQL.SYMBOL.BRACKET")
    val AQL_NUMBER = AqlElementType("AQL.NUMBER")

    class AqlElementType(debugName: String): IElementType(debugName, AqlLanguage)
}