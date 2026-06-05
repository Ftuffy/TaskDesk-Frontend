package com.example.manajementugas

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentContainerView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.manajementugas.notification.AlarmScheduler
import com.example.manajementugas.notification.NotificationHelper
import com.example.manajementugas.network.RetrofitClient
import com.example.manajementugas.ui.kaia.KaiaFragment
import com.example.manajementugas.ui.profile.ProfileFragment
import com.example.manajementugas.utils.SessionManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var taskAdapter: TaskAdapter
    private lateinit var chipGroup: ChipGroup
    private lateinit var fabAdd: FloatingActionButton
    private lateinit var tvTaskCount: TextView
    private lateinit var emptyStateLayout: LinearLayout
    private lateinit var tvUsername: TextView
    private lateinit var sharedPref: SharedPreferences
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var fragmentContainer: FragmentContainerView
    private lateinit var bubbleKaia: CardView
    private lateinit var sessionManager: SessionManager

    private val allTasks = mutableListOf<Task>()
    private var currentFilter = "All"
    private val categories = listOf("School", "Work")

    private val TASK_PREFS    = "TaskPrefs"
    private val TASK_LIST_KEY = "taskList"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sharedPref     = getSharedPreferences("UserPref", MODE_PRIVATE)
        sessionManager = SessionManager(this)

        NotificationHelper.createNotificationChannels(this)
        AlarmScheduler.scheduleDailyReminder(this)
        requestNotificationPermission()

        initViews()
        showUserProfile()
        setupRecyclerView()
        setupChipFilters()
        setupFAB()
        setupBottomNavigation()
        setupBubbleKaia()
        // Cek apakah dibuka dari Taskfilter untuk buka Profile
        if (intent.getBooleanExtra("open_profile", false)) {
            bottomNavigation.selectedItemId = R.id.nav_profile
        }

        // Load task dari API
        loadTasksFromApi()
    }

    override fun onResume() {
        super.onResume()
        loadTasksFromApi()
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    101
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun initViews() {
        recyclerView      = findViewById(R.id.recyclerViewTasks)
        chipGroup         = findViewById(R.id.chipGroup)
        fabAdd            = findViewById(R.id.fabAddTask)
        tvTaskCount       = findViewById(R.id.tvTaskCount)
        emptyStateLayout  = findViewById(R.id.emptyStateLayout)
        tvUsername        = findViewById(R.id.tvUsername)
        bottomNavigation  = findViewById(R.id.bottomNavigation)
        fragmentContainer = findViewById(R.id.fragmentContainer)
        bubbleKaia        = findViewById(R.id.bubbleKaia)
    }

    // ── Load task dari API ────────────────────────────────────
    private fun loadTasksFromApi() {
        val token = "Bearer ${sessionManager.getToken()}"

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getTasks(token)

                if (response.isSuccessful) {
                    val tasks = response.body()?.tasks ?: emptyList()
                    allTasks.clear()
                    // Hanya tampilkan task yang BELUM selesai
                    allTasks.addAll(tasks.filter { !it.isCompleted })
                    saveTasksToPrefs()
                    updateUI()
                } else {
                    // Jika API gagal, load dari cache lokal
                    loadTasksFromPrefs()
                    updateUI()
                }

            } catch (e: Exception) {
                // Jika tidak ada koneksi, load dari cache lokal
                loadTasksFromPrefs()
                updateUI()
            }
        }
    }

    private fun loadTasksFromPrefs() {
        val prefs = getSharedPreferences(TASK_PREFS, MODE_PRIVATE)
        val json  = prefs.getString(TASK_LIST_KEY, null)
        allTasks.clear()
        if (!json.isNullOrEmpty()) {
            val type   = object : TypeToken<MutableList<Task>>() {}.type
            val loaded = Gson().fromJson<MutableList<Task>>(json, type)
            allTasks.addAll(loaded.filter { !it.isCompleted })
        }
    }

    private fun saveTasksToPrefs() {
        val prefs = getSharedPreferences(TASK_PREFS, MODE_PRIVATE)
        prefs.edit().putString(TASK_LIST_KEY, Gson().toJson(allTasks)).apply()
    }

    // ── Tampilkan nama user dari SessionManager ───────────────
    private fun showUserProfile() {
        val name = sessionManager.getUserName() ?: "User"
        tvUsername.text = name
    }

    private fun setupRecyclerView() {
        taskAdapter = TaskAdapter(
            mutableListOf(),
            object : TaskAdapter.OnTaskActionListener {

                override fun onEdit(task: Task) {
                    val intent = Intent(this@MainActivity, daftartugasActivity::class.java)
                    intent.putExtra("TASK_ID", task.id)
                    startActivity(intent)
                }

                override fun onDelete(task: Task) {
                    AlertDialog.Builder(this@MainActivity)
                        .setTitle("Hapus Tugas")
                        .setMessage("Yakin ingin menghapus tugas \"${task.title}\"?")
                        .setPositiveButton("Hapus") { _, _ ->
                            deleteTaskFromApi(task)
                        }
                        .setNegativeButton("Batal", null)
                        .show()
                }

                override fun onCompleteChanged(task: Task, isCompleted: Boolean) {
                    if (isCompleted) {
                        // Toggle di API lalu hapus dari tampilan
                        toggleTaskComplete(task)
                    }
                }
            }
        )
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter       = taskAdapter
    }

    // ── Toggle task selesai di API ────────────────────────────
    private fun toggleTaskComplete(task: Task) {
        val token = "Bearer ${sessionManager.getToken()}"

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.toggleTask(token, task.id)

                if (response.isSuccessful) {
                    // Hapus dari list & batalkan alarm
                    AlarmScheduler.cancelTaskAlarm(this@MainActivity, task)
                    allTasks.remove(task)
                    saveTasksToPrefs()
                    updateUI()
                } else {
                    // Jika gagal, kembalikan checkbox ke unchecked
                    loadTasksFromApi()
                }

            } catch (e: Exception) {
                // Jika tidak ada koneksi, update lokal saja
                AlarmScheduler.cancelTaskAlarm(this@MainActivity, task)
                allTasks.remove(task)
                saveTasksToPrefs()
                updateUI()
            }
        }
    }

    // ── Delete task di API ────────────────────────────────────
    private fun deleteTaskFromApi(task: Task) {
        val token = "Bearer ${sessionManager.getToken()}"

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.deleteTask(token, task.id)

                if (response.isSuccessful) {
                    AlarmScheduler.cancelTaskAlarm(this@MainActivity, task)
                    allTasks.remove(task)
                    saveTasksToPrefs()
                    updateUI()
                } else {
                    loadTasksFromApi()
                }

            } catch (e: Exception) {
                // Jika tidak ada koneksi, hapus lokal saja
                AlarmScheduler.cancelTaskAlarm(this@MainActivity, task)
                allTasks.remove(task)
                saveTasksToPrefs()
                updateUI()
            }
        }
    }

    private fun setupChipFilters() {
        val chipAll = chipGroup.findViewById<Chip>(R.id.chipAll)
        chipAll?.setOnClickListener {
            currentFilter = "All"
            updateUI()
        }
        if (chipGroup.childCount > 1) {
            chipGroup.removeViews(1, chipGroup.childCount - 1)
        }
        for (category in categories) {
            val chip = Chip(this)
            chip.text        = category
            chip.isCheckable = true
            chip.setOnClickListener {
                currentFilter = category
                updateUI()
            }
            chipGroup.addView(chip)
        }
    }

    private fun updateUI() {
        val filtered = if (currentFilter == "All") allTasks
        else allTasks.filter { it.category == currentFilter }

        taskAdapter.updateTasks(filtered)
        emptyStateLayout.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
        recyclerView.visibility     = if (filtered.isEmpty()) View.GONE else View.VISIBLE

        val pendingCount = allTasks.count { !it.isCompleted }
        tvTaskCount.text = "You have $pendingCount tasks pending"
    }

    private fun setupFAB() {
        fabAdd.setOnClickListener {
            startActivity(Intent(this, daftartugasActivity::class.java))
        }
    }

    // ── Setup Bottom Navigation ───────────────────────────────
    private fun setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {

                R.id.nav_home -> {
                    fragmentContainer.visibility = View.GONE
                    findViewById<View>(R.id.contentContainer).visibility = View.VISIBLE
                    findViewById<View>(R.id.headerCard).visibility = View.VISIBLE
                    findViewById<View>(R.id.chipScrollView).visibility = View.VISIBLE
                    fabAdd.visibility     = View.VISIBLE
                    bubbleKaia.visibility = View.VISIBLE
                    true
                }

                R.id.nav_tasks -> {
                    // ✅ Pindah ke Taskfilter Activity
                    startActivity(Intent(this, Taskfilter::class.java))
                    false // false agar Home tetap selected saat kembali
                }

                R.id.nav_profile -> {
                    findViewById<View>(R.id.contentContainer).visibility = View.GONE
                    findViewById<View>(R.id.headerCard).visibility = View.GONE
                    findViewById<View>(R.id.chipScrollView).visibility = View.GONE
                    fabAdd.visibility     = View.GONE
                    bubbleKaia.visibility = View.GONE
                    fragmentContainer.visibility = View.VISIBLE
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, ProfileFragment())
                        .commit()
                    true
                }

                else -> false
            }
        }
    }

    // ── Setup Bubble Kaia ─────────────────────────────────────
    private fun setupBubbleKaia() {
        bubbleKaia.setOnClickListener {
            findViewById<View>(R.id.contentContainer).visibility = View.GONE
            findViewById<View>(R.id.headerCard).visibility = View.GONE
            findViewById<View>(R.id.chipScrollView).visibility = View.GONE
            fabAdd.visibility     = View.GONE
            bubbleKaia.visibility = View.GONE
            fragmentContainer.visibility = View.VISIBLE
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, KaiaFragment())
                .commit()
        }
    }

}