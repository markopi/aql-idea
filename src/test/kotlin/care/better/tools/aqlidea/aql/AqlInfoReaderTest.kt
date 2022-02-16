package care.better.tools.aqlidea.aql

import care.better.tools.aqlidea.aql.AqlInfo.Column
import care.better.tools.aqlidea.aql.AqlInfo.VarPath
import org.junit.Test
import kotlin.test.assertEquals


internal class AqlInfoReaderTest {
    @Test
    fun readSimpleColumn() {
        val info = AqlInfoReader.readInfo("select c from composition c")
        assertEquals(listOf(Column(VarPath("c", null, 7, 8, AqlClause.select), null)), info.columns)
    }

    @Test
    fun readTwoSimpleColumns() {
        val info = AqlInfoReader.readInfo("select c, o from composition c contains observation o")
        assertEquals(
            listOf(
                Column(VarPath("c", null, 7, 8, AqlClause.select), null),
                Column(VarPath("o", null, 10, 11, AqlClause.select), null)
            ), info.columns
        )
    }

    @Test
    fun readAliasedColumn() {
        val info = AqlInfoReader.readInfo("select c as comp from composition c")
        assertEquals(listOf(Column(VarPath("c", null, 7, 8, AqlClause.select), "comp")), info.columns)
    }

    @Test
    fun readColumnWithPath() {
        val info = AqlInfoReader.readInfo("select c/template_id/value from composition c")
        assertEquals(listOf(Column(VarPath("c", "/template_id/value", 7, 26, AqlClause.select), null)), info.columns)
    }

    @Test
    fun readColumnWithComplexPath() {
        val info =
            AqlInfoReader.readInfo("select a/data[at0001, 'Data']/items[at0002 and name/value='Test']/value/value from composition c contains observation a")
        assertEquals(
            listOf(
                Column(
                    VarPath("a", "/data[at0001, 'Data']/items[at0002 and name/value='Test']/value/value", 7, 77, AqlClause.select),
                    null
                )
            ), info.columns
        )
    }

    @Test
    fun readColumnWithComplexPathAndAlias() {
        val info =
            AqlInfoReader.readInfo("select a/data[at0001, 'Data']/items[at0002 and name/value='Test']/value/value as test from composition c contains observation a")
        assertEquals(
            listOf(
                Column(
                    VarPath("a", "/data[at0001, 'Data']/items[at0002 and name/value='Test']/value/value", 7, 77, AqlClause.select),
                    "test"
                )
            ), info.columns
        )
    }

    @Test
    fun readMixedColumns() {
        val info =
            AqlInfoReader.readInfo("select c/template_id/value as template_id, c as comp, a, a/data[at0001, 'Data']/items[at0002 and name/value='Test']/value/value as test from composition c contains observation a")
        assertEquals(
            listOf(
                Column(VarPath("c", "/template_id/value", 7, 26, AqlClause.select), "template_id"),
                Column(VarPath("c", null, 43, 44, AqlClause.select), "comp"),
                Column(VarPath("a", null, 54, 55, AqlClause.select), null),
                Column(
                    VarPath("a", "/data[at0001, 'Data']/items[at0002 and name/value='Test']/value/value", 57, 127, AqlClause.select),
                    "test"
                )
            ),
            info.columns
        )
    }

    @Test
    fun readSimpleVar() {
        val info = AqlInfoReader.readInfo("select c from composition c")
        assertEquals(mapOf("c" to AqlInfo.FromVar("c", "composition", null, 26, 27)), info.vars)
    }

    @Test
    fun readTwoSimpleVars() {
        val info = AqlInfoReader.readInfo("select c from composition c contains observation o")
        assertEquals(
            mapOf(
                "c" to AqlInfo.FromVar("c", "composition", null, 26, 27),
                "o" to AqlInfo.FromVar("o", "observation", null, 49, 50)
            ), info.vars
        )
    }

    @Test
    fun readVarWithArchetypeId() {
        val info = AqlInfoReader.readInfo("select c from composition c[openEHR-EHR-COMPOSITION.encounter.v1]")
        assertEquals(
            mapOf("c" to AqlInfo.FromVar("c", "composition", "openEHR-EHR-COMPOSITION.encounter.v1", 26, 64)),
            info.vars
        )
    }

    @Test
    fun readTwoVarsWithArchetypeId() {
        val info =
            AqlInfoReader.readInfo("select c from composition c[openEHR-EHR-COMPOSITION.encounter.v1] contains observation o[openEHR-EHR-OBSERVATION.blood_pressure-mnd.v1]")
        assertEquals(
            mapOf(
                "c" to AqlInfo.FromVar("c", "composition", "openEHR-EHR-COMPOSITION.encounter.v1", 26, 64),
                "o" to AqlInfo.FromVar("o", "observation", "openEHR-EHR-OBSERVATION.blood_pressure-mnd.v1", 87, 134)
            ), info.vars
        )
    }

    @Test
    fun readVarPaths() {
        val info =
            AqlInfoReader.readInfo("""
select 
    c, 
    c/name/value, 
    a/data/name as mydata 
from composition c[openEHR-EHR-COMPOSITION.encounter.v1] 
contains observation o[openEHR-EHR-OBSERVATION.blood_pressure-mnd.v1] 
where c/uid='abc' and a/data[at0001, 'Data']/items[at0002 and name/value='Test']/value/value = 'uef' 
order by c/context/start_time desc""".trimIndent())

        assertEquals(
            listOf(
                "c" to null,
                "c" to "/name/value",
                "a" to "/data/name",
                "c" to "/uid",
                "a" to "/data[at0001, 'Data']/items[at0002 and name/value='Test']/value/value",
                "c" to "/context/start_time"

            ), info.varPaths.map { it.varName to it.path }
        )
    }
    @Test
    fun readVarPathPositions() {
        val info =
            AqlInfoReader.readInfo("""
select 
    c, 
    c/name/value, 
    a/data/name as mydata 
from composition c[openEHR-EHR-COMPOSITION.encounter.v1] 
contains observation o[openEHR-EHR-OBSERVATION.blood_pressure-mnd.v1] 
where c/uid='abc' and a/data[at0001, 'Data']/items[at0002 and name/value='Test']/value/value = 'uef' 
order by c/context/start_time desc""".trimIndent())

        assertEquals(
            listOf(
                VarPath("c", null, 12, 13, AqlClause.select),
                VarPath("c", "/name/value", 20, 32, AqlClause.select),
                VarPath("a", "/data/name", 39, 50, AqlClause.select),
                VarPath("c", "/uid", 197, 202, AqlClause.where),
                VarPath("a", "/data[at0001, 'Data']/items[at0002 and name/value='Test']/value/value", 213, 283, AqlClause.where),
                VarPath("c", "/context/start_time", 302, 322, AqlClause.order)

            ), info.varPaths
        )
    }

}