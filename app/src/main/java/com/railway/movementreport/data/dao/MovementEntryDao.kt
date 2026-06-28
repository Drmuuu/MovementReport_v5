package com.railway.movementreport.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.railway.movementreport.data.entity.MovementEntry

@Dao
interface MovementEntryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: MovementEntry): Long

    @Update
    suspend fun update(entry: MovementEntry)

    @Delete
    suspend fun delete(entry: MovementEntry)

    @Query("SELECT * FROM movement_entries ORDER BY date DESC")
    fun getAllEntries(): LiveData<List<MovementEntry>>

    @Query("SELECT * FROM movement_entries WHERE date LIKE :monthYear || '%' ORDER BY date ASC")
    fun getEntriesByMonth(monthYear: String): LiveData<List<MovementEntry>>

    @Query("SELECT * FROM movement_entries WHERE date LIKE :monthYear || '%' ORDER BY date ASC")
    suspend fun getEntriesByMonthSync(monthYear: String): List<MovementEntry>

    @Query("SELECT * FROM movement_entries WHERE date = :date ORDER BY id ASC")
    fun getEntriesByDate(date: String): LiveData<List<MovementEntry>>

    @Query("SELECT * FROM movement_entries WHERE id = :id")
    suspend fun getEntryById(id: Long): MovementEntry?

    @Query("SELECT * FROM movement_entries WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    fun getEntriesBetweenDates(startDate: String, endDate: String): LiveData<List<MovementEntry>>

    @Query("SELECT COUNT(*) FROM movement_entries WHERE date LIKE :monthYear || '%' AND remarks = 'Duty'")
    suspend fun getDutyCountForMonth(monthYear: String): Int

    @Query("SELECT COUNT(*) FROM movement_entries WHERE date LIKE :monthYear || '%' AND remarks = 'Rest'")
    suspend fun getRestCountForMonth(monthYear: String): Int

    @Query("SELECT COUNT(*) FROM movement_entries WHERE date LIKE :monthYear || '%' AND remarks = 'C-Rest'")
    suspend fun getCRestCountForMonth(monthYear: String): Int

    @Query("SELECT DISTINCT strftime('%Y-%m', date) FROM movement_entries ORDER BY date DESC")
    fun getDistinctMonths(): LiveData<List<String>>
}
