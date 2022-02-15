package care.better.tools.aqlidea.aql.autocomplete

import care.better.tools.aqlidea.aql.AqlInfo
import care.better.tools.aqlidea.aql.AqlInfo.FromVar
import care.better.tools.aqlidea.aql.AqlInfo.SelectVar
import care.better.tools.aqlidea.aql.AqlInfoReader
import care.better.tools.aqlidea.aql.LexedAql
import care.better.tools.aqlidea.thinkehr.ThinkEhrClient
import care.better.tools.aqlidea.thinkehr.ThinkEhrTarget

class AqlServerAutocompletionProvider(private val client: ThinkEhrClient) {

    fun getAutocompletions(lexedAql: LexedAql, offset: Int, server: ThinkEhrTarget): List<AqlAutocompletion> {

        val aqlInfo = AqlInfoReader.readInfo(lexedAql)
        val fromVars = aqlInfo.vars.values.filterIsInstance<FromVar>()

        val fromVar = fromVars.find { offset in it.start..it.end }
        if (fromVar != null) {
            return getArchetypeAutocompletions(fromVar, server)
        }
        val varPath = aqlInfo.varPaths.find { offset in it.start..it.end }
        if (varPath != null) {
            return getPathAutocompletions(aqlInfo, varPath, server)
        }
        return listOf()
    }


    private fun getArchetypeAutocompletions(
        fromVar: FromVar,
        server: ThinkEhrTarget
    ): List<AqlAutocompletion.Archetype> {
        val allArchetypes = client.listArchetypeInfos(server)
        val archetypesOfType = allArchetypes.filter { it.type.equals(fromVar.rmType, ignoreCase = true) }
        var candidates = archetypesOfType
        if (fromVar.predicate != null) {
            val predicate: String = fromVar.predicate
            candidates =
                candidates.filter { predicate.startsWith(it.archetypeId) || it.archetypeId.startsWith(predicate) }
        }

        return candidates.map {
            AqlAutocompletion.Archetype(
                it.archetypeId,
                it.names.first(),
                fromVar.name,
                it.type,
                fromVar.start + fromVar.name.length,
                fromVar.end
            )
        }
    }

    private fun getPathAutocompletions(
        aqlInfo: AqlInfo,
        varPath: AqlInfo.VarPath,
        server: ThinkEhrTarget
    ): List<AqlAutocompletion> {
        val (fromVar, pathPrefix) = getReferencedVar(aqlInfo, varPath.varName)
        // did not find the referred root variable, do not provide autocompletions
        if (fromVar == null) return listOf()

        val archetypeId = fromVar.predicate?.takeIf { it.startsWith("openEHR-", ignoreCase = true) }
        if (archetypeId == null) return listOf()

        val currentPath = varPath.path ?: ""
        val archetypeDetails = client.getArchetypeDetails(server, archetypeId)

        val start = varPath.start + varPath.varName.length
        val end = varPath.end

        val candidateFields = archetypeDetails.fields.filter { it.aql.startsWith(currentPath) }

        return candidateFields.map {
            AqlAutocompletion.Path(it.aql, it.type, it.names.firstOrNull(), start, end, varPath.clause)
        }

    }

    private fun getReferencedVar(aqlInfo: AqlInfo, varName: String): Pair<FromVar?, String> {
        var pathPrefix = ""
        var curVar = aqlInfo.vars[varName]
        while (true) {
            when (curVar) {
                null -> return null to ""
                is FromVar -> return curVar to pathPrefix
                is SelectVar -> {
                    pathPrefix = curVar.column.varPath.path.orEmpty() + pathPrefix
                    curVar = aqlInfo.vars[curVar.name]
                }
            }.run { }
        }
    }

}
