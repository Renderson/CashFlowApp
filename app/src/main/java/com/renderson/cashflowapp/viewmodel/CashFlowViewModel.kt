package com.renderson.cashflowapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.renderson.cashflowapp.data.repository.ClashFlowRepository
import com.renderson.cashflowapp.enums.TypeExtract
import com.renderson.cashflowapp.model.DataExtract
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CashFlowViewModel @Inject internal constructor(
    private val repository: ClashFlowRepository
): ViewModel() {

    private val _extract = MutableLiveData<DataExtract>()
    val extract: LiveData<DataExtract> = _extract

    init {
        loadExtract()
    }

    private fun loadExtract() {
        viewModelScope.launch {
            repository.getExtract().collect { dataExtract ->
                _extract.value = dataExtract
            }
        }
    }

    fun saveExtract(dataExtract: DataExtract) {
        viewModelScope.launch {
            repository.saveExtract(dataExtract)
            loadExtract()
        }
    }

    fun saveTransaction(date: String, description: String, type: TypeExtract, amount: Double) {
        viewModelScope.launch {
            repository.addTransaction(date, description, type, amount)
            loadExtract()
        }
    }
}