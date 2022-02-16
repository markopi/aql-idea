package care.better.tools.aqlidea.thinkehr

import care.better.tools.aqlidea.plugin.AqlPluginException
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import com.intellij.openapi.diagnostic.Logger
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import java.time.Duration
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit

interface ThinkEhrClient {
    fun query(target: ThinkEhrTarget, aql: String): QueryResponse
    fun listArchetypeInfos(target: ThinkEhrTarget): List<ThinkEhrArchetypeInfo>
    fun getArchetypeDetails(target: ThinkEhrTarget, archetypeId: String): ThinkEhrArchetypeDetails

    class QueryResponse(val rawRequest: String, val rawResponse: String, val response: ThinkEhrQueryResponse)

}

class CachingThinkEhrClient(private val delegate: ThinkEhrClient) : ThinkEhrClient {
    private val cache = CacheBuilder.newBuilder()
        .expireAfterWrite(60, TimeUnit.SECONDS)
        .build<Any, Any>()

    override fun query(target: ThinkEhrTarget, aql: String): ThinkEhrClient.QueryResponse {
        // queries are not cached
        return delegate.query(target, aql)
    }

    override fun listArchetypeInfos(target: ThinkEhrTarget): List<ThinkEhrArchetypeInfo> {
        return cache.getUnwrapped(ListArchetypeInfosKey(target)) { delegate.listArchetypeInfos(target) }
    }

    override fun getArchetypeDetails(target: ThinkEhrTarget, archetypeId: String): ThinkEhrArchetypeDetails {
        val key = ArchetypeDetailsKey(target, archetypeId)
        return cache.getUnwrapped(key) { delegate.getArchetypeDetails(target, archetypeId) }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <K : Any, V> Cache<K, Any>.getUnwrapped(key: K, loader: Callable<V>): V {
        try {
            return this.get(key, loader) as V
        } catch (e: ExecutionException) {
            // guava cache wraps loader exceptions in ExecutionException
            throw e.cause ?: e
        } catch (e: Exception) {
            throw e
        }
    }


    private class ListArchetypeInfosKey(val target: ThinkEhrTarget)
    private class ArchetypeDetailsKey(val target: ThinkEhrTarget, val archetypeId: String)
}

class ThinkEhrClientImpl : ThinkEhrClient {
    private val httpClient = HttpClient.newHttpClient()
    private val objectMapper: ObjectMapper = ObjectMapper().apply {
        registerModule(KotlinModule())
        disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
    }

    private val log: Logger = Logger.getInstance(ThinkEhrClient::class.java)
    private val timeout = Duration.ofSeconds(60)

    override fun query(target: ThinkEhrTarget, aql: String): ThinkEhrClient.QueryResponse {
        val url = target.url + "/rest/v1/query"
        val requestBody = buildRequestBodyString(aql)
        val request = HttpRequest.newBuilder(URI.create(url))
            .POST(HttpRequest.BodyPublishers.ofString(requestBody))
            .header("Authorization", buildAuthorizationHeader(target))
            .header("Content-Type", "application/json")
            .timeout(timeout)
            .build()
        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        ensureSuccess(response, url)
        val rawResponseBody = response.body()
        val parsedResponse =  objectMapper.readValue(rawResponseBody, ThinkEhrQueryResponse::class.java)
        return ThinkEhrClient.QueryResponse(requestBody, rawResponseBody, parsedResponse)
    }

    private fun ensureSuccess(response: HttpResponse<String>, url: String) {
        if (response.statusCode() != 200) {
            log.error("Ehr url $url returned error response code [${response.statusCode()}]: ${response.body()}")
            val errorMessage = extractServerErrorMessage(response) ?: response.body()
            throw AqlPluginException("ThinkEhrServer [${response.statusCode()}]: $errorMessage")
        }
    }

    override fun listArchetypeInfos(target: ThinkEhrTarget): List<ThinkEhrArchetypeInfo> {
        val url = target.url + "/rest/v1/archetype/flat"
        val request = HttpRequest.newBuilder(URI.create(url))
            .GET()
            .header("Authorization", buildAuthorizationHeader(target))
            .header("Content-Type", "application/json")
            .timeout(timeout)
            .build()

        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        ensureSuccess(response, url)

        val type = objectMapper.typeFactory.constructCollectionType(List::class.java, ThinkEhrArchetypeInfo::class.java)
        return objectMapper.readValue(response.body(), type)
    }

    override fun getArchetypeDetails(target: ThinkEhrTarget, archetypeId: String): ThinkEhrArchetypeDetails {
        val url = target.url + "/rest/v1/archetype/flat/${encode(archetypeId)}"
        val request = HttpRequest.newBuilder(URI.create(url))
            .GET()
            .header("Authorization", buildAuthorizationHeader(target))
            .header("Content-Type", "application/json")
            .timeout(timeout)
            .build()

        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        ensureSuccess(response, url)

        return objectMapper.readValue(response.body(), ThinkEhrArchetypeDetails::class.java)
    }

    private fun encode(value: String): String = URLEncoder.encode(value, StandardCharsets.UTF_8)

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