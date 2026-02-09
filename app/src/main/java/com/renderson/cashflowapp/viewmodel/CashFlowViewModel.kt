package com.renderson.cashflowapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.renderson.cashflowapp.data.repository.ClashFlowRepository
import com.renderson.cashflowapp.enums.TransactionCategory
import com.renderson.cashflowapp.enums.TypeExtract
import com.renderson.cashflowapp.model.DataExtract
import com.renderson.cashflowapp.usecase.SuggestTransactionCategoryUseCase
import com.renderson.cashflowapp.model.Transaction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CashFlowViewModel @Inject internal constructor(
    private val repository: ClashFlowRepository,
    private val suggestTransactionCategoryUseCase: SuggestTransactionCategoryUseCase
): ViewModel() {

    private val _extract = MutableLiveData<DataExtract>()
    val extract: LiveData<DataExtract> = _extract

    private val _transactionToEdit = MutableStateFlow<Transaction?>(null)
    val transactionToEdit: StateFlow<Transaction?> = _transactionToEdit.asStateFlow()

    private val _selectedCategory = MutableStateFlow<TransactionCategory>(TransactionCategory.OUTROS)
    val selectedCategory: StateFlow<TransactionCategory> = _selectedCategory.asStateFlow()

    fun suggestCategory(description: String): TransactionCategory =
        suggestTransactionCategoryUseCase(description)

    fun setSelectedCategory(category: TransactionCategory) {
        _selectedCategory.value = category
    }

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

    fun saveTransaction(date: String, description: String, type: TypeExtract, category: TransactionCategory, amount: Double) {
        viewModelScope.launch {
            repository.addTransaction(date, description, type, category, amount)
            loadExtract()
        }
    }

    fun setTransactionToEdit(transaction: Transaction?) {
        _transactionToEdit.value = transaction
        _selectedCategory.value = transaction?.category ?: TransactionCategory.OUTROS
    }

    fun clearTransactionToEdit() {
        _transactionToEdit.value = null
    }

    fun updateTransaction(
        transactionId: Int,
        date: String,
        description: String,
        type: TypeExtract,
        category: TransactionCategory,
        amount: Double
    ) {
        viewModelScope.launch {
            repository.updateTransaction(transactionId, date, description, type, category, amount)
            loadExtract()
            clearTransactionToEdit()
        }
    }

    fun deleteTransaction(transactionId: Int) {
        viewModelScope.launch {
            repository.deleteTransaction(transactionId)
            loadExtract()
            clearTransactionToEdit()
        }
    }
}