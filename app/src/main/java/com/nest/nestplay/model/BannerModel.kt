package com.nest.nestplay.model

import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.parcelize.Parcelize

@Parcelize
data class BannerModel(
    val active: Boolean? = true,
    val expireDate: Timestamp? = Timestamp.now(),
    val role: String? = "tv",
    val url: String? = "0"
): Parcelable