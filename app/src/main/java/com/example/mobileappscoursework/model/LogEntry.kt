package com.example.mobileappscoursework.model

data class LogEntry (
    val title: String,
    val description: String,
    val date: String,
    val hours: Int,
    val tags: List<String>
)