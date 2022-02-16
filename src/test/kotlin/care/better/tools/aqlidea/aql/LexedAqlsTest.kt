package care.better.tools.aqlidea.aql

import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import org.junit.Test


class LexedAqlsTest {

     @Test
     fun emptyFile() {
         val actual = LexedAqls.of("")
         assertThat(actual.parts).isEmpty()
     }

     @Test
     fun singleAql() {
         val actual = LexedAqls.of("select c from composition c")
         assertThat(actual.parts).hasSize(1)
         assertThat(actual.parts[0].lexed.aql).isEqualTo("select c from composition c")
         assertThat(actual.parts[0].offset).isEqualTo(0)
     }

     @Test
     fun threeAqls() {
         val actual = LexedAqls.of("""select a from composition a
select b from composition b
select c from composition c""".trimIndent())
         assertThat(actual.parts).hasSize(3)
         assertThat(actual.parts[0].lexed.aql).isEqualTo("select a from composition a")
         assertThat(actual.parts[0].offset).isEqualTo(0)
         assertThat(actual.parts[1].lexed.aql).isEqualTo("select b from composition b")
         assertThat(actual.parts[1].offset).isEqualTo(28)
         assertThat(actual.parts[2].lexed.aql).isEqualTo("select c from composition c")
         assertThat(actual.parts[2].offset).isEqualTo(56)
     }

     @Test
     fun threeAqlsDelimitedWithSemicolons() {
         val actual = LexedAqls.of("""select a from composition a;
select b from composition b;
select c from composition c""".trimIndent())
         assertThat(actual.parts).hasSize(3)
         assertThat(actual.parts[0].lexed.aql).isEqualTo("select a from composition a")
         assertThat(actual.parts[0].offset).isEqualTo(0)
         assertThat(actual.parts[1].lexed.aql).isEqualTo("select b from composition b")
         assertThat(actual.parts[1].offset).isEqualTo(29)
         assertThat(actual.parts[2].lexed.aql).isEqualTo("select c from composition c")
         assertThat(actual.parts[2].offset).isEqualTo(58)
     }
     @Test
     fun twoAqlsWithComment() {
         val contents = """-- weird aql
select
    e/ehr_id/value as ehr_id,
    c/uid/value as uuid,
    c/context/start_time/value as start_time,
    c/name/value as template_id
from ehr e
contains composition c[openEHR-EHR-COMPOSITION.referral-mnd.v1]
where c/items[at0003]/value=''

select c from composition c
"""
         val actual = LexedAqls.of(contents.trimIndent())
         assertThat(actual.parts).hasSize(2)
         val firstSelectIndex = contents.indexOf("select")
         assertThat(actual.parts[0].offset).isEqualTo(firstSelectIndex)
         val secondSelectIndex = contents.indexOf("select", firstSelectIndex+1)
         assertThat(actual.parts[1].offset).isEqualTo(secondSelectIndex)
         assertThat(actual.parts[1].lexed.aql).isEqualTo("select c from composition c")
     }

 }