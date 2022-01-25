package com.github.markopi.ideafirstplugin.plugin.editor

import com.intellij.ide.highlighter.custom.CustomHighlighterColors
import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.psi.CustomHighlighterTokenType
import com.intellij.psi.StringEscapesTokenTypes
import com.intellij.psi.tree.IElementType

class AqlSyntaxHighlighter: SyntaxHighlighterBase() {
    private val ourKeys: Map<IElementType, TextAttributesKey>
    init {
        val ourKeys = HashMap<IElementType, TextAttributesKey>()

        ourKeys[CustomHighlighterTokenType.KEYWORD_1] =
            CustomHighlighterColors.CUSTOM_KEYWORD1_ATTRIBUTES
        ourKeys[CustomHighlighterTokenType.KEYWORD_2] =
            CustomHighlighterColors.CUSTOM_KEYWORD2_ATTRIBUTES
        ourKeys[CustomHighlighterTokenType.KEYWORD_3] =
            CustomHighlighterColors.CUSTOM_KEYWORD3_ATTRIBUTES
        ourKeys[CustomHighlighterTokenType.KEYWORD_4] =
            CustomHighlighterColors.CUSTOM_KEYWORD4_ATTRIBUTES
        ourKeys[CustomHighlighterTokenType.NUMBER] =
            CustomHighlighterColors.CUSTOM_NUMBER_ATTRIBUTES
        ourKeys[CustomHighlighterTokenType.STRING] =
            CustomHighlighterColors.CUSTOM_STRING_ATTRIBUTES
        ourKeys[CustomHighlighterTokenType.SINGLE_QUOTED_STRING] =
            CustomHighlighterColors.CUSTOM_STRING_ATTRIBUTES
        ourKeys[StringEscapesTokenTypes.VALID_STRING_ESCAPE_TOKEN] =
            CustomHighlighterColors.CUSTOM_VALID_STRING_ESCAPE
        ourKeys[StringEscapesTokenTypes.INVALID_CHARACTER_ESCAPE_TOKEN] =
            CustomHighlighterColors.CUSTOM_INVALID_STRING_ESCAPE
        ourKeys[StringEscapesTokenTypes.INVALID_UNICODE_ESCAPE_TOKEN] =
            CustomHighlighterColors.CUSTOM_INVALID_STRING_ESCAPE
        ourKeys[CustomHighlighterTokenType.LINE_COMMENT] =
            CustomHighlighterColors.CUSTOM_LINE_COMMENT_ATTRIBUTES
        ourKeys[CustomHighlighterTokenType.MULTI_LINE_COMMENT] =
            CustomHighlighterColors.CUSTOM_MULTI_LINE_COMMENT_ATTRIBUTES

        this.ourKeys = ourKeys
    }
    override fun getHighlightingLexer(): Lexer {
        return CustomAqlLexer()

    }

    override fun getTokenHighlights(tokenType: IElementType): Array<TextAttributesKey> {
//        return when (tokenType) {
//            AqlTextTokenTypes.AQL_NUMBER -> arrayOf(AqlHighlighterColors.NUMBER)
//            AqlTextTokenTypes.AQL_STRING -> arrayOf(AqlHighlighterColors.STRING)
//            AqlTextTokenTypes.AQL_KEYWORD -> arrayOf(AqlHighlighterColors.KEYWORD)
//            AqlTextTokenTypes.AQL_SYMBOL -> arrayOf(AqlHighlighterColors.SYMBOL)
//            AqlTextTokenTypes.AQL_TEXT -> arrayOf(AqlHighlighterColors.TEXT)
//            else -> arrayOf(AqlHighlighterColors.TEXT)
//        }
        return pack(ourKeys[tokenType])

    }
}