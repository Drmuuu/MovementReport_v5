package com.railway.movementreport.ui.rest

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.railway.movementreport.MovementReportApp
import com.railway.movementreport.R
import com.railway.movementreport.data.entity.MovementEntry
import com.railway.movementreport.databinding.ActivityAddRestBinding
import com.railway.movementreport.ui.MovementViewModel
import com.railway.movementreport.ui.MovementViewModelFactory
import com.railway.movementreport.utils.DateUtils
import kotlinx.coroutines.launch
import java.util.Calendar

class AddRestActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddRestBinding
    private val viewModel: MovementViewModel by viewModels {
        MovementViewModelFactory((application as MovementReportApp).repository)
    }
    private var editEntryId: Long = -1L
    private var selectedDate: String = DateUtils.getTodayDbFormat()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddRestBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        editEntryId = intent.getLongExtra("entry_id", -1L)
        binding.etDate.setText(DateUtils.dbToDisplay(selectedDate))
        if (editEntryId != -1L) {
            supportActionBar?.title = "Edit Rest Entry"
            loadEntryForEdit()
        } else supportActionBar?.title = "Add Rest"
        binding.etDate.setOnClickListener { showDatePicker() }
        binding.tilDate.setEndIconOnClickListener { showDatePicker() }
        binding.btnSave.setOnClickListener { saveEntry() }
        binding.btnCancel.setOnClickListener { finish() }
    }

    private fun loadEntryForEdit() {
        lifecycleScope.launch {
            viewModel.getEntryById(editEntryId)?.let {
                selectedDate = it.date
                binding.etDate.setText(DateUtils.dbToDisplay(it.date))
                when (it.remarks) {
                    "Rest" -> binding.rgRestType.check(R.id.rbRest)
                    "C-Rest" -> binding.rgRestType.check(R.id.rbCasualRest)
                }
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

    private fun saveEntry() {
        val remarks = when (binding.rgRestType.checkedRadioButtonId) {
            R.id.rbCasualRest -> "C-Rest"
            else -> "Rest"
        }
        val entry = MovementEntry(
            id = if (editEntryId != -1L) editEntryId else 0,
            date = selectedDate,
            trainNumber = "", stationFrom = "", stationTo = "",
            remarks = remarks, entryType = "REST"
        )
        if (editEntryId != -1L) {
            viewModel.updateEntry(entry)
            Toast.makeText(this, "Rest entry updated!", Toast.LENGTH_SHORT).show()
        } else {
            viewModel.insertEntry(entry)
            Toast.makeText(this, "Rest entry saved!", Toast.LENGTH_SHORT).show()
        }
        finish()
    }

    override fun onSupportNavigateUp(): Boolean { onBackPressedDispatcher.onBackPressed(); return true }
}
