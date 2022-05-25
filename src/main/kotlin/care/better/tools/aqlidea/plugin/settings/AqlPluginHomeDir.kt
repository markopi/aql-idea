package care.better.tools.aqlidea.plugin.settings

import care.better.tools.aqlidea.plugin.toolWindow.servers.AqlServer
import org.apache.commons.lang3.SystemUtils
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.streams.toList


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

    private fun serverDir(server: AqlServer, createIfNeeded: Boolean): Path {
        val path = homeDir().resolve("servers").resolve(server.id)
        if (createIfNeeded && !Files.exists(path)) {
            Files.createDirectories(path)
        }
        return path
    }

    private fun serverConsolesDir(server: AqlServer) = serverDir(server, false).resolve("consoles")

    fun listConsoleFiles(server: AqlServer): List<Path> {
        val consoleDir = serverConsolesDir(server)
        if (Files.isDirectory(consoleDir)) return listOf()
        return Files.list(consoleDir).use { it.toList() }
    }

    fun deleteConsoleFile(server: AqlServer, path: Path) {
        val serverConsolesDir = serverConsolesDir(server)
        if (serverConsolesDir != path.parent) return
        Files.deleteIfExists(path)
    }

    fun createConsoleFile(server: AqlServer, name: String): Path {
        val serverConsolesDir = serverConsolesDir(server)
        if (!Files.exists(serverConsolesDir)) Files.createDirectories(serverConsolesDir)

        val path = serverConsolesDir.resolve(name + ".aql")
        Files.createFile(path)
        return path
    }

    private fun isInSubDirectory(dir: Path, file: Path): Boolean {
        val base = dir.toAbsolutePath()

        var parentFile: Path? = file.toAbsolutePath()
        while (parentFile != null) {
            if (base == parentFile) {
                return true
            }
            parentFile = parentFile.parent
        }
        return false
    }
}