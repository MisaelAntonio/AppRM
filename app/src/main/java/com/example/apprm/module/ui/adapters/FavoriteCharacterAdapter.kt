package com.example.apprm.module.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.apprm.databinding.ItemFavoriteCharacterBinding
import com.example.apprm.module.db.FavoriteCharacter

class FavoriteCharacterAdapter(private val onClick: (FavoriteCharacter) -> Unit) :
    ListAdapter<FavoriteCharacter, FavoriteCharacterAdapter.FavoriteCharacterViewHolder>(FavoriteCharacterDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteCharacterViewHolder {
        val binding = ItemFavoriteCharacterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FavoriteCharacterViewHolder(binding, onClick)
    }

    override fun onBindViewHolder(holder: FavoriteCharacterViewHolder, position: Int) {
        val character = getItem(position)
        holder.bind(character)
    }

    class FavoriteCharacterViewHolder(
        private val binding: ItemFavoriteCharacterBinding,
        private val onClick: (FavoriteCharacter) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(character: FavoriteCharacter) {
            binding.textViewFavoriteName.text = character.name
            binding.textViewFavoriteStatusSpecies.text = "${character.status} - ${character.species}"
            binding.textViewFavoriteLocation.text = "Ubicación: ${character.locationName}"

            Glide.with(binding.imageViewFavoriteCharacter.context)
                .load(character.imageUrl)
                .circleCrop() // Opcional: hacer la imagen circular
                .into(binding.imageViewFavoriteCharacter)

            itemView.setOnClickListener { onClick(character) }
        }
    }

    private class FavoriteCharacterDiffCallback : DiffUtil.ItemCallback<FavoriteCharacter>() {
        override fun areItemsTheSame(oldItem: FavoriteCharacter, newItem: FavoriteCharacter): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: FavoriteCharacter, newItem: FavoriteCharacter): Boolean {
            return oldItem == newItem
        }
    }
}