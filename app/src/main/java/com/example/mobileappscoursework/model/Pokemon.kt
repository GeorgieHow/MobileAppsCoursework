package com.example.mobileappscoursework.model

import com.google.gson.annotations.SerializedName

data class Pokemon(
    val name: String,
    val weight: Int,
    val types: List<PokemonTypeEntry>,
    val sprites: PokemonSprites
)

data class PokemonTypeEntry(
    val type: PokemonType
)

data class PokemonType(
    val name: String
)

data class PokemonSprites(
    @SerializedName("front_default") val frontDefault: String
)