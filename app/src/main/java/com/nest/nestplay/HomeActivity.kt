package com.nest.nestplay

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import com.nest.nestplay.adpters.MoviesListAdpter
import com.nest.nestplay.databinding.ActivityHomeBinding
import com.nest.nestplay.model.Genres.Companion.genres
import com.nest.nestplay.model.ListMovieModel
import com.nest.nestplay.model.TimeModel
import com.nest.nestplay.utils.Common
import com.nest.nestplay.utils.Constants
import java.util.Date

class HomeActivity: FragmentActivity(), View.OnKeyListener,  MoviesListAdpter.OnItemFocusChangeListener {
    private lateinit var binding: ActivityHomeBinding
    val MoviesListFragment = ListFragment()
    var selectedMenu = Constants.MENU_HOME
    lateinit var navBar: BrowseFrameLayout

    lateinit var btnSearch: TextView
    lateinit var btnHome: TextView
    lateinit var btnMovie: TextView
    lateinit var btnSeries: TextView
    lateinit var btnGenres: TextView
    lateinit var btnFavs: TextView
    lateinit var loadingDialog: Dialog
    var loading: Boolean = true
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

        loadingDialog = Common.loadingDialog(this)

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

        ChangeTextCatogy("Home")
        GetRecentsWatchList()

