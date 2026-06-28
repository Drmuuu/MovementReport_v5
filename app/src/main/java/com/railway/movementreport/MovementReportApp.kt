package com.railway.movementreport

import android.app.Application
import com.railway.movementreport.data.database.MovementDatabase
import com.railway.movementreport.data.repository.MovementRepository
import com.railway.movementreport.data.repository.NightDutyRepository

class MovementReportApp : Application() {
    val database by lazy { MovementDatabase.getDatabase(this) }
    val repository by lazy { MovementRepository(database.movementEntryDao()) }
    val nightDutyRepository by lazy { NightDutyRepository(database.nightDutyDao()) }
}
