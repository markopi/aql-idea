package care.better.tools.aqlidea.thinkehr

import care.better.tools.aqlidea.plugin.AqlPluginException
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.intellij.openapi.diagnostic.Logger
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.util.*

object ThinkEhrClient {
    private val httpClient = HttpClient.newHttpClient()
    private val objectMapper: ObjectMapper = ObjectMapper().apply {
        disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
    }

    private val log: Logger = Logger.getInstance(ThinkEhrClient::class.java)

    fun query(target: ThinkEhrTarget, aql: String): ThinkEhrQueryResponse {
        val url = target.url + "/rest/v1/query"
        val request = HttpRequest.newBuilder(URI.create(url))
            .POST(HttpRequest.BodyPublishers.ofString(buildRequestBodyString(aql)))
            .header("Authorization", buildAuthorizationHeader(target))
            .header("Content-Type", "application/json")
            .timeout(Duration.ofSeconds(60))
            .build()
        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        if (response.statusCode() != 200) {
            log.error("Ehr url $url returned error response code [${response.statusCode()}]: ${response.body()}")
            val errorMessage = extractServerErrorMessage(response) ?: response.body()
            throw AqlPluginException("ThinkEhrServer [${response.statusCode()}]: $errorMessage")
        }
        return objectMapper.readValue(response.body(), ThinkEhrQueryResponse::class.java)
    }

    private fun extractServerErrorMessage(response: HttpResponse<String>): String? {
        val body = response.body().trim()
        if (body.isEmpty()) return null
        return try {
            val map = objectMapper.readValue(body, Map::class.java)
            map["message"] as? String?
        } catch (e: JsonMappingException) {
            null
        }
    }

    private fun buildAuthorizationHeader(target: ThinkEhrTarget): String {
        return "Basic " + Base64.getEncoder().encodeToString((target.username + ":" + target.password).toByteArray())
    }

    private fun buildRequestBodyString(aql: String): String {
        val request = mutableMapOf<String, Any?>()
        request["aql"] = aql
        return objectMapper.writeValueAsString(request)
    }
}