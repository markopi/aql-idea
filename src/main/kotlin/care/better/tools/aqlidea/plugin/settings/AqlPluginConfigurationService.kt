package care.better.tools.aqlidea.plugin.settings

import care.better.tools.aqlidea.plugin.AqlUtils
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.vfs.VfsUtil
import org.apache.commons.io.FilenameUtils
import org.apache.commons.lang3.SystemUtils
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.streams.toList


object AqlPluginConfigurationService {
    val objectMapper = ObjectMapper().apply {
        registerModule(KotlinModule())
        setSerializationInclusion(JsonInclude.Include.NON_NULL)
    }

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

    /** Remove any orphaned servers that are no longer in configuration.
     * Should be called only after the configuration is saved. */
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

    fun readAqlServerConsoleHistoryHistory(consolePath: Path): AqlServerConsoleHistory {
        val historyPath = Paths.get(FilenameUtils.removeExtension(consolePath.toString()) + ".history")
        if (!Files.exists(historyPath)) {
            return AqlServerConsoleHistory(ArrayDeque(20))
        }
        val history = try {
            objectMapper.readValue(historyPath.toFile(), AqlServerConsoleHistory::class.java)
        } catch (e: Exception) {
            Files.delete(historyPath)
            AqlServerConsoleHistory(ArrayDeque(20))
        }
        return history
    }

    fun writeAqlServerConsoleHistory(consolePath: Path, history: AqlServerConsoleHistory) {
        val historyPath = Paths.get(FilenameUtils.removeExtension(consolePath.toString()) + ".history")
        objectMapper.writeValue(historyPath.toFile(), history)
    }

    fun createConsoleFile(server: AqlServer, name: String): Path {
        var name = name
        if (!name.endsWith(".aql")) name = name + ".aql"
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