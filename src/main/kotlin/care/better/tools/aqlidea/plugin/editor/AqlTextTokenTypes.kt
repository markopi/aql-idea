package care.better.tools.aqlidea.plugin.editor

import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.IFileElementType

object AqlTextTokenTypes {
    val AQL_FILE = IFileElementType(AqlLanguage)

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