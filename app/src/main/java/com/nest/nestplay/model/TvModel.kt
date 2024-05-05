package com.nest.nestplay.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


data class ListTvModel(
    val list: MutableList<ListTvModel.TvModel> = mutableListOf(),
    val title: String = ""
) {
    @Parcelize
    data class TvModel(
        val tvgId: String,
        val tvgName: String,
        val tvgLogo: String,
        val groupTitle: String,
        val channelName: String,
        val channelUrl: String
    ): Parcelable
}