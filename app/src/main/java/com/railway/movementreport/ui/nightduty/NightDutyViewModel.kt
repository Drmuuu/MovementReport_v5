package com.railway.movementreport.ui.nightduty

import androidx.lifecycle.*
import com.railway.movementreport.data.entity.NightDutyEntry
import com.railway.movementreport.data.repository.NightDutyRepository
import kotlinx.coroutines.launch

class NightDutyViewModel(private val repo: NightDutyRepository) : ViewModel() {
    val allEntries = repo.allEntries
    val distinctMonths = repo.distinctMonths

    private val _month = MutableLiveData<String>()
    val entriesForMonth: LiveData<List<NightDutyEntry>> = _month.switchMap { repo.getByMonth(it) }

    fun setMonth(m: String) { _month.value = m }
    fun insert(e: NightDutyEntry) = viewModelScope.launch { repo.insert(e) }
    fun update(e: NightDutyEntry) = viewModelScope.launch { repo.update(e) }
    fun delete(e: NightDutyEntry) = viewModelScope.launch { repo.delete(e) }
    suspend fun getById(id: Long) = repo.getById(id)
    suspend fun getByMonthSync(m: String) = repo.getByMonthSync(m)
}

class NightDutyViewModelFactory(private val repo: NightDutyRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NightDutyViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST") return NightDutyViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel")
    }
}
