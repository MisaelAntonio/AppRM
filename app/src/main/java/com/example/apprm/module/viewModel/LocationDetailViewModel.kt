package com.example.apprm.module.viewModel

import android.location.Address
import android.location.Geocoder
import android.util.Log
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import com.example.apprm.module.db.model.DetailedLocation
import com.example.apprm.module.db.repository.CharacterRepository
import com.google.android.gms.maps.model.LatLng

class LocationDetailViewModel(
    private val repository: CharacterRepository, // Recibe el repositorio
    private val locationUrl: String // Recibe la URL para cargar la ubicación
) : ViewModel() {

    private val _detailedLocation = MutableLiveData<DetailedLocation?>() // *** NUEVO LIVE DATA ***
    val detailedLocation: LiveData<DetailedLocation?> = _detailedLocation

    private val _coordinates = MutableLiveData<LatLng?>()
    val coordinates: LiveData<LatLng?> = _coordinates

    private val _geocodingError = MutableLiveData<String>()
    val geocodingError: LiveData<String> = _geocodingError

    private val _isLoadingLocation = MutableLiveData<Boolean>() // Para la carga de la ubicación detallada
    val isLoadingLocation: LiveData<Boolean> = _isLoadingLocation

    private val _errorMessage = MutableLiveData<String>() // Para errores de API o geocodificación
    val errorMessage: LiveData<String> = _errorMessage

    init {
        // Al inicializar, cargar los detalles de la ubicación
        fetchDetailedLocation(locationUrl)
    }

    private fun fetchDetailedLocation(url: String) {
        viewModelScope.launch {
            _isLoadingLocation.value = true
            _errorMessage.value = ""
            _detailedLocation.value = null

            try {
                val response = repository.getDetailedLocationByUrl(url)
                if (response.isSuccessful) {
                    val location = response.body()
                    _detailedLocation.value = location
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown API error"
                    _errorMessage.value = "Error al cargar ubicación: ${response.code()} - $errorBody"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error de red al cargar ubicación: ${e.message}"
                Log.e("LocationDetailViewModel", "Error fetching detailed location", e)
            } finally {
                _isLoadingLocation.value = false
            }
        }
    }

    fun geocodeLocation(geocoder: Geocoder) {
        val locationName = _detailedLocation.value?.name // Usar el nombre de la ubicación detallada
        if (locationName.isNullOrBlank() || locationName.contains("unknown", ignoreCase = true)) {
            _geocodingError.value = "Ubicación desconocida o no geocodificable."
            return
        }

        viewModelScope.launch {
            _geocodingError.value = "" // Limpiar errores anteriores
            _coordinates.value = null // Limpiar coordenadas anteriores


            withContext(Dispatchers.IO) {
                try {
                    @Suppress("DEPRECATION")
                    val addresses: List<Address>? = geocoder.getFromLocationName(locationName, 1)

                    if (!addresses.isNullOrEmpty()) {
                        val address = addresses[0]
                        _coordinates.postValue(LatLng(address.latitude, address.longitude))
                    } else {
                        _geocodingError.postValue("No se encontraron coordenadas para '$locationName'. (Puede ser una ubicación ficticia)")
                    }
                } catch (e: IOException) {
                    _geocodingError.postValue("Error de red o servicio de geocodificación: ${e.message}")
                    Log.e("LocationDetailViewModel", "Geocoding error", e)
                } catch (e: Exception) {
                    _geocodingError.postValue("Error inesperado al geocodificar: ${e.message}")
                    Log.e("LocationDetailViewModel", "Unexpected geocoding error", e)
                } finally {
                    // Ocultar progressBar en el mapa después de intentar geocodificar
                    // Esto debe hacerse en el hilo principal
                    withContext(Dispatchers.Main) {

                    }
                }
            }
        }
    }
}