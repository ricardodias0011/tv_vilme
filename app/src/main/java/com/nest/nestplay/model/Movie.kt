package com.nest.nestplay.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MovieModel(
    val adult: Boolean = false,
    var backdrop_path: String = "",
    val first_air_date: String = "",
    val genre_ids: List<Int> = listOf(2024),
    val id: Int = 0,
    val name: String = "",
    val origin_country: List<String> = listOf(),
    val original_language: String = "",
    val original_name: String = "",
    val original_title: String = "",
    val overview: String = "",
    val popularity: Double = 0.0,
    var poster_path: String = "",
    val release_date: String = "",
    val title: String = "",
    val vote_average: Double = 0.0,
    val vote_count: Int = 0,
    var url: String = "",
    val original_title_lowcase: String? = "",
    var contentType: String? = "",
    var current_ep: Int? = 0,
    var season: Int? = 0,
    var total_seasons: Int? = 0,
    var subtitles: List<String>? = listOf(),
    var urls_subtitle: List<String>? = listOf(),
    var beginningStart: Boolean? = false,
    var idEpsode: String? = "",
    var listEpsodes:  MutableList<ListEpisodesModel>? = null,
    var currentSeason: Int? = null,
    var isTvLink: Boolean? = false
    ): Parcelable