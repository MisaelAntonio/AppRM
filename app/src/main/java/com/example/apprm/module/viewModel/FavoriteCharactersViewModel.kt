package com.example.apprm.module.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import com.example.apprm.module.db.FavoriteCharacter
import com.example.apprm.module.db.repository.CharacterRepository


class FavoriteCharactersViewModel(private val repository: CharacterRepository) : ViewModel() {

    val favoriteCharacters: LiveData<List<FavoriteCharacter>> = repository.getAllFavoriteCharacters().asLiveData()

    // Puedes añadir más lógica aquí si necesitas interactuar con los favoritos,
    // como eliminar uno o actualizar la lista.
}