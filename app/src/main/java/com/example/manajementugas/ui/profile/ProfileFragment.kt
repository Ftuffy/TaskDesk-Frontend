package com.example.manajementugas.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.manajementugas.R
import com.example.manajementugas.loginActivity
import com.example.manajementugas.network.RetrofitClient
import com.example.manajementugas.utils.SessionManager
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private lateinit var sessionManager: SessionManager

    // Views
    private lateinit var tvAvatarInitial: TextView
    private lateinit var tvUserName: TextView
    private lateinit var tvUserHandle: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvMemberSince: TextView
    private lateinit var tvTotalTasks: TextView
    private lateinit var tvDoneTasks: TextView
    private lateinit var tvPendingTasks: TextView
    private lateinit var btnLogout: LinearLayout
    private lateinit var menuNotifikasi: LinearLayout
    private lateinit var menuPrivasi: LinearLayout
    private lateinit var menuBantuan: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())

        // Inisialisasi semua view
        tvAvatarInitial = view.findViewById(R.id.tvAvatarInitial)
        tvUserName      = view.findViewById(R.id.tvUserName)
        tvUserHandle    = view.findViewById(R.id.tvUserHandle)
        tvEmail         = view.findViewById(R.id.tvEmail)
        tvMemberSince   = view.findViewById(R.id.tvMemberSince)
        tvTotalTasks    = view.findViewById(R.id.tvTotalTasks)
        tvDoneTasks     = view.findViewById(R.id.tvDoneTasks)
        tvPendingTasks  = view.findViewById(R.id.tvPendingTasks)
        btnLogout       = view.findViewById(R.id.btnLogout)
        menuNotifikasi  = view.findViewById(R.id.menuNotifikasi)
        menuPrivasi     = view.findViewById(R.id.menuPrivasi)
        menuBantuan     = view.findViewById(R.id.menuBantuan)

        // Tampilkan data dari sesi lokal
        loadSessionData()

        // Fetch statistik task dari API
        fetchTaskStats()

        // Setup semua tombol
        setupClickListeners()
    }

    // ── Tampilkan data dari SessionManager ───────────────────────
    private fun loadSessionData() {
        val name  = sessionManager.getUserName() ?: "Pengguna"
        val email = sessionManager.getUserEmail() ?: "-"

        tvUserName.text     = name
        tvUserHandle.text   = email
        tvEmail.text        = email
        tvMemberSince.text  = sessionManager.getMemberSince() ?: "-"
        tvAvatarInitial.text = getInitials(name)
    }

    // ── Fetch statistik task dari API ─────────────────────────────
    private fun fetchTaskStats() {
        val token = sessionManager.getToken() ?: return

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getTasks("Bearer $token")
                if (response.isSuccessful) {
                    val tasks = response.body()?.tasks ?: return@launch
                    val total   = tasks.size
                    val done    = tasks.count { it.isCompleted }
                    val pending = tasks.count { !it.isCompleted }

                    tvTotalTasks.text   = total.toString()
                    tvDoneTasks.text    = done.toString()
                    tvPendingTasks.text = pending.toString()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // ── Setup semua click listener ────────────────────────────────
    private fun setupClickListeners() {

        menuNotifikasi.setOnClickListener {
            toast("Notifikasi (coming soon)")
        }

        menuPrivasi.setOnClickListener {
            toast("Privasi & Keamanan (coming soon)")
        }

        menuBantuan.setOnClickListener {
            toast("Bantuan (coming soon)")
        }

        btnLogout.setOnClickListener {
            showLogoutDialog()
        }
    }

    // ── Dialog konfirmasi logout ──────────────────────────────────
    private fun showLogoutDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Keluar dari Akun")
            .setMessage("Apakah kamu yakin ingin keluar?")
            .setPositiveButton("Keluar") { _, _ -> performLogout() }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun performLogout() {
        val token = sessionManager.getToken()

        // Hapus sesi lokal
        sessionManager.clearSession()

        // Panggil logout API di background
        if (token != null) {
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    RetrofitClient.instance.logout("Bearer $token")
                } catch (e: Exception) {
                    // Abaikan error — sesi lokal sudah dihapus
                }
            }
        }

        // Kembali ke halaman login
        val intent = Intent(requireContext(), loginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
    }

    // ── Ambil inisial dari nama ───────────────────────────────────
    private fun getInitials(name: String): String {
        val parts = name.trim().split(" ")
        return when {
            parts.size >= 2 -> "${parts[0].first()}${parts[1].first()}".uppercase()
            parts.isNotEmpty() -> parts[0].take(2).uppercase()
            else -> "?"
        }
    }

    private fun toast(msg: String) =
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
}