package com.nest.nestplay

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Base64
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
import com.google.gson.Gson
import com.nest.nestplay.adpters.MoviesListAdpter
import com.nest.nestplay.databinding.ActivityHomeBinding
import com.nest.nestplay.model.Genres.Companion.genres
import com.nest.nestplay.model.ListMovieModel
import com.nest.nestplay.model.TimeModel
import com.nest.nestplay.model.UserModel
import com.nest.nestplay.utils.Common
import com.nest.nestplay.utils.Constants
import java.util.Date
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class HomeActivity: FragmentActivity(), View.OnKeyListener,  MoviesListAdpter.OnItemFocusChangeListener {
    private lateinit var binding: ActivityHomeBinding
    val MoviesListFragment = ListFragment()
    val TvLiveListFragment = ListLiveTvFragment()

    var selectedMenu = Constants.MENU_HOME
    lateinit var navBar: BrowseFrameLayout

    lateinit var btnSearch: TextView
    lateinit var btnHome: TextView
    lateinit var btnTvLive: TextView
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


        MoviesListFragment.setOnContentSelectedListener { movie ->
            if(movie != null){
                updateMainMovie(movie)
            }
        }

        MoviesListFragment.setOnItemClickListener {movie ->
            if(movie.isNoPartContent != null){
                val intent = Intent(this, GenreActivity::class.java)
                intent.putExtra("id", movie.isNoPartContent)
                startActivity(intent)
            }else{
                val intent = Intent(this, DetailMovieActivity::class.java)
                intent.putExtra("id", movie.id)
                startActivity(intent)
            }
        }

        ChangeTextCatogy("Home")
        onGetCategorys("Home", true)
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
        btnTvLive = binding.btnTv

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
            GetPopularityMovies(key, true)
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
                            val time = document.toObject(TimeModel::class.java)
                            time?.content_id?.let { movieList.add(it) }
                            time?.updatedAt?.let { movieListDate.add(it) }
                        }
                    }
                    if(movieList.isEmpty()){
                        GetPopularityMovies("Movie", true)
                    }else{
                        GetRecentsWatchMovies(movieList, movieListDate)
                    }
                }
                .addOnFailureListener { it
                    if(it.message == Common.msgPermissionDENIED){
                        accessDenied()
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
                    val firstDocument = documents.firstOrNull()
                    if (firstDocument != null) {
                        val firstMovie = firstDocument.toObject(ListMovieModel.Movie::class.java)
                        updateMainMovie(firstMovie)
                    }
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

                GetPopularityMovies("", movieDatePairs.isEmpty())
            }
            .addOnFailureListener { it
                if(it.message == Common.msgPermissionDENIED){
                    accessDenied()
                }
            }
    }

    private fun GetPopularityMovies(type: String?, updateFirstBanner: Boolean?) {
        loadingDialog.show()
        val idCategory = 101
        var query = fetchMoviesAndUpdateList()
            .orderBy("popularity", Query.Direction.DESCENDING)
            .whereArrayContainsAny("genre_ids", listOf(14L, 28L, 12L, 16L, 35L, 80L, 36L, 878L, 53L, 5L))
            .limit(15)
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
                if (firstDocument != null && updateFirstBanner == true) {
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
                    movieList.add(createMoreItem(idCategory))
                    UpdateListItem(movieList, titleList)

                }
                loadingDialog.dismiss()
                GetMainMovies()
            }
            .addOnFailureListener { it
                if(it.message == Common.msgPermissionDENIED){
                    accessDenied()
                }
            }
    }



    private fun GetMainMovies() {
        val idCategory = 28

        var query=  fetchMoviesAndUpdateList().limit(12)
        if(lastSelectedCategory != "Home"){
            query = query.whereEqualTo("contentType", lastSelectedCategory)
        }
        query
            .whereArrayContains("genre_ids", idCategory)
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
                    movieList.add(createMoreItem(idCategory))
                    UpdateListItem(movieList, "Luz, Câmera, Ação")
                }
                GetComedyMovies()
            }
    }

    private fun GetComedyMovies() {
        val idCategory = 35

        var query=  fetchMoviesAndUpdateList().limit(12)
        if(lastSelectedCategory != "Home"){
            query = query.whereEqualTo("contentType", lastSelectedCategory)
        }
        query
            .whereArrayContains("genre_ids", idCategory)
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
                    movieList.add(createMoreItem(idCategory))
                    UpdateListItem(movieList, "Comédia")
                }
                GetNovela()
            }
    }


    private fun GetNovela() {
        val idCategory = 10766

        if(lastSelectedCategory != "Home"){
            getCrimes()
            return
        }

        var query=  fetchMoviesAndUpdateList() .limit(20)
        query
            .whereArrayContains("genre_ids", idCategory)
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
                    movieList.add(createMoreItem(idCategory))
                    UpdateListItem(movieList, "Novelas")
                }
                getCrimes()
            }
    }



    private fun getCrimes() {
        val idCategory = 80

        var query=  fetchMoviesAndUpdateList() .limit(20)
        if(lastSelectedCategory != "Home"){
            query = query.whereEqualTo("contentType", lastSelectedCategory)
        }
        query
            .whereArrayContains("genre_ids", idCategory)
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
                    movieList.add(createMoreItem(idCategory))
                    UpdateListItem(movieList, "Mentes Criminosas")
                }
                getVotes()
            }
    }

    private fun getVotes() {
        val idCategory = 100
        var query=  fetchMoviesAndUpdateList() .limit(20)
        if(lastSelectedCategory != "Home"){
            query = query.whereEqualTo("contentType", lastSelectedCategory)
        }
        query
            .whereGreaterThan("vote_average", 7)
            .orderBy("vote_count", Query.Direction.DESCENDING)
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
                    movieList.add(createMoreItem(idCategory))
                    UpdateListItem(movieList, "Recomendados pela Crítica")
                }
                GetHorrorMovies()
            }
    }

    private fun GetHorrorMovies() {
        val idCategory = 27
        var query=  fetchMoviesAndUpdateList() .limit(15)
        if(lastSelectedCategory != "Home"){
            query = query.whereEqualTo("contentType", lastSelectedCategory)
        }
        query
            .whereArrayContains("genre_ids", idCategory)
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
                    movieList.add(createMoreItem(idCategory))
                    UpdateListItem(movieList, "Noites de terror")
                }
                GetAnimes()
            }
    }


    private fun GetAnimes() {
        val idCategory = 5
        var query=  fetchMoviesAndUpdateList().limit(20)
        if(lastSelectedCategory != "Home"){
            query = query.whereEqualTo("contentType", lastSelectedCategory)
        }
        query
            .whereArrayContains("genre_ids", idCategory)
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
                    movieList.add(createMoreItem(idCategory))
                    UpdateListItem(movieList,"Animes")
                }
                GetNetflixMovies()
            }
    }

    private fun GetNetflixMovies() {
        val idCategory = 7
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
                    movieList.add(createMoreItem(idCategory))
                    UpdateListItem(movieList, "NETFLIX")
                }
                getKids()
            }
            .addOnFailureListener {
            }
    }

    private fun getKids() {
        val idCategory = 10762
        var query=  fetchMoviesAndUpdateList() .limit(12)
        if(lastSelectedCategory != "Home"){
            query = query.whereEqualTo("contentType", lastSelectedCategory)
        }
        query
            .whereArrayContains("genre_ids", idCategory)
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
                    movieList.add(createMoreItem(idCategory))
                    UpdateListItem(movieList, "Animação")
                }
                GetForFamily()
            }
    }


    private fun GetForFamily() {
        val idCategory = 10751
        var query=  fetchMoviesAndUpdateList() .limit(12)
        if(lastSelectedCategory != "Home"){
            query = query.whereEqualTo("contentType", lastSelectedCategory)
        }
        query
            .whereArrayContains("genre_ids", idCategory)
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
                    movieList.add(createMoreItem(idCategory))
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
            .whereArrayContainsAny("genre_ids", listOf(14L, 28L, 12L, 16L, 35L, 80L, 36L, 878L, 53L, 5L))
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
                    movieList.add(createMoreItem(0))
                    UpdateListItem(movieList,"Nacionais")
                }
                GetScienceFictionMovies()
            }
            .addOnFailureListener {
            }
    }


    private fun GetScienceFictionMovies() {
        val idCategory = 878
        var query=  fetchMoviesAndUpdateList().limit(12)
        if(lastSelectedCategory != "Home"){
            query = query.whereEqualTo("contentType", lastSelectedCategory)
        }
        query
            .whereArrayContains("genre_ids", idCategory)
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
                    movieList.add(createMoreItem(idCategory))
                    UpdateListItem(movieList,"Ficção científica")
                }
                GetRomance()
            }
            .addOnFailureListener {
            }
    }

    private fun GetRomance() {
        val idCategory = 10749
        var query=  fetchMoviesAndUpdateList().limit(12)
        if(lastSelectedCategory != "Home"){
            query = query.whereEqualTo("contentType", lastSelectedCategory)
        }
        query
            .whereArrayContains("genre_ids", idCategory)
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
                    movieList.add(createMoreItem(idCategory))
                    UpdateListItem(movieList,"Romances")
                }
                GetWestern()
            }
            .addOnFailureListener {
            }
    }

    private fun GetWestern() {
        val idCategory = 37
        var query=  fetchMoviesAndUpdateList().limit(12)
        if(lastSelectedCategory != "Home"){
            query = query.whereEqualTo("contentType", lastSelectedCategory)
        }
        query
            .whereArrayContains("genre_ids", idCategory)
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
                    movieList.add(createMoreItem(idCategory))
                    UpdateListItem(movieList,"Faroeste")
                }
                GetMusic()
            }
            .addOnFailureListener {
            }
    }

    private fun GetMusic() {
        val idCategory = 10402
        var query=  fetchMoviesAndUpdateList().limit(12)
        if(lastSelectedCategory != "Home"){
            query = query.whereEqualTo("contentType", lastSelectedCategory)
        }
        query
            .whereArrayContains("genre_ids", idCategory)
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
                    movieList.add(createMoreItem(idCategory))
                    UpdateListItem(movieList,"Música")
                }
                GetDocs()
            }
            .addOnFailureListener {
            }
    }

    private fun GetDocs() {
        val idCategory = 99
        var query=  fetchMoviesAndUpdateList().limit(12)
        if(lastSelectedCategory != "Home"){
            query = query.whereEqualTo("contentType", lastSelectedCategory)
        }
        query
            .whereArrayContains("genre_ids", idCategory)
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
                    movieList.add(createMoreItem(idCategory))
                    UpdateListItem(movieList,"Documentário")
                }
            }
            .addOnFailureListener {
            }
    }

    private fun UpdateListItem(movieList: MutableList<ListMovieModel.Movie>, title: String) {
        MoviesListFragment.bindData(ListMovieModel(movieList.toMutableList(), title))
    }


    private fun updateMainMovie(movie: ListMovieModel.Movie){
        if(movie.isNoPartContent != null) {
            return
        }
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

    fun accessDenied () {
        val sharedPreferences = applicationContext.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val gson = Gson()
        val userJson = sharedPreferences.getString("user", null)
        val user = gson.fromJson(userJson, UserModel::class.java)

        val i = Intent(this, PaymentRequiredActivity::class.java)
        i.putExtra("user", user)
        startActivity(i)
        finish()
    }

    fun createMoreItem(idCategory: Int): ListMovieModel.Movie {
       var iconMoreItem = ListMovieModel.Movie(
           adult = false,
           backdrop_path = "",
           first_air_date = "",
           genre_ids = listOf(),
           id = 0,
           name = "Mais",
           origin_country = listOf(),
           original_language = "",
           original_name = "",
           original_title = "",
           overview = "",
           popularity = 0.0,
           poster_path = decrypt("qT4+nb5/6hsxHKX/0Qqtz6v3A+Z3QKkPX2sbYlK4DffxZWMlgX8HgEIe99NIXMMp1h8jZIxWlXIrJR9AmynJRvsxwiFoxRZaE//N6U5w2TCjg3AM2hVBUv83waGhmbgC6L626vyoI2Dt8dOYO5ai2h9X4sLbq1lGX9/JzjrsyC6KQMgEAvjvN5jGEu3PTL9X"),
           release_date = "",
           title = "",
           vote_average = 0.0,
           vote_count = 0,
           url = "",
           original_title_lowcase = "",
           contentType = "",
           subtitles = listOf(),
           urls_subtitle = listOf(),
           isNoPartContent = idCategory
       )

        return iconMoreItem
    }

    fun decrypt(msg: String, password: String = "770E75DC61635CCC61A1D7D8FFF9D1B0"): String {
        var decripeted = ""
            try {
                val key32Char = hexStringToByteArray(password)
                val iv32Char = hexStringToByteArray(password)
                val secretKeySpec = SecretKeySpec(key32Char, "AES")
                val ivParameterSpec = IvParameterSpec(iv32Char)
                val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
                cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec)
                val dstBuff = cipher.doFinal(Base64.decode(msg, Base64.DEFAULT))
                decripeted = String(dstBuff)
            }catch (ex: Exception) {
                ex.printStackTrace()
            }
        return decripeted
    }

private fun hexStringToByteArray(s: String): ByteArray {
    val len = s.length
    val data = ByteArray(len / 2)
    var i = 0
    while (i < len) {
        data[i / 2] = ((Character.digit(s[i], 16) shl 4)
                + Character.digit(s[i + 1], 16)).toByte()
        i += 2
    }
    return data
}
    override fun onResume() {
        super.onResume()
    }
}