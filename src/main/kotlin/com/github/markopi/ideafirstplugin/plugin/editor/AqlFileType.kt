package com.github.markopi.ideafirstplugin.plugin.editor

import com.github.markopi.ideafirstplugin.plugin.icons.AqlPluginIcons
import com.intellij.openapi.fileTypes.LanguageFileType
import javax.swing.Icon

class AqlFileType: LanguageFileType(AqlLanguage) {
    override fun getName(): String = "AQL"

    override fun getDescription(): String = "Archetype query language"

    override fun getDefaultExtension(): String = DEFAULT_ASSOCIATED_EXTENSIONS.first()

    override fun getIcon(): Icon = AqlPluginIcons.AqlFileType

    companion object {
        val DEFAULT_ASSOCIATED_EXTENSIONS = listOf("aql")

        @JvmStatic
        val INSTANCE = AqlFileType()
    }
}