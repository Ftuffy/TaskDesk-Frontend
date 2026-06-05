package com.example.manajementugas

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.manajementugas.model.LoginRequest
import com.example.manajementugas.network.RetrofitClient
import com.example.manajementugas.utils.SessionManager
import kotlinx.coroutines.launch

class loginActivity : AppCompatActivity() {

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var ivTogglePassword: ImageView
    private lateinit var cbRemember: CheckBox
    private lateinit var tvForgot: TextView
    private lateinit var btnSignIn: Button
    private lateinit var tvRegister: TextView

    // Tambahkan ProgressBar (loading indicator)
    private lateinit var progressBar: ProgressBar

    private lateinit var sessionManager: SessionManager

    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_login)

        sessionManager = SessionManager(this)

        // Jika sudah login sebelumnya, langsung ke MainActivity
        if (sessionManager.isLoggedIn()) {
            navigateToMain()
            return
        }

        etEmail          = findViewById(R.id.etEmail)
        etPassword       = findViewById(R.id.etPassword)
        ivTogglePassword = findViewById(R.id.ivTogglePassword)
        cbRemember       = findViewById(R.id.cbRemember)
        tvForgot         = findViewById(R.id.tvForgot)
        btnSignIn        = findViewById(R.id.btnSignIn)
        tvRegister       = findViewById(R.id.tvRegister)

        // Jika di layout-mu belum ada ProgressBar, kita pakai toast loading saja
        // progressBar = findViewById(R.id.progressBar)

        setupListeners()
    }

    private fun setupListeners() {

        // ── Toggle show/hide password ─────────────────────────────
        ivTogglePassword.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            etPassword.inputType = if (isPasswordVisible)
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            else
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            etPassword.setSelection(etPassword.text.length)
        }

        // ── Tombol Sign In ────────────────────────────────────────
        btnSignIn.setOnClickListener {
            val email    = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            when {
                email.isEmpty() ->
                    toast("Email tidak boleh kosong")
                !Patterns.EMAIL_ADDRESS.matcher(email).matches() ->
                    toast("Format email tidak valid")
                password.isEmpty() ->
                    toast("Password tidak boleh kosong")
                password.length < 6 ->
                    toast("Password minimal 6 karakter")
                else ->
                    // ✅ Panggil API login
                    performLogin(email, password)
            }
        }

        // ── Lupa password ─────────────────────────────────────────
        tvForgot.setOnClickListener {
            toast("Fitur lupa password belum tersedia")
        }

        // ── Link ke halaman Register ──────────────────────────────
        tvRegister.setOnClickListener {
            startActivity(Intent(this, registerActivity::class.java))
            overridePendingTransition(
                android.R.anim.fade_in,
                android.R.anim.fade_out
            )
        }
    }

    // ── Fungsi Login ke API ───────────────────────────────────────
    private fun performLogin(email: String, password: String) {

        // Nonaktifkan tombol agar tidak bisa diklik dua kali
        btnSignIn.isEnabled = false
        btnSignIn.text = "Memproses..."

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.login(
                    LoginRequest(email, password)
                )

                if (response.isSuccessful) {
                    val body = response.body()

                    if (body?.accessToken != null) {
                        // ✅ Login berhasil — simpan token ke sesi
                        sessionManager.saveSession(
                            token       = body.accessToken,
                            userId      = "",   // API-mu tidak return ID, kosongkan
                            name        = email.substringBefore("@"), // sementara pakai email
                            email       = email
                        )

                        toast(body.message ?: "Login berhasil!")
                        navigateToMain()

                    } else {
                        toast("Login gagal: ${body?.message ?: "Terjadi kesalahan"}")
                        resetButton()
                    }

                } else {
                    // Response error dari server (400, 401, dll)
                    toast("Email atau password salah")
                    resetButton()
                }

            } catch (e: Exception) {
                // Gagal koneksi ke server
                toast("Gagal terhubung ke server, periksa koneksi internetmu")
                resetButton()
            }
        }
    }

    // ── Reset tombol setelah error ────────────────────────────────
    private fun resetButton() {
        btnSignIn.isEnabled = true
        btnSignIn.text = "Sign In"
    }

    // ── Navigasi ke MainActivity ──────────────────────────────────
    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        overridePendingTransition(
            android.R.anim.fade_in,
            android.R.anim.fade_out
        )
        finish()
    }

    private fun toast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}