package com.example.manajementugas.utils

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME    = "taskdesk_session"
        const val KEY_TOKEN            = "auth_token"
        const val KEY_USER_ID          = "user_id"
        const val KEY_USER_NAME        = "user_name"
        const val KEY_USER_EMAIL       = "user_email"
        const val KEY_USER_PHONE       = "user_phone"
        const val KEY_USER_HANDLE      = "user_handle"
        const val KEY_MEMBER_SINCE     = "member_since"
        const val KEY_IS_LOGGED_IN     = "is_logged_in"
    }

    /** Simpan sesi setelah login berhasil */
    fun saveSession(
        token: String,
        userId: String,
        name: String,
        email: String,
        phone: String = "",
        handle: String = "",
        memberSince: String = ""
    ) {
        prefs.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, true)
            putString(KEY_TOKEN, token)
            putString(KEY_USER_ID, userId)
            putString(KEY_USER_NAME, name)
            putString(KEY_USER_EMAIL, email)
            putString(KEY_USER_PHONE, phone)
            putString(KEY_USER_HANDLE, handle)
            putString(KEY_MEMBER_SINCE, memberSince)
            apply()
        }
    }

    fun getToken(): String?      = prefs.getString(KEY_TOKEN, null)
    fun getUserId(): String?     = prefs.getString(KEY_USER_ID, null)
    fun getUserName(): String?   = prefs.getString(KEY_USER_NAME, null)
    fun getUserEmail(): String?  = prefs.getString(KEY_USER_EMAIL, null)
    fun getUserPhone(): String?  = prefs.getString(KEY_USER_PHONE, null)
    fun getUserHandle(): String? = prefs.getString(KEY_USER_HANDLE, null)
    fun getMemberSince(): String?= prefs.getString(KEY_MEMBER_SINCE, null)
    fun isLoggedIn(): Boolean    = prefs.getBoolean(KEY_IS_LOGGED_IN, false)

    /** Update data profil setelah edit */
    fun updateProfile(name: String, phone: String) {
        prefs.edit().apply {
            putString(KEY_USER_NAME, name)
            putString(KEY_USER_PHONE, phone)
            apply()
        }
    }

    /** Hapus semua sesi saat logout */
    fun clearSession() {
        prefs.edit().clear().apply()
    }
}