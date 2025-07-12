package com.example.apprm.module.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.apprm.databinding.ItemEpisodeBinding
import com.example.apprm.module.db.model.Episode


// No necesita un listener de clic en este ejemplo, pero podrías añadirlo si quisieras
class EpisodeAdapter : ListAdapter<Episode, EpisodeAdapter.EpisodeViewHolder>(EpisodeDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EpisodeViewHolder {
        val binding = ItemEpisodeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EpisodeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EpisodeViewHolder, position: Int) {
        val episode = getItem(position)
        holder.bind(episode)
    }

    class EpisodeViewHolder(private val binding: ItemEpisodeBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(episode: Episode) {
            binding.apply {
                textViewEpisodeName.text = "Episodio: ${episode.id} - ${episode.name}"
                textViewEpisodeAirDate.text = "Fecha de Emisión: ${episode.airDate}"
            }
        }
    }

    private class EpisodeDiffCallback : DiffUtil.ItemCallback<Episode>() {
        override fun areItemsTheSame(oldItem: Episode, newItem: Episode): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Episode, newItem: Episode): Boolean {
            return oldItem == newItem
        }
    }
}