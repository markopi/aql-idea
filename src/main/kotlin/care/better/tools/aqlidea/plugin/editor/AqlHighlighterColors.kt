package care.better.tools.aqlidea.plugin.editor

import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey

object AqlHighlighterColors {
    val COMMENT = TextAttributesKey.createTextAttributesKey(
        "AQL.COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT
    )
    val KEYWORD = TextAttributesKey.createTextAttributesKey(
        "AQL.KEYWORD", DefaultLanguageHighlighterColors.KEYWORD
    )
    val TEXT = TextAttributesKey.createTextAttributesKey(
        "AQL.TEXT", DefaultLanguageHighlighterColors.IDENTIFIER
    )
    val NUMBER = TextAttributesKey.createTextAttributesKey(
        "AQL.NUMBER", DefaultLanguageHighlighterColors.NUMBER
    )
    val SYMBOL = TextAttributesKey.createTextAttributesKey(
        "AQL.SYMBOL", DefaultLanguageHighlighterColors.PREDEFINED_SYMBOL
    )
    val STRING = TextAttributesKey.createTextAttributesKey(
        "AQL.STRING", DefaultLanguageHighlighterColors.STRING
    )
}