package com.railway.movementreport.data.repository

import androidx.lifecycle.LiveData
import com.railway.movementreport.data.dao.NightDutyDao
import com.railway.movementreport.data.entity.NightDutyEntry

class NightDutyRepository(private val dao: NightDutyDao) {
    val allEntries: LiveData<List<NightDutyEntry>> = dao.getAllEntries()
    val distinctMonths: LiveData<List<String>> = dao.getDistinctMonths()
    suspend fun insert(e: NightDutyEntry) = dao.insert(e)
    suspend fun update(e: NightDutyEntry) = dao.update(e)
    suspend fun delete(e: NightDutyEntry) = dao.delete(e)
    fun getByMonth(my: String) = dao.getEntriesByMonth(my)
    suspend fun getByMonthSync(my: String) = dao.getEntriesByMonthSync(my)
    suspend fun getById(id: Long) = dao.getById(id)
}
