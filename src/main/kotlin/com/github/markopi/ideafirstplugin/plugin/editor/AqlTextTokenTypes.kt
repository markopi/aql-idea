package com.github.markopi.ideafirstplugin.plugin.editor

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
    val AQL_STRING = AqlElementType("AQL.STRING")
    val AQL_SYMBOL = AqlElementType("AQL.SYMBOL")
    val AQL_NUMBER = AqlElementType("AQL.NUMBER")

    class AqlElementType(debugName: String): IElementType(debugName, AqlLanguage)
}