package com.nest.nestplay.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ListEpisodesModel(
    val url: String = "",
    val id_content: Int = 0,
    val ep_number: Int = 0,
    val season: Int = 0
): Parcelable