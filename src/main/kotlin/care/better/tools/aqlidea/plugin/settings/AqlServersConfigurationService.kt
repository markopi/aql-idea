package care.better.tools.aqlidea.plugin.settings

import org.slf4j.LoggerFactory
import java.io.IOException
import java.nio.file.Files

class AqlServersConfigurationService {
    private val log = LoggerFactory.getLogger(AqlServersConfiguration::class.java)
    private var current: AqlServersSerialized = loadFromFile()

    fun save(configuration: AqlServersConfiguration) {
        val serialized = toSerialized(configuration)
        val filename = AqlPluginConfigurationService.homeDir().resolve("servers.json")
        AqlPluginConfigurationService.objectMapper.writeValue(filename.toFile(), serialized)
        current = serialized
        AqlPluginConfigurationService.cleanup(configuration)
    }

    fun load(): AqlServersConfiguration {
        return fromSerialized(current)
    }
    private fun loadFromFile(): AqlServersSerialized {
        val filename = AqlPluginConfigurationService.homeDir().resolve("servers.json")

        return if (Files.exists(filename)) {
            try {
                AqlPluginConfigurationService.objectMapper.readValue(
                    filename.toFile(),
                    AqlServersSerialized::class.java
                )
            } catch (e: IOException) {
                log.error("Could not load aql servers configurations, defaulting to empty", e)
                newEmptyAqlServers()
            }
        } else newEmptyAqlServers()
    }

    private fun newEmptyAqlServers(): AqlServersSerialized = AqlServersSerialized(listOf())

    private fun fromSerialized(from: AqlServersSerialized) = AqlServersConfiguration(
        from.servers.map { fromSerialized(it) }.toMutableList()
    )

    private fun fromSerialized(from: AqlServerSerialized) = AqlServer(
        id = from.id,
        name = from.name,
        serverUrl = from.serverUrl,
        username = from.username,
        password = from.password,
        default = false
    )

    private fun toSerialized(from: AqlServersConfiguration): AqlServersSerialized = AqlServersSerialized(
        servers = from.servers.map { toSerialized(it) }
    )

    private fun toSerialized(from: AqlServer) = AqlServerSerialized(
        id = from.id,
        name = from.name,
        serverUrl = from.serverUrl,
        username = from.username,
        password = from.password,
    )


    private data class AqlServersSerialized(val servers: List<AqlServerSerialized>)
    private data class AqlServerSerialized(
        val id: String,
        val name: String,
        val serverUrl: String,
        val username: String,
        val password: String
    )

    companion object {
        val INSTANCE = AqlServersConfigurationService()
    }
}