package care.better.tools.aqlidea.plugin.settings

import com.intellij.openapi.fileEditor.impl.NonProjectFileWritingAccessExtension
import com.intellij.openapi.vfs.VirtualFile
import java.nio.file.Path

/**
 * Avoid write protection dialogs for aql console files.
 */
class AqlConsoleWriteUnlocker : NonProjectFileWritingAccessExtension {
    override fun isWritable(file: VirtualFile): Boolean {
        if (!file.isInLocalFileSystem) return false
        if (!file.path.endsWith(".aql")) return false
        val path = file.toNioPath()

        if (path.isUnderParentDir(AqlPluginHomeDir.homeDir())) return true

        return super.isWritable(file)
    }

    private fun Path.isUnderParentDir(dir: Path): Boolean {
        val absoluteParent = dir.toAbsolutePath()
        var path: Path? = this.toAbsolutePath()
        while (path != null) {
            if (path == absoluteParent) return true
            path = path.parent
        }
        return false
    }

}