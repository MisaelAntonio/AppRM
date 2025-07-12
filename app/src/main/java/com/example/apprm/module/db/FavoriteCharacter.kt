package com.example.apprm.module.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.apprm.module.db.model.Location
import kotlinx.serialization.Serializable // Aunque Room no lo usa directamente, es bueno mantenerlo si compartes modelos

@Entity(tableName = "favorite_characters") // Define el nombre de la tabla en la DB
@Serializable // Mantenemos la anotación por si en el futuro se serializa este objeto de Room a JSON
data class FavoriteCharacter(
    @PrimaryKey val id: Int, // El ID de la API es perfecto como clave primaria
    val name: String,
    val imageUrl: String, // Solo guardamos los datos mínimos para mostrar en una lista de favoritos
    val status: String,
    val species: String,
    val gender: String,
    val originName: String,
    val originUrl: String,
    val locationName: String,
    val locationUrl: String,
    val created: String
    // Puedes añadir más campos si necesitas mostrarlos en una lista de favoritos
    // O podrías guardar el Character completo como JSON en una columna si prefieres.
)