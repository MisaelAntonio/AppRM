package com.example.apprm.module.viewModel

import android.location.Address
import android.location.Geocoder
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

class CharacterMapViewModel(private val locationName: String) : ViewModel() {

    private val _coordinates = MutableLiveData<LatLng?>()
    val coordinates: LiveData<LatLng?> = _coordinates

    private val _geocodingError = MutableLiveData<String>()
    val geocodingError: LiveData<String> = _geocodingError

    private val _isLoadingMap = MutableLiveData<Boolean>()
    val isLoadingMap: LiveData<Boolean> = _isLoadingMap

    fun geocodeLocation(geocoder: Geocoder) {
        if (locationName.isBlank() || locationName.equals("unknown", ignoreCase = true)) {
            _geocodingError.value = "Ubicación desconocida o no geocodificable."
            _coordinates.value = null // Asegurar que no haya coordenadas si el nombre es inválido
            return
        }

        viewModelScope.launch {
            _geocodingError.value = "" // Limpiar errores anteriores
            _coordinates.value = null // Limpiar coordenadas anteriores
            _isLoadingMap.value = true // Mostrar ProgressBar

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
                    Log.e("CharacterMapViewModel", "Geocoding error", e)
                } catch (e: Exception) {
                    _geocodingError.postValue("Error inesperado al geocodificar: ${e.message}")
                    Log.e("CharacterMapViewModel", "Unexpected geocoding error", e)
                } finally {
                    _isLoadingMap.postValue(false) // Ocultar ProgressBar
                }
            }
        }
    }
}