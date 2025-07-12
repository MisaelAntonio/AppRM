package com.example.apprm.module.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.apprm.module.db.repository.CharacterRepository
import com.example.apprm.module.viewModel.LocationDetailViewModel

class LocationDetailViewModelFactory(
    private val repository: CharacterRepository, // Recibe el repositorio
    private val locationUrl: String // Recibe la URL
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LocationDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LocationDetailViewModel(repository, locationUrl) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}