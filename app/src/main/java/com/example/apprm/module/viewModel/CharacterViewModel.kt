package com.example.apprm.module.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apprm.module.db.repository.CharacterRepository
import com.example.apprm.module.db.model.Character

import kotlinx.coroutines.launch

class CharacterViewModel(private val repository: CharacterRepository) : ViewModel() {

    // LiveData para la lista de personajes
    private val _characters = MutableLiveData<List<Character>>()
    val characters: LiveData<List<Character>> = _characters

    // LiveData para manejar el estado de carga
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // LiveData para manejar mensajes de error
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    // Propiedades para los filtros actuales (útiles si quieres persistirlos o mostrarlos en la UI)
    private val _currentNameFilter = MutableLiveData<String?>(null)
    val currentNameFilter: LiveData<String?> = _currentNameFilter

    private val _currentStatusFilter = MutableLiveData<String?>(null)
    val currentStatusFilter: LiveData<String?> = _currentStatusFilter

    private val _currentSpeciesFilter = MutableLiveData<String?>(null)
    val currentSpeciesFilter: LiveData<String?> = _currentSpeciesFilter

    init {
        // Al inicializar, cargamos todos los personajes sin filtros
        fetchCharacters(null, null, null)
    }

    /**
     * Carga personajes de la API con filtros opcionales.
     * @param name Nombre del personaje (opcional).
     * @param status Estado del personaje (Alive, Dead, unknown) (opcional).
     * @param species Especie del personaje (opcional).
     */
    fun fetchCharacters(name: String?, status: String?, species: String?) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = "" // Limpia cualquier mensaje de error previo

            // Actualiza los LiveData de los filtros
            _currentNameFilter.value = name
            _currentStatusFilter.value = status
            _currentSpeciesFilter.value = species

            try {
                // Llama al método searchCharacters del repositorio
                val response = repository.searchCharacters(name, status, species)
                if (response.isSuccessful) {
                    response.body()?.let {
                        _characters.value = it.results
                    } ?: run {
                        _errorMessage.value = "Respuesta de la API vacía o no se encontraron resultados."
                        _characters.value = emptyList() // Opcional: limpiar la lista si no hay resultados
                    }
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Error desconocido"
                    _errorMessage.value = "Error al cargar: ${response.code()} - $errorBody"
                    _characters.value = emptyList() // Opcional: limpiar la lista en caso de error
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error de conexión: ${e.message}"
                _characters.value = emptyList() // Opcional: limpiar la lista en caso de error
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Función para limpiar todos los filtros y recargar los datos
    fun clearFiltersAndFetchAllCharacters() {
        fetchCharacters(null, null, null)
    }

    // Ejemplo de otra función para obtener un personaje por ID (si la UI lo necesitara)
    // No la usaremos en este MainActivity, pero es un ejemplo de lógica en el ViewModel
    fun fetchCharacterById(id: Int) {
        viewModelScope.launch {
            // ... lógica similar para _selectedCharacter o algo así ...
        }
    }
}