package com.teahouse.marvelmovies

import android.databinding.DataBindingUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.teahouse.marvelmovies.databinding.ItemCharacterBinding

class CharactersAdapter(var characterResponse: List<Model.Character>) : RecyclerView.Adapter<CharactersAdapter.ItemCharacterViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemCharacterViewHolder {
        val itemCharacterBinding = DataBindingUtil.inflate<ItemCharacterBinding>(LayoutInflater.from(parent.context), R.layout.item_character, parent, false)

        return ItemCharacterViewHolder(itemCharacterBinding)

    }

    override fun onBindViewHolder(holder: ItemCharacterViewHolder, position: Int) {
        holder.bindItemCharacter(characterResponse[position])
    }

    override fun getItemCount() = characterResponse.size

    class ItemCharacterViewHolder(val binding: ItemCharacterBinding) : RecyclerView.ViewHolder(binding.cardView) {
        fun bindItemCharacter(character: Model.Character) {
            var viewmodel = ViewModel.CharacterViewModel(itemView.context, character)
            binding.cardView.setOnClickListener({ viewmodel.openDetailActivity() })
            binding.viewmodel = viewmodel

        }

    }

}

