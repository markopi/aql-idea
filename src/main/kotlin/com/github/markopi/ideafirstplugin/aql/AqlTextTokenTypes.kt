package com.github.markopi.ideafirstplugin.aql

import com.intellij.lang.ASTFactory
import com.intellij.lang.ASTNode
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.IFileElementType

object AqlTextTokenTypes {
    val AQL_FILE: IElementType = object : IFileElementType("AQL_FILE", AqlLanguage) {
        override fun parseContents(chameleon: ASTNode): ASTNode {
            return ASTFactory.leaf(AQL_TEXT, chameleon.chars)
        }
    }
    val AQL_TEXT = IElementType("AQL_TEXT", AqlLanguage)
}