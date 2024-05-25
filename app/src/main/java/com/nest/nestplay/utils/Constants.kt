package com.nest.nestplay.utils
import java.io.FileInputStream
import java.util.Properties
object Constants {

    const val MENU_SEARCH = "search"
    const val MENU_HOME = "home"
    const val MENU_TV = "tv"
    const val MENU_SERIES = "series"
    const val MENU_GENRES = "genres"
    const val MENU_FAVS = "favs"
    const val MENU_MOVIE = "movie"
    const val MENU_SETTINGS = "settings"
    const val MENI_ANIME = "animes"
    var KEY_D = ""

}

fun getApiKey(): String {
    try {
        val properties = Properties()
        val file = FileInputStream("api.properties")
        properties.load(file)
        println(properties.getProperty("DC_KEY"))
        return properties.getProperty("DC_KEY")
    }catch (e: Exception){
        println(e)
        return  ""
    }

}