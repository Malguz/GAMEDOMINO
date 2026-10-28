package com.example.gamedomino.data.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class User(
    val id: String = "",
    val email: String = "",
    val username: String = ""
)