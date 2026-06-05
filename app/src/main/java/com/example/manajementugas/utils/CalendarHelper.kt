package com.example.manajementugas.utils

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.GridLayout
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CalendarHelper(
    private val context: Context,
    private val gridLayout: GridLayout,
    private val onDateSelected: (Long) -> Unit
) {
    private var selectedDate: Calendar = Calendar.getInstance()
    private var currentMonth: Calendar = Calendar.getInstance()

    // ── DIPERBAIKI: Simpan referensi view + info isToday ─────────
    // Agar saat deselect, warna bisa dikembalikan dengan benar
    private var selectedDayView: TextView? = null
    private var selectedDayIsToday: Boolean = false

    // ── Setup awal kalender dengan tanggal tertentu ───────────────
    fun setupCalendar(date: Long = System.currentTimeMillis()) {
        currentMonth.timeInMillis = date
        selectedDate.timeInMillis = date
        // Reset referensi saat setup ulang
        selectedDayView = null
        selectedDayIsToday = false
        populateCalendar()
    }

    fun goToPreviousMonth() {
        currentMonth.add(Calendar.MONTH, -1)
        // DIPERBAIKI: Reset referensi view lama sebelum rebuild grid
        // Karena removeAllViews() akan menghapus semua view termasuk selectedDayView
        selectedDayView = null
        selectedDayIsToday = false
        populateCalendar()
    }

    fun goToNextMonth() {
        currentMonth.add(Calendar.MONTH, 1)
        // DIPERBAIKI: Reset referensi view lama sebelum rebuild grid
        selectedDayView = null
        selectedDayIsToday = false
        populateCalendar()
    }

    fun getMonthYearText(): String {
        val sdf = SimpleDateFormat("MMMM yyyy", Locale("id", "ID"))
        return sdf.format(currentMonth.time)
    }

    // ── Bangun grid kalender ──────────────────────────────────────
    private fun populateCalendar() {
        gridLayout.removeAllViews()

        // Hitung hari pertama bulan ini
        val calendar = currentMonth.clone() as Calendar
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

        // Jumlah hari dalam bulan ini
        val maxDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        // Konversi: Sunday=1 → offset Senin=0, Minggu=6
        val firstDayOffset = if (firstDayOfWeek == Calendar.SUNDAY) 6
        else firstDayOfWeek - 2

        // Sel kosong sebelum hari pertama
        for (i in 0 until firstDayOffset) {
            addEmptyCell()
        }

        // Sel hari dalam bulan ini
        for (day in 1..maxDays) {
            addDayCell(day)
        }

        // Sel kosong setelah hari terakhir
        val totalCells = firstDayOffset + maxDays
        val remainingCells = if (totalCells % 7 == 0) 0 else 7 - (totalCells % 7)
        for (i in 0 until remainingCells) {
            addEmptyCell()
        }
    }

    // ── Sel kosong ───────────────────────────────────────────────
    private fun addEmptyCell() {
        val emptyView = View(context)
        // DIPERBAIKI: Gunakan 0dp + weight agar responsive di semua ukuran layar
        val params = GridLayout.LayoutParams().apply {
            width = 0
            height = 44.dpToPx()
            setMargins(2.dpToPx(), 2.dpToPx(), 2.dpToPx(), 2.dpToPx())
            columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            rowSpec    = GridLayout.spec(GridLayout.UNDEFINED)
        }
        emptyView.layoutParams = params
        gridLayout.addView(emptyView)
    }

    // ── Sel hari ─────────────────────────────────────────────────
    private fun addDayCell(day: Int) {
        val isSelected = isSameDay(day)
        val isToday    = isTodayDate(day)

        val dayView = TextView(context).apply {
            text    = day.toString()
            gravity = Gravity.CENTER
            textSize = 13f

            // DIPERBAIKI: Gunakan 0dp + weight agar responsive
            val params = GridLayout.LayoutParams().apply {
                width  = 0
                height = 44.dpToPx()
                setMargins(2.dpToPx(), 2.dpToPx(), 2.dpToPx(), 2.dpToPx())
                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                rowSpec    = GridLayout.spec(GridLayout.UNDEFINED)
            }
            layoutParams = params
        }

        // ── Tampilan awal berdasarkan status ──────────────────────
        when {
            isSelected -> {
                // Tanggal terpilih — lingkaran biru solid
                dayView.background = circleDrawable(Color.parseColor("#5271FF"))
                dayView.setTextColor(Color.WHITE)
                dayView.setTypeface(null, Typeface.BOLD)
                selectedDayView      = dayView
                selectedDayIsToday   = isToday
            }
            isToday -> {
                // Hari ini — teks biru bold tanpa background
                dayView.background = circleDrawable(Color.TRANSPARENT)
                dayView.setTextColor(Color.parseColor("#5271FF"))
                dayView.setTypeface(null, Typeface.BOLD)
            }
            else -> {
                // Hari biasa
                dayView.background = circleDrawable(Color.TRANSPARENT)
                dayView.setTextColor(Color.parseColor("#333333"))
                dayView.setTypeface(null, Typeface.NORMAL)
            }
        }

        // ── Click listener ────────────────────────────────────────
        dayView.setOnClickListener {

            // DIPERBAIKI: Kembalikan warna view sebelumnya dengan benar
            // Cek apakah view sebelumnya adalah "hari ini" agar warnanya
            // dikembalikan ke biru bukan ke hitam
            selectedDayView?.let { prev ->
                prev.background = circleDrawable(Color.TRANSPARENT)
                if (selectedDayIsToday) {
                    prev.setTextColor(Color.parseColor("#5271FF"))
                    prev.setTypeface(null, Typeface.BOLD)
                } else {
                    prev.setTextColor(Color.parseColor("#333333"))
                    prev.setTypeface(null, Typeface.NORMAL)
                }
            }

            // Tampilkan hari yang baru dipilih
            dayView.background = circleDrawable(Color.parseColor("#5271FF"))
            dayView.setTextColor(Color.WHITE)
            dayView.setTypeface(null, Typeface.BOLD)

            // Simpan referensi baru
            selectedDayView    = dayView
            selectedDayIsToday = isToday

            // Update selectedDate
            selectedDate.apply {
                set(Calendar.YEAR,         currentMonth.get(Calendar.YEAR))
                set(Calendar.MONTH,        currentMonth.get(Calendar.MONTH))
                set(Calendar.DAY_OF_MONTH, day)
                set(Calendar.HOUR_OF_DAY,  0)
                set(Calendar.MINUTE,       0)
                set(Calendar.SECOND,       0)
                set(Calendar.MILLISECOND,  0)
            }

            // Callback ke Activity
            onDateSelected(selectedDate.timeInMillis)
        }

        gridLayout.addView(dayView)
    }

    // ── Buat drawable lingkaran ───────────────────────────────────
    private fun circleDrawable(color: Int): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(color)
        }
    }

    // ── Cek apakah day == tanggal terpilih ───────────────────────
    private fun isSameDay(day: Int): Boolean {
        return selectedDate.get(Calendar.YEAR)  == currentMonth.get(Calendar.YEAR) &&
                selectedDate.get(Calendar.MONTH) == currentMonth.get(Calendar.MONTH) &&
                selectedDate.get(Calendar.DAY_OF_MONTH) == day
    }

    // ── Cek apakah day == hari ini ───────────────────────────────
    private fun isTodayDate(day: Int): Boolean {
        val today = Calendar.getInstance()
        return today.get(Calendar.YEAR)  == currentMonth.get(Calendar.YEAR) &&
                today.get(Calendar.MONTH) == currentMonth.get(Calendar.MONTH) &&
                today.get(Calendar.DAY_OF_MONTH) == day
    }

    // ── Extension: Int → px ──────────────────────────────────────
    private fun Int.dpToPx(): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            this.toFloat(),
            context.resources.displayMetrics
        ).toInt()
    }
}