package care.better.tools.aqlidea.aql.autocomplete

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.containsOnly
import assertk.assertions.hasSize
import assertk.assertions.isTrue
import care.better.tools.aqlidea.aql.LexedAql
import care.better.tools.aqlidea.thinkehr.ThinkEhrArchetypeDetails
import care.better.tools.aqlidea.thinkehr.ThinkEhrArchetypeInfo
import care.better.tools.aqlidea.thinkehr.ThinkEhrClient
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock


class AqlServerAutocompletionProviderTest {
    @Test
    fun autocompleteArchetypeVarOnly() {
        val lexedAql = LexedAql.of("select c from composition c", false)
        val autocompletions = subject.getAutocompletions(lexedAql, lexedAql.aql.length, mock())


        assertThat(autocompletions)
            .transform { it.map { (it as AqlAutocompletion.Archetype).archetypeId } }
            .containsExactly("openEHR-EHR-COMPOSITION.report.v1")

    }

    @Test
    fun autocompleteArchetypePartial() {
        val lexedAql = LexedAql.of("select c from observation o[openEHR-EHR-OBSERVATION.gl", false)
        val autocompletions = subject.getAutocompletions(lexedAql, lexedAql.aql.length, mock())

        assertThat(autocompletions)
            .transform { it.map { (it as AqlAutocompletion.Archetype).archetypeId } }
            .containsExactly("openEHR-EHR-OBSERVATION.global_zn.v1")
    }

    @Test
    fun autocompletePathFromVarnameOnly() {
        val lexedAql = LexedAql.of("select o from observation o[openEHR-EHR-OBSERVATION.ideal_body_mass.v1]", false)
        val completions = subject.getAutocompletions(lexedAql, 8, mock())
        val pathCompletions = completions.filterIsInstance<AqlAutocompletion.Path>()

        assertThat(pathCompletions).hasSize(3)
        assertThat(pathCompletions.map { listOf(it.path, it.type, it.name) }).containsOnly(
            listOf("/data[at0001]/events[at0002]/data[at0003]/items[at0004]/value", "DV_QUANTITY", "Ideal body mass"),
            listOf("/data[at0001]/events[at0002]/time", "DV_DATE_TIME", "Time"),
            listOf("/subject", "PARTY_PROXY", "Subject"),
        )
        assertThat(pathCompletions.all { it.start == 8 && it.end == 8 }).isTrue()
    }

    @Test
    fun autocompletePathFromPartialPath() {
        val lexedAql = LexedAql.of("select o/data from observation o[openEHR-EHR-OBSERVATION.ideal_body_mass.v1]", false)
        val completions = subject.getAutocompletions(lexedAql, 13, mock())
        val pathCompletions = completions.filterIsInstance<AqlAutocompletion.Path>()

        assertThat(pathCompletions).hasSize(2)
        assertThat(pathCompletions.map { listOf(it.path, it.type, it.name) }).containsOnly(
            listOf("/data[at0001]/events[at0002]/data[at0003]/items[at0004]/value", "DV_QUANTITY", "Ideal body mass"),
            listOf("/data[at0001]/events[at0002]/time", "DV_DATE_TIME", "Time"),
        )
        assertThat(pathCompletions.all { it.start == 8 && it.end == 13 }).isTrue()
    }

    companion object {
        val subject = AqlServerAutocompletionProvider(createTestThinkEhrClient())

        private fun readFlatArchetypePaths(objectMapper: ObjectMapper, archetypeId: String): ThinkEhrArchetypeDetails {
            val filename = "details/$archetypeId.json"
            val stream = AqlServerAutocompletionProviderTest::class.java.getResourceAsStream(filename)
                ?: throw IllegalArgumentException("Missing test resource file $filename")
            return objectMapper.readValue(stream, ThinkEhrArchetypeDetails::class.java)
        }

        private fun createTestThinkEhrClient(): ThinkEhrClient {
            val objectMapper = ObjectMapper().apply {
                registerModule(KotlinModule())
            }
            val archetypeInfoList: List<ThinkEhrArchetypeInfo> = objectMapper.readValue(
                AqlServerAutocompletionProviderTest::class.java.getResourceAsStream("ThinkEhrClient-listArchetypeInfos.json"),
                objectMapper.typeFactory.constructCollectionType(
                    ArrayList::class.java,
                    ThinkEhrArchetypeInfo::class.java
                )
            )

            return mock {
                on { listArchetypeInfos(any()) } doReturn archetypeInfoList
                on { getArchetypeDetails(any(), any()) } doAnswer { inv ->
                    readFlatArchetypePaths(
                        objectMapper,
                        inv.getArgument(1) as String
                    )
                }
            }

        }
    }

}