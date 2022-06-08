package care.better.tools.aqlidea.plugin.console

import care.better.tools.aqlidea.plugin.AqlUtils
import care.better.tools.aqlidea.plugin.settings.AqlServersConfigurationService
import com.intellij.execution.console.ConsoleRootType

class AqlRootType : ConsoleRootType("AQL", "AQL Console") {

    override fun getDefaultFileExtension(): String {
        return "aql"
    }

    override fun getContentPathName(id: String): String {
        val conf = AqlServersConfigurationService.INSTANCE.load()
        val server = conf.servers.firstOrNull { it.id == id }
        return AqlUtils.sanitizeFilename(server?.name ?: id)
    }


    companion object {
        val INSTANCE: AqlRootType
            get() = findByClass(AqlRootType::class.java)
    }

}