package com.railway.movementreport.ui.duty

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.railway.movementreport.MovementReportApp
import com.railway.movementreport.data.entity.MovementEntry
import com.railway.movementreport.databinding.ActivityAddDutyBinding
import com.railway.movementreport.ui.MovementViewModel
import com.railway.movementreport.ui.MovementViewModelFactory
import com.railway.movementreport.utils.DateUtils
import kotlinx.coroutines.launch
import java.util.Calendar

class AddDutyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddDutyBinding
    private val viewModel: MovementViewModel by viewModels {
        MovementViewModelFactory((application as MovementReportApp).repository)
    }
    private var editEntryId: Long = -1L
    private var selectedDate: String = DateUtils.getTodayDbFormat()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddDutyBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        editEntryId = intent.getLongExtra("entry_id", -1L)
        binding.etDate.setText(DateUtils.dbToDisplay(selectedDate))
        if (editEntryId != -1L) {
            supportActionBar?.title = "Edit Duty Entry"
            loadEntryForEdit()
        } else supportActionBar?.title = "Add Duty"
        binding.etDate.setOnClickListener { showDatePicker() }
        binding.tilDate.setEndIconOnClickListener { showDatePicker() }
        binding.btnSave.setOnClickListener { if (validateInputs()) saveEntry() }
        binding.btnCancel.setOnClickListener { finish() }
    }

    private fun loadEntryForEdit() {
        lifecycleScope.launch {
            viewModel.getEntryById(editEntryId)?.let {
                selectedDate = it.date
                binding.etDate.setText(DateUtils.dbToDisplay(it.date))
                binding.etTrainNumber.setText(it.trainNumber)
                binding.etStationFrom.setText(it.stationFrom)
                binding.etStationTo.setText(it.stationTo)
            }
        }
    }

    private fun showDatePicker() {
        val cal = Calendar.getInstance()
        val parts = selectedDate.split("-")
        if (parts.size == 3) cal.set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt())
        DatePickerDialog(this, { _, y, m, d ->
            selectedDate = String.format("%04d-%02d-%02d", y, m + 1, d)
            binding.etDate.setText(DateUtils.dbToDisplay(selectedDate))
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun validateInputs(): Boolean {
        var valid = true
        if (binding.etTrainNumber.text.toString().trim().isEmpty()) {
            binding.tilTrainNumber.error = "Train number is required"; valid = false
        } else binding.tilTrainNumber.error = null
        if (binding.etStationFrom.text.toString().trim().isEmpty()) {
            binding.tilStationFrom.error = "Station From is required"; valid = false
        } else binding.tilStationFrom.error = null
        if (binding.etStationTo.text.toString().trim().isEmpty()) {
            binding.tilStationTo.error = "Station To is required"; valid = false
        } else binding.tilStationTo.error = null
        return valid
    }

    private fun saveEntry() {
        val entry = MovementEntry(
            id = if (editEntryId != -1L) editEntryId else 0,
            date = selectedDate,
            trainNumber = binding.etTrainNumber.text.toString().trim().uppercase(),
            stationFrom = binding.etStationFrom.text.toString().trim().uppercase(),
            stationTo = binding.etStationTo.text.toString().trim().uppercase(),
            remarks = "Duty",
            entryType = "DUTY"
        )
        if (editEntryId != -1L) {
            viewModel.updateEntry(entry)
            Toast.makeText(this, "Duty entry updated!", Toast.LENGTH_SHORT).show()
        } else {
            viewModel.insertEntry(entry)
            Toast.makeText(this, "Duty entry saved!", Toast.LENGTH_SHORT).show()
        }
        finish()
    }

    override fun onSupportNavigateUp(): Boolean { onBackPressedDispatcher.onBackPressed(); return true }
}
