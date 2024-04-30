package com.nest.nestplay.model


data class ListMovieModel(
    val list: MutableList<Movie> = mutableListOf(),
    val title: String = ""
) {
    data class Movie(
        val adult: Boolean = false,
        var backdrop_path: String = "",
        val first_air_date: String = "",
        val genre_ids: List<Int> = listOf(),
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
        val url: String = "",
        val original_title_lowcase: String? = "",
        val contentType: String? = "",
        val subtitles: List<String>? = listOf(),
        val urls_subtitle: List<String>? = listOf()
    )
}