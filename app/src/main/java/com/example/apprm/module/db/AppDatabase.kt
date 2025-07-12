package com.example.apprm.module.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.apprm.module.db.dao.FavoriteCharacterDao

@Database(entities = [FavoriteCharacter::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun favoriteCharacterDao(): FavoriteCharacterDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "rick_and_morty_db" // Nombre del archivo de la base de datos
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}