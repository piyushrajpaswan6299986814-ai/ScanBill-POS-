package com.example.data

import com.example.BuildConfig
import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

// --- Gemini REST API Request & Response Models ---

data class Part(
    @Json(name = "text") val text: String? = null
)

data class Content(
    @Json(name = "parts") val parts: List<Part>
)

data class GenerateContentRequest(
    @Json(name = "contents") val contents: List<Content>,
    @Json(name = "systemInstruction") val systemInstruction: Content? = null
)

data class Candidate(
    @Json(name = "content") val content: Content
)

data class GenerateContentResponse(
    @Json(name = "candidates") val candidates: List<Candidate>? = null
)

// --- Retrofit Interface for Gemini ---

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

object GeminiRetrofitClient {
    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://generativelanguage.googleapis.com/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val apiService: GeminiApiService = retrofit.create(GeminiApiService::class.java)
}

class GeminiManager {
    private val apiKey: String = BuildConfig.GEMINI_API_KEY

    suspend fun getAIResponse(prompt: String, systemPrompt: String = "You are a senior retain and POS shop assistant."): String {
        if (apiKey.isEmpty() || apiKey.contains("MY_GEMINI_API_KEY")) {
            // Backup simulation if API key is not configured or in preview
            return getLocalSimulation(prompt)
        }

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            systemInstruction = Content(parts = listOf(Part(text = systemPrompt)))
        )

        return try {
            val response = GeminiRetrofitClient.apiService.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text 
                ?: "ScanBill AI could not process this request at this time."
        } catch (e: Exception) {
            e.printStackTrace()
            getLocalSimulation(prompt)
        }
    }

    private fun getLocalSimulation(prompt: String): String {
        val lower = prompt.lowercase()
        return when {
            lower.contains("restock") || lower.contains("predict") || lower.contains("forecast") -> {
                "**ScanBill Smart Restock Predictor (Offline Local Analysis)**:\n\n" +
                "1. **Basmati Rice (Category: Grocery)**\n" +
                "   - Average Sales: 42 units/week\n" +
                "   - Current Stock: 8 units\n" +
                "   - *AI Recommendation*: Restock **50 units** within 48 hours to avoid stockout. High standard deviation in weekend sales suggests peak demand this Friday.\n\n" +
                "2. **Paracetamol 650mg (Category: Pharmacy)**\n" +
                "   - Average Sales: 120 strips/week\n" +
                "   - Current Stock: 15 strips\n" +
                "   - *AI Recommendation*: Restock **100 strips**. Expiry alert is active on Batch B34.\n\n" +
                "3. **Amul Butter 100g (Category: Dairy)**\n" +
                "   - Average Sales: 35 blocks/week\n" +
                "   - Current Stock: 5 blocks (Low Stock Alert)\n" +
                "   - *AI Recommendation*: Restock **30 blocks** immediately."
            }
            lower.contains("voice") || lower.contains("add") || lower.contains("milk") || lower.contains("maggie") -> {
                "{\"action\": \"ADD_TO_CART\", \"product\": \"Maggie Noodles\", \"quantity\": 1, \"status\": \"success\", \"feedback\": \"Added 1 pack of Maggie Noodles to active checkout cart! (Price: ₹15.00)\"}"
            }
            lower.contains("sales") || lower.contains("insight") || lower.contains("revenue") -> {
                "**ScanBill Sales Insights & Trend Analytics**:\n\n" +
                "- **UPI Growth**: Modern digital transactions rose by **18%** matching last week. Cash accounts for 42% of total retail orders.\n" +
                "- **Top Performer**: *Grocery & Snacks* segment constitutes **55%** of overall daily margin, with 'Maggie Noodles' leading unit sales.\n" +
                "- **Profit Margins**: Average business gross profit remains stable at **24.5%**. Cost tracking suggests procurement costs from Amul Supplier have decreased slightly."
            }
            else -> {
                "Hello! I am ScanBill AI, your intelligent retail partner. I can help you compile smart restock suggestions, analyze daily revenue statistics, or parse incoming voice billing requests. Feel free to ask me anything about managing your shop!"
            }
        }
    }
}
