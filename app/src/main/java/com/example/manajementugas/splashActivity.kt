package com.example.manajementugas

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AnimationSet
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton

class splashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Sembunyikan action bar di splash screen
        supportActionBar?.hide()

        setContentView(R.layout.activity_splash)

        // ── Ambil semua view sesuai id di activity_splash.xml ────
        val ivLogo: ImageView          = findViewById(R.id.iv_logo)
        val logoContainer: ConstraintLayout = findViewById(R.id.logo_container)
        val tvAppName: TextView = findViewById(R.id.tv_app_name)
        val tvTagline: TextView        = findViewById(R.id.tv_tagline)
        val btnNext: FloatingActionButton = findViewById(R.id.btn_next)

        // ── Tampilkan semua view yang awalnya invisible ───────────
        logoContainer.visibility = View.VISIBLE
        tvAppName.visibility     = View.VISIBLE
        tvTagline.visibility     = View.VISIBLE
        btnNext.visibility       = View.VISIBLE

        // ── Animasi logo: fade + zoom bersamaan ───────────────────
        val animSet = AnimationSet(true).apply {
            addAnimation(AnimationUtils.loadAnimation(this@splashActivity, R.anim.fade_in))
            addAnimation(AnimationUtils.loadAnimation(this@splashActivity, R.anim.zoom_in))
        }
        ivLogo.startAnimation(animSet)

        // ── Animasi nama app: slide up ────────────────────────────
        val slideUp = AnimationUtils.loadAnimation(this, R.anim.anim_slide_up_fade)
        tvAppName.startAnimation(slideUp)

        // ── Animasi tagline: slide up dengan sedikit delay ───────
        val slideUpTagline = AnimationUtils.loadAnimation(this, R.anim.anim_slide_up_fade)
        slideUpTagline.startOffset = 150
        tvTagline.startAnimation(slideUpTagline)

        // ── Animasi tombol panah muncul terakhir ──────────────────
        val btnAnim = AnimationUtils.loadAnimation(this, R.anim.anim_button_appear)
        btnNext.startAnimation(btnAnim)

        // ── Klik tombol → pindah ke loginActivity ─────────────────
        // Tidak ada timer — user yang menentukan kapan berpindah
        btnNext.setOnClickListener {
            startActivity(Intent(this, loginActivity::class.java))
            overridePendingTransition(
                android.R.anim.fade_in,
                android.R.anim.fade_out
            )
            // Tutup splash agar user tidak bisa back ke sini
            finish()
        }
    }
}

