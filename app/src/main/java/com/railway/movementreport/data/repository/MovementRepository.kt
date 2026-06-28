package com.railway.movementreport.data.repository

import androidx.lifecycle.LiveData
import com.railway.movementreport.data.dao.MovementEntryDao
import com.railway.movementreport.data.entity.MovementEntry

class MovementRepository(private val dao: MovementEntryDao) {

    val allEntries: LiveData<List<MovementEntry>> = dao.getAllEntries()
    val distinctMonths: LiveData<List<String>> = dao.getDistinctMonths()

    suspend fun insert(entry: MovementEntry): Long = dao.insert(entry)
    suspend fun update(entry: MovementEntry) = dao.update(entry)
    suspend fun delete(entry: MovementEntry) = dao.delete(entry)

    fun getEntriesByMonth(monthYear: String): LiveData<List<MovementEntry>> =
        dao.getEntriesByMonth(monthYear)

    suspend fun getEntriesByMonthSync(monthYear: String): List<MovementEntry> =
        dao.getEntriesByMonthSync(monthYear)

    fun getEntriesByDate(date: String): LiveData<List<MovementEntry>> =
        dao.getEntriesByDate(date)

    suspend fun getEntryById(id: Long): MovementEntry? = dao.getEntryById(id)

    fun getEntriesBetweenDates(start: String, end: String): LiveData<List<MovementEntry>> =
        dao.getEntriesBetweenDates(start, end)

    suspend fun getDutyCount(monthYear: String): Int = dao.getDutyCountForMonth(monthYear)
    suspend fun getRestCount(monthYear: String): Int = dao.getRestCountForMonth(monthYear)
    suspend fun getCRestCount(monthYear: String): Int = dao.getCRestCountForMonth(monthYear)
}
