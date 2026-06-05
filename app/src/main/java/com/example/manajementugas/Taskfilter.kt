package com.example.manajementugas

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.manajementugas.databinding.ActivityTaskfilterBinding
import com.example.manajementugas.network.RetrofitClient
import com.example.manajementugas.utils.SessionManager
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.launch

class Taskfilter : AppCompatActivity(), TaskAdapter.OnTaskActionListener {

    private lateinit var binding: ActivityTaskfilterBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var adapter: TaskAdapter

    private var allTasks: List<Task> = emptyList()

    private var selectedCategory: String? = null
    private var selectedType: String?     = null
    private var showCompleted: Boolean    = false
    private var searchQuery: String       = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaskfilterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        setupRecyclerView()
        setupTabs()
        setupChips()
        setupSearch()
        setupBottomNav()
        fetchTasks()
    }

    override fun onResume() {
        super.onResume()
        fetchTasks()
    }

    // ── Setup RecyclerView ────────────────────────────────────
    private fun setupRecyclerView() {
        adapter = TaskAdapter(mutableListOf(), this)
        binding.rvTasks.layoutManager = LinearLayoutManager(this)
        binding.rvTasks.adapter = adapter
    }

    // ── Setup Tab Active / Completed ─────────────────────────
    private fun setupTabs() {
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Active"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Completed"))

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                showCompleted = tab.position == 1
                applyFilter()
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    // ── Setup Chip Filter ─────────────────────────────────────
    private fun setupChips() {
        // Kategori
        binding.chipGroupCategory.setOnCheckedStateChangeListener { _, checkedIds ->
            selectedCategory = when {
                checkedIds.contains(R.id.chipSchool) -> "School"
                checkedIds.contains(R.id.chipWork)   -> "Work"
                else                                 -> null // chipAll
            }
            applyFilter()
        }

        // Tipe Task
        binding.chipGroupType.setOnCheckedStateChangeListener { _, checkedIds ->
            selectedType = when {
                checkedIds.contains(R.id.chipDaily)   -> "daily"
                checkedIds.contains(R.id.chipWeekly)  -> "weekly"
                checkedIds.contains(R.id.chipMonthly) -> "monthly"
                else                                  -> null // chipAllType
            }
            applyFilter()
        }
    }

    // ── Setup Search ──────────────────────────────────────────
    private fun setupSearch() {
        binding.searchView.setOnQueryTextListener(object :
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false
            override fun onQueryTextChange(newText: String?): Boolean {
                searchQuery = newText.orEmpty()
                applyFilter()
                return true
            }
        })
    }

    // ── Setup Bottom Navigation ───────────────────────────────
    private fun setupBottomNav() {
        binding.bottomNavigation.selectedItemId = R.id.nav_tasks

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    })
                    finish()
                    true
                }
                R.id.nav_tasks -> true // sudah di halaman ini
                R.id.nav_profile -> {
                    val intent = Intent(this, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        putExtra("open_profile", true)
                    }
                    startActivity(intent)
                    finish()
                    true
                }
                else -> false
            }
        }
    }

    // ── Fetch Tasks dari API ──────────────────────────────────
    private fun fetchTasks() {
        val token = sessionManager.getToken() ?: return

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getTasks("Bearer $token")
                if (response.isSuccessful) {
                    allTasks = response.body()?.tasks ?: emptyList()
                    applyFilter()
                } else {
                    toast("Gagal memuat tugas")
                }
            } catch (e: Exception) {
                toast("Error: ${e.message}")
            }
        }
    }

    // ── Apply semua filter sekaligus ──────────────────────────
    private fun applyFilter() {
        var filtered = allTasks

        filtered = filtered.filter { it.isCompleted == showCompleted }

        if (selectedCategory != null) {
            filtered = filtered.filter { it.category == selectedCategory }
        }

        if (selectedType != null) {
            filtered = filtered.filter { it.tipeTask == selectedType }
        }

        if (searchQuery.isNotBlank()) {
            filtered = filtered.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                        it.description.contains(searchQuery, ignoreCase = true)
            }
        }

        adapter.updateTasks(filtered)
    }

    // ── TaskAdapter Listener ──────────────────────────────────
    override fun onEdit(task: Task) {
        startActivity(Intent(this, daftartugasActivity::class.java).apply {
            putExtra("TASK_ID", task.id)
        })
    }

    override fun onDelete(task: Task) {
        AlertDialog.Builder(this)
            .setTitle("Hapus Tugas")
            .setMessage("Yakin ingin menghapus \"${task.title}\"?")
            .setPositiveButton("Hapus") { _, _ -> deleteTask(task) }
            .setNegativeButton("Batal", null)
            .show()
    }

    override fun onCompleteChanged(task: Task, isCompleted: Boolean) {
        val token = sessionManager.getToken() ?: return

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.toggleTask("Bearer $token", task.id)
                if (response.isSuccessful) {
                    allTasks = allTasks.map {
                        if (it.id == task.id) it.copy(isCompleted = isCompleted) else it
                    }
                    applyFilter()
                } else {
                    toast("Gagal update status tugas")
                }
            } catch (e: Exception) {
                toast("Error: ${e.message}")
            }
        }
    }

    private fun deleteTask(task: Task) {
        val token = sessionManager.getToken() ?: return

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.deleteTask("Bearer $token", task.id)
                if (response.isSuccessful) {
                    allTasks = allTasks.filter { it.id != task.id }
                    applyFilter()
                    toast("Tugas berhasil dihapus")
                } else {
                    toast("Gagal menghapus tugas")
                }
            } catch (e: Exception) {
                toast("Error: ${e.message}")
            }
        }
    }

    private fun toast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}