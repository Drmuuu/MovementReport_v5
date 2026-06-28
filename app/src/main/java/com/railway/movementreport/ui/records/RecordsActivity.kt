package com.railway.movementreport.ui.records

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.railway.movementreport.MovementReportApp
import com.railway.movementreport.R
import com.railway.movementreport.data.entity.MovementEntry
import com.railway.movementreport.databinding.ActivityRecordsBinding
import com.railway.movementreport.ui.MovementViewModel
import com.railway.movementreport.ui.MovementViewModelFactory
import com.railway.movementreport.ui.duty.AddDutyActivity
import com.railway.movementreport.ui.rest.AddRestActivity
import com.railway.movementreport.utils.DateUtils
import java.util.Calendar

class RecordsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecordsBinding
    private val viewModel: MovementViewModel by viewModels {
        MovementViewModelFactory((application as MovementReportApp).repository)
    }
    private lateinit var adapter: MovementEntryAdapter
    private var isSearchMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecordsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "View Records"

        adapter = MovementEntryAdapter(
            onEdit = { entry ->
                val intent = when {
                    entry.entryType == "DUTY" && entry.remarks == "OD" ->
                        Intent(this, com.railway.movementreport.ui.duty.AddOfficeDutyActivity::class.java)
                    entry.entryType == "DUTY" -> Intent(this, AddDutyActivity::class.java)
                    entry.entryType == "LEAVE" -> Intent(this, com.railway.movementreport.ui.leave.AddLeaveActivity::class.java)
                    else -> Intent(this, AddRestActivity::class.java)
                }
                intent.putExtra("entry_id", entry.id)
                startActivity(intent)
            },
            onDelete = { entry ->
                AlertDialog.Builder(this)
                    .setTitle("Delete Entry")
                    .setMessage("Delete entry for ${DateUtils.dbToDisplay(entry.date)}?")
                    .setPositiveButton("Delete") { _, _ -> viewModel.deleteEntry(entry) }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        )
        binding.rvEntries.layoutManager = LinearLayoutManager(this)
        binding.rvEntries.adapter = adapter

        val currentMonth = DateUtils.getCurrentMonthYear()
        viewModel.setSelectedMonth(currentMonth)
        binding.tvCurrentFilter.text = "Month: ${DateUtils.getDisplayMonthYear(currentMonth)}"

        viewModel.entriesForSelectedMonth.observe(this) { entries ->
            if (!isSearchMode) { adapter.submitList(entries); updateState(entries) }
        }
        viewModel.entriesForSearchDate.observe(this) { entries ->
            if (isSearchMode) { adapter.submitList(entries); updateState(entries) }
        }

        binding.btnPickMonth.setOnClickListener { showMonthPicker() }
        binding.btnSearchDate.setOnClickListener { showDateSearchPicker() }
        binding.btnClearSearch.setOnClickListener { clearSearch() }
    }

    private fun showMonthPicker() {
        val months = arrayOf("January","February","March","April","May","June",
            "July","August","September","October","November","December")
        val cal = Calendar.getInstance()
        var selMonth = cal.get(Calendar.MONTH)
        var selYear = cal.get(Calendar.YEAR)
        val view = layoutInflater.inflate(R.layout.dialog_month_picker, null)
        val npMonth = view.findViewById<android.widget.NumberPicker>(R.id.npMonth)
        val npYear = view.findViewById<android.widget.NumberPicker>(R.id.npYear)
        npMonth.minValue = 0; npMonth.maxValue = 11; npMonth.displayedValues = months; npMonth.value = selMonth
        npYear.minValue = 2020; npYear.maxValue = 2035; npYear.value = selYear
        npMonth.setOnValueChangedListener { _, _, v -> selMonth = v }
        npYear.setOnValueChangedListener { _, _, v -> selYear = v }
        AlertDialog.Builder(this).setTitle("Select Month").setView(view)
            .setPositiveButton("OK") { _, _ ->
                isSearchMode = false
                val monthYear = String.format("%04d-%02d", selYear, selMonth + 1)
                viewModel.setSelectedMonth(monthYear)
                binding.tvCurrentFilter.text = "Month: ${DateUtils.getDisplayMonthYear(monthYear)}"
                binding.btnClearSearch.visibility = View.GONE
            }.setNegativeButton("Cancel", null).show()
    }

    private fun showDateSearchPicker() {
        val cal = Calendar.getInstance()
        DatePickerDialog(this, { _, y, m, d ->
            isSearchMode = true
            val date = String.format("%04d-%02d-%02d", y, m + 1, d)
            viewModel.setSearchDate(date)
            binding.tvCurrentFilter.text = "Date: ${DateUtils.dbToDisplay(date)}"
            binding.btnClearSearch.visibility = View.VISIBLE
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun clearSearch() {
        isSearchMode = false
        binding.btnClearSearch.visibility = View.GONE
        val currentMonth = DateUtils.getCurrentMonthYear()
        viewModel.setSelectedMonth(currentMonth)
        binding.tvCurrentFilter.text = "Month: ${DateUtils.getDisplayMonthYear(currentMonth)}"
    }

    private fun updateState(entries: List<MovementEntry>) {
        val isEmpty = entries.isEmpty()
        binding.tvEmpty.visibility   = if (isEmpty) View.VISIBLE else View.GONE
        binding.rvEntries.visibility = if (isEmpty) View.GONE else View.VISIBLE
        val duty  = entries.count { it.remarks == "Duty" }
        val rest  = entries.count { it.remarks == "Rest" }
        val cRest = entries.count { it.remarks == "C-Rest" }
        val cl    = entries.count { it.remarks == "CL" }
        val lap   = entries.count { it.remarks == "LAP" }
        val sick  = entries.count { it.remarks == "SICK" }
        val scl   = entries.count { it.remarks == "SCL" }
        val od    = entries.count { it.remarks == "OD" }
        val sb    = StringBuilder("Duty: $duty  |  Rest: $rest  |  C-Rest: $cRest")
        if (cl   > 0) sb.append("  |  CL: $cl")
        if (lap  > 0) sb.append("  |  LAP: $lap")
        if (sick > 0) sb.append("  |  Sick: $sick")
        if (scl  > 0) sb.append("  |  SCL: $scl")
        if (od   > 0) sb.append("  |  OD: $od")
        sb.append("  |  Total: ${entries.size}")
        binding.tvSummary.text = sb.toString()
    }

    override fun onSupportNavigateUp(): Boolean { onBackPressedDispatcher.onBackPressed(); return true }
}
