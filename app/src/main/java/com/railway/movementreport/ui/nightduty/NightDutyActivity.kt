package com.railway.movementreport.ui.nightduty

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
import androidx.recyclerview.widget.LinearLayoutManager
import com.railway.movementreport.MovementReportApp
import com.railway.movementreport.R
import com.railway.movementreport.databinding.ActivityNightDutyBinding
import com.railway.movementreport.utils.DateUtils
import com.railway.movementreport.utils.NightDutyPdfGenerator
import com.railway.movementreport.utils.UserPreferences
import kotlinx.coroutines.launch
import java.util.Calendar

class NightDutyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNightDutyBinding
    private val viewModel: NightDutyViewModel by viewModels {
        NightDutyViewModelFactory((application as MovementReportApp).nightDutyRepository)
    }
    private lateinit var adapter: NightDutyAdapter
    private var selectedMonth = DateUtils.getCurrentMonthYear()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNightDutyBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Night Duty Allowance"

        adapter = NightDutyAdapter(
            onEdit = { e ->
                startActivity(Intent(this, AddNightDutyActivity::class.java)
                    .putExtra("entry_id", e.id))
            },
            onDelete = { e ->
                AlertDialog.Builder(this)
                    .setTitle("Delete Entry")
                    .setMessage("Delete entry for ${DateUtils.dbToDisplay(e.fromDate)}?")
                    .setPositiveButton("Delete") { _, _ -> viewModel.delete(e) }
                    .setNegativeButton("Cancel", null).show()
            }
        )
        binding.rvNightDuty.layoutManager = LinearLayoutManager(this)
        binding.rvNightDuty.adapter = adapter

        viewModel.setMonth(selectedMonth)
        binding.tvCurrentMonth.text = DateUtils.getDisplayMonthYear(selectedMonth)

        viewModel.entriesForMonth.observe(this) { entries ->
            adapter.submitList(entries)
            binding.tvEmpty.visibility  = if (entries.isEmpty()) View.VISIBLE else View.GONE
            binding.rvNightDuty.visibility = if (entries.isEmpty()) View.GONE else View.VISIBLE
            binding.tvCount.text = "${entries.size} entries"
        }

        binding.btnAddNightDuty.setOnClickListener {
            startActivity(Intent(this, AddNightDutyActivity::class.java))
        }
        binding.btnPickMonth.setOnClickListener { showMonthPicker() }
        binding.btnExportNd.setOnClickListener  { promptPlace() }
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
        AlertDialog.Builder(this).setTitle("Select Month").setView(view)
            .setPositiveButton("OK") { _, _ ->
                selectedMonth = String.format("%04d-%02d", selY, selM+1)
                viewModel.setMonth(selectedMonth)
                binding.tvCurrentMonth.text = DateUtils.getDisplayMonthYear(selectedMonth)
            }.setNegativeButton("Cancel", null).show()
    }

    // Only ask Place — pay/level come from stored profile
    private fun promptPlace() {
        val et = EditText(this).apply {
            hint = "Enter Place (e.g. Lucknow)"
            setPadding(48, 24, 48, 24)
        }
        AlertDialog.Builder(this)
            .setTitle("Export Night Duty Bill")
            .setView(et)
            .setPositiveButton("Generate PDF") { _, _ ->
                exportPdf(et.text.toString().trim().ifEmpty { "—" })
            }.setNegativeButton("Cancel", null).show()
    }

    private fun exportPdf(place: String) {
        binding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val entries = viewModel.getByMonthSync(selectedMonth)
                if (entries.isEmpty()) {
                    Toast.makeText(this@NightDutyActivity,
                        "No entries for this month!", Toast.LENGTH_SHORT).show()
                    binding.progressBar.visibility = View.GONE
                    return@launch
                }
                val prefs = UserPreferences(this@NightDutyActivity)
                val file  = NightDutyPdfGenerator(this@NightDutyActivity).generate(
                    entries, selectedMonth, prefs, place, DateUtils.getExportDate()
                )
                binding.progressBar.visibility = View.GONE
                val uri = FileProvider.getUriForFile(
                    this@NightDutyActivity, "${packageName}.provider", file)
                startActivity(Intent.createChooser(
                    Intent(Intent.ACTION_SEND).apply {
                        type = "application/pdf"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        putExtra(Intent.EXTRA_SUBJECT,
                            "Night Duty Bill - ${DateUtils.getDisplayMonthYear(selectedMonth)}")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }, "Share or Save PDF"
                ))
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this@NightDutyActivity,
                    "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean { onBackPressedDispatcher.onBackPressed(); return true }
}
