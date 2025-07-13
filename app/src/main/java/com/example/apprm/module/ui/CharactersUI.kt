package com.example.apprm.module.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.apprm.R
import com.example.apprm.databinding.ViewCharactersBinding
import com.example.apprm.module.apiService.ClientApi
import com.example.apprm.module.db.AppDatabase
import com.example.apprm.module.db.repository.CharacterRepository
import com.example.apprm.module.ui.adapters.CharacterAdapter
import com.example.apprm.module.viewModel.CharacterViewModel
import kotlinx.coroutines.launch

class CharactersUI : AppCompatActivity() {

    private val TAG = "MainActivity"
    private lateinit var binding: ViewCharactersBinding // Para View Binding
    private lateinit var characterAdapter: CharacterAdapter // Adaptador para RecyclerView
    private lateinit var viewModel: CharacterViewModel

    // Opciones para los Spinners (Status y Species)
    // Estas listas son estáticas para el ejemplo. En una app real, podrían venir de la API o ser más dinámicas.
    private val statusOptions = arrayOf("Any", "Alive", "Dead", "unknown")
    private val speciesOptions = arrayOf("Any", "Human", "Alien", "Poopybutthole", "Mythological Creature", "Humanoid", "Animal", "Robot", "Cronenberg", "Disease", "unknown")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ViewCharactersBinding.inflate(layoutInflater) // Inflar el layout
        setContentView(binding.root) // Establecer la vista raíz

        setupRecyclerView()
        setupSpinners() // Configurar los Spinners
        setupListeners() // Configurar los listeners para botones

        // --- INICIALIZACIÓN DEL REPOSITORIO CON DAO ---

        // 1. Obtener la instancia del ApiService (ya existente)
        val apiService = ClientApi.apiService

        // 2. Obtener la instancia del FavoriteCharacterDao de la base de datos Room
        // Se usa applicationContext para evitar posibles memory leaks si usáramos 'this'
        val favoriteCharacterDao = AppDatabase.getDatabase(applicationContext).favoriteCharacterDao()

        // 3. Instanciar CharacterRepository pasando apiService y favoriteCharacterDao
        val repository = CharacterRepository(apiService, favoriteCharacterDao)

        // 4. Instanciar la Factory del ViewModel con el nuevo repositorio
        val factory = CharacterFactory(repository)
        viewModel = ViewModelProvider(this, factory).get(CharacterViewModel::class.java)
        // --- FIN INICIALIZACIÓN DEL REPOSITORIO ---

        observeViewModel() // Empieza a observar los LiveData del ViewModel

        // *** NUEVO OnClickListener para el botón "Mis Favoritos" ***
        binding.buttonMyFavorites.setOnClickListener {
            val intent = Intent(this, FavoriteCharactersUI::class.java)
            startActivity(intent)
        }
        // *** FIN NUEVO OnClickListener ***
    }

    private fun setupRecyclerView() {
        // Pasa una lambda al adaptador para manejar el clic en el elemento
        characterAdapter = CharacterAdapter { character ->
            // Cuando se hace clic en un personaje, iniciamos CharacterDetailActivity
            val intent = Intent(this, CharacterDetailUI::class.java).apply {
                // Pasamos el ID del personaje a la nueva actividad
                putExtra("CHARACTER_ID", character.id)
            }
            startActivity(intent)
        }
        binding.recyclerViewCharacters.apply {
            layoutManager = LinearLayoutManager(this@CharactersUI)
            adapter = characterAdapter
        }
    }

    private fun setupSpinners() {
        // Adaptador para el Spinner de Estado
        val statusAdapter = ArrayAdapter(this, R.layout.dropdown_menu_item, statusOptions)
        binding.autoCompleteTextViewStatus.setAdapter(statusAdapter)
        binding.autoCompleteTextViewStatus.setText(statusOptions[0], false) // "Any" por defecto

        // Adaptador para el Spinner de Especie
        val speciesAdapter = ArrayAdapter(this, R.layout.dropdown_menu_item, speciesOptions)
        binding.autoCompleteTextViewSpecies.setAdapter(speciesAdapter)
        binding.autoCompleteTextViewSpecies.setText(speciesOptions[0], false) // "Any" por defecto
    }

    private fun setupListeners() {
        // *** AQUÍ ES DONDE APLICAMOS EL EVENTO ONCLICK AL BOTÓN "Aplicar Filtros" ***
        binding.buttonApplyFilters.setOnClickListener {
            applyFilters() // Llama a nuestra función para aplicar los filtros
        }

        // Evento OnClick para el botón "Limpiar Filtros"
        binding.buttonClearFilters.setOnClickListener {
            clearFilters() // Llama a nuestra función para limpiar los filtros
        }

        // Opcional: Para una barra de búsqueda más dinámica, podrías añadir un TextWatcher
        // a editTextName y llamar a applyFilters() después de un pequeño retraso (debounce)
        // para evitar llamadas excesivas a la API mientras el usuario escribe.
        // Por ahora, solo se activa con el botón "Aplicar Filtros".
    }

    private fun applyFilters() {
        // 1. Recopilar los valores de los campos de entrada de la UI
        val nameFilter = binding.editTextName.text.toString().trim().takeIf { it.isNotEmpty() }
        val statusFilter = binding.autoCompleteTextViewStatus.text.toString().takeIf { it != "Any" }
        val speciesFilter = binding.autoCompleteTextViewSpecies.text.toString().takeIf { it != "Any" }

        Log.d(TAG, "Aplicando filtros: Nombre='$nameFilter', Estado='$statusFilter', Especie='$speciesFilter'")

        // 2. Llamar al ViewModel para que inicie la carga de datos con los filtros
        viewModel.fetchCharacters(nameFilter, statusFilter, speciesFilter)

        // Opcional: Cerrar el teclado después de aplicar filtros
        // val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        // imm.hideSoftInputFromWindow(binding.root.windowToken, 0)
    }

    private fun clearFilters() {
        // 1. Restablecer los campos de entrada en la UI
        binding.editTextName.setText("")
        binding.autoCompleteTextViewStatus.setText(statusOptions[0], false) // "Any"
        binding.autoCompleteTextViewSpecies.setText(speciesOptions[0], false) // "Any"

        // 2. Llamar al ViewModel para limpiar los filtros y cargar todos los personajes
        viewModel.clearFiltersAndFetchAllCharacters()

        Toast.makeText(this, "Filtros limpiados.", Toast.LENGTH_SHORT).show()
        Log.d(TAG, "Filtros limpiados y recargando todos los personajes.")
    }

    private fun observeViewModel() {
        // Observar la lista de personajes del ViewModel y actualizar el adaptador del RecyclerView
        viewModel.characters.observe(this) { characters ->
            characterAdapter.submitList(characters)
            Log.d(TAG, "UI actualizada con ${characters.size} personajes.")
            if (characters.isEmpty() && viewModel.isLoading.value == false) {
                // Mostrar un mensaje si no hay resultados y la carga ha terminado
                Toast.makeText(this, "No se encontraron personajes con esos filtros.", Toast.LENGTH_LONG).show()
            }
        }

        // Observar el estado de carga y mostrar/ocultar el ProgressBar y el RecyclerView
        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.recyclerViewCharacters.visibility = if (isLoading) View.GONE else View.VISIBLE
        }

        // Observar mensajes de error y mostrarlos como un Toast
        viewModel.errorMessage.observe(this) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                Log.e(TAG, "Error en la UI: $it")
            }
        }
    }
}