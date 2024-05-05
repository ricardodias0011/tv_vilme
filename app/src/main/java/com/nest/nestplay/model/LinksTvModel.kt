package com.nest.nestplay.model

import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.parcelize.Parcelize
@Parcelize
data class LinksTvModel(
    val isActive: Boolean = false,
    val link: String = "",
    val createdAt: Timestamp = Timestamp.now()
): Parcelable