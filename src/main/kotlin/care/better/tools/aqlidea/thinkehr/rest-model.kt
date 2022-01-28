package care.better.tools.aqlidea.thinkehr

// we cannot use them in constructor, since we do not want to add a dependency to jackson datatype kotlin just for this
class ThinkEhrQueryResponse {
    lateinit var resultSet: List<LinkedHashMap<String, Any>>
}