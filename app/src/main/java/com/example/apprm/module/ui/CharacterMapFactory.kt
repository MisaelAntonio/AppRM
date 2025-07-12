package com.example.apprm.module.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.apprm.module.viewModel.CharacterMapViewModel

class CharacterMapViewModelFactory(private val locationName: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CharacterMapViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CharacterMapViewModel(locationName) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}