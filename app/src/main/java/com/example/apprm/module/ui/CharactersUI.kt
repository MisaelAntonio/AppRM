package com.example.apprm.module.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.apprm.R
import com.example.apprm.databinding.ViewCharactersBinding
import com.example.apprm.module.apiService.ClientApi
import com.example.apprm.module.db.AppDatabase
import com.example.apprm.module.db.repository.CharacterRepository
import com.example.apprm.module.ui.adapters.CharacterAdapter
import com.example.apprm.module.viewModel.CharacterViewModel
import kotlinx.coroutines.launch
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricManager
import java.util.concurrent.Executor
import java.util.concurrent.Executors // Para el Executor

class CharactersUI : AppCompatActivity() {

    private val TAG = "MainActivity"
    private lateinit var binding: ViewCharactersBinding // Para View Binding
    private lateinit var characterAdapter: CharacterAdapter // Adaptador para RecyclerView
    private lateinit var viewModel: CharacterViewModel

    // Executor para BiometricPrompt
    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo


    private var selectedStatus: String? = null
    private var selectedSpecies: String? = null

    // Opciones para los Spinners (Status y Species)
    // Estas listas son estáticas para el ejemplo. En una app real, podrían venir de la API o ser más dinámicas.
    private val statusOptions = arrayOf("Any", "Alive", "Dead", "unknown")
    private val speciesOptions = arrayOf("Any", "Human", "Alien", "Poopybutthole", "Mythological Creature", "Humanoid", "Animal", "Robot", "Cronenberg", "Disease", "unknown")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ViewCharactersBinding.inflate(layoutInflater) // Inflar el layout
        setContentView(binding.root) // Establecer la vista raíz

        setupViewModel()
        setupRecyclerView()
        setupObservers()
        setupAutoCompleteFilters()
        setupSearchAndFavorites() // Renombré para incluir el botón de favoritos

        setupBiometrics() // Nuevo método para configurar la biometría

