package com.railway.movementreport.ui.nightduty

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import com.railway.movementreport.MovementReportApp
import com.railway.movementreport.data.entity.NightDutyEntry
import com.railway.movementreport.databinding.ActivityAddNightDutyBinding
import com.railway.movementreport.utils.DateUtils
import kotlinx.coroutines.launch
import java.util.Calendar

class AddNightDutyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddNightDutyBinding
    private val viewModel: NightDutyViewModel by viewModels {
        NightDutyViewModelFactory((application as MovementReportApp).nightDutyRepository)
    }

    private var editId: Long = -1L
    private var selectedFromDate = DateUtils.getTodayDbFormat()
    private var selectedToDate   = DateUtils.getTodayDbFormat()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddNightDutyBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        editId = intent.getLongExtra("entry_id", -1L)
        supportActionBar?.title = if (editId != -1L) "Edit Night Duty" else "Add Night Duty"

        binding.etFromDate.setText(DateUtils.dbToDisplay(selectedFromDate))
        binding.etToDate.setText(DateUtils.dbToDisplay(selectedToDate))

        if (editId != -1L) loadForEdit()

        binding.etFromDate.setOnClickListener { pickDate(isFrom = true) }
        binding.tilFromDate.setEndIconOnClickListener { pickDate(isFrom = true) }
        binding.etToDate.setOnClickListener { pickDate(isFrom = false) }
        binding.tilToDate.setEndIconOnClickListener { pickDate(isFrom = false) }
        binding.etNightFrom.setOnClickListener { pickTime(binding.etNightFrom, isFromField = true) }
        binding.tilNightFrom.setEndIconOnClickListener { pickTime(binding.etNightFrom, isFromField = true) }
        binding.etNightTo.setOnClickListener { pickTime(binding.etNightTo, isFromField = false) }
        binding.tilNightTo.setEndIconOnClickListener { pickTime(binding.etNightTo, isFromField = false) }

        binding.chipFromDash.setOnClickListener {
            binding.etNightFrom.setText("--")
            autoComputeTotalHrs()
        }
        binding.chipToDash.setOnClickListener {
            binding.etNightTo.setText("--")
            autoComputeTotalHrs()
        }

        binding.btnSave.setOnClickListener { if (validate()) save() }
        binding.btnCancel.setOnClickListener { finish() }
    }

    private fun loadForEdit() {
        lifecycleScope.launch {
            viewModel.getById(editId)?.let { e ->
                selectedFromDate = e.fromDate
                selectedToDate   = e.toDate
                binding.etFromDate.setText(DateUtils.dbToDisplay(e.fromDate))
                binding.etTrainNumber.setText(e.trainNumber)
                binding.etStationFrom.setText(e.stationFrom)
                binding.etStationTo.setText(e.stationTo)
                binding.etNightFrom.setText(e.nightDutyFrom)
                binding.etNightTo.setText(e.nightDutyTo)
                binding.etToDate.setText(DateUtils.dbToDisplay(e.toDate))
                binding.etTotalNightHrs.setText(e.totalNightHrs)
                binding.etRemark.setText(e.remark)
            }
        }
    }

    private fun pickDate(isFrom: Boolean) {
        val cal   = Calendar.getInstance()
        val src   = if (isFrom) selectedFromDate else selectedToDate
        val parts = src.split("-")
        if (parts.size == 3) cal.set(parts[0].toInt(), parts[1].toInt()-1, parts[2].toInt())
        DatePickerDialog(this, { _, y, m, d ->
            val date = String.format("%04d-%02d-%02d", y, m+1, d)
            if (isFrom) {
                selectedFromDate = date
                binding.etFromDate.setText(DateUtils.dbToDisplay(date))
                autoUpdateToDate()
            } else {
                selectedToDate = date
                binding.etToDate.setText(DateUtils.dbToDisplay(date))
            }
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }

    /**
     * Time picker with validation: only 22:00–23:59 and 00:00–06:00 allowed.
     * -- is always allowed via the chip buttons.
     */
    private fun pickTime(field: TextInputEditText, isFromField: Boolean) {
        val cal = Calendar.getInstance()
        TimePickerDialog(this, { _, h, m ->
            // Validate: must be 22–23 or 00–06
            val valid = h >= 22 || h <= 6
            if (!valid) {
                Toast.makeText(this,
                    "Night duty time must be between 22:00 and 06:00",
                    Toast.LENGTH_SHORT).show()
                return@TimePickerDialog
            }
            field.setText(String.format("%02d:%02d", h, m))
            if (isFromField) autoUpdateToDate()
            autoComputeTotalHrs()
        }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
    }

    private fun autoUpdateToDate() {
        val fromStr = binding.etNightFrom.text.toString().trim()
        if (fromStr.length < 5 || fromStr == "--") return
        val parts = fromStr.split(":")
        if (parts.size != 2) return
        val fromHour = parts[0].toIntOrNull() ?: return
        // Crosses midnight if start hour is 22–23
        val autoToDate = if (fromHour >= 22) DateUtils.addDays(selectedFromDate, 1)
                         else selectedFromDate
        selectedToDate = autoToDate
        binding.etToDate.setText(DateUtils.dbToDisplay(autoToDate))
    }

    /**
     * Auto-compute total night hours:
     * -- From → assume 22:00
     * -- To   → assume 06:00
     * Both -- → 22:00 to 06:00 = 08:00
     */
    private fun autoComputeTotalHrs() {
        val fromStr = binding.etNightFrom.text.toString().trim()
        val toStr   = binding.etNightTo.text.toString().trim()

        val fromMins = if (fromStr == "--" || fromStr.isEmpty()) 22 * 60
                       else parseToMins(fromStr) ?: 22 * 60
        val toMins   = if (toStr == "--" || toStr.isEmpty()) 6 * 60
                       else parseToMins(toStr) ?: 6 * 60

        // Handle midnight crossing: if toMins <= fromMins, add 24hrs to toMins
        val diff = if (toMins > fromMins) toMins - fromMins
                   else toMins + 24 * 60 - fromMins

        val hrs = diff / 60
        val mins = diff % 60
        binding.etTotalNightHrs.setText(String.format("%02d:%02d", hrs, mins))
    }

    private fun parseToMins(time: String): Int? {
        val parts = time.split(":")
        if (parts.size != 2) return null
        val h = parts[0].toIntOrNull() ?: return null
        val m = parts[1].toIntOrNull() ?: return null
        return h * 60 + m
    }

    private fun validate(): Boolean {
        var ok = true
        if (binding.etStationFrom.text.toString().trim().isEmpty()) {
            binding.tilStationFrom.error = "Required"; ok = false
        } else binding.tilStationFrom.error = null
        if (binding.etStationTo.text.toString().trim().isEmpty()) {
            binding.tilStationTo.error = "Required"; ok = false
        } else binding.tilStationTo.error = null
        if (binding.etNightFrom.text.toString().trim().isEmpty()) {
            binding.tilNightFrom.error = "Required"; ok = false
        } else binding.tilNightFrom.error = null
        if (binding.etNightTo.text.toString().trim().isEmpty()) {
            binding.tilNightTo.error = "Required"; ok = false
        } else binding.tilNightTo.error = null
        if (binding.etTotalNightHrs.text.toString().trim().isEmpty()) {
            binding.tilTotalNightHrs.error = "Required"; ok = false
        } else binding.tilTotalNightHrs.error = null
        return ok
    }

    private fun save() {
        val entry = NightDutyEntry(
            id            = if (editId != -1L) editId else 0,
            fromDate      = selectedFromDate,
            trainNumber   = binding.etTrainNumber.text.toString().trim().uppercase(),
            stationFrom   = binding.etStationFrom.text.toString().trim().uppercase(),
            stationTo     = binding.etStationTo.text.toString().trim().uppercase(),
            nightDutyFrom = binding.etNightFrom.text.toString().trim().ifEmpty { "--" },
            nightDutyTo   = binding.etNightTo.text.toString().trim().ifEmpty { "--" },
            toDate        = selectedToDate,
            totalNightHrs = binding.etTotalNightHrs.text.toString().trim(),
            remark        = binding.etRemark.text.toString().trim()
        )
        if (editId != -1L) viewModel.update(entry) else viewModel.insert(entry)
        Toast.makeText(this, if (editId != -1L) "Updated!" else "Saved!", Toast.LENGTH_SHORT).show()
        finish()
    }

    override fun onSupportNavigateUp(): Boolean { onBackPressedDispatcher.onBackPressed(); return true }
}
