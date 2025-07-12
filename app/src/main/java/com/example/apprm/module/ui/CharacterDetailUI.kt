package com.example.apprm.module.ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.apprm.databinding.ViewCharacterDetailBinding
import com.example.apprm.module.apiService.ClientApi
import com.example.apprm.module.db.AppDatabase
import com.example.apprm.module.db.repository.CharacterRepository
import com.example.apprm.module.ui.adapters.EpisodeAdapter
import com.example.apprm.module.viewModel.CharacterDetailViewModel


class CharacterDetailUI : AppCompatActivity() {

    private val TAG = "CharacterDetailActivity"
    private lateinit var binding: ViewCharacterDetailBinding
    private lateinit var viewModel: CharacterDetailViewModel
    private lateinit var episodeAdapter: EpisodeAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ViewCharacterDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Obtener el ID del personaje del Intent
        val characterId = intent.getIntExtra("CHARACTER_ID", -1)
        if (characterId == -1) {
            Toast.makeText(this, "Error: ID de personaje no encontrado.", Toast.LENGTH_SHORT).show()
            finish() // Cierra la actividad si no hay ID válido
            return
        }

        // 2. Configurar el ViewModel
        // --- Configuración del Repositorio con Room DAO ---
        val apiService = ClientApi.apiService
        val favoriteCharacterDao = AppDatabase.getDatabase(applicationContext).favoriteCharacterDao()
        val repository = CharacterRepository(apiService, favoriteCharacterDao) // Pasar el DAO aquí
        // --- Fin Configuración del Repositorio ---

        val factory = CharacterDetailViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory).get(CharacterDetailViewModel::class.java)

        // Configurar el RecyclerView de episodios
        setupEpisodeRecyclerView()
        observeViewModel()

        // 3. Observar los LiveData del ViewModel
        observeViewModel()

        // 4. Solicitar los detalles del personaje al ViewModel
        viewModel.fetchCharacterDetails(characterId)

        // Configurar el listener para el Checkbox
        binding.checkboxFavorite.setOnClickListener {
            viewModel.toggleFavorite() // Llama al método del ViewModel al hacer clic
        }

        // OnClickListener al textViewDetailLocation
        binding.textViewDetailLocation.setOnClickListener {
            // Obtener la ubicación del personaje cargado
            val location = viewModel.character.value?.location
            location?.let {
                val intent = Intent(this, LocationDetailUI::class.java).apply {
                    // Pasar el objeto Location completo (debe ser Serializable)
                    // La clase Location (data/Location.kt) debe estar anotada con @Serializable
                    putExtra("LOCATION_DATA", it)
                }
                startActivity(intent)
            } ?: run {
                Toast.makeText(this, "Ubicación no disponible.", Toast.LENGTH_SHORT).show()
            }
        }

        binding.buttonViewOnMap.setOnClickListener {
            val location = viewModel.character.value?.location
            location?.let {
                if (it.name.isNotBlank() && it.name.lowercase() != "unknown") {
                    val intent = Intent(this, CharacterMapUI::class.java).apply { // *** CAMBIADO A CharacterMapActivity ***
                        putExtra("LOCATION_NAME", it.name) // Pasamos el nombre
                        putExtra("LOCATION_URL", it.url)   // Pasamos la URL
                    }
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "Ubicación desconocida para mostrar en el mapa.", Toast.LENGTH_SHORT).show()
                }
            } ?: run {
                Toast.makeText(this, "Ubicación no disponible.", Toast.LENGTH_SHORT).show()
            }
        }

        // Opcional: Configurar la barra de acción para mostrar el botón de atrás y el título
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Detalles del Personaje"
    }

    // Para manejar el clic en el botón de atrás de la barra de acción
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed() // Vuelve a la actividad anterior
        return true
    }

    // método para configurar el RecyclerView de episodios
    private fun setupEpisodeRecyclerView() {
        episodeAdapter = EpisodeAdapter()
        binding.recyclerViewEpisodes.apply {
            layoutManager = LinearLayoutManager(this@CharacterDetailUI)
            adapter = episodeAdapter
        }
    }

    private fun observeViewModel() {
        viewModel.character.observe(this) { character ->
            character?.let {
                // Aquí actualizas tu UI con TODOS los datos del objeto 'it' (que es el Character)
                binding.textViewDetailName.text = it.name
                binding.textViewDetailStatus.text = "Estado: ${it.status}"
                binding.textViewDetailSpecies.text = "Especie: ${it.species}"
                binding.textViewDetailType.text = "Tipo: ${it.type.ifEmpty { "N/A" }}"
                binding.textViewDetailGender.text = "Género: ${it.gender}"
                binding.textViewDetailOrigin.text = "Origen: ${it.origin.name}"
                binding.textViewDetailLocation.text = "Última Ubicación: ${it.location.name}"
                binding.textViewDetailEpisodeCount.text = "Aparece en: ${it.episode.size} episodios"


                Glide.with(this)
                    .load(it.image)
                    .into(binding.imageViewDetailCharacter)

                supportActionBar?.title = it.name
            }
        }
        // observación de isLoading y errorMessage
        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBarDetail.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.scrollView.visibility = if (isLoading) View.GONE else View.VISIBLE
        }
        viewModel.errorMessage.observe(this) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
            }
        }

        // Observar el estado de carga de los episodios
        viewModel.isLoadingEpisodes.observe(this) { isLoadingEpisodes ->
            binding.progressBarEpisodes.visibility = if (isLoadingEpisodes) View.VISIBLE else View.GONE
            binding.recyclerViewEpisodes.visibility = if (isLoadingEpisodes) View.GONE else View.VISIBLE
        }

        // *** Observar la lista de episodios y actualizar el adaptador ***
        viewModel.episodes.observe(this) { episodes ->
            episodeAdapter.submitList(episodes)
            Log.d(TAG, "Episodios cargados: ${episodes.size}")
        }


        // *** Observar el estado de favorito y actualizar el Checkbox ***
        viewModel.isFavorite.observe(this) { isFavorite ->
            // Asegura que el listener no se active cuando el estado se establece programáticamente
            binding.checkboxFavorite.setOnCheckedChangeListener(null)
            binding.checkboxFavorite.isChecked = isFavorite
            binding.checkboxFavorite.setOnCheckedChangeListener { _, _ -> // Restablece el listener
                viewModel.toggleFavorite()
            }
        }
    }
}