package com.renderson.cashflowapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.renderson.cashflowapp.data.repository.ClashFlowRepository
import com.renderson.cashflowapp.enums.TypeExtract
import com.renderson.cashflowapp.model.DataExtract
import com.renderson.cashflowapp.model.Months
import com.renderson.cashflowapp.model.Transaction
import com.renderson.cashflowapp.model.Years
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CashFlowViewModel @Inject internal constructor(
    private val repository: ClashFlowRepository
): ViewModel() {

    private var _deposit = MutableStateFlow(0.0)
    val deposit: StateFlow<Double> = _deposit

    private var _payment = MutableStateFlow(0.0)
    val payment: StateFlow<Double> = _payment

    private val _extract = MutableLiveData<DataExtract>()
    val extract: LiveData<DataExtract> = _extract

    val data = DataExtract(
        years = listOf(
            Years(
                year = "2023",
                months = listOf(
                    Months(
                        month = "2023-06-15",
                        transactions = listOf(
                            Transaction(
                                date = "2023-06-15",
                                description = "Mp *Revanche 1/2",
                                type = TypeExtract.PAYMENT,
                                amount = 1.000
                            ),
                            Transaction(
                                date = "2023-06-20",
                                description = "Mp *Revanche 1/2",
                                type = TypeExtract.PAYMENT,
                                amount = 100.0
                            )
                        )
                    ),
                    Months(
                        month = "2023-07-23",
                        transactions = listOf(
                            Transaction(
                                date = "2023-07-23",
                                description = "Mp *Revanche 1/2",
                                type = TypeExtract.PAYMENT,
                                amount = 100.0
                            )
                        )
                    ),
                    Months(
                        month = "2023-08-15",
                        transactions = listOf(
                            Transaction(
                                date = "2023-08-15",
                                description = "Mp *C&A",
                                type = TypeExtract.DEPOSIT,
                                amount = 5000.0
                            )
                        )
                    )
                )
            ),
            Years(
                year = "2024",
                months = listOf(
                    Months(
                        month = "2024-08-15",
                        transactions = listOf(
                            Transaction(
                                date = "2024-08-15",
                                description = "Mp *Revanche 1/2",
                                type = TypeExtract.PAYMENT,
                                amount = 100.0
                            ),
                            Transaction(
                                date = "2024-08-20",
                                description = "Mp *Revanche 1/2",
                                type = TypeExtract.DEPOSIT,
                                amount = 1000.0
                            ),
                            Transaction(
                                date = "2024-08-21",
                                description = "South System",
                                type = TypeExtract.DEPOSIT,
                                amount = 11000.0
                            )
                        )
                    )
                )
            )
        )
    )

    init {
        loadExtract()
    }

    private fun loadExtract() {
        viewModelScope.launch {
            repository.getExtract().collect { dataExtract ->
                _extract.value = dataExtract
                getTotalDeposit()
                getTotalPayment()
            }
        }
    }

    fun saveExtract(dataExtract: DataExtract) {
        viewModelScope.launch {
            repository.saveExtract(dataExtract)
            loadExtract()
        }
    }

    private fun getTotalDeposit() {
        _deposit.value = _extract.value?.years?.lastOrNull()?.months?.sumOf { month ->
            month.transactions.sumOf { transaction ->
                if (transaction.type == TypeExtract.DEPOSIT) transaction.amount else 0.0
            }
        } ?: 0.0
    }

    private fun getTotalPayment() {
        _payment.value = _extract.value?.years?.lastOrNull()?.months?.sumOf { month ->
            month.transactions.sumOf { transaction ->
                if (transaction.type == TypeExtract.PAYMENT) transaction.amount else 0.0
            }
        } ?: 0.0
    }

    fun getBalance(): Double = _deposit.value - _payment.value
}