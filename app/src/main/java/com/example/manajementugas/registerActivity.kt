package com.example.manajementugas

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.MotionEvent
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.manajementugas.model.RegisterRequest
import com.example.manajementugas.network.RetrofitClient
import kotlinx.coroutines.launch

class registerActivity : AppCompatActivity() {

    private lateinit var etUsername: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var cbTerms: CheckBox
    private lateinit var btnSignUp: Button
    private lateinit var tvLogin: TextView

    private var passwordVisible = false
    private var confirmVisible  = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_register)

        etUsername        = findViewById(R.id.etUsername)
        etEmail           = findViewById(R.id.etEmail)
        etPassword        = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        cbTerms           = findViewById(R.id.cbTerms)
        btnSignUp         = findViewById(R.id.btnSignUp)
        tvLogin           = findViewById(R.id.tvLogin)

        setupPasswordToggle()
        setupListeners()
    }

    private fun setupPasswordToggle() {
        etPassword.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawable = etPassword.compoundDrawables[2]
                if (drawable != null &&
                    event.rawX >= etPassword.right - drawable.bounds.width() - 40
                ) {
                    passwordVisible = !passwordVisible
                    etPassword.inputType = if (passwordVisible)
                        InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                    else
                        InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                    etPassword.setSelection(etPassword.text.length)
                    return@setOnTouchListener true
                }
            }
            false
        }

        etConfirmPassword.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawable = etConfirmPassword.compoundDrawables[2]
                if (drawable != null &&
                    event.rawX >= etConfirmPassword.right - drawable.bounds.width() - 40
                ) {
                    confirmVisible = !confirmVisible
                    etConfirmPassword.inputType = if (confirmVisible)
                        InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                    else
                        InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                    etConfirmPassword.setSelection(etConfirmPassword.text.length)
                    return@setOnTouchListener true
                }
            }
            false
        }
    }

    private fun setupListeners() {
        btnSignUp.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val email    = etEmail.text.toString().trim()
            val password = etPassword.text.toString()
            val confirm  = etConfirmPassword.text.toString()

            when {
                username.isEmpty()   -> toast("Username wajib diisi")
                email.isEmpty()      -> toast("Email wajib diisi")
                !isValidEmail(email) -> toast("Format email tidak valid")
                password.isEmpty()   -> toast("Password wajib diisi")
                password.length < 8  -> toast("Password minimal 8 karakter")
                password != confirm  -> toast("Konfirmasi password tidak cocok")
                !cbTerms.isChecked   -> toast("Setujui syarat & ketentuan dulu")
                else -> performRegister(username, email, password, confirm)
            }
        }

        tvLogin.setOnClickListener {
            navigateToLogin()
        }
    }

    private fun performRegister(
        username: String,
        email: String,
        password: String,
        confirmPassword: String
    ) {
        btnSignUp.isEnabled = false
        btnSignUp.text = "Mendaftar..."

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.register(
                    RegisterRequest(
                        name     = username,
                        email    = email,
                        password = password
                    )
                )

                if (response.isSuccessful) {
                    toast("Registrasi berhasil! Silakan login.")
                    navigateToLogin()
                } else {
                    val errorBody = response.errorBody()?.string()
                    toast("Registrasi gagal: $errorBody")
                    resetButton()
                }

            } catch (e: Exception) {
                toast("Gagal terhubung ke server: ${e.message}")
                resetButton()
            }
        }
    }

    private fun resetButton() {
        btnSignUp.isEnabled = true
        btnSignUp.text = "Sign Up"
    }

    private fun navigateToLogin() {
        startActivity(Intent(this, loginActivity::class.java))
        overridePendingTransition(
            android.R.anim.fade_in,
            android.R.anim.fade_out
        )
        finish()
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun toast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}