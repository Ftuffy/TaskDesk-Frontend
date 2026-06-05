package com.example.manajementugas

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class TaskAdapter(
    private val tasks: MutableList<Task>,
    private val listener: OnTaskActionListener
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    interface OnTaskActionListener {
        fun onEdit(task: Task)
        fun onDelete(task: Task)
        fun onCompleteChanged(task: Task, isCompleted: Boolean)
    }

    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val checkboxComplete: CheckBox = itemView.findViewById(R.id.checkboxComplete)
        val tvCategory: TextView       = itemView.findViewById(R.id.tvCategory)
        val tvTaskType: TextView       = itemView.findViewById(R.id.tvTaskType)
        val tvTitle: TextView          = itemView.findViewById(R.id.tvTaskTitle)
        val tvDescription: TextView    = itemView.findViewById(R.id.tvTaskDescription)
        val tvDeadline: TextView       = itemView.findViewById(R.id.tvDeadline)
        val btnMore: ImageButton       = itemView.findViewById(R.id.btnMore)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]

        holder.tvTitle.text       = task.title
        holder.tvDescription.text = task.description
        holder.tvCategory.text    = task.category

        // Tampilkan tipe task dalam Bahasa Indonesia
        holder.tvTaskType.text = when (task.tipeTask) {
            "weekly"  -> "Mingguan"
            "monthly" -> "Bulanan"
            else      -> "Harian"
        }

        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        holder.tvDeadline.text = sdf.format(Date(task.dueDate))

        // Prevent recycled listener from firing
        holder.checkboxComplete.setOnCheckedChangeListener(null)
        holder.checkboxComplete.isChecked = task.isCompleted

        holder.checkboxComplete.setOnCheckedChangeListener { _, isChecked ->
            task.isCompleted = isChecked
            listener.onCompleteChanged(task, isChecked)
        }

        holder.btnMore.setOnClickListener { v ->
            val popup = PopupMenu(v.context, v)
            popup.inflate(R.menu.menu_task_item)
            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.action_edit -> {
                        listener.onEdit(task)
                        true
                    }
                    R.id.action_delete -> {
                        listener.onDelete(task)
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }
    }

    override fun getItemCount(): Int = tasks.size

    fun updateTasks(newTasks: List<Task>) {
        tasks.clear()
        tasks.addAll(newTasks)
        notifyDataSetChanged()
    }
}