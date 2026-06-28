package com.railway.movementreport.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "night_duty_entries")
data class NightDutyEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fromDate: String,
    val trainNumber: String,    // NEW — train number
    val stationFrom: String,
    val stationTo: String,
    val nightDutyFrom: String,
    val nightDutyTo: String,
    val toDate: String,
    val totalNightHrs: String,
    val remark: String
)
