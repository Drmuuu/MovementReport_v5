package com.railway.movementreport.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.railway.movementreport.R
import com.railway.movementreport.databinding.ActivityHomeBinding
import com.railway.movementreport.ui.duty.AddDutyActivity
import com.railway.movementreport.ui.duty.AddOfficeDutyActivity
import androidx.appcompat.app.AlertDialog
import com.railway.movementreport.ui.export.ExportActivity
import com.railway.movementreport.ui.leave.AddLeaveActivity
import com.railway.movementreport.ui.nightduty.NightDutyActivity
import com.railway.movementreport.ui.records.RecordsActivity
import com.railway.movementreport.ui.rest.AddRestActivity
import com.railway.movementreport.ui.settings.SettingsActivity
import com.railway.movementreport.utils.UserPreferences

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        val prefs = UserPreferences(this)
        binding.tvWelcome.text     = "Welcome, ${prefs.name}"
        binding.tvDesignation.text = prefs.designation

        binding.cardAddDuty.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Add Duty")
                .setItems(arrayOf("Add Duty", "Add Office Duty")) { _, which ->
                    when (which) {
                        0 -> startActivity(Intent(this, AddDutyActivity::class.java))
                        1 -> startActivity(Intent(this, AddOfficeDutyActivity::class.java))
                    }
                }.show()
        }
        binding.cardAddRest.setOnClickListener     { startActivity(Intent(this, AddRestActivity::class.java)) }
        binding.cardAddLeave.setOnClickListener    { startActivity(Intent(this, AddLeaveActivity::class.java)) }
        binding.cardViewRecords.setOnClickListener { startActivity(Intent(this, RecordsActivity::class.java)) }
        binding.cardExport.setOnClickListener      { startActivity(Intent(this, ExportActivity::class.java)) }
        binding.cardNightDuty.setOnClickListener   { startActivity(Intent(this, NightDutyActivity::class.java)) }
        binding.fabSettings.setOnClickListener     { startActivity(Intent(this, SettingsActivity::class.java)) }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_home, menu); return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_about -> { showAboutDialog(); true }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showAboutDialog() {
        AlertDialog.Builder(this)
            .setTitle("About Movement Report App")
            .setMessage(
                "Movement Report\nVersion 5.0\n\n" +
                "Developed by\nVinayak Chakraborty\n\n" +
                "───────────────────────\n\n" +
                "Features:\n\n" +
                "• Add Duty entries with train number and stations\n\n" +
                "• Add Rest and Casual Rest entries\n\n" +
                "• Add Casual Leave (CL) and Leave on Average Pay (LAP)\n\n" +
                "• View, edit and delete entries by month or date\n\n" +
                "• Search entries by specific date\n\n" +
                "• Export monthly Movement Report as A4 PDF\n\n" +
                "• Night Duty Allowance Bill — add, view and export\n\n" +
                "• Share or save PDFs via any app\n\n" +
                "• Edit your profile from Settings\n\n" +
                "────────────────────────\n\n" +
                "🔒 100% Offline & Private\n\n" +
                "All data is stored locally on your device only. No internet required."
            )
            .setPositiveButton("OK", null).show()
    }
}
