package com.dasuni.healthmate

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class RecordHistoryState(
    val isLoading: Boolean = false,
    val records: List<HealthRecord> = emptyList(),
    val error: String? = null
)

class RecordHistoryViewModel(application: Application) :
    AndroidViewModel(application) {

    private val repository = HealthRecordRepository(application)

    private val _historyState = MutableStateFlow(RecordHistoryState())
    val historyState = _historyState.asStateFlow()

    private var loadJob: Job? = null

    fun loadRecords() {
        loadJob?.cancel()

        loadJob = viewModelScope.launch {
            _historyState.value = RecordHistoryState(isLoading = true)

            try {
                val records = withContext(Dispatchers.IO) {
                    repository.getRecords()
                }

                _historyState.value = RecordHistoryState(
                    records = records
                )
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _historyState.value = RecordHistoryState(
                    error = "Could not load records. Please try again."
                )
            }
        }
    }
    fun deleteRecord(recordId: Long) {
        if (_historyState.value.isLoading) return

        loadJob?.cancel()

        loadJob = viewModelScope.launch {
            _historyState.value = _historyState.value.copy(
                isLoading = true,
                error = null
            )

            try {
                val records = withContext(Dispatchers.IO) {
                    val deletedRows = repository.deleteRecord(recordId)

                    check(deletedRows == 1) {
                        "The record was not found."
                    }

                    repository.getRecords()
                }

                _historyState.value = RecordHistoryState(
                    records = records
                )
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _historyState.value = _historyState.value.copy(
                    isLoading = false,
                    error = "Could not complete deletion. Tap to reload records."
                )
            }
        }
    }
}
