package care.better.tools.aqlidea.plugin.settings

import care.better.tools.aqlidea.plugin.AqlUtils
import care.better.tools.aqlidea.plugin.toolWindow.servers.AqlServer
import care.better.tools.aqlidea.plugin.toolWindow.servers.AqlServersConfiguration
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileChooser.ex.LocalFsFinder.VfsFile
import com.intellij.openapi.util.io.FileAttributes
import com.intellij.openapi.vfs.VfsUtil
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import org.apache.commons.io.file.PathUtils
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

    fun cleanup(configuration: AqlServersConfiguration) {
        val existingServerIds = configuration.servers.map { it.id }.toSet()
        val serversPath = homeDir().resolve("servers")
        if (Files.exists(serversPath))
            for (path in Files.list(serversPath).use { it.toList() }) {
                val name = path.fileName.toString()
                if (name !in existingServerIds) {
                    val file = VfsUtil.findFile(path, false)!!
                    ApplicationManager.getApplication().runWriteAction {
                        file.delete(ApplicationManager.getApplication())
                    }
                }
            }
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
        if (!Files.isDirectory(consoleDir)) return listOf()
        return Files.list(consoleDir).use { it.toList() }
    }

    fun deleteConsoleFile(server: AqlServer, path: Path) {
        val serverConsolesDir = serverConsolesDir(server)
        if (serverConsolesDir != path.parent) return
        Files.deleteIfExists(path)
    }

    fun createConsoleFile(server: AqlServer, name: String): Path {
        var name=name
        if (!name.endsWith(".aql")) name=name+".aql"
        val serverConsolesDir = serverConsolesDir(server)
        if (!Files.exists(serverConsolesDir)) Files.createDirectories(serverConsolesDir)

        val path = serverConsolesDir.resolve(name)
        Files.createFile(path)
        return path
    }

    fun getMainConsoleFile(server: AqlServer): Path {
        val name = AqlUtils.sanitizeFilename(server.name) + ".aql"
        val path = serverDir(server, true).resolve(name)
        if (!Files.exists(path)) Files.createFile(path)
        return path
    }
}