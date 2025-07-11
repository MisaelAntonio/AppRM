package com.example.apprm.module.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide // Para cargar imágenes
import com.example.apprm.databinding.ItemCharacterBinding
import com.example.apprm.module.db.model.Character

class CharacterAdapter : ListAdapter<Character, CharacterAdapter.CharacterViewHolder>(CharacterDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CharacterViewHolder {
        val binding = ItemCharacterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CharacterViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CharacterViewHolder, position: Int) {
        val character = getItem(position)
        holder.bind(character)
    }

    class CharacterViewHolder(private val binding: ItemCharacterBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(character: Character) {
            binding.apply {
                textViewName.text = character.name
                textViewSpeciesStatus.text  = "Origen: ${character.species}"
                textViewStatus.text = "Estado: ${character.status}"

                // Cargar imagen con Glide
                Glide.with(imageViewCharacter.context)
                    .load(character.image)
                    .circleCrop() // O .centerCrop(), .fitCenter()
                    .into(imageViewCharacter)
            }
        }
    }

    // Para una actualización eficiente del RecyclerView
    private class CharacterDiffCallback : DiffUtil.ItemCallback<Character>() {
        override fun areItemsTheSame(oldItem: Character, newItem: Character): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Character, newItem: Character): Boolean {
            return oldItem == newItem
        }
    }
}