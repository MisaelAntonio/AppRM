package com.example.apprm.module.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.apprm.module.db.repository.CharacterRepository
import com.example.apprm.module.viewModel.CharacterDetailViewModel


class CharacterDetailViewModelFactory(private val repository: CharacterRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CharacterDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CharacterDetailViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}