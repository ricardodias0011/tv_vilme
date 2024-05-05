package com.nest.nestplay.model

import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.parcelize.Parcelize

@Parcelize
data class TimeModel(
    val content: String? = "",
    val content_id: Int? = 0,
    val user_id: String? = "",
    val current_time: Long? = 0,
    val createdAt: Timestamp? = Timestamp.now(),
    val updatedAt: Timestamp? = Timestamp.now(),
    val episode: Int? = 0,
    val season: Int? = 0,
): Parcelable