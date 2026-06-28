package com.railway.movementreport.ui.setup

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.railway.movementreport.databinding.ActivitySetupBinding
import com.railway.movementreport.ui.home.HomeActivity
import com.railway.movementreport.utils.UserPreferences

class SetupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySetupBinding
    private lateinit var userPrefs: UserPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySetupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        userPrefs = UserPreferences(this)

        if (userPrefs.isSetupDone) { goToHome(); return }

        binding.btnSaveSetup.setOnClickListener {
            if (validateInputs()) saveAndContinue()
        }
    }

    private fun validateInputs(): Boolean {
        var valid = true
        if (binding.etName.text.toString().trim().isEmpty()) {
            binding.tilName.error = "Name is required"; valid = false
        } else binding.tilName.error = null
        if (binding.etDesignation.text.toString().trim().isEmpty()) {
            binding.tilDesignation.error = "Designation is required"; valid = false
        } else binding.tilDesignation.error = null
        if (binding.etPfNumber.text.toString().trim().isEmpty()) {
            binding.tilPfNumber.error = "PF Number is required"; valid = false
        } else binding.tilPfNumber.error = null
        if (binding.etPay.text.toString().trim().isEmpty()) {
            binding.tilPay.error = "Pay is required"; valid = false
        } else binding.tilPay.error = null
        if (binding.etLevel.text.toString().trim().isEmpty()) {
            binding.tilLevel.error = "Level is required"; valid = false
        } else binding.tilLevel.error = null
        return valid
    }

    private fun saveAndContinue() {
        userPrefs.saveUserDetails(
            name        = binding.etName.text.toString().trim(),
            designation = binding.etDesignation.text.toString().trim(),
            pfNumber    = binding.etPfNumber.text.toString().trim(),
            pay         = binding.etPay.text.toString().trim(),
            level       = binding.etLevel.text.toString().trim()
        )
        Toast.makeText(this, "Profile saved!", Toast.LENGTH_SHORT).show()
        goToHome()
    }

    private fun goToHome() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }
}
