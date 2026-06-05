package com.example.manajementugas.ui.kaia

data class ChatMessage(
    val message: String,
    val isFromKaia: Boolean,
    val time: String = ""
)