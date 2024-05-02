package com.example.mobileappscoursework.api

import com.example.mobileappscoursework.model.Pokemon
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface PokeApiService {
    @GET("pokemon/{id}")
    fun getPokemonById(@Path("id") id: Int): Call<Pokemon>
    @GET("pokemon")
    fun getPokemonList(): Call<PokemonListResponse>
}

data class PokemonListResponse(
    val count: Int
)