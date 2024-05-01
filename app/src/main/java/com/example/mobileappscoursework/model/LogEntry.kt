package com.example.mobileappscoursework.model

data class LogEntry (
    val id: String,
    val title: String,
    val description: String,
    val date: String,
    val hours: Int,
    val tags: List<String>,
    val location: String,
    val imageUri: String
)