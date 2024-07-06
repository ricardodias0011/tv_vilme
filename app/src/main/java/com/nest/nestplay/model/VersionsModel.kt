package com.nest.nestplay.model
import com.google.firebase.Timestamp
data class VersionsModel(
    val active: Boolean = false,
    val overview: List<String>? = listOf(),
    val createdAt: Timestamp? = null,
    val type: String = "TV",
    val url_donwload: String = "",
    val version: Double = 1.0
    )