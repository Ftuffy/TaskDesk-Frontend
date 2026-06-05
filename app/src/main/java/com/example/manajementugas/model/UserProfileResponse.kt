package com.example.manajementugas.model

import com.google.gson.annotations.SerializedName

// ── Request Login ─────────────────────────────────────────────
data class LoginRequest(
    @SerializedName("email")    val email: String,
    @SerializedName("password") val password: String
)

// ── Response Login (sesuai API-mu) ────────────────────────────
data class LoginResponse(
    @SerializedName("message")      val message: String?,
    @SerializedName("access_token") val accessToken: String?,
    @SerializedName("token_type")   val tokenType: String?
)

// ── Data User ─────────────────────────────────────────────────
data class UserData(
    @SerializedName("id")         val id: String,
    @SerializedName("name")       val name: String,
    @SerializedName("email")      val email: String,
    @SerializedName("phone")      val phone: String?,
    @SerializedName("username")   val username: String?,
    @SerializedName("created_at") val createdAt: String?
)

// ── Response Profile ──────────────────────────────────────────
data class ProfileResponse(
    @SerializedName("message") val message: String?,
    @SerializedName("data")    val data: UserData?
)

// ── Response Statistik Task ───────────────────────────────────
data class TaskStatsResponse(
    @SerializedName("message") val message: String?,
    @SerializedName("data")    val data: TaskStats?
)

data class TaskStats(
    @SerializedName("total")   val total: Int = 0,
    @SerializedName("done")    val done: Int = 0,
    @SerializedName("pending") val pending: Int = 0
)

// ── Response Umum (logout, dll) ───────────────────────────────
data class BaseResponse(
    @SerializedName("message") val message: String?
)

data class RegisterRequest(
    @SerializedName("name")     val name: String,
    @SerializedName("email")    val email: String,
    @SerializedName("password") val password: String
)

// ── Task Request ──────────────────────────────────────────
data class TaskRequest(
    @SerializedName("nama_tugas") val namaTugas: String,
    @SerializedName("deskripsi")  val deskripsi: String? = null,
    @SerializedName("kategori")   val kategori: String,
    @SerializedName("tipe_task")  val tipeTask: String = "daily",
    @SerializedName("deadline")   val deadline: String
)

// ── Task Response (single) ────────────────────────────────
data class TaskResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("task")    val task: com.example.manajementugas.Task?
)

// ── Task Response (list) ──────────────────────────────────
data class TaskListResponse(
    @SerializedName("success")          val success: Boolean,
    @SerializedName("pending_count")    val pendingCount: Int = 0,
    @SerializedName("pending_per_tipe") val pendingPerTipe: Map<String, Int>? = null,
    @SerializedName("tasks")            val tasks: List<com.example.manajementugas.Task>?
)