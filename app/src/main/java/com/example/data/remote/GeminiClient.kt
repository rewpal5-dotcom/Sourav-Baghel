package com.example.data.remote

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Thrown whenever the AI call cannot be completed. The message is written so it can be
 * shown to the user directly — it always says what actually went wrong.
 */
class GeminiApiException(message: String) : Exception(message)

object GeminiClient {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    // Active Flash model. gemini-3.5-flash is deprecated and now auto-routed by Google.
    private const val MODEL_NAME = "gemini-3.8-flash"
    private const val BASE_URL =
        "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent"

    private val PERCENT_REGEX = Regex("\\d{1,3}\\s?%")

    // ---------------------------------------------------------------- diagnosis

    suspend fun diagnosePlantIssue(
        plantType: String,
        symptoms: String,
        additionalInfo: String,
        base64Image: String? = null
    ): PlantDiagnosisResult = withContext(Dispatchers.IO) {
        val apiKey = requireApiKey()
        val hasPhoto = !base64Image.isNullOrBlank()
        val prompt = buildDiagnosisPrompt(plantType, symptoms, additionalInfo, hasPhoto)

        val parts = JSONArray().apply {
            put(JSONObject().put("text", prompt))
            if (hasPhoto) {
                put(
                    JSONObject().put(
                        "inline_data",
                        JSONObject().apply {
                            put("mime_type", "image/jpeg")
                            put("data", base64Image)
                        }
                    )
                )
            }
        }

        val payload = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    put("parts", parts)
                })
            })
            put(
                "generationConfig",
                JSONObject().apply {
                    put("temperature", 0.4)
                    put("maxOutputTokens", 2048)
                }
            )
        }

        val answer = callGemini(apiKey, payload)
        parseDiagnosisResult(plantType, answer)
    }

    private fun buildDiagnosisPrompt(
        plantType: String,
        symptoms: String,
        additionalInfo: String,
        hasPhoto: Boolean
    ): String {
        val photoLine = if (hasPhoto) {
            "Carefully examine the attached leaf/plant photo and base the diagnosis mainly on what you see in the photo."
        } else {
            "No photo was provided, so base the diagnosis on the reported symptoms."
        }

        return """
        You are Paud Pandit (पौध पंडित), an expert agricultural scientist, botanist and plant doctor.
        $photoLine

        Provided information:
        - Plant Category: $plantType
        - User Reported Symptoms: $symptoms
        - Additional Context: $additionalInfo

        Answer ONLY in this exact markdown structure, with these exact headings:

        ### Disease / Condition
        [Name of the disease or issue. One short line.]

        ### Severity & Confidence
        [High / Medium / Low | Confidence: NN%]

        ### Visual Analysis & Root Cause
        [What you see in the photo (yellowing pattern, fungal spots, pest damage, overwatering signs, sunburn) and why it happened]

        ### Organic Remedy
        [Step by step natural treatment, e.g. Neem oil spray (नीम तेल), wood ash, adjusted watering, pruning]

        ### Chemical Solution
        [Optional bio-pesticide or balanced NPK fertilizer, or write "Not required"]

        ### Prevention Tips
        [Exactly 3 short care tips]

        Keep it practical for an Indian home gardener. Use simple English with Hindi terms in brackets where helpful.
        """.trimIndent()
    }

    private fun parseDiagnosisResult(plantType: String, fullText: String): PlantDiagnosisResult {
        val sections = splitSections(fullText)

        val condition = sections.entries.firstOrNull { (heading, _) ->
            heading.contains("disease", true) || heading.contains("condition", true)
        }?.value?.firstMeaningfulLine()

        val severitySection = sections.entries.firstOrNull { (heading, _) ->
            heading.contains("severity", true)
        }?.value

        val confidence = PERCENT_REGEX.find(severitySection ?: "")
            ?.value
            ?: severitySection?.firstMeaningfulLine()
            ?: "—"

        val remedySection = sections.entries.firstOrNull { (heading, _) ->
            heading.contains("organic", true) || heading.contains("remedy", true)
        }?.value

        val remedySummary = remedySection
            ?.firstMeaningfulLine()
            ?.take(220)
            ?: "Poori salah neeche di gayi hai."

        return PlantDiagnosisResult(
            plantName = plantType.ifBlank { "Unidentified Plant" },
            conditionName = condition ?: "AI diagnosis",
            confidence = confidence,
            fullAnalysisMarkdown = fullText.trim(),
            remedySummary = remedySummary
        )
    }

    // --------------------------------------------------------------------- chat

    suspend fun askPanditChat(userQuery: String, history: List<ChatMessage>): String =
        withContext(Dispatchers.IO) {
            val apiKey = requireApiKey()

            val systemPrompt =
                "You are Paud Pandit (पौध पंडित), a warm, wise and encouraging Indian plant doctor and " +
                    "gardening expert. Reply in natural, polite Hindi (or English if the user writes in English). " +
                    "Give practical plant care advice using familiar terms like खाद, मिट्टी, सिंचाई, धूप, नीम तेल, " +
                    "कंपोस्ट. Keep answers short, clear and actionable."

            val contents = JSONArray().apply {
                history.takeLast(6).forEach { msg ->
                    put(
                        JSONObject().apply {
                            put("role", if (msg.isUser) "user" else "model")
                            put("parts", JSONArray().apply {
                                put(JSONObject().put("text", msg.message))
                            })
                        }
                    )
                }
                put(
                    JSONObject().apply {
                        put("role", "user")
                        put("parts", JSONArray().apply {
                            put(JSONObject().put("text", userQuery))
                        })
                    }
                )
            }

            val payload = JSONObject().apply {
                put(
                    "systemInstruction",
                    JSONObject().put("parts", JSONArray().apply {
                        put(JSONObject().put("text", systemPrompt))
                    })
                )
                put("contents", contents)
                put(
                    "generationConfig",
                    JSONObject().apply {
                        put("temperature", 0.7)
                        put("maxOutputTokens", 1024)
                    }
                )
            }

            callGemini(apiKey, payload)
        }

    // ------------------------------------------------------------------ network

    private fun requireApiKey(): String {
        val key = BuildConfig.GEMINI_API_KEY
        if (key.isBlank() || key == "MY_GEMINI_API_KEY" || key == "YOUR_GEMINI_API_KEY") {
            throw GeminiApiException(
                "AI connect nahi ho paya — GEMINI_API_KEY set nahi hai.\n\n" +
                    "Project folder ki .env file mein ye line daalein:\n" +
                    "GEMINI_API_KEY=<aapki key>\n\n" +
                    "Key muft milti hai: https://aistudio.google.com/apikey\n" +
                    "Uske baad app dobara build karein."
            )
        }
        return key
    }

    private fun callGemini(apiKey: String, payload: JSONObject): String {
        val requestBody = payload.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url("$BASE_URL?key=$apiKey")
            .post(requestBody)
            .build()

        var statusCode = 0
        var bodyText = ""

        try {
            client.newCall(request).execute().use { response ->
                statusCode = response.code
                bodyText = response.body?.string().orEmpty()
            }
        } catch (e: Exception) {
            throw GeminiApiException(
                "AI server se connection nahi bana: ${e.message ?: e.javaClass.simpleName}\n\n" +
                    "Internet check karein aur dobara try karein."
            )
        }

        if (statusCode !in 200..299) {
            throw GeminiApiException(describeHttpError(statusCode, bodyText))
        }

        val candidates = JSONObject(bodyText).optJSONArray("candidates")
        val parts = candidates
            ?.optJSONObject(0)
            ?.optJSONObject("content")
            ?.optJSONArray("parts")
        val text = parts?.optJSONObject(0)?.optString("text").orEmpty()

        if (text.isBlank()) {
            throw GeminiApiException("AI ne khaali jawab diya. Thodi der baad dobara koshish karein.")
        }
        return text
    }

    private fun describeHttpError(code: Int, bodyText: String): String {
        val apiMessage = runCatching {
            JSONObject(bodyText).optJSONObject("error")?.optString("message")
        }.getOrNull()

        val reason = when (code) {
            400 -> "AI ne request reject kar di (400)."
            401, 403 -> "API key galat ya band ho gayi hai ($code). Nee key banayein."
            404 -> "Model '$MODEL_NAME' is key ke liye available nahi hai (404)."
            429 -> "AI ka free quota khatam ho gaya (429). Thodi der baad try karein."
            in 500..599 -> "Google ke AI server par problem hai ($code)."
            else -> "AI request fail ho gayi ($code)."
        }

        return if (apiMessage.isNullOrBlank()) reason else "$reason\n\n$apiMessage"
    }

    // ------------------------------------------------------------------ parsing

    /** Splits markdown into a map of heading -> body, using the "### Heading" markers. */
    private fun splitSections(markdown: String): Map<String, String> {
        val sections = linkedMapOf<String, String>()
        var heading: String? = null
        val buffer = StringBuilder()

        markdown.lines().forEach { line ->
            val trimmed = line.trim()
            if (trimmed.startsWith("###")) {
                val current = heading
                if (current != null) {
                    sections[current] = buffer.toString().trim()
                }
                heading = trimmed.removePrefix("###").trim().trim('*', '#', ':', ' ')
                buffer.setLength(0)
            } else if (heading != null) {
                buffer.append(line).append('\n')
            }
        }

        val last = heading
        if (last != null) {
            sections[last] = buffer.toString().trim()
        }
        return sections
    }

    private fun String.firstMeaningfulLine(): String? =
        lines()
            .map { it.trim() }
            .map { it.removePrefix("- ").removePrefix("* ").trim().trim('*', '#', ':', ' ') }
            .firstOrNull { line ->
                line.isNotBlank() && !line.startsWith("[") && !line.matches(Regex("^\\d+\\.?$"))
            }
}

data class PlantDiagnosisResult(
    val plantName: String,
    val conditionName: String,
    val confidence: String,
    val fullAnalysisMarkdown: String,
    val remedySummary: String
)

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val message: String,
    val isUser: Boolean,
    val timestamp: String = java.text.SimpleDateFormat(
        "hh:mm a",
        java.util.Locale.getDefault()
    ).format(java.util.Date())
)
