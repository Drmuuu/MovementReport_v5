package com.railway.movementreport.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class UserPreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME      = "movement_report_prefs"
        private const val KEY_SETUP_DONE = "setup_done"
        private const val KEY_NAME       = "user_name"
        private const val KEY_DESIGNATION= "user_designation"
        private const val KEY_PF_NUMBER  = "user_pf_number"
        private const val KEY_PAY        = "user_pay"
        private const val KEY_LEVEL      = "user_level"
    }

    var isSetupDone: Boolean
        get() = prefs.getBoolean(KEY_SETUP_DONE, false)
        set(value) = prefs.edit { putBoolean(KEY_SETUP_DONE, value) }

    var name: String
        get() = prefs.getString(KEY_NAME, "") ?: ""
        set(value) = prefs.edit { putString(KEY_NAME, value) }

    var designation: String
        get() = prefs.getString(KEY_DESIGNATION, "") ?: ""
        set(value) = prefs.edit { putString(KEY_DESIGNATION, value) }

    var pfNumber: String
        get() = prefs.getString(KEY_PF_NUMBER, "") ?: ""
        set(value) = prefs.edit { putString(KEY_PF_NUMBER, value) }

    var pay: String
        get() = prefs.getString(KEY_PAY, "") ?: ""
        set(value) = prefs.edit { putString(KEY_PAY, value) }

    var level: String
        get() = prefs.getString(KEY_LEVEL, "") ?: ""
        set(value) = prefs.edit { putString(KEY_LEVEL, value) }

    // Setup saves all 5 fields
    fun saveUserDetails(
        name: String, designation: String, pfNumber: String,
        pay: String, level: String
    ) {
        prefs.edit {
            putString(KEY_NAME, name)
            putString(KEY_DESIGNATION, designation)
            putString(KEY_PF_NUMBER, pfNumber)
            putString(KEY_PAY, pay)
            putString(KEY_LEVEL, level)
            putBoolean(KEY_SETUP_DONE, true)
        }
    }
}
