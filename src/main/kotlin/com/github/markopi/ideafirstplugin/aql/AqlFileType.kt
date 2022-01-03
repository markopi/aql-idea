package com.github.markopi.ideafirstplugin.aql

import com.github.markopi.ideafirstplugin.icons.AqlPluginIcons
import com.intellij.openapi.fileTypes.LanguageFileType
import javax.swing.Icon

class AqlFileType: LanguageFileType(AqlLanguage) {
    override fun getName(): String = "AQL"

    override fun getDescription(): String = "Archetype query language"

    override fun getDefaultExtension(): String = "aql"

    override fun getIcon(): Icon = AqlPluginIcons.AqlFileType
    companion object {
        @JvmStatic
        val INSTANCE = AqlFileType()
    }
}