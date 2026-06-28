package com.railway.movementreport.ui

import androidx.lifecycle.*
import com.railway.movementreport.data.entity.MovementEntry
import com.railway.movementreport.data.repository.MovementRepository
import kotlinx.coroutines.launch

class MovementViewModel(private val repository: MovementRepository) : ViewModel() {

    val allEntries: LiveData<List<MovementEntry>> = repository.allEntries
    val distinctMonths: LiveData<List<String>> = repository.distinctMonths

    private val _selectedMonthYear = MutableLiveData<String>()
    val selectedMonthYear: LiveData<String> = _selectedMonthYear

    val entriesForSelectedMonth: LiveData<List<MovementEntry>> =
        _selectedMonthYear.switchMap { month -> repository.getEntriesByMonth(month) }

    private val _searchDate = MutableLiveData<String>()
    val entriesForSearchDate: LiveData<List<MovementEntry>> =
        _searchDate.switchMap { date -> repository.getEntriesByDate(date) }

    private val _operationResult = MutableLiveData<String>()
    val operationResult: LiveData<String> = _operationResult

    fun setSelectedMonth(monthYear: String) { _selectedMonthYear.value = monthYear }
    fun setSearchDate(date: String) { _searchDate.value = date }

    fun insertEntry(entry: MovementEntry) = viewModelScope.launch {
        repository.insert(entry)
        _operationResult.value = "Entry saved successfully"
    }

    fun updateEntry(entry: MovementEntry) = viewModelScope.launch {
        repository.update(entry)
        _operationResult.value = "Entry updated successfully"
    }

    fun deleteEntry(entry: MovementEntry) = viewModelScope.launch {
        repository.delete(entry)
        _operationResult.value = "Entry deleted"
    }

    suspend fun getEntryById(id: Long): MovementEntry? = repository.getEntryById(id)

    suspend fun getEntriesForMonthSync(monthYear: String): List<MovementEntry> =
        repository.getEntriesByMonthSync(monthYear)

    suspend fun getSummary(monthYear: String): Triple<Int, Int, Int> = Triple(
        repository.getDutyCount(monthYear),
        repository.getRestCount(monthYear),
        repository.getCRestCount(monthYear)
    )
}

class MovementViewModelFactory(private val repository: MovementRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MovementViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MovementViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
