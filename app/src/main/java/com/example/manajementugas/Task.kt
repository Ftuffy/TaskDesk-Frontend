package com.example.manajementugas

import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.Locale

data class Task(

    // ── ID tugas ──────────────────────────────────────────────────
    @SerializedName("id")
    var id: Long = 0,

    // ── Nama tugas (Laravel: nama_tugas) ──────────────────────────
    @SerializedName("nama_tugas")
    var title: String = "",

    // ── Deskripsi (Laravel: deskripsi) ────────────────────────────
    @SerializedName("deskripsi")
    var description: String = "",

    // ── Kategori: School / Work (Laravel: kategori) ───────────────
    @SerializedName("kategori")
    var category: String = "",

    // ── Tipe task: daily / weekly / monthly (Laravel: tipe_task) ──
    @SerializedName("tipe_task")
    var tipeTask: String = "daily",

    // ── Deadline dalam format string "yyyy-MM-dd" (Laravel: deadline)
    @SerializedName("deadline")
    var deadline: String = "",

    // ── Status selesai (Laravel: selesai) ─────────────────────────
    @SerializedName("selesai")
    var isCompleted: Boolean = false,

    // ── Tanggal diselesaikan (Laravel: completed_at) ──────────────
    // Null artinya tugas belum selesai
    @SerializedName("completed_at")
    var completedDate: String? = null

) {
    // ── Helper: konversi deadline string → Long timestamp ─────────
    // Digunakan oleh AlarmScheduler dan CalendarHelper yang
    // membutuhkan format Long (milliseconds)
    val dueDate: Long
        get() {
            return try {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                sdf.parse(deadline)?.time ?: System.currentTimeMillis()
            } catch (e: Exception) {
                System.currentTimeMillis()
            }
        }

    // ── Helper: konversi completedDate string → Long timestamp ────
    // Digunakan untuk menampilkan tanggal selesai di tab Completed
    val completedDateMillis: Long?
        get() {
            return try {
                completedDate?.let {
                    // Laravel bisa kirim format "yyyy-MM-dd HH:mm:ss"
                    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    sdf.parse(it)?.time
                }
            } catch (e: Exception) {
                null
            }
        }

    // ── Helper: label tipe task dalam Bahasa Indonesia ────────────
    val tipeTaskLabel: String
        get() = when (tipeTask) {
            "weekly"  -> "Mingguan"
            "monthly" -> "Bulanan"
            else      -> "Harian"
        }

    // ── Helper: deadline dalam format tampilan ────────────────────
    // Contoh output: "Senin, 26 Mei 2026"
    val deadlineFormatted: String
        get() {
            return try {
                val inputSdf  = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val outputSdf = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id", "ID"))
                val date      = inputSdf.parse(deadline)
                if (date != null) outputSdf.format(date) else deadline
            } catch (e: Exception) {
                deadline
            }
        }
}