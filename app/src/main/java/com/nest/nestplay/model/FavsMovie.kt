package com.nest.nestplay.model

import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.parcelize.Parcelize
@Parcelize
data class FavsMovie(
    val content_id: Int = 0,
    val user_id: String = "",
    val createdAt: Timestamp = Timestamp.now()
): Parcelable