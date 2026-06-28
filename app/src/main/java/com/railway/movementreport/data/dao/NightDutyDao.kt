package com.railway.movementreport.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.railway.movementreport.data.entity.NightDutyEntry

@Dao
interface NightDutyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: NightDutyEntry): Long

    @Update
    suspend fun update(entry: NightDutyEntry)

    @Delete
    suspend fun delete(entry: NightDutyEntry)

    @Query("SELECT * FROM night_duty_entries ORDER BY fromDate DESC")
    fun getAllEntries(): LiveData<List<NightDutyEntry>>

    @Query("SELECT * FROM night_duty_entries WHERE fromDate LIKE :monthYear || '%' ORDER BY fromDate ASC")
    fun getEntriesByMonth(monthYear: String): LiveData<List<NightDutyEntry>>

    @Query("SELECT * FROM night_duty_entries WHERE fromDate LIKE :monthYear || '%' ORDER BY fromDate ASC")
    suspend fun getEntriesByMonthSync(monthYear: String): List<NightDutyEntry>

    @Query("SELECT * FROM night_duty_entries WHERE id = :id")
    suspend fun getById(id: Long): NightDutyEntry?

    @Query("SELECT DISTINCT strftime('%Y-%m', fromDate) FROM night_duty_entries ORDER BY fromDate DESC")
    fun getDistinctMonths(): LiveData<List<String>>
}
