package com.example.apprm.module.ui

import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.apprm.databinding.ActivityCharacterMapBinding
import com.example.apprm.module.viewModel.CharacterMapViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class CharacterMapUI : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityCharacterMapBinding
    private lateinit var viewModel: CharacterMapViewModel
    private var googleMap: GoogleMap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCharacterMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializar el mapa
        val mapFragment = supportFragmentManager.findFragmentById(binding.mapFragment.id) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Observar LiveData del ViewModel
        observeViewModel()

        // Configurar la barra de acción
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Mapa: Mi ubicacion actual" // Título con el nombre de la ubicación
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap?.uiSettings?.isZoomControlsEnabled = true

        // Intentar geocodificar la ubicación
        val geocoder = Geocoder(this)
        viewModel.geocodeLocation(geocoder)
    }

    private fun observeViewModel() {
        viewModel.coordinates.observe(this) { latLng ->
            binding.progressBarMap.visibility = View.GONE // Ocultar progreso al tener coordenadas
            latLng?.let {
                googleMap?.apply {
                    clear() // Limpia marcadores anteriores
                    val marker = addMarker(MarkerOptions().position(it).title("ubicacion actual"))
                    marker?.showInfoWindow() // Muestra la ventana de información del marcador
                    moveCamera(CameraUpdateFactory.newLatLngZoom(it, 10f)) // Mueve la cámara y hace zoom
                }
            } ?: run {
                // Si las coordenadas son nulas, mostrar un mensaje o un mapa por defecto
                Toast.makeText(this, viewModel.geocodingError.value ?: "No se pudieron obtener coordenadas para mi ubicacaion actual'.", Toast.LENGTH_LONG).show()
                // Opcional: Centrar el mapa en una ubicación por defecto o en el mundo si no se encuentra la ubicación
                googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(0.0, 0.0), 1f)) // Mapa mundial
            }
        }

        viewModel.geocodingError.observe(this) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
            }
        }

        viewModel.isLoadingMap.observe(this) { isLoading ->
            binding.progressBarMap.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }
}