package com.example.manajementugas.ui.kaia

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.manajementugas.R

class ChatAdapter(
    private val messages: MutableList<ChatMessage>
) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    inner class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val layoutKaia: LinearLayout = itemView.findViewById(R.id.layoutKaia)
        val layoutUser: LinearLayout = itemView.findViewById(R.id.layoutUser)
        val tvKaiaMessage: TextView  = itemView.findViewById(R.id.tvKaiaMessage)
        val tvKaiaTime: TextView     = itemView.findViewById(R.id.tvKaiaTime)
        val tvUserMessage: TextView  = itemView.findViewById(R.id.tvUserMessage)
        val tvUserTime: TextView     = itemView.findViewById(R.id.tvUserTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chat = messages[position]

        if (chat.isFromKaia) {
            // Tampilkan balon Kaia
            holder.layoutKaia.visibility = View.VISIBLE
            holder.layoutUser.visibility = View.GONE
            holder.tvKaiaMessage.text    = chat.message
            holder.tvKaiaTime.text       = chat.time
        } else {
            // Tampilkan balon User
            holder.layoutUser.visibility = View.VISIBLE
            holder.layoutKaia.visibility = View.GONE
            holder.tvUserMessage.text    = chat.message
            holder.tvUserTime.text       = chat.time
        }
    }

    override fun getItemCount() = messages.size

    // Tambah pesan baru
    fun addMessage(message: ChatMessage) {
        messages.add(message)
        notifyItemInserted(messages.size - 1)
    }

    // Update pesan terakhir (untuk animasi typing)
    fun updateLastMessage(newText: String) {
        if (messages.isNotEmpty()) {
            messages[messages.size - 1] = messages[messages.size - 1].copy(message = newText)
            notifyItemChanged(messages.size - 1)
        }
    }

    // Hapus pesan terakhir (hapus indikator typing)
    fun removeLastMessage() {
        if (messages.isNotEmpty()) {
            messages.removeAt(messages.size - 1)
            notifyItemRemoved(messages.size)
        }
    }
}