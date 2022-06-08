package care.better.tools.aqlidea.plugin.settings

import care.better.tools.aqlidea.plugin.AqlUtils
import com.intellij.openapi.fileEditor.impl.NonProjectFileWritingAccessExtension
import com.intellij.openapi.vfs.VirtualFile

/**
 * Avoid write protection dialogs for aql console files.
 */
class AqlConsoleWriteUnlocker : NonProjectFileWritingAccessExtension {
    override fun isWritable(file: VirtualFile): Boolean {
        if (!file.isInLocalFileSystem) return false
        if (!file.path.endsWith(".aql")) return false
        val path = file.toNioPath()

        if (AqlUtils.parentPathContainsDir(AqlPluginConfigurationService.homeDir(), path)) return true

        return super.isWritable(file)
    }


}