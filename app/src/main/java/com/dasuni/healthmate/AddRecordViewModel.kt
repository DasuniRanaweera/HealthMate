package com.dasuni.healthmate

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed interface SaveRecordState {
    data object Idle : SaveRecordState
    data object Saving : SaveRecordState
    data class Success(val recordId: Long) : SaveRecordState
    data class Error(val message: String) : SaveRecordState
}

class AddRecordViewModel(application: Application) :
    AndroidViewModel(application) {

    private val repository = HealthRecordRepository(application)

    private val _saveState =
        MutableStateFlow<SaveRecordState>(SaveRecordState.Idle)

    val saveState = _saveState.asStateFlow()

    fun saveRecord(record: HealthRecord) {
        if (_saveState.value == SaveRecordState.Saving ||
            _saveState.value is SaveRecordState.Success
        ) {
            return
        }

        _saveState.value = SaveRecordState.Saving

        viewModelScope.launch {
            try {
                val recordId = withContext(Dispatchers.IO) {
                    if (record.id > 0) {
                        val updatedRows = repository.updateRecord(record)

                        check(updatedRows == 1) {
                            "The record was not found."
                        }

                        record.id
                    } else {
                        repository.addRecord(record)
                    }
                }

                _saveState.value = SaveRecordState.Success(recordId)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _saveState.value = SaveRecordState.Error(
                    "Could not save the record. Please try again."
                )
            }
        }
    }
}
