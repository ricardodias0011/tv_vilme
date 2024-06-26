package com.nest.nestplay.model

import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.parcelize.Parcelize

@Parcelize
data class ListEpisodesModel(
    val url: String = "",
    val id_content: Int = 0,
    val ep_number: Int = 0,
    val season: Int = 0,
    val subtitles: List<String>? = listOf(),
    val urls_subtitle: List<String>? = listOf(),
    val last_seen: Timestamp? = null,
    var idEpsode: String? = "",
    var updatedAt: Timestamp? = null
): Parcelable