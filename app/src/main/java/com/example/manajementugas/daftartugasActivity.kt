package com.example.manajementugas

import android.os.Bundle
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.manajementugas.model.TaskRequest
import com.example.manajementugas.network.RetrofitClient
import com.example.manajementugas.notification.AlarmScheduler
import com.example.manajementugas.utils.CalendarHelper
import com.example.manajementugas.utils.SessionManager
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class daftartugasActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var tvMonthYear: TextView
    private lateinit var tvSelectedDate: TextView
    private lateinit var btnPrevMonth: ImageButton
    private lateinit var btnNextMonth: ImageButton
    private lateinit var calendarGrid: GridLayout
    private lateinit var cardSchool: MaterialCardView
    private lateinit var cardWork: MaterialCardView
    private lateinit var chipGroupTaskType: ChipGroup
    private lateinit var chipDaily: Chip
    private lateinit var chipWeekly: Chip
    private lateinit var chipMonthly: Chip
    private lateinit var etTaskName: TextInputEditText
    private lateinit var etTaskDescription: TextInputEditText
    private lateinit var btnSave: MaterialButton

    private lateinit var sessionManager: SessionManager
    private lateinit var calendarHelper: CalendarHelper

    private var currentTask: Task?       = null
    private var selectedDueDate: Long    = System.currentTimeMillis()
    private var selectedCategory: String = "School"
    private var selectedTipeTask: String = "daily"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_daftartugas)

        sessionManager = SessionManager(this)

        initViews()
        setupToolbar()
        setupCalendar()
        setupCategorySelection()
        setupTipeTaskSelection()
        setupSaveButton()

        val taskId = intent.getLongExtra("TASK_ID", -1L)
        if (taskId != -1L) {
            loadTaskForEditing(taskId)
        } else {
            updateCategorySelection("School")
            updateSelectedDateLabel(selectedDueDate)
        }
    }

    private fun initViews() {
        toolbar           = findViewById(R.id.toolbar)
        tvMonthYear       = findViewById(R.id.tvMonthYear)
        tvSelectedDate    = findViewById(R.id.tvSelectedDate)
        btnPrevMonth      = findViewById(R.id.btnPrevMonth)
        btnNextMonth      = findViewById(R.id.btnNextMonth)
        calendarGrid      = findViewById(R.id.calendarGrid)
        cardSchool        = findViewById(R.id.cardSchool)
        cardWork          = findViewById(R.id.cardWork)
        chipGroupTaskType = findViewById(R.id.chipGroupTaskType)
        chipDaily         = findViewById(R.id.chipDaily)
        chipWeekly        = findViewById(R.id.chipWeekly)
        chipMonthly       = findViewById(R.id.chipMonthly)
        etTaskName        = findViewById(R.id.etTaskName)
        etTaskDescription = findViewById(R.id.etTaskDescription)
        btnSave           = findViewById(R.id.btnSave)
    }

    private fun setupToolbar() {
        val isEditMode = intent.getLongExtra("TASK_ID", -1L) != -1L
        toolbar.title = if (isEditMode) "Edit Tugas" else "Tambah Tugas"
        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupCalendar() {
        calendarHelper = CalendarHelper(
            context        = this,
            gridLayout     = calendarGrid,
            onDateSelected = { timestamp ->
                selectedDueDate = timestamp
                updateSelectedDateLabel(timestamp)
            }
        )
        calendarHelper.setupCalendar(selectedDueDate)
        tvMonthYear.text = calendarHelper.getMonthYearText()
        updateSelectedDateLabel(selectedDueDate)

        btnPrevMonth.setOnClickListener {
            calendarHelper.goToPreviousMonth()
            tvMonthYear.text = calendarHelper.getMonthYearText()
        }
        btnNextMonth.setOnClickListener {
            calendarHelper.goToNextMonth()
            tvMonthYear.text = calendarHelper.getMonthYearText()
        }
    }

    private fun updateSelectedDateLabel(timestamp: Long) {
        val sdf = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id", "ID"))
        tvSelectedDate.text = "📅 Deadline: ${sdf.format(Date(timestamp))}"
    }

    private fun setupCategorySelection() {
        updateCategorySelection(selectedCategory)
        cardSchool.setOnClickListener {
            selectedCategory = "School"
            updateCategorySelection("School")
        }
        cardWork.setOnClickListener {
            selectedCategory = "Work"
            updateCategorySelection("Work")
        }
    }

    private fun updateCategorySelection(category: String) {
        selectedCategory = category
        if (category == "School") {
            cardSchool.strokeWidth = 4
            cardSchool.strokeColor = getColor(android.R.color.white)
            cardWork.strokeWidth   = 2
            cardWork.strokeColor   = getColor(android.R.color.transparent)
        } else {
            cardWork.strokeWidth   = 4
            cardWork.strokeColor   = getColor(android.R.color.white)
            cardSchool.strokeWidth = 2
            cardSchool.strokeColor = getColor(android.R.color.transparent)
        }
    }

    private fun setupTipeTaskSelection() {
        chipGroupTaskType.setOnCheckedStateChangeListener { _, checkedIds ->
            selectedTipeTask = when {
                checkedIds.contains(R.id.chipWeekly)  -> "weekly"
                checkedIds.contains(R.id.chipMonthly) -> "monthly"
                else                                  -> "daily"
            }
        }
    }

    private fun updateTipeTaskSelection(tipeTask: String) {
        selectedTipeTask = tipeTask
        when (tipeTask) {
            "weekly"  -> chipWeekly.isChecked  = true
            "monthly" -> chipMonthly.isChecked = true
            else      -> chipDaily.isChecked   = true
        }
    }

    private fun setupSaveButton() {
        btnSave.setOnClickListener { saveTask() }
    }

    // ── Load task dari API untuk mode edit ────────────────────────
    private fun loadTaskForEditing(taskId: Long) {
        val token = sessionManager.getToken() ?: return

        btnSave.isEnabled = false
        btnSave.text      = "Memuat..."

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getTask("Bearer $token", taskId)
                if (response.isSuccessful) {
                    val task = response.body()?.task
                    if (task != null) {
                        currentTask = task
                        populateForm(task)
                    } else {
                        toast("Data tugas tidak ditemukan")
                        finish()
                    }
                } else {
                    toast("Gagal memuat data tugas")
                    finish()
                }
            } catch (e: Exception) {
                toast("Gagal terhubung ke server")
                finish()
            } finally {
                btnSave.isEnabled = true
                btnSave.text      = "Update Tugas"
            }
        }
    }

    // ── Isi form dengan data task yang akan diedit ────────────────
    private fun populateForm(task: Task) {
        etTaskName.setText(task.title)
        etTaskDescription.setText(task.description)
        updateCategorySelection(task.category)
        updateTipeTaskSelection(task.tipeTask)
        selectedDueDate  = task.dueDate
        updateSelectedDateLabel(task.dueDate)
        calendarHelper.setupCalendar(task.dueDate)
        tvMonthYear.text = calendarHelper.getMonthYearText()
    }

    private fun longToDateString(timestamp: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    // ── Simpan task ke API ────────────────────────────────────────
    private fun saveTask() {
        val title       = etTaskName.text.toString().trim()
        val description = etTaskDescription.text.toString().trim()

        if (title.isEmpty()) {
            etTaskName.error = "Nama tugas tidak boleh kosong"
            return
        }
        if (description.isEmpty()) {
            etTaskDescription.error = "Deskripsi tidak boleh kosong"
            return
        }

        val deadlineString = longToDateString(selectedDueDate)
        val token          = "Bearer ${sessionManager.getToken()}"

        btnSave.isEnabled = false
        btnSave.text      = "Menyimpan..."

        lifecycleScope.launch {
            try {
                val taskRequest = TaskRequest(
                    namaTugas = title,
                    deskripsi = description,
                    kategori  = selectedCategory,
                    tipeTask  = selectedTipeTask,
                    deadline  = deadlineString
                )

                if (currentTask == null) {
                    // ── Mode Tambah Baru ──────────────────────────
                    val response = RetrofitClient.instance.createTask(token, taskRequest)
                    if (response.isSuccessful) {
                        val newTask = response.body()?.task
                        if (newTask != null) {
                            AlarmScheduler.scheduleTaskAlarm(this@daftartugasActivity, newTask)
                        }
                        toast("Tugas berhasil ditambahkan! ✅")
                        finish()
                    } else {
                        toast("Gagal: ${response.errorBody()?.string()}")
                        resetButton()
                    }

                } else {
                    // ── Mode Edit ─────────────────────────────────
                    val response = RetrofitClient.instance.updateTask(
                        token, currentTask!!.id, taskRequest
                    )
                    if (response.isSuccessful) {
                        val updatedTask = response.body()?.task
                        if (updatedTask != null) {
                            AlarmScheduler.cancelTaskAlarm(this@daftartugasActivity, currentTask!!)
                            AlarmScheduler.scheduleTaskAlarm(this@daftartugasActivity, updatedTask)
                        }
                        toast("Tugas berhasil diupdate! ✅")
                        finish()
                    } else {
                        toast("Gagal update: ${response.errorBody()?.string()}")
                        resetButton()
                    }
                }

            } catch (e: Exception) {
                toast("Gagal terhubung ke server: ${e.message}")
                resetButton()
            }
        }
    }

    private fun resetButton() {
        btnSave.isEnabled = true
        btnSave.text      = if (currentTask == null) "Selesai" else "Update Tugas"
    }

    private fun toast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}