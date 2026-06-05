package com.example.manajementugas.ui.kaia

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.manajementugas.BuildConfig
import com.example.manajementugas.R
import com.example.manajementugas.model.GeminiConfig
import com.example.manajementugas.model.GeminiContent
import com.example.manajementugas.model.GeminiPart
import com.example.manajementugas.model.GeminiRequest
import com.example.manajementugas.network.GeminiRetrofitClient
import com.example.manajementugas.utils.KaiaPersonality
import com.example.manajementugas.utils.SessionManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class KaiaFragment : Fragment() {

    private lateinit var rvChat: RecyclerView
    private lateinit var etMessage: EditText
    private lateinit var btnSend: CardView
    private lateinit var tvKaiaStatus: TextView

    private lateinit var chatAdapter: ChatAdapter
    private lateinit var sessionManager: SessionManager

    // Riwayat chat untuk konteks percakapan
    private val chatHistory = mutableListOf<GeminiContent>()

    private var isKaiaTyping = false
    private var apiKey = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_kaia, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())

        rvChat       = view.findViewById(R.id.rvChat)
        etMessage    = view.findViewById(R.id.etMessage)
        btnSend      = view.findViewById(R.id.btnSend)
        tvKaiaStatus = view.findViewById(R.id.tvKaiaStatus)

        // Ambil API key
        apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty()) {
            apiKey = "" // ← kosongkan, jangan isi API key langsung
        }

        setupRecyclerView()
        setupListeners()
        showGreeting()
    }

    // ── Setup RecyclerView ────────────────────────────────────────
    private fun setupRecyclerView() {
        chatAdapter = ChatAdapter(mutableListOf())
        rvChat.adapter = chatAdapter
        rvChat.layoutManager = LinearLayoutManager(requireContext()).apply {
            stackFromEnd = true
        }
    }

    // ── Tampilkan sapaan Kaia ─────────────────────────────────────
    private fun showGreeting() {
        val userName = sessionManager.getUserName() ?: "Kamu"
        addKaiaMessage(KaiaPersonality.getGreeting(userName))
    }

    // ── Setup listeners ───────────────────────────────────────────
    private fun setupListeners() {
        btnSend.setOnClickListener {
            sendMessage()
        }

        etMessage.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendMessage()
                true
            } else false
        }
    }

    // ── Kirim pesan ke Gemini via Retrofit ───────────────────────
    private fun sendMessage() {
        val message = etMessage.text.toString().trim()
        if (message.isEmpty() || isKaiaTyping) return

        addUserMessage(message)
        etMessage.text.clear()

        // Tambah pesan user ke history
        chatHistory.add(
            GeminiContent(
                role  = "user",
                parts = listOf(GeminiPart(message))
            )
        )

        isKaiaTyping = true
        tvKaiaStatus.text = "● ${KaiaPersonality.TYPING_MESSAGE}"
        addKaiaMessage(KaiaPersonality.TYPING_MESSAGE)

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val request = GeminiRequest(
                    contents = chatHistory,
                    systemInstruction = GeminiContent(
                        role  = "user",
                        parts = listOf(GeminiPart(KaiaPersonality.SYSTEM_PROMPT))
                    ),
                    generationConfig = GeminiConfig(
                        temperature     = 0.9f,
                        maxOutputTokens = 1024
                    )
                )

                val response = GeminiRetrofitClient.instance
                    .generateContent(apiKey, request)

                if (response.isSuccessful) {
                    val reply = response.body()
                        ?.candidates
                        ?.firstOrNull()
                        ?.content
                        ?.parts
                        ?.firstOrNull()
                        ?.text
                        ?: KaiaPersonality.ERROR_MESSAGE

                    // Tambah balasan Kaia ke history
                    chatHistory.add(
                        GeminiContent(
                            role  = "model",
                            parts = listOf(GeminiPart(reply))
                        )
                    )

                    chatAdapter.removeLastMessage()
                    addKaiaMessage(reply)

                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("KaiaFragment", "Error response: $errorBody")
                    chatAdapter.removeLastMessage()
                    addKaiaMessage("Error ${response.code()}: $errorBody")
                }

            } catch (e: Exception) {
                Log.e("KaiaFragment", "Error: ${e.message}")
                chatAdapter.removeLastMessage()
                addKaiaMessage("Error: ${e.message}")
            } finally {
                isKaiaTyping = false
                tvKaiaStatus.text = "● Online"
            }
        }
    }

    // ── Helper tambah pesan Kaia ──────────────────────────────────
    private fun addKaiaMessage(message: String) {
        chatAdapter.addMessage(
            ChatMessage(
                message    = message,
                isFromKaia = true,
                time       = getCurrentTime()
            )
        )
        scrollToBottom()
    }

    // ── Helper tambah pesan User ──────────────────────────────────
    private fun addUserMessage(message: String) {
        chatAdapter.addMessage(
            ChatMessage(
                message    = message,
                isFromKaia = false,
                time       = getCurrentTime()
            )
        )
        scrollToBottom()
    }

    // ── Scroll ke pesan terbaru ───────────────────────────────────
    private fun scrollToBottom() {
        rvChat.post {
            rvChat.smoothScrollToPosition(chatAdapter.itemCount - 1)
        }
    }

    // ── Ambil waktu sekarang ──────────────────────────────────────
    private fun getCurrentTime(): String {
        return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
    }
}