package com.example.apprm.module.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.apprm.databinding.ViewFavoriteCharactersBinding
import com.example.apprm.module.apiService.ClientApi
import com.example.apprm.module.db.AppDatabase
import com.example.apprm.module.db.repository.CharacterRepository
import com.example.apprm.module.ui.adapters.FavoriteCharacterAdapter
import com.example.apprm.module.viewModel.FavoriteCharactersViewModel


class FavoriteCharactersUI : AppCompatActivity() {

    private lateinit var binding: ViewFavoriteCharactersBinding
    private lateinit var viewModel: FavoriteCharactersViewModel
    private lateinit var favoriteCharacterAdapter: FavoriteCharacterAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ViewFavoriteCharactersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configurar el repositorio y ViewModel
        val apiService = ClientApi.apiService // Aunque no se usa para favoritos, el repositorio lo requiere
        val favoriteCharacterDao = AppDatabase.getDatabase(applicationContext).favoriteCharacterDao()
        val repository = CharacterRepository(apiService, favoriteCharacterDao)

        val factory = FavoriteCharactersFactory(repository)
        viewModel = ViewModelProvider(this, factory).get(FavoriteCharactersViewModel::class.java)

        setupRecyclerView()
        observeViewModel()

        // Configurar la barra de acción
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Mis Favoritos"
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    private fun setupRecyclerView() {
        favoriteCharacterAdapter = FavoriteCharacterAdapter { character ->
            // Al hacer clic en un personaje favorito, abre su detalle
            val intent = Intent(this, CharacterDetailUI::class.java).apply {
                putExtra("CHARACTER_ID", character.id)
            }
            startActivity(intent)
        }
        binding.recyclerViewFavorites.apply {
            layoutManager = LinearLayoutManager(this@FavoriteCharactersUI)
            adapter = favoriteCharacterAdapter
        }
    }

    private fun observeViewModel() {
        viewModel.favoriteCharacters.observe(this) { favorites ->
            favoriteCharacterAdapter.submitList(favorites)
            // Mostrar mensaje si la lista está vacía
            binding.textViewNoFavorites.visibility = if (favorites.isNullOrEmpty()) View.VISIBLE else View.GONE
            binding.recyclerViewFavorites.visibility = if (favorites.isNullOrEmpty()) View.GONE else View.VISIBLE
        }
    }
}