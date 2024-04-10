package com.nest.nestplay

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.fragment.app.FragmentActivity
import androidx.leanback.widget.BrowseFrameLayout
import com.bumptech.glide.Glide
import com.google.firebase.Firebase
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import com.nest.nestplay.adpters.MoviesListAdpter
import com.nest.nestplay.databinding.ActivityHomeBinding
import com.nest.nestplay.model.Genres.Companion.genres
import com.nest.nestplay.model.ListMovieModel
import com.nest.nestplay.utils.Common
import com.nest.nestplay.utils.Constants

class HomeActivity: FragmentActivity(), View.OnKeyListener,  MoviesListAdpter.OnItemFocusChangeListener {
    private lateinit var binding: ActivityHomeBinding
    val MoviesListFragment = ListFragment()
    var selectedMenu = Constants.MENU_HOME
    lateinit var navBar: BrowseFrameLayout

    lateinit var btnSearch: TextView
    lateinit var btnHome: TextView
    lateinit var btnMovie: TextView
    lateinit var btnSeries: TextView

    var allMoviesList = mutableListOf<ListMovieModel>()

    var URLPATHIMAGE = "https://image.tmdb.org/t/p/w500"
    var SIDE_MENU = false

    lateinit var lastSelectedMenu: View
    lateinit var lastSelectedCategory: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        addFragment(MoviesListFragment, R.id.mainMoviesHome)

        navBar = findViewById(R.id.blfNavBar)

        MoviesListFragment.setOnItemClickListener {movie ->
            val intent = Intent(this, DetailMovieActivity::class.java)
            intent.putExtra("id", movie.id)
            startActivity(intent)
        }

        MoviesListFragment.setOnContentSelectedListener { movie ->
            if(movie != null){
                updateMainMovie(movie)
            }
        }

        MoviesListFragment.setOnItemClickListener {movie ->
            val intent = Intent(this, DetailMovieActivity::class.java)
            intent.putExtra("id", movie.id)
            startActivity(intent)
        }
        GetPopularityMovies("Home")
        val onFocusChangeListener = View.OnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                openMenu()
                SIDE_MENU = true
            }
        }
        binding.btnSearch.setOnClickListener {
            val intent = Intent(this, SearchMovie::class.java)
            startActivity(intent)
        }
        btnHome = binding.btnHome
        btnMovie = binding.btnMovies
        btnSearch = binding.btnSearch
        btnSeries =  binding.btnSeries

        btnHome.setOnKeyListener(this)
        btnHome.onFocusChangeListener = onFocusChangeListener

        btnSearch.setOnKeyListener(this)
        btnSearch.onFocusChangeListener = onFocusChangeListener

        btnMovie.setOnKeyListener(this)
        btnMovie.onFocusChangeListener = onFocusChangeListener

        btnSeries.setOnKeyListener(this)
        btnSeries.onFocusChangeListener = onFocusChangeListener

        lastSelectedMenu = btnHome
        lastSelectedMenu.isActivated = true

        lastSelectedCategory = "Home"
    }


    override fun onKey(view: View?, i: Int, key_event: KeyEvent?): Boolean {
        when (i) {
            KeyEvent.KEYCODE_DPAD_CENTER -> {

                lastSelectedMenu.isActivated = false
                view?.isActivated = true
                lastSelectedMenu = view!!
                println(view.id)
                when (view.id) {

                    R.id.btn_search -> {
                        selectedMenu = Constants.MENU_SEARCH
                        closeMenu()
                    }
                    R.id.btn_home -> {
                        selectedMenu = Constants.MENU_HOME
                        clearList()
                        lastSelectedCategory = "Home"
                        GetPopularityMovies("Home")
                        closeMenu()
                    }
                    R.id.btn_movies -> {
                        selectedMenu = Constants.MENU_MOVIE
                        clearList()
                        lastSelectedCategory = "Movie"
                        GetPopularityMovies("Movie")
                        closeMenu()
                    }
                    R.id.btn_series -> {
                        selectedMenu = Constants.MENU_SERIES
                        clearList()
                        lastSelectedCategory = "Serie"
                        GetPopularityMovies("Serie")
                        closeMenu()
                    }
                }

            }

            KeyEvent.KEYCODE_DPAD_LEFT -> {
                if (!SIDE_MENU) {
                    switchToLastSelectedMenu()
                    openMenu()
                    SIDE_MENU = true
                }
            }
        }
        return false
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT && SIDE_MENU) {
            SIDE_MENU = false
            closeMenu()
        }

        return super.onKeyDown(keyCode, event)
    }

    override fun onBackPressed() {
        if (SIDE_MENU) {
            SIDE_MENU = false
            closeMenu()
        } else {
            super.onBackPressed()
        }
    }

    override fun onItemFocused(movie: ListMovieModel.Movie) {
        updateMainMovie(movie)
    }

    private fun clearList() {
        allMoviesList = mutableListOf()
        updateMoviesList(allMoviesList)
    }
    private fun GetPopularityMovies(type: String?) {
        var query = fetchMoviesAndUpdateList()
            .orderBy("popularity", Query.Direction.DESCENDING)
            .limit(10)
        if (type == "Serie") {
            query = query.whereEqualTo("contentType", "Serie")
        }
        else if (type == "Movie") {
            query = query.whereEqualTo("contentType", "Movie")
        }
        query.get()
            .addOnSuccessListener { documents ->
                val movieList = mutableListOf<ListMovieModel.Movie>()
                for (document in documents){
                    val firstDocument = documents.firstOrNull()
                    if (firstDocument != null) {
                        val firstMovie = firstDocument.toObject(ListMovieModel.Movie::class.java)
                        updateMainMovie(firstMovie)
                    }
                    if(document != null){
                        val movie = document.toObject(ListMovieModel.Movie::class.java)
                        movie.poster_path = URLPATHIMAGE + movie.poster_path
                        movieList.add(movie)
                    }
                }
                allMoviesList.add(ListMovieModel(movieList, "Filmes populares"))
                GetMainMovies()

            }
    }



    private fun GetMainMovies() {

        fetchMoviesAndUpdateList()
            .limit(12)
            .get()
            .addOnSuccessListener { documents ->
                val movieList = mutableListOf<ListMovieModel.Movie>()
                for (document in documents) {
                    if(document != null){
                        val movie = document.toObject(ListMovieModel.Movie::class.java)
                        movie.poster_path = URLPATHIMAGE + movie.poster_path
                        movieList.add(movie)
                    }
                }
                allMoviesList.add(ListMovieModel(movieList, "Luz, Câmera, Ação"))
                GetComedyMovies()
            }
    }




    private fun GetComedyMovies() {
        fetchMoviesAndUpdateList()
            .whereArrayContains("genre_ids", 35)
            .limit(10)
            .get()
            .addOnSuccessListener { documents ->
                val movieList = mutableListOf<ListMovieModel.Movie>()
                for (document in documents){
                    if(document != null){
                        val movie = document.toObject(ListMovieModel.Movie::class.java)
                        movie.poster_path = URLPATHIMAGE + movie.poster_path
                        movieList.add(movie)
                    }
                }
                allMoviesList.add(ListMovieModel(movieList, "Comédia"))
                GetHorrorMovies()
            }
    }

    private fun GetHorrorMovies() {
        fetchMoviesAndUpdateList()
            .whereArrayContains("genre_ids", 27)
            .limit(10)
            .get()
            .addOnSuccessListener { documents ->
                val movieList = mutableListOf<ListMovieModel.Movie>()
                for (document in documents){
                    if(document != null){
                        val movie = document.toObject(ListMovieModel.Movie::class.java)
                        movie.poster_path = URLPATHIMAGE + movie.poster_path
                        movieList.add(movie)
                    }
                }
                allMoviesList.add(ListMovieModel(movieList, "Noites de terror"))
                GetAnimations()
            }
    }

    private fun GetAnimations() {
        fetchMoviesAndUpdateList()
            .whereArrayContains("genre_ids", 16)
            .limit(10)
            .get()
            .addOnSuccessListener { documents ->
                val movieList = mutableListOf<ListMovieModel.Movie>()
                for (document in documents){
                    if(document != null){
                        val movie = document.toObject(ListMovieModel.Movie::class.java)
                        movie.poster_path = URLPATHIMAGE + movie.poster_path
                        movieList.add(movie)
                    }
                }
                allMoviesList.add(ListMovieModel(movieList, "Animação"))
                GetForFamily()
            }
    }

    private fun GetForFamily() {
        fetchMoviesAndUpdateList()
            .whereArrayContains("genre_ids", 10751)
            .limit(10)
            .get()
            .addOnSuccessListener { documents ->
                val movieList = mutableListOf<ListMovieModel.Movie>()
                for (document in documents){
                    if(document != null){
                        val movie = document.toObject(ListMovieModel.Movie::class.java)
                        movie.poster_path = URLPATHIMAGE + movie.poster_path
                        movieList.add(movie)
                    }
                }
                allMoviesList.add(ListMovieModel(movieList, "Para Toda familia"))
                GetNacionalMovies()
            }
    }

    private fun GetNacionalMovies() {
        fetchMoviesAndUpdateList()
            .whereEqualTo("original_language", "pt")
            .limit(12)
            .get()
            .addOnSuccessListener { documents ->
                val movieList = mutableListOf<ListMovieModel.Movie>()
                for (document in documents) {
                    if(document != null){
                        val movie = document.toObject(ListMovieModel.Movie::class.java)
                        movie.poster_path = URLPATHIMAGE + movie.poster_path
                        movieList.add(movie)
                    }
                }
                allMoviesList.add(ListMovieModel(movieList, "Filmes nacionais"))
                updateMoviesList(allMoviesList)
            }
    }



    private fun updateMainMovie(movie: ListMovieModel.Movie){

        var date = ""
        if(movie?.contentType == "Serie" && movie.first_air_date != null){
            binding.mainTitle.setText(movie?.name)
            date = movie.first_air_date
        }else{
            binding.mainTitle.setText(movie?.title)
            date = movie.release_date
        }

        val genresList = genres.filter { it.id in movie.genre_ids }
        val genresNames = genresList.joinToString(" ● ") { "<font color='#08B44E'> ${it.name}</font>" }
        if(genresNames != null){
            binding.infosMainMovie.text = HtmlCompat.fromHtml("$genresNames - ${date.slice(0..3)}", HtmlCompat.FROM_HTML_MODE_LEGACY)
        }

        binding.mainTitle.maxLines = 2
        binding.description.maxLines = 3

        binding.description.setText(movie.overview)
        Glide.with(this)
            .load("https://image.tmdb.org/t/p/w1280" + movie.backdrop_path)
            .into(binding.imgBanner)
    }

    private fun fetchMoviesAndUpdateList(): CollectionReference {
        val db = Firebase.firestore
        val docRef = db.collection("catalog")
        return docRef
    }

    private fun addFragment(similarFragment: ListFragment, containerId: Int) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.add(containerId, similarFragment)
        transaction.commit()
    }
    private fun updateMoviesList(allMoviesList: MutableList<ListMovieModel>) {
        MoviesListFragment.bindData(allMoviesList)
    }

    fun switchToLastSelectedMenu() {
        when (selectedMenu) {
            Constants.MENU_HOME -> {
                btnHome.requestFocus()
            }
            Constants.MENU_MOVIE -> {
                btnMovie.requestFocus()
            }
            Constants.MENU_SERIES -> {
                btnSeries.requestFocus()
            }
        }
    }

    fun openMenu() {
        if(!SIDE_MENU){
            val animSlide : Animation = AnimationUtils.loadAnimation(this, R.anim.slide_in_left)
            navBar.startAnimation(animSlide)
        }
        binding.btnHome.text = "Inicio"
        binding.btnSearch.text = "Pesquisar"
        binding.btnMovies.text = "Filmes"
        binding.btnSeries.text = "Séries"
        navBar.requestLayout()
        navBar.layoutParams.width = Common.getWidthInPercent(this, 16)
    }

    fun closeMenu() {
        binding.btnHome.text = ""
        binding.btnSearch.text = ""
        binding.btnMovies.text = ""
        binding.btnSeries.text = ""
        navBar.requestLayout()
        navBar.layoutParams.width = Common.getWidthInPercent(this, 7)
        SIDE_MENU = false
    }

}