package com.railway.movementreport.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "movement_entries")
data class MovementEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String,           // Format: yyyy-MM-dd
    val trainNumber: String,
    val stationFrom: String,
    val stationTo: String,
    val remarks: String,        // "Duty", "Rest", "C-Rest"
    val entryType: String       // "DUTY", "REST"
)