        // Cargar los caracteres iniciales
        if (savedInstanceState == null) {
            viewModel.fetchCharacters()
        }
    }

    private fun setupViewModel() {
        val apiService = ClientApi.apiService
        val database = AppDatabase.getDatabase(applicationContext)
        val favoriteCharacterDao = database.favoriteCharacterDao()
        val repository = CharacterRepository(apiService, favoriteCharacterDao)
        val factory = CharacterFactory(repository)
        viewModel = ViewModelProvider(this, factory)[CharacterViewModel::class.java]
    }

    private fun setupRecyclerView() {
        characterAdapter = CharacterAdapter { character ->
            val intent = Intent(this, CharacterDetailUI::class.java).apply {
                putExtra("character_id", character.id)
            }
            startActivity(intent)
        }
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@CharactersUI)
            adapter = characterAdapter
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val visibleItemCount = layoutManager.childCount
                    val totalItemCount = layoutManager.itemCount
                    val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                    if (!viewModel.isLoading.value!! && dy > 0) {
                        val threshold = 5
                        if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount - threshold
                            && firstVisibleItemPosition >= 0) {
                            viewModel.loadMoreCharacters()
                        }
                    }
                }
            })
        }
    }

    private fun setupObservers() {
        viewModel.characters.observe(this) { characters ->
            characterAdapter.submitList(characters)
        }

        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) android.view.View.VISIBLE else android.view.View.GONE
        }

        viewModel.errorMessage.observe(this) { message ->
            message?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
              //  viewModel.errorMessage.value = ""
            }
        }
    }

    // *** MÉTODO ACTUALIZADO PARA INCLUIR SEARCHVIEW Y BOTÓN DE FAVORITOS ***
    private fun setupSearchAndFavorites() {
        // --- Configuración de SearchView para el filtro de nombre ---
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                performSearchWithCurrentFilters(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Para búsqueda en tiempo real (con debounce en ViewModel)
                performSearchWithCurrentFilters(newText)
                return true
            }
        })

        // --- Botón para Mis Favoritos ---
        binding.buttonMyFavorites.setOnClickListener {
            val intent = Intent(this, FavoriteCharactersUI::class.java) // Asume esta actividad
            startActivity(intent)
        }

        // --- Botón para restablecer filtros ---
        // *** CAMBIO AQUÍ: Ahora se inicia la autenticación biométrica ***
        binding.buttonMyFavorites.setOnClickListener {
            // Verificar si la biometría está disponible y si hay credenciales
            val biometricManager = BiometricManager.from(this)
            when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)) {
                BiometricManager.BIOMETRIC_SUCCESS -> {
                    // La biometría está disponible, iniciar el prompt
                    biometricPrompt.authenticate(promptInfo)
                }
                BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                    Toast.makeText(this, "El dispositivo no tiene hardware biométrico.", Toast.LENGTH_SHORT).show()
                }
                BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                    Toast.makeText(this, "El hardware biométrico no está disponible.", Toast.LENGTH_SHORT).show()
                }
                BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                    // No hay huellas dactilares o credenciales faciales enrolladas
                    Toast.makeText(this, "No hay datos biométricos configurados. Por favor, configura uno en los ajustes del dispositivo.", Toast.LENGTH_LONG).show()
                    // Opcional: Redirigir al usuario a la configuración para enrolar biometría
                    // val enrollIntent = Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
                    //     putExtra(Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED, BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
                    // }
                    // startActivity(enrollIntent)
                }
                else -> {
                    Toast.makeText(this, "Error desconocido en la biometría.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    // *******************************************************************

    private fun setupAutoCompleteFilters() {
        // Adaptador para Estado
        val statusAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, arrayOf("Todos") + statusOptions)
        binding.autoCompleteTextViewStatus.setAdapter(statusAdapter)

        binding.autoCompleteTextViewStatus.setOnItemClickListener { parent, view, position, id ->
            val selectedOption = parent.getItemAtPosition(position).toString()
            selectedStatus = if (selectedOption == "Todos") null else selectedOption
            // Al seleccionar un filtro, aplicamos la búsqueda con el nombre actual
            performSearchWithCurrentFilters(binding.searchView.query?.toString()) // Usar searchView.query
        }

        // Adaptador para Especie
        val speciesAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, arrayOf("Todos") + speciesOptions)
        binding.autoCompleteTextViewSpecies.setAdapter(speciesAdapter)

        binding.autoCompleteTextViewSpecies.setOnItemClickListener { parent, view, position, id ->
            val selectedOption = parent.getItemAtPosition(position).toString()
            selectedSpecies = if (selectedOption == "Todos") null else selectedOption
            // Al seleccionar un filtro, aplicamos la búsqueda con el nombre actual
            performSearchWithCurrentFilters(binding.searchView.query?.toString()) // Usar searchView.query
        }
    }

    // Helper para realizar la búsqueda/filtrado con los valores actuales de la UI
    private fun performSearchWithCurrentFilters(nameQuery: String?) {
        viewModel.searchCharacters(
            nameQuery = nameQuery ?: "",
            statusFilter = selectedStatus,
            speciesFilter = selectedSpecies
        )
    }

    private fun setupBiometrics() {
        executor = Executors.newSingleThreadExecutor()

        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Toast.makeText(applicationContext, "Error de autenticación: $errString", Toast.LENGTH_SHORT).show()
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    // ¡Autenticación exitosa! Ahora puedes lanzar la actividad de favoritos
                    runOnUiThread { // Asegurarse de ejecutar en el hilo principal para la UI
                        val intent = Intent(this@CharactersUI, FavoriteCharactersUI::class.java)
                        startActivity(intent)
                        Toast.makeText(applicationContext, "Autenticación exitosa!", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(applicationContext, "Autenticación fallida", Toast.LENGTH_SHORT).show()
                }
            })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Acceso a Favoritos")
            .setSubtitle("Autentícate para ver tus personajes favoritos")
            .setDescription("Coloca tu huella digital o usa la credencial de tu dispositivo.")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
            // .setNegativeButtonText("Cancelar") // ¡Elimina esta línea!
            .build()
    }
}