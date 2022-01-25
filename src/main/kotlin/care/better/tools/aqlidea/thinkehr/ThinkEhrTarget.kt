package care.better.tools.aqlidea.thinkehr

data class ThinkEhrTarget(val url: String, val username: String, val password: String) {
    companion object {
        val DEFAULT = ThinkEhrTarget(
            url="http://thinkehr2.better.care:8082/rest/openehr/v1",
            username="ihe",
            password="ihe123")
    }
}
