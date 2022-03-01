package care.better.tools.aqlidea.thinkehr

import care.better.tools.aqlidea.plugin.AqlPluginException
import com.fasterxml.jackson.databind.DeserializationFeature
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

    class QueryResponse(
        val rawRequest: Request,
        val rawResponse: Response,
        val response: ThinkEhrQueryResponse?
    )

    data class Request(val url: String, val body: String?)
    data class Response(val code: Int, val body: String?)

    sealed class ThinkEhrAqlException(
        message: String,
        val request: Request,
        val response: Response?,
        cause: Throwable? = null
    ) :
        AqlPluginException(message, cause)

    class ThinkEhrBadResponseException(message: String, request: Request, response: Response) :
        ThinkEhrAqlException(message, request, response)

    class ThinkEhrCallException(message: String, request: Request, cause: Throwable) :
        ThinkEhrAqlException(message, request, null, cause)

    class ThinkEhrParseException(message: String, request: Request, response: Response, cause: Throwable) :
        ThinkEhrAqlException(message, request, response, cause)

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


    private data class ListArchetypeInfosKey(val target: ThinkEhrTarget)
    private data class ArchetypeDetailsKey(val target: ThinkEhrTarget, val archetypeId: String)
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
        val req = ThinkEhrClient.Request(url, requestBody)
        val request = HttpRequest.newBuilder(URI.create(url))
            .POST(HttpRequest.BodyPublishers.ofString(requestBody))
            .header("Authorization", buildAuthorizationHeader(target))
            .header("Content-Type", "application/json")
            .timeout(timeout)
            .build()
        val (resp, response) = sendRequest(req, request)
        ensureSuccess(req, resp)

        val parsedResponse = if (response.statusCode() == 200) {
            parseResponse(req, resp, ThinkEhrQueryResponse::class.java)
        } else {
            null
        }
        return ThinkEhrClient.QueryResponse(req, resp, parsedResponse)
    }


    override fun listArchetypeInfos(target: ThinkEhrTarget): List<ThinkEhrArchetypeInfo> {
        val url = target.url + "/rest/v1/archetype/flat"
        val request = HttpRequest.newBuilder(URI.create(url))
            .GET()
            .header("Authorization", buildAuthorizationHeader(target))
            .header("Content-Type", "application/json")
            .timeout(timeout)
            .build()

        val req = ThinkEhrClient.Request(url, null)
        val (resp, response) = sendRequest(req, request)
        ensureSuccess(req, resp)

        return parseListResponse(req, resp, ThinkEhrArchetypeInfo::class.java)
    }


    override fun getArchetypeDetails(target: ThinkEhrTarget, archetypeId: String): ThinkEhrArchetypeDetails {
        val url = target.url + "/rest/v1/archetype/flat/${encode(archetypeId)}"
        val request = HttpRequest.newBuilder(URI.create(url))
            .GET()
            .header("Authorization", buildAuthorizationHeader(target))
            .header("Content-Type", "application/json")
            .timeout(timeout)
            .build()

        val req = ThinkEhrClient.Request(url, null)
        val (resp, response) = sendRequest(req, request)
        ensureSuccess(req, resp)
        return parseResponse(req, resp, ThinkEhrArchetypeDetails::class.java)
    }


    private fun buildAuthorizationHeader(target: ThinkEhrTarget): String {
        return "Basic " + Base64.getEncoder().encodeToString((target.username + ":" + target.password).toByteArray())
    }

    private fun buildRequestBodyString(aql: String): String {
        val request = mutableMapOf<String, Any?>()
        request["aql"] = aql
        return objectMapper.writeValueAsString(request)
    }

    private fun ensureSuccess(request: ThinkEhrClient.Request, response: ThinkEhrClient.Response) {
        if (response.code !in 200..299) {
//            log.error("Ehr url ${request.url} returned error response code [${response.code}]: ${response.body}")
            val errorMessage = (extractServerErrorMessage(response.body) ?: "").take(160)
            throw ThinkEhrClient.ThinkEhrBadResponseException(
                "ThinkEhrServer [response code ${response.code}]: $errorMessage",
                request, response
            )
        }
    }

    private fun sendRequest(
        r: ThinkEhrClient.Request,
        request: HttpRequest
    ): Pair<ThinkEhrClient.Response, HttpResponse<String>> {
        val response = try {
            httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        } catch (e: Exception) {
            throw ThinkEhrClient.ThinkEhrCallException(e.toString(), r, e)
        }
        val resp = ThinkEhrClient.Response(response.statusCode(), response.body())
        return resp to response
    }

    private fun encode(value: String): String = URLEncoder.encode(value, StandardCharsets.UTF_8)

    private fun extractServerErrorMessage(responseBody: String?): String? {
        val body = responseBody?.trim() ?: return null
        if (body.isEmpty()) return null
        return try {
            val map = objectMapper.readValue(body, Map::class.java)
            map["message"] as? String?
        } catch (e: Exception) {
            null
        }
    }

    private fun <T> parseListResponse(
        req: ThinkEhrClient.Request,
        resp: ThinkEhrClient.Response,
        itemType: Class<T>
    ): List<T> {
        val type = objectMapper.typeFactory.constructCollectionType(List::class.java, ThinkEhrArchetypeInfo::class.java)
        return try {
            objectMapper.readValue(resp.body, type) as List<T>
        } catch (e: Exception) {
            throw ThinkEhrClient.ThinkEhrParseException(e.toString(), req, resp, e)
        }
    }

    private fun <T> parseResponse(
        req: ThinkEhrClient.Request,
        resp: ThinkEhrClient.Response,
        type: Class<T>
    ): T = try {
        objectMapper.readValue(resp.body, type)
    } catch (e: Exception) {
        throw ThinkEhrClient.ThinkEhrParseException(e.toString(), req, resp, e)
    }


}