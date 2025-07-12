package com.example.apprm.module.ui

import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.apprm.databinding.ViewLocationDetailBinding
import com.example.apprm.module.apiService.ClientApi
import com.example.apprm.module.db.AppDatabase
import com.example.apprm.module.db.model.Location
import com.example.apprm.module.db.repository.CharacterRepository
import com.example.apprm.module.viewModel.LocationDetailViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class LocationDetailUI : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ViewLocationDetailBinding
    private lateinit var viewModel: LocationDetailViewModel
    private var googleMap: GoogleMap? = null
    // private lateinit var residentAdapter: ResidentAdapter // Ya no es necesario

    private var locationUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ViewLocationDetailBinding.inflate(layoutInflater) // Initialize it
        setContentView(binding.root)

        // Obtener la URL de la ubicación del Intent
        // La Location simple (name, url) se sigue pasando desde CharacterDetailActivity
        val basicLocation = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra("LOCATION_DATA", Location::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getSerializableExtra("LOCATION_DATA") as? Location
        }

        locationUrl = basicLocation?.url

        if (locationUrl.isNullOrEmpty()) {
            Toast.makeText(this, "Error: URL de ubicación no encontrada.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val apiService = ClientApi.apiService
        val favoriteCharacterDao = AppDatabase.getDatabase(applicationContext).favoriteCharacterDao()
        val repository = CharacterRepository(apiService, favoriteCharacterDao)

        val factory = LocationDetailViewModelFactory(repository, locationUrl!!)
        viewModel = ViewModelProvider(this, factory).get(LocationDetailViewModel::class.java)

        // setupResidentRecyclerView() // Eliminar esta llamada
        observeViewModel()

        val mapFragment = supportFragmentManager.findFragmentById(binding.mapFragment.id) as SupportMapFragment
        mapFragment.getMapAsync(this)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = basicLocation?.name ?: "Detalles de Ubicación"
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }


    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap?.uiSettings?.isZoomControlsEnabled = true
    }

    private fun observeViewModel() {
        viewModel.detailedLocation.observe(this) { detailedLocation ->
            detailedLocation?.let {
                binding.textViewLocationName.text = it.name
                binding.textViewLocationType.text = "Tipo: ${it.type}"
                binding.textViewLocationDimension.text = "Dimensión: ${it.dimension}"
                binding.textViewLocationCreated.text = "Creado: ${it.created.substringBefore("T")}"
                binding.textViewLocationUrl.text = it.url
                supportActionBar?.title = it.name

                // Ya no se actualiza la lista de residentes
                // residentAdapter.submitList(it.residents)

                val geocoder = Geocoder(this)
                viewModel.geocodeLocation(geocoder)
            } ?: run {
                Toast.makeText(this, "No se pudieron cargar los detalles de la ubicación.", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.isLoadingLocation.observe(this) { isLoading ->
            binding.progressBarOverallLocation.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.scrollViewLocationDetail.visibility = if (isLoading) View.GONE else View.VISIBLE
        }

        viewModel.coordinates.observe(this) { latLng ->
            binding.progressBarLocationMap.visibility = View.GONE
            latLng?.let {
                googleMap?.apply {
                    clear()
                    val marker = addMarker(MarkerOptions().position(it).title(viewModel.detailedLocation.value?.name))
                    marker?.showInfoWindow()
                    moveCamera(CameraUpdateFactory.newLatLngZoom(it, 10f))
                }
            } ?: run {
                googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(0.0, 0.0), 1f))
            }
        }

        viewModel.errorMessage.observe(this) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
            }
        }
    }
}