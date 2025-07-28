package ru.ngtu.myfridge.data.network

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Credentials
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.net.InetSocketAddress
import java.net.Proxy
import java.util.concurrent.TimeUnit

object OpenAIApi {
    private const val baseUrl = "https://api.openai.com/v1/chat/completions"
    private const val apiKey = "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"

    // HTTP-прокси по адресу 104.165.1.188:1821
    private val proxy = Proxy(
        Proxy.Type.HTTP,
        InetSocketAddress("104.165.1.188", 1821)
    )

    private val client = OkHttpClient.Builder()
        .proxy(proxy)
        .proxyAuthenticator { _, response ->
            val credential = Credentials.basic("user301679", "1z9ury")
            response.request.newBuilder()
                .header("Proxy-Authorization", credential)
                .build()
        }
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun analyzeFridgePhoto(photo: Bitmap): DeepSeekResponse? = withContext(Dispatchers.IO) {
        try {
            // Конвертим Bitmap в base64
            val byteArrayOutputStream = ByteArrayOutputStream()
            photo.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
            val base64Image = Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT)

            // Формируем JSON-запрос для OpenAI
            val jsonObject = JSONObject().apply {
                put("model", "gpt-4.1-mini")
                put("messages", JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "user")
                        put("content", JSONArray().apply {
                            put(JSONObject().apply {
                                put("type", "text")
                                put(
                                    "text", "Analyze this fridge photo and list all products inside in Russian. " +
                                            "For each product, include quantity (e.g., 1шт, 5шт) and shelf life (СГ) in days " +
                                            "based on common knowledge (e.g., if opened, use minimal shelf life; if whole, use maximum). " +
                                            "Format the response as a single line with products separated by commas, like this: " +
                                            "[product] [quantity] СГ [days], [product] [quantity] СГ [days]. " +
                                            "Provide the response in Russian and strictly in the structure that I gave you, so that there is nothing superfluous."
                                )
                            })
                            put(JSONObject().apply {
                                put("type", "image_url")
                                put("image_url", JSONObject().apply {
                                    put("url", "data:image/jpeg;base64,$base64Image")
                                    put("detail", "low")
                                })
                            })
                        })
                    })
                })
                put("max_tokens", 700)
            }

            val requestBody = jsonObject
                .toString()
                .toRequestBody("application/json".toMediaType())

            // Собираем HTTP-запрос
            val request = Request.Builder()
                .url(baseUrl)
                .post(requestBody)
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader("Content-Type", "application/json")
                .build()

            // Выполняем запрос
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errorBody = response.body?.string()
                    Log.e("OpenAI", "Response code: ${response.code}, message: $errorBody")
                    return@withContext null
                }
                val responseBody = response.body?.string() ?: return@withContext null
                Log.d("OpenAI", "Raw response: $responseBody")

                // Парсим ответ
                val json = JSONObject(responseBody)
                val description = json
                    .getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content")

                val products = description
                    .split(",")
                    .map { it.trim() }

                DeepSeekResponse(description, products)
            }
        } catch (e: Exception) {
            Log.e("OpenAI", "Exception: ${e.message}", e)
            null
        }
    }
}

data class DeepSeekResponse(
    val description: String,
    val products: List<String>
)
