package com.nest.nestplay.model

class Genre(val id: Int, val name: String)
class Genres {
    companion object {
        val genres = listOf(
            Genre(28, "Ação"),
            Genre(12, "Aventura"),
            Genre(16, "Animação"),
            Genre(35, "Comédia"),
            Genre(80, "Crime"),
            Genre(99, "Documentário"),
            Genre(18, "Drama"),
            Genre(10751, "Família"),
            Genre(14, "Fantasia"),
            Genre(36, "História"),
            Genre(27, "Terror"),
            Genre(10402, "Música"),
            Genre(9648, "Mistério"),
            Genre(10749, "Romance"),
            Genre(878, "Ficção científica"),
            Genre(10770, "Cinema TV"),
            Genre(53, "Thriller"),
            Genre(10752, "Guerra"),
            Genre(37, "Faroeste")
        )
    }
}

