package com.example.manajementugas.network

import com.example.manajementugas.model.BaseResponse
import com.example.manajementugas.model.LoginRequest
import com.example.manajementugas.model.LoginResponse
import com.example.manajementugas.model.RegisterRequest
import com.example.manajementugas.model.TaskResponse
import com.example.manajementugas.model.TaskRequest
import com.example.manajementugas.model.TaskListResponse
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ── Auth ──────────────────────────────────────────────────
    @POST("login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    @POST("register")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<BaseResponse>

    @POST("logout")
    suspend fun logout(
        @Header("Authorization") token: String
    ): Response<BaseResponse>

    // ── Tasks ─────────────────────────────────────────────────
    @GET("tasks")
    suspend fun getTasks(
        @Header("Authorization") token: String
    ): Response<TaskListResponse>

    @POST("tasks")
    suspend fun createTask(
        @Header("Authorization") token: String,
        @Body request: TaskRequest
    ): Response<TaskResponse>

    @GET("tasks/{id}")
    suspend fun getTask(
        @Header("Authorization") token: String,
        @Path("id") id: Long
    ): Response<TaskResponse>

    @PUT("tasks/{id}")
    suspend fun updateTask(
        @Header("Authorization") token: String,
        @Path("id") id: Long,
        @Body request: TaskRequest
    ): Response<TaskResponse>

    @DELETE("tasks/{id}")
    suspend fun deleteTask(
        @Header("Authorization") token: String,
        @Path("id") id: Long
    ): Response<BaseResponse>

    @PATCH("tasks/{id}/toggle")
    suspend fun toggleTask(
        @Header("Authorization") token: String,
        @Path("id") id: Long
    ): Response<BaseResponse>
}