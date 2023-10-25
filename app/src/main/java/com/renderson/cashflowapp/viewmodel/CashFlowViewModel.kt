package com.renderson.cashflowapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CashFlowViewModel @Inject internal constructor(): ViewModel() {

    private val _showBottomNavItem = MutableLiveData(true)
    val showBottomNavItem: LiveData<Boolean> = _showBottomNavItem

    fun setShowBottomNavItem(value: Boolean) {
        _showBottomNavItem.value = value
    }
}