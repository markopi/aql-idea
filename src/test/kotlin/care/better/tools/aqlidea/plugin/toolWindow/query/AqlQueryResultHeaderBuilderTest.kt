package care.better.tools.aqlidea.plugin.toolWindow.query

import assertk.Assert
import assertk.assertThat
import assertk.assertions.*
import assertk.assertions.support.expected
import care.better.tools.aqlidea.thinkehr.ThinkEhrQueryResponse
import care.better.tools.aqlidea.ui.treetable.TreeTableNode
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.junit.Test

class AqlQueryResultHeaderBuilderTest {

    @Test
    fun simpleScalar() {
        val res = loadResponse("simple-scalar")
        val ttd = AqlQueryResultHeaderBuilder().build(res)

        assertThat(ttd.headers).extracting { it.name }.containsOnly("#0", "#1")
        assertThat(ttd.headers).extracting { it.children }.each { it.isEmpty() }
        assertThat(ttd.data).containsExactly(
            listOf("68ea6d53-c786-4f31-b68d-04d44f664d7f", "c878316d-5d6a-4204-8d5e-320b2a53732a::ihe.marand.si::1"),
            listOf("68ea6d53-c786-4f31-b68d-04d44f664d7f", "65a3c952-c1b5-4b07-8050-512c5bea0ceb::ihe.marand.si::1")
        )

    }

    @Test
    fun simpleScalarNamed() {
        val res = loadResponse("simple-scalar-named")
        val ttd = AqlQueryResultHeaderBuilder().build(res)

        assertThat(ttd.headers).extracting { it.name }.containsOnly("ehr_id", "uid")
        assertThat(ttd.headers).extracting { it.children }.each { it.isEmpty() }
        assertThat(ttd.data).containsExactly(
            listOf("68ea6d53-c786-4f31-b68d-04d44f664d7f", "c878316d-5d6a-4204-8d5e-320b2a53732a::ihe.marand.si::1"),
            listOf("68ea6d53-c786-4f31-b68d-04d44f664d7f", "65a3c952-c1b5-4b07-8050-512c5bea0ceb::ihe.marand.si::1")
        )
    }

    @Test
    fun ehr() {
        val res = loadResponse("ehr_id")
        val ttd = AqlQueryResultHeaderBuilder().build(res)

        assertThat(ttd.headers).extracting { it.name }.containsOnly("#0")
        val column = ttd.headers.first()
        val row = ttd.data.first()

        assertThat(column.children).extracting { it.name }
            .containsOnly("system_id", "ehr_id", "time_created", "ehr_status")

        column.children.first().let { system_id ->
            assertThat(system_id)
                .has(name = "system_id", dataIndex = -1, children = listOf("value"))
            assertThat(system_id.children.first())
                .has(name = "value", dataIndex = 0, children = listOf())
        }
        assertThat(row[0]).isEqualTo("ihe.marand.si")
        column.children[1].let { ehr_id ->
            assertThat(ehr_id)
                .has(name = "ehr_id", dataIndex = -1, children = listOf("value"))
            assertThat(ehr_id.children.first())
                .has(name = "value", dataIndex = 1, children = listOf())
        }
        assertThat(row[1]).isEqualTo("68ea6d53-c786-4f31-b68d-04d44f664d7f")

        column.children[3].let { ehr_status ->
            assertThat(ehr_status)
                .has(
                    name = "ehr_status",
                    dataIndex = -1,
                    children = listOf("uid", "subject", "queryable", "modifiable")
                )
            assertThat(ehr_status.children[1])
                .has(name = "subject", dataIndex = -1, children = listOf("external_ref"))
        }

    }

    fun Assert<TreeTableNode>.has(name: String, dataIndex: Int, children: List<String>) = given { actual ->
        if (name != actual.name)
            expected("Unexpected name", expected = name, actual = actual.name)
        if (dataIndex != actual.dataIndex)
            expected("Unexpected dataIndex", expected = dataIndex, actual = actual.dataIndex)
        val actualChildren = actual.children.map { it.name }
        if (children != actualChildren)
            expected("Unexpected children", expected = children, actual = actual.children)
    }


    private fun loadResponse(name: String): ThinkEhrQueryResponse {
        return javaClass.getResourceAsStream("response/$name.json").use {
            objectMapper.readValue(it, ThinkEhrQueryResponse::class.java)
        }
    }

    private companion object {
        val objectMapper = ObjectMapper().apply {
            disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            registerModule(KotlinModule())
        }
    }
}