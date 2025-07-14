package com.example.apprm.module.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apprm.module.db.repository.CharacterRepository
import com.example.apprm.module.db.model.Character
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay

import kotlinx.coroutines.launch

class CharacterViewModel(private val repository: CharacterRepository) : ViewModel() {

    private val TAG = "CharacterViewModel"

    // LiveData para la lista de personajes
    private val _characters = MutableLiveData<List<Character>>()
    val characters: LiveData<List<Character>> = _characters

    // LiveData para manejar el estado de carga
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // LiveData para manejar mensajes de error
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    // --- Propiedades para los filtros actuales ---
    private var currentNameFilter: String? = null
    private var currentStatusFilter: String? = null
    private var currentSpeciesFilter: String? = null
    // ------------------------------------------

    // *** NUEVA VARIABLE PARA LA URL DE LA SIGUIENTE PÁGINA ***
    private var nextCharactersPageUrl: String? = null
    // *** Eliminamos 'currentPage' y 'isLastPage' como variables de estado directo,
    // *** ya que 'nextCharactersPageUrl' y 'null' indicarán el final.
    // private var currentPage = 1 // No se usa directamente para la URL de la siguiente página
    // private var isLastPage = false // Determinado por nextCharactersPageUrl == null
    // --------------------------------------------------------

    private var searchJob: Job? = null // Para manejar el debounce de la búsqueda

    init {
        _errorMessage.value = ""
        // fetchCharacters() // Comentado si la Main Activity ya lo llama en onCreate
    }

    /**
     * Obtiene los personajes de la API, aplicando los filtros actuales.
     * Esta función es llamada para la carga inicial o para aplicar nuevos filtros/búsquedas.
     *
     * @param name Filtro por nombre (opcional). Si es nulo, se usa el filtro actual.
     * @param status Filtro por estado (opcional). Si es nulo, se usa el filtro actual.
     * @param species Filtro por especie (opcional). Si es nulo, se usa el filtro actual.
     */
    fun fetchCharacters(
        name: String? = null, // Usamos null para forzar un reinicio de filtros si no se especifica
        status: String? = null,
        species: String? = null
    ) {
        if (_isLoading.value == true) return

        // Actualizar los filtros actuales del ViewModel
        currentNameFilter = name?.trim()
        currentStatusFilter = status?.trim()
        currentSpeciesFilter = species?.trim()

        _isLoading.value = true
        _errorMessage.value = ""
        // Reiniciar la URL de la siguiente página para una nueva consulta
        nextCharactersPageUrl = null

        viewModelScope.launch {
            try {
                // Para la primera carga, siempre usamos la página 1 y los filtros
                val response = repository.getCharacters(
                    page = 1, // Siempre inicia en la página 1 para una nueva búsqueda/filtro
                    name = currentNameFilter,
                    status = currentStatusFilter,
                    species = currentSpeciesFilter
                )

                if (response.isSuccessful) {
                    val characterResponse = response.body()
                    val newCharacters = characterResponse?.results ?: emptyList()

                    _characters.value = newCharacters
                    nextCharactersPageUrl = characterResponse?.info?.next // Guardar la URL de la siguiente página

                  //  Log.d(TAG, "Fetched characters: ${newCharacters.size} with filters: name=${currentNameFilter}, status=${currentStatusFilter}, species=${currentSpeciesFilter}. Next URL: ${nextCharactersPageUrl}")
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = "Error al cargar personajes: ${response.code()} - $errorBody"
                    _errorMessage.value = errorMessage
                    _characters.value = emptyList()
                   // Log.e(TAG, errorMessage)
                }
            } catch (e: Exception) {
                val errorMessage = "Excepción al cargar personajes: ${e.localizedMessage ?: "Error desconocido"}"
                _errorMessage.value = errorMessage
                _characters.value = emptyList()
             //   Log.e(TAG, errorMessage, e)
            } finally {
                _isLoading.value = false
            }
        }
    }


    /**
     * Carga más personajes (la siguiente página) utilizando la URL proporcionada por la API.
     */
    fun loadMoreCharacters() {
        // No cargar más si ya está cargando o si no hay una URL para la siguiente página
        if (_isLoading.value == true || nextCharactersPageUrl == null) {
          //  Log.d(TAG, "Not loading more: isLoading=${_isLoading.value}, nextCharactersPageUrl=${nextCharactersPageUrl}")
            return
        }

        _isLoading.value = true
        _errorMessage.value = ""

        viewModelScope.launch {
            try {
                // *** Usar la URL de la siguiente página proporcionada por la API ***
                val response = repository.getCharactersByFullUrl(nextCharactersPageUrl!!)

                if (response.isSuccessful) {
                    val characterResponse = response.body()
                    val newCharacters = characterResponse?.results ?: emptyList()

                    val currentList = _characters.value.orEmpty().toMutableList()
                    currentList.addAll(newCharacters)
                    _characters.value = currentList

                    nextCharactersPageUrl = characterResponse?.info?.next // Actualizar la URL de la siguiente página

                    //Log.d(TAG, "Loaded more characters: ${newCharacters.size}. Total: ${currentList.size}. Next URL: ${nextCharactersPageUrl}")
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = "Error al cargar más personajes: ${response.code()} - $errorBody"
                    _errorMessage.value = errorMessage
                  //  Log.e(TAG, errorMessage)
                }
            } catch (e: Exception) {
                val errorMessage = "Excepción al cargar más personajes: ${e.localizedMessage ?: "Error desconocido"}"
                _errorMessage.value = errorMessage
              //  Log.e(TAG, errorMessage, e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Realiza una búsqueda de personajes por nombre, aplicando también los filtros de estado y especie.
     * Implementa un "debounce" para evitar llamadas excesivas a la API.
     *
     * @param nameQuery La cadena de búsqueda para el nombre.
     * @param statusFilter Filtro por estado (opcional).
     * @param speciesFilter Filtro por especie (opcional).
     * // NOTA: Este método no requiere el parámetro 'page' en su firma,
     * // ya que siempre inicia una nueva búsqueda desde la página 1 internamente.
     */
    fun searchCharacters(
        nameQuery: String,
        statusFilter: String? = null,
        speciesFilter: String? = null
    ) {
        searchJob?.cancel() // Cancelar cualquier búsqueda anterior pendiente

        // Actualizar los filtros actuales del ViewModel
        currentNameFilter = nameQuery.trim()
        currentStatusFilter = statusFilter?.trim()
        currentSpeciesFilter = speciesFilter?.trim()

        // Reiniciar la URL de la siguiente página para una nueva búsqueda
        nextCharactersPageUrl = null
        _errorMessage.value = ""

        // Si todos los filtros están vacíos, cargar la lista original sin filtros
        if (currentNameFilter.isNullOrEmpty() && currentStatusFilter.isNullOrEmpty() && currentSpeciesFilter.isNullOrEmpty()) {
            fetchCharacters(null, null, null) // Llamar con null para limpiar filtros
            return
        }

        _isLoading.value = true
        searchJob = viewModelScope.launch {
            delay(300) // Debounce

            try {
                // Usamos getCharacters ya que ahora maneja todos los filtros
                val response = repository.getCharacters(
                    page = 1, // Siempre inicia en la página 1 para una nueva búsqueda/filtro
                    name = currentNameFilter,
                    status = currentStatusFilter,
                    species = currentSpeciesFilter
                )

                if (response.isSuccessful) {
                    val characterResponse = response.body()
                    val searchResults = characterResponse?.results ?: emptyList()

                    _characters.value = searchResults
                    nextCharactersPageUrl = characterResponse?.info?.next // Guardar la URL de la siguiente página

                //    Log.d(TAG, "Search results for name='${currentNameFilter}', status='${currentStatusFilter}', species='${currentSpeciesFilter}': ${searchResults.size}. Next URL: ${nextCharactersPageUrl}")
                } else {
                    if (response.code() == 404) {
                        _characters.value = emptyList()
                        _errorMessage.value = "No se encontraron personajes con los filtros aplicados."
                        nextCharactersPageUrl = null // No hay más páginas
                    //    Log.d(TAG, "No characters found for current filters.")
                    } else {
                        val errorBody = response.errorBody()?.string()
                        val errorMessage = "Error en la búsqueda/filtro: ${response.code()} - $errorBody"
                        _errorMessage.value = errorMessage
                        _characters.value = emptyList()
                       // Log.e(TAG, errorMessage)
                    }
                }
            } catch (e: Exception) {
                val errorMessage = "Excepción en la búsqueda/filtro: ${e.localizedMessage ?: "Error desconocido"}"
                _errorMessage.value = errorMessage
                _characters.value = emptyList()
             //   Log.e(TAG, errorMessage, e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Restablece todos los filtros y carga la primera página de personajes sin filtros.
     */
    fun resetFilters() {
        fetchCharacters(null, null, null)
    }
}