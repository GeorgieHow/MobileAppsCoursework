package com.example.mobileappscoursework

data class LeaderboardEntry(
    val rank: Int,
    val playerName: String,
    val score: Int,
    val isCurrentChampion: Boolean = false
)
