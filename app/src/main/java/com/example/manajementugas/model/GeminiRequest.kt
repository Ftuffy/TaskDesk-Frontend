package com.example.manajementugas.model

import com.google.gson.annotations.SerializedName

// ── Request ke Gemini API ─────────────────────────────────
data class GeminiRequest(
    @SerializedName("contents")
    val contents: List<GeminiContent>,
    @SerializedName("systemInstruction")
    val systemInstruction: GeminiContent? = null,
    @SerializedName("generationConfig")
    val generationConfig: GeminiConfig? = null
)

data class GeminiContent(
    @SerializedName("parts")
    val parts: List<GeminiPart>,
    @SerializedName("role")
    val role: String = "user"
)

data class GeminiPart(
    @SerializedName("text")
    val text: String
)

data class GeminiConfig(
    @SerializedName("temperature")
    val temperature: Float = 0.9f,
    @SerializedName("maxOutputTokens")
    val maxOutputTokens: Int = 1024,
    // ✅ Ditambahkan untuk menonaktifkan thinking mode
    @SerializedName("thinkingConfig")
    val thinkingConfig: ThinkingConfig? = ThinkingConfig()
)

// ✅ Tambahan baru
data class ThinkingConfig(
    @SerializedName("thinkingBudget")
    val thinkingBudget: Int = 0
)

// ── Response dari Gemini API ──────────────────────────────
data class GeminiResponse(
    @SerializedName("candidates")
    val candidates: List<GeminiCandidate>?
)

data class GeminiCandidate(
    @SerializedName("content")
    val content: GeminiContent?
)