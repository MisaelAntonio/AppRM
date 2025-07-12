package com.example.apprm.module.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.apprm.module.db.FavoriteCharacter

import kotlinx.coroutines.flow.Flow // Usaremos Flow para observar cambios en la base de datos

@Dao
interface FavoriteCharacterDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE) // Si el favorito ya existe (por su ID), lo reemplaza
    suspend fun insertFavorite(character: FavoriteCharacter)

    @Delete
    suspend fun deleteFavorite(character: FavoriteCharacter)

    // Consulta para obtener un favorito por su ID
    @Query("SELECT * FROM favorite_characters WHERE id = :characterId")
    suspend fun getFavoriteById(characterId: Int): FavoriteCharacter? // Puede ser nulo si no es favorito

    // Consulta para obtener todos los favoritos (útil para una lista de favoritos)
    @Query("SELECT * FROM favorite_characters")
    fun getAllFavorites(): Flow<List<FavoriteCharacter>> // Flow para observar cambios en tiempo real
}