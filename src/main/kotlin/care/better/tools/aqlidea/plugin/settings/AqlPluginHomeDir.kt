package care.better.tools.aqlidea.plugin.settings

import care.better.tools.aqlidea.plugin.toolWindow.AqlServer
import org.apache.commons.lang3.SystemUtils
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

@Deprecated("")
object AqlPluginHomeDir {
    fun homeDir(): Path {
        val userHomeDir = Paths.get(System.getProperty("user.home"))
        val pluginConfDir = userHomeDir.resolve(".aql-idea")
        if (!Files.exists(pluginConfDir)) {
            Files.createDirectory(pluginConfDir)
            if (SystemUtils.IS_OS_WINDOWS) {
                Files.setAttribute(pluginConfDir, "dos:hidden", true)
            }
        }
        return pluginConfDir
    }

    fun getConsoleFile(server: AqlServer): Path {
        val consoleDir = homeDir().resolve("servers").resolve(server.id).resolve("consoles")
        Files.createDirectories(consoleDir)
        val consoleFile = consoleDir.resolve("console.aql")
        if (!Files.exists(consoleFile)) {
            Files.createFile(consoleFile)
        }
        return consoleFile
    }
}