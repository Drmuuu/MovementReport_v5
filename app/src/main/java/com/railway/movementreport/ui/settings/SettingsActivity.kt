package com.railway.movementreport.ui.settings

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.railway.movementreport.databinding.ActivitySettingsBinding
import com.railway.movementreport.utils.UserPreferences

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var userPrefs: UserPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Settings"
        userPrefs = UserPreferences(this)

        binding.etName.setText(userPrefs.name)
        binding.etDesignation.setText(userPrefs.designation)
        binding.etPfNumber.setText(userPrefs.pfNumber)
        binding.etPay.setText(userPrefs.pay)
        binding.etLevel.setText(userPrefs.level)

        binding.btnSaveSettings.setOnClickListener { if (validateInputs()) saveSettings() }
    }

    private fun validateInputs(): Boolean {
        var valid = true
        if (binding.etName.text.toString().trim().isEmpty()) {
            binding.tilName.error = "Required"; valid = false } else binding.tilName.error = null
        if (binding.etDesignation.text.toString().trim().isEmpty()) {
            binding.tilDesignation.error = "Required"; valid = false } else binding.tilDesignation.error = null
        if (binding.etPfNumber.text.toString().trim().isEmpty()) {
            binding.tilPfNumber.error = "Required"; valid = false } else binding.tilPfNumber.error = null
        if (binding.etPay.text.toString().trim().isEmpty()) {
            binding.tilPay.error = "Required"; valid = false } else binding.tilPay.error = null
        if (binding.etLevel.text.toString().trim().isEmpty()) {
            binding.tilLevel.error = "Required"; valid = false } else binding.tilLevel.error = null
        return valid
    }

    private fun saveSettings() {
        userPrefs.saveUserDetails(
            name        = binding.etName.text.toString().trim(),
            designation = binding.etDesignation.text.toString().trim(),
            pfNumber    = binding.etPfNumber.text.toString().trim(),
            pay         = binding.etPay.text.toString().trim(),
            level       = binding.etLevel.text.toString().trim()
        )
        Toast.makeText(this, "Settings saved!", Toast.LENGTH_SHORT).show()
        finish()
    }

    override fun onSupportNavigateUp(): Boolean { onBackPressedDispatcher.onBackPressed(); return true }
}