        val onFocusChangeListener = View.OnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                if (!SIDE_MENU) {
                    openMenu()
                    SIDE_MENU = true
                }
            }
        }
        binding.btnSearch.setOnClickListener {
            val intent = Intent(this, SearchMovie::class.java)
            startActivity(intent)
        }
        btnHome = binding.btnHome
        btnMovie = binding.btnMovies
        btnFavs = binding.btnFavs
        btnSearch = binding.btnSearch
        btnSeries =  binding.btnSeries
        btnGenres =  binding.btnGenres


        btnHome.setOnKeyListener(this)
        btnHome.setOnClickListener {
            selectedMenu = Constants.MENU_HOME
            ChangeTextCatogy("Home")
            onGetCategorys("Home", true)
        }
        btnHome.onFocusChangeListener = onFocusChangeListener

        btnSearch.setOnKeyListener(this)
        btnSearch.onFocusChangeListener = onFocusChangeListener

        btnFavs.setOnKeyListener(this)
        btnFavs.onFocusChangeListener = onFocusChangeListener
        btnFavs.setOnClickListener {
            val intent = Intent(this, MovieFavActivity::class.java)
            startActivity(intent)
        }

        btnMovie.setOnKeyListener(this)
        btnMovie.setOnClickListener {
            selectedMenu = Constants.MENU_MOVIE
            ChangeTextCatogy("Filmes")
            onGetCategorys("Movie", false)
        }
        btnMovie.onFocusChangeListener = onFocusChangeListener

        btnSeries.setOnKeyListener(this)
        btnSeries.setOnClickListener {
            selectedMenu = Constants.MENU_SERIES
            ChangeTextCatogy("Séries")
            onGetCategorys("Serie", false)
        }
        btnSeries.onFocusChangeListener = onFocusChangeListener

        btnGenres.setOnKeyListener(this)
        btnGenres.setOnClickListener {
            val intent = Intent(this, GenreActivity::class.java)
            startActivity(intent)
        }
        btnGenres.onFocusChangeListener = onFocusChangeListener

        lastSelectedMenu = btnHome
        lastSelectedMenu.isActivated = true

        lastSelectedCategory = "Home"
    }

    private fun onGetCategorys(key: String, isHome: Boolean?) {
        clearList()
        if(isHome == true){
            GetRecentsWatchList()
        }else{
            GetPopularityMovies(key)
        }
        lastSelectedCategory = key
    }

    override fun onKey(view: View?, i: Int, key_event: KeyEvent?): Boolean {
        when (i) {
            KeyEvent.KEYCODE_DPAD_CENTER -> {

                lastSelectedMenu.isActivated = false

                when (view?.id) {

                    R.id.btn_search -> {
                        closeMenu()
                    }
                    R.id.btn_home -> {
                        selectedMenu = Constants.MENU_HOME
                        lastSelectedCategory = "Home"
                        view.isActivated = true
                        lastSelectedMenu = view
                        closeMenu()
                    }
                    R.id.btn_movies -> {
                        selectedMenu = Constants.MENU_MOVIE
                        view.isActivated = true
                        lastSelectedMenu = view
                        lastSelectedCategory = "Movie"
                        closeMenu()
                    }
                    R.id.btn_series -> {
                        selectedMenu = Constants.MENU_SERIES
                        view.isActivated = true
                        lastSelectedMenu = view
                        lastSelectedCategory = "Serie"
                        closeMenu()
                    }
                    R.id.btn_genres -> {
                        closeMenu()
                    }
                    R.id.btn_favs -> {
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

    override fun onItemFocused(movie: ListMovieModel.Movie, itemView: Int) {
        updateMainMovie(movie)
    }

    private fun clearList() {
        binding.mainTitle.setText("")
        binding.description.setText("")
        binding.infosMainMovie.setText("")
        Glide.with(this)
            .clear(binding.imgBanner)
        allMoviesList.clear()
        MoviesListFragment.clearAll()
    }


    private fun GetRecentsWatchList() {
        val currentUser = Firebase.auth.currentUser
        val db = Firebase.firestore
        val docRef = db.collection("content_watch")

        currentUser?.let { user ->
            docRef
                .whereEqualTo("user_id", user.uid)
                .orderBy("updatedAt", Query.Direction.DESCENDING)
                .limit(12)
                .get()
                .addOnSuccessListener { documents ->
                    val movieList = mutableListOf<Int>()
                    val movieListDate = mutableListOf<Timestamp>()
                    for (document in documents){
                        if(document != null){
                            val movie = document.toObject(TimeModel::class.java)
                            movieList.add(movie.content_id)
                            movieListDate.add(movie.updatedAt)
                        }
                    }
                    if(movieList.isEmpty()){
                        GetPopularityMovies("Movie")
                    }else{
                        GetRecentsWatchMovies(movieList, movieListDate)
                    }
                }
        }
    }
    private fun GetRecentsWatchMovies(list: List<Int>, dates: List<Timestamp>) {
        var query = fetchMoviesAndUpdateList()
            .whereIn("id", list)
            .limit(22)
        query.get()
            .addOnSuccessListener { documents ->
                val movieDatePairs = mutableListOf<ListMovieModel.Movie>()
                documents.forEachIndexed { index, document ->
                    if(document != null){
                        val movie = document.toObject(ListMovieModel.Movie::class.java)
                        movie.poster_path = URLPATHIMAGE + movie.poster_path
                        movieDatePairs.add(movie)
                    }
                }
                movieDatePairs.sortByDescending { movie ->
                    val index = list.indexOf(movie.id)
                    if (index != -1 && index < dates.size) {
                        dates[index].toDate()
                    } else {
                        Date(0)
                    }
                }
                UpdateListItem(movieDatePairs, "Continue assistindo")
                GetPopularityMovies("")
            }
    }

    private fun GetPopularityMovies(type: String?) {
        loadingDialog.show()
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
                val firstDocument = documents.firstOrNull()
                if (firstDocument != null) {
                    val firstMovie = firstDocument.toObject(ListMovieModel.Movie::class.java)
                    updateMainMovie(firstMovie)
                }
                for (document in documents){

                    if(document != null){
                        val movie = document.toObject(ListMovieModel.Movie::class.java)
                        movie.poster_path = URLPATHIMAGE + movie.poster_path
                        movieList.add(movie)
                    }
                }
                if(!movieList.isEmpty()){
                    val titleList = if (type == "Serie") "Séries populares" else "Populares"
                    UpdateListItem(movieList, titleList)

                }
                loadingDialog.dismiss()
                GetMainMovies()
            }
    }



    private fun GetMainMovies() {
        var query=  fetchMoviesAndUpdateList().limit(12)
        if(lastSelectedCategory != "Home"){
            query = query.whereEqualTo("contentType", lastSelectedCategory)
        }
        query
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
                if(!movieList.isEmpty()){
                    UpdateListItem(movieList, "Luz, Câmera, Ação")
                }
                GetComedyMovies()
            }
    }




    private fun GetComedyMovies() {
        var query=  fetchMoviesAndUpdateList() .limit(12)
        if(lastSelectedCategory != "Home"){
            query = query.whereEqualTo("contentType", lastSelectedCategory)
        }
        query
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
                if(!movieList.isEmpty()){
                    UpdateListItem(movieList, "Comédia")
                }
                GetHorrorMovies()
            }
    }

    private fun GetHorrorMovies() {
        var query=  fetchMoviesAndUpdateList() .limit(12)
        if(lastSelectedCategory != "Home"){
            query = query.whereEqualTo("contentType", lastSelectedCategory)
        }
        query
            .whereArrayContains("genre_ids", 27)
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
                if(!movieList.isEmpty()){
                    UpdateListItem(movieList, "Noites de terror")
                }
                GetAnimations()
            }
    }

    private fun GetAnimations() {
            var query=  fetchMoviesAndUpdateList() .limit(12)
            if(lastSelectedCategory != "Home"){
                query = query.whereEqualTo("contentType", lastSelectedCategory)
            }
            query
            .whereArrayContains("genre_ids", 16)
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
                if(!movieList.isEmpty()){
                    UpdateListItem(movieList, "Animação")
                }
                GetForFamily()
            }
    }

    private fun GetForFamily() {
            var query=  fetchMoviesAndUpdateList() .limit(12)
            if(lastSelectedCategory != "Home"){
                query = query.whereEqualTo("contentType", lastSelectedCategory)
            }
            query
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
                if(!movieList.isEmpty()){
                    UpdateListItem(movieList,"Para toda familia")
                }
                GetNacionalMovies()
            }
    }

    private fun GetNacionalMovies() {
            var query=  fetchMoviesAndUpdateList().limit(12)
            if(lastSelectedCategory != "Home"){
                query = query.whereEqualTo("contentType", lastSelectedCategory)
            }
            query
            .whereEqualTo("original_language", "pt")
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
                if(!movieList.isEmpty()){
                    UpdateListItem(movieList,"Nacionais")
                }
                GetNetflixMovies()
            }
            .addOnFailureListener {
            }
    }

    private fun GetNetflixMovies() {
        var query=  fetchMoviesAndUpdateList().limit(12)
        if(lastSelectedCategory != "Home"){
            query = query.whereEqualTo("contentType", lastSelectedCategory)
        }
        query
            .whereEqualTo("distributed", "Netflix")
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
                if(!movieList.isEmpty()){
                    UpdateListItem(movieList,"Netflix")
                }
                GetScienceFictionMovies()
            }
            .addOnFailureListener {
            }
    }

    private fun GetScienceFictionMovies() {
        var query=  fetchMoviesAndUpdateList().limit(12)
        if(lastSelectedCategory != "Home"){
            query = query.whereEqualTo("contentType", lastSelectedCategory)
        }
        query
            .whereArrayContains("genre_ids", 878)
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
                if(!movieList.isEmpty()){
                    UpdateListItem(movieList,"Ficção científica")
                }
            }
            .addOnFailureListener {
            }
    }

    private fun UpdateListItem(movieList: MutableList<ListMovieModel.Movie>, title: String) {
        MoviesListFragment.bindData(ListMovieModel(movieList.toMutableList(), title))
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
//        MoviesListFragment.bindData(allMoviesList)
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
        switchToLastSelectedMenu()

        binding.btnHome.text = "Inicio"
        binding.btnSearch.text = "Pesquisar"
        binding.btnMovies.text = "Filmes"
        binding.btnSeries.text = "Séries"
        binding.btnFavs.text = "Minha lista"
        binding.btnGenres.text = "Género"

        navBar.requestLayout()
        navBar.setBackgroundResource(R.drawable.banner_gradient)
        navBar.layoutParams.width = Common.getWidthInPercent(this, 13)

    }

    fun closeMenu() {
        binding.btnHome.text = ""
        binding.btnSearch.text = ""
        binding.btnMovies.text = ""
        binding.btnSeries.text = ""
        binding.btnFavs.text = ""
        binding.btnGenres.text = ""
        navBar.requestLayout()
        navBar.setBackgroundResource(R.drawable.no_selected_bg)
        navBar.layoutParams.width = Common.getWidthInPercent(this, 7)
        SIDE_MENU = false
    }

    fun ChangeTextCatogy(text: String) {
        val animSlide: Animation = AnimationUtils.loadAnimation(this, R.anim.slide_in_left)
        binding.fragCatogyContainer.startAnimation(animSlide)
        binding.fragCatogy.setText(text)

        binding.fragCatogyContainer.setBackgroundResource(R.drawable.selected_category)

        Handler(Looper.getMainLooper()).postDelayed({
            binding.fragCatogy.setText("")
            binding.fragCatogyContainer.setBackgroundResource(android.R.color.transparent)
        }, 5000)
    }
}