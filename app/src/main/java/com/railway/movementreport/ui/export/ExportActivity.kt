package com.railway.movementreport.ui.export

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.railway.movementreport.MovementReportApp
import com.railway.movementreport.R
import com.railway.movementreport.databinding.ActivityExportBinding
import com.railway.movementreport.ui.MovementViewModel
import com.railway.movementreport.ui.MovementViewModelFactory
import com.railway.movementreport.utils.DateUtils
import com.railway.movementreport.utils.PdfGenerator
import com.railway.movementreport.utils.UserPreferences
import kotlinx.coroutines.launch
import java.util.Calendar

class ExportActivity : AppCompatActivity() {

    private lateinit var binding: ActivityExportBinding
    private val viewModel: MovementViewModel by viewModels {
        MovementViewModelFactory((application as MovementReportApp).repository)
    }
    private lateinit var userPrefs: UserPreferences
    private var selectedMonthYear: String = DateUtils.getCurrentMonthYear()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExportBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Export Report"
        userPrefs = UserPreferences(this)
        updateMonthDisplay()
        loadPreview()
        binding.btnPickMonth.setOnClickListener { showMonthPicker() }
        binding.btnExport.setOnClickListener { promptPlace() }
    }

    private fun updateMonthDisplay() {
        binding.tvSelectedMonth.text = DateUtils.getDisplayMonthYear(selectedMonthYear)
    }

    private fun loadPreview() {
        viewModel.setSelectedMonth(selectedMonthYear)
        viewModel.entriesForSelectedMonth.observe(this) { entries ->
            if (entries.isEmpty()) {
                binding.tvPreviewSummary.text = "No entries found for selected month."
                binding.btnExport.isEnabled = false
            } else {
                val duty  = entries.count { it.remarks == "Duty" }
                val rest  = entries.count { it.remarks == "Rest" }
                val cRest = entries.count { it.remarks == "C-Rest" }
                val cl    = entries.count { it.remarks == "CL" }
                val lap   = entries.count { it.remarks == "LAP" }
                binding.tvPreviewSummary.text =
                    "Total Entries: ${entries.size}\n" +
                    "Duty: $duty  |  Rest: $rest  |  C-Rest: $cRest" +
                    (if (cl > 0) "  |  CL: $cl" else "") +
                    (if (lap > 0) "  |  LAP: $lap" else "")
                binding.btnExport.isEnabled = true
            }
        }
    }

    private fun showMonthPicker() {
        val months = arrayOf("January","February","March","April","May","June",
            "July","August","September","October","November","December")
        val cal = Calendar.getInstance()
        var selM = cal.get(Calendar.MONTH); var selY = cal.get(Calendar.YEAR)
        val view = layoutInflater.inflate(R.layout.dialog_month_picker, null)
        val npM = view.findViewById<android.widget.NumberPicker>(R.id.npMonth)
        val npY = view.findViewById<android.widget.NumberPicker>(R.id.npYear)
        npM.minValue=0; npM.maxValue=11; npM.displayedValues=months; npM.value=selM
        npY.minValue=2020; npY.maxValue=2035; npY.value=selY
        npM.setOnValueChangedListener{_,_,v->selM=v}
        npY.setOnValueChangedListener{_,_,v->selY=v}
        AlertDialog.Builder(this).setTitle("Select Month for Export").setView(view)
            .setPositiveButton("OK") { _, _ ->
                selectedMonthYear = String.format("%04d-%02d", selY, selM+1)
                updateMonthDisplay(); loadPreview()
            }.setNegativeButton("Cancel", null).show()
    }

    // Only ask for Place — pay/level come from stored profile
    private fun promptPlace() {
        val et = EditText(this).apply {
            hint = "Enter Place (e.g. Mumbai)"
            setPadding(48, 24, 48, 24)
        }
        AlertDialog.Builder(this)
            .setTitle("Export Movement Report")
            .setView(et)
            .setPositiveButton("Generate PDF") { _, _ ->
                generateAndShare(et.text.toString().trim().ifEmpty { "—" })
            }.setNegativeButton("Cancel", null).show()
    }

    private fun generateAndShare(place: String) {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnExport.isEnabled = false
        lifecycleScope.launch {
            try {
                val entries = viewModel.getEntriesForMonthSync(selectedMonthYear)
                if (entries.isEmpty()) {
                    Toast.makeText(this@ExportActivity, "No entries for this month!", Toast.LENGTH_SHORT).show()
                    binding.progressBar.visibility = View.GONE
                    binding.btnExport.isEnabled = true
                    return@launch
                }
                val file = PdfGenerator(this@ExportActivity).generateReport(
                    entries, selectedMonthYear, userPrefs, place, DateUtils.getExportDate()
                )
                binding.progressBar.visibility = View.GONE
                binding.btnExport.isEnabled = true
                val uri = FileProvider.getUriForFile(
                    this@ExportActivity, "${packageName}.provider", file)
                startActivity(Intent.createChooser(
                    Intent(Intent.ACTION_SEND).apply {
                        type = "application/pdf"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        putExtra(Intent.EXTRA_SUBJECT,
                            "Movement Report - ${DateUtils.getDisplayMonthYear(selectedMonthYear)}")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }, "Share or Save PDF"
                ))
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                binding.btnExport.isEnabled = true
                Toast.makeText(this@ExportActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean { onBackPressedDispatcher.onBackPressed(); return true }
}
