package care.better.tools.aqlidea.thinkehr

import com.fasterxml.jackson.annotation.JsonAnySetter

// we cannot use them in constructor, since we do not want to add a dependency to jackson datatype kotlin just for this
class ThinkEhrQueryResponse {
    lateinit var resultSet: List<LinkedHashMap<String, Any>>
}

abstract class ThinkEhrRestObject() {
    private lateinit var missing: MutableMap<String, Any?>

    @JsonAnySetter
    private fun setMissing(property: String, value: Any?) {
        if (!::missing.isInitialized) {
            missing = mutableMapOf()
        }
        missing[property] = value
    }

}

class ThinkEhrArchetypeInfo(
    val type: String,
    val archetypeId: String,
    val names: List<String> = listOf(),
    val localizedNames: List<Map<String, String>> = listOf()
) : ThinkEhrRestObject()

class ThinkEhrArchetypeDetails(
    val type: String,
    val archetypeId: String,
    val names: List<String> = listOf(),
    val localizedNames: List<Map<String, String>> = listOf(),
    val fields: List<Field> = listOf()
) : ThinkEhrRestObject() {

    class Field(
        val id: String,
        val aql: String,
        val type: String,
        val names: List<String> = listOf(),
        val localizedNames: List<Map<String, String>> = listOf(),
        val inputs: List<List<Input>> = listOf()
    ) : ThinkEhrRestObject()

    class Input(val suffix: String?, val type: String, val validation: Validation?) : ThinkEhrRestObject()
    class Validation(val range: Range?) : ThinkEhrRestObject() {
        class Range(val minOp: String?, val min: Any?, val maxOp: String?, val max: Any?) : ThinkEhrRestObject()
    }

}
