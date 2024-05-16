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
import android.widget.Toast
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
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
import com.nest.nestplay.model.ChannelTVModel
import com.nest.nestplay.model.Genres.Companion.genres
import com.nest.nestplay.model.LinksTvModel
import com.nest.nestplay.model.ListChannelTVModel
import com.nest.nestplay.model.ListMovieModel
import com.nest.nestplay.model.MovieModel
import com.nest.nestplay.model.TimeModel
import com.nest.nestplay.model.UserModel
import com.nest.nestplay.model.parseM3uPlaylist
import com.nest.nestplay.service.ApiClient
import com.nest.nestplay.utils.Common
import com.nest.nestplay.utils.Constants
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class HomeActivity: FragmentActivity(), View.OnKeyListener,  MoviesListAdpter.OnItemFocusChangeListener {
    private lateinit var binding: ActivityHomeBinding
    val MoviesListFragment = ListFragment()

    var selectedMenu = Constants.MENU_HOME
    lateinit var navBar: BrowseFrameLayout


    lateinit var btnSearch: TextView
    lateinit var btnHome: TextView
    lateinit var btnTvLive: TextView
    lateinit var btnMovie: TextView
    lateinit var btnSeries: TextView
    lateinit var btnGenres: TextView
    lateinit var btnFavs: TextView
    lateinit var btnSettings: TextView
    lateinit var loadingDialog: Dialog
    var loadingMovies: Boolean = false
    var allMoviesList = mutableListOf<ListMovieModel>()

    var URLPATHIMAGE = "https://image.tmdb.org/t/p/w500"
    var SIDE_MENU = false

    var lastRowTitle = ""
    var FirstGrouploaded = false
    var SecondGrouploaded = false
    var ThirdGrouploaded = false
    var FourGrouploaded = false
    var FiveGrouploaded = false
    var SixGrouploaded = false
    lateinit var lastSelectedMenu: View
    lateinit var lastSelectedCategory: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        addFragment(MoviesListFragment, R.id.mainMoviesHome)

        loadingDialog = Common.loadingDialog(this)

        navBar = findViewById(R.id.blfNavBar)

        MoviesListFragment.setOnContentSelectedListener { movie, title ->
            var _lc = lastSelectedCategory
            if (lastRowTitle != title) {
                lastRowTitle = title
                if (title == "Luz, Câmera, Ação" && !FirstGrouploaded) {
                    FirstGrouploaded = true
                    if (lastSelectedCategory == "Home") {
                        fatchDataContent(10766, "Novelas", _lc)
                    }
                    fatchDataContent(80, "Mentes Criminosas", _lc)

                    val formataData = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val currentDate = Date()
                    val todayString = formataData.format(currentDate)
                    var query = fetchMoviesAndUpdateList().limit(12)
                        .whereLessThan("release_date", todayString)
                        .orderBy("release_date", Query.Direction.DESCENDING)
                    fatchDataContentWidthQuery(query, "Lançados recentemente", 3, lastSelectedCategory)
                }
                if (title == "Mentes Criminosas" && !SecondGrouploaded) {
                    SecondGrouploaded = true
                    fatchDataContent(27, "Noites de terror", _lc)
                    var queryRecomentCritic = fetchMoviesAndUpdateList().limit(12)
                        .whereGreaterThan("vote_average", 8)
                        .orderBy("vote_count", Query.Direction.DESCENDING)
                    fatchDataContentWidthQuery(queryRecomentCritic, "Recomendados pela critica", 8, _lc)
                    fatchDataContent(5, "Animes", _lc)
                }
                if(title == "Recomendados pela critica" && !ThirdGrouploaded){
                    ThirdGrouploaded = true
                    fatchDataContent(16, "Animação", _lc)
                    var query = fetchMoviesAndUpdateList().limit(12)
                        .whereEqualTo("distributed", "Netflix")
                    fatchDataContentWidthQuery(query, "Netflix", 7, _lc)
                    var query2 = fetchMoviesAndUpdateList().limit(12)
                        .whereEqualTo("distributed", "Marvel")
                    fatchDataContentWidthQuery(query2, "Heróis da Marvel", 8, _lc)
                }
                if (title == "Netflix" && !FourGrouploaded) {
                    FourGrouploaded = true
                    fatchDataContent(10751, "Para toda familia", _lc)
                    fatchDataContent(878, "Ficção científica", _lc)
                    fatchDataContent(10749, "Romances", _lc)
                }
                if (title == "Romances" && !FiveGrouploaded) {
                    FiveGrouploaded = true
                    fatchDataContent(37, "Faroeste", _lc)
                    fatchDataContent(10764, "Realitys", _lc)
                    fatchDataContent(10402, "Música", _lc)
                }
                if(title == "Música" && !SixGrouploaded){
                    SixGrouploaded = true
                    fatchDataContent(99, "Documentário", _lc)
                    fatchDataContent(10767, "Talk shows", _lc)
                }
            }

            if (movie != null && movie is ListMovieModel.Movie) {
                updateMainMovie(movie, false)
            }
        }

        MoviesListFragment.setOnContentTvOnlineSelectedListener { tv ->
            if (tv != null && tv is ChannelTVModel) {
                try {
                    updateMainMovie(createListMovieModelFromTvItem(tv), true)
                } catch (e: Exception) {
                    Toast.makeText(this, "Erro: 2", Toast.LENGTH_LONG).show()
                }
            }
        }


        MoviesListFragment.setOnItemTvOnlineClickListener { tv ->
            println(tv)
            if(tv is ChannelTVModel){
                val intent = Intent(this, VideoPlayActivity::class.java)
                intent.putExtra("movie", createMovieModelFromTvItem(tv))
                startActivity(intent)
            }else{
                Toast.makeText(this, "Canal offline", Toast.LENGTH_LONG).show()
            }

        }


        MoviesListFragment.setOnItemClickListener { movie ->
            if (movie is ListMovieModel.Movie) {
                if (movie.isNoPartContent != null) {
                    val intent = Intent(this, GenreActivity::class.java)
                    intent.putExtra("id", movie.isNoPartContent)
                    startActivity(intent)
                } else {
                    val intent = Intent(this, DetailMovieActivity::class.java)
                    intent.putExtra("id", movie.id)
                    startActivity(intent)
                }
            }
        }


        ChangeTextCatogy("Início")
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
        btnSeries = binding.btnSeries
        btnGenres = binding.btnGenres
        btnTvLive = binding.btnTv
        btnSettings = binding.btnSettings

        btnSettings.setOnClickListener {
            val intent = Intent(this, SettingsAccountActivity::class.java)
            startActivity(intent)
        }


        val user = getCurrentUser()
        if(user?.activeOnlineTv == true){
            btnTvLive.visibility == View.VISIBLE
        }else{
            btnTvLive.visibility = View.GONE
        }

        btnTvLive.setOnKeyListener(this)
        btnTvLive.onFocusChangeListener = onFocusChangeListener
        btnSettings.onFocusChangeListener = onFocusChangeListener

        btnTvLive.setOnClickListener {
            lastSelectedCategory = "Tv online"
            selectedMenu = Constants.MENU_TV
            ChangeTextCatogy("Tv online")
            getListTvLive()
        }

        btnHome.setOnKeyListener(this)
        btnHome.setOnClickListener {
            selectedMenu = Constants.MENU_HOME
            ChangeTextCatogy("Início")
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
        MoviesListFragment.clearAll()
        FirstGrouploaded = false
        SecondGrouploaded = false
        ThirdGrouploaded = false
        FourGrouploaded = false
        FiveGrouploaded = false
        SixGrouploaded = false
        if(loadingMovies == true){
            Toast.makeText(this, "Aguarde o carregamento atual finalizar", Toast.LENGTH_LONG).show()
            return
        }else {
            loadingMovies = true
            if (isHome == true) {
                GetRecentsWatchList()
            } else {
                GetPopularityMovies(key, true)
            }
            lastSelectedCategory = key
        }
    }

    private fun getListTvLive() {
        loadingMovies = true
        clearList()
        clearMainMovie()
        try {
            loadingDialog.show()

            val user = getCurrentUser()
            if(user?.iptv_list_link != null && user.iptv_list_link != "" && user?.use_default_list_tv == false){
                val DecryptedLink = Common.decrypt(user.iptv_list_link)
                val (baseUrl, endpoint) = Common.parseBaseUrlAndEndpoint(DecryptedLink)
                getListITvOnline(baseUrl, endpoint)
                return
            }

            var query=  fetchGetLinkUrlTv().limit(1).whereEqualTo("isActive", true)
            query
                .get()
                .addOnSuccessListener { documents ->
                    loadingMovies = false
                    val firstDocument = documents.firstOrNull()
                    println(firstDocument)
                    if (firstDocument != null) {
                        val firstMovie = firstDocument.toObject(LinksTvModel::class.java)
                        if (firstMovie?.link != null) {
                            val DecryptedLink = Common.decrypt(firstMovie?.link)
                            val (baseUrl, endpoint) = Common.parseBaseUrlAndEndpoint(DecryptedLink)
                            getListITvOnline(baseUrl, endpoint)
                        }
                    }
                }
                .addOnFailureListener {
                    loadingMovies = false
                    loadingDialog.hide()
                    Toast.makeText(
                        applicationContext,
                        "Falha ao buscar canais",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        } catch (e: Exception) {
            loadingDialog.hide()
            println(e)
            Toast.makeText(
                applicationContext,
                "Não foi possivel carregar lista de canais",
                Toast.LENGTH_LONG
            ).show()
            loadingMovies = false
        }
    }

    fun getListITvOnline(baseUrl: String, endpoint: String) {
        val fullUrl = "$baseUrl$endpoint"
        val apiService = ApiClient.getApiClient(baseUrl)
        apiService.fetchDataM3u(fullUrl).enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.isSuccessful) {
                    loadingMovies = false
                    val playlistText = response.body()
                    if (playlistText != null) {
                        val playlistItem = parseM3uPlaylist(playlistText, applicationContext)
                        val groupedItems =
                            playlistItem.groupBy { it.groupTitlePattern ?: "Outros" }
                        try {
                            groupedItems.map { (category, items) ->
                                println(category)
                                if (items.isNotEmpty()) {
                                    val listItem = ListChannelTVModel(
                                        list = items.toMutableList(),
                                        title = category
                                    )
                                    if(lastSelectedCategory == "Tv online"){
                                        MoviesListFragment.bindDataTvOnline(listItem)
                                    }
                                }
                            }
                            loadingMovies = false
                        } catch (e: Exception) {
                            Toast.makeText(
                                applicationContext,
                                "Erro: 14; Falha ao buscar canais",
                                Toast.LENGTH_SHORT
                            ) .show()
                            loadingMovies = false
                        }
                    }
                }
                loadingDialog.hide()
            }
            override fun onFailure(call: Call<String>, t: Throwable) {
                loadingDialog.hide()
                Toast.makeText(
                    applicationContext,
                    "Falha ao buscar canais",
                    Toast.LENGTH_SHORT
                ).show()
                println(t)
            }
        })
    }

    fun getCurrentUser(): UserModel? {
        val sharedPreferences = applicationContext.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val gson = Gson()
        val userJson = sharedPreferences.getString("user", null)
        val user = gson.fromJson(userJson, UserModel::class.java)

        return user
    }

    override fun onKey(view: View?, i: Int, key_event: KeyEvent?): Boolean {
        when (i) {
            KeyEvent.KEYCODE_DPAD_CENTER -> {

                lastSelectedMenu.isActivated = false

                when (view?.id) {

                    R.id.btn_search -> {
                        closeMenu()
                    }
                    R.id.btn_tv -> {
                        selectedMenu = Constants.MENU_TV
                        view.isActivated = true
                        lastSelectedMenu = view
                        lastSelectedCategory = "Tv Online"
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
                    R.id.btn_settings -> {
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
        updateMainMovie(movie, false)
    }

    private fun clearList() {
        allMoviesList = mutableListOf<ListMovieModel>()
        allMoviesList.clear()
        MoviesListFragment.clearAll()

        binding.mainTitle.text = ""
        binding.description.text = ""
        binding.infosMainMovie.text = ""
        Glide.with(this)
            .clear(binding.imgBanner)
    }


    private fun GetRecentsWatchList() {
        val currentUser = Firebase.auth.currentUser
        val db = Firebase.firestore
        val docRef = db.collection("content_watch")

        currentUser?.let { user ->
            docRef
                .whereEqualTo("user_id", user.uid)
                .orderBy("updatedAt", Query.Direction.DESCENDING)
                .limit(20)
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
        clearList()
        loadingDialog.show()
        var query = fetchMoviesAndUpdateList()
            .whereIn("id", list)
            .limit(20)
        query.get()
            .addOnSuccessListener { documents ->
                val movieDatePairs = mutableListOf<ListMovieModel.Movie>()
                documents.forEachIndexed { index, document ->
                    val firstDocument = documents.firstOrNull()
                    if (firstDocument != null) {
                        val firstMovie = firstDocument.toObject(ListMovieModel.Movie::class.java)
                        updateMainMovie(firstMovie, false)
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

    private fun GetPopularityMovies(type: String, updateFirstBanner: Boolean?) {
        loadingDialog.show()
        if(updateFirstBanner == true){
            clearList()
        }
        val idCategory = 101
        var query = fetchMoviesAndUpdateList()
            .orderBy("popularity", Query.Direction.DESCENDING)
            .whereArrayContainsAny("genre_ids", listOf(14L, 28L, 12L, 16L, 35L, 80L, 36L, 878L, 53L, 5L, 9648L))
            .limit(18)
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
                    updateMainMovie(firstMovie, false)
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
                if(lastSelectedCategory == "Serie"){
                    var queryNewEpsodes = fetchMoviesAndUpdateList().limit(12)
                        .whereEqualTo("contentType", "Serie")
                        .orderBy("updatedAt", Query.Direction.DESCENDING)
                    fatchDataContentWidthQuery(queryNewEpsodes, "Novos episódios", 10, type)
                }
                if(lastSelectedCategory == "Home"){
                    fatchDataContent(200, "Filmes e Séries em Destaque", type)
                }
                fatchDataContent(28, "Luz, Câmera, Ação", type)
                fatchDataContent(35, "Comédia", type)
                loadingMovies = false
            }
            .addOnFailureListener { it
                if(it.message == Common.msgPermissionDENIED){
                    accessDenied()
                    loadingMovies = false
                }
            }
    }



    private fun fatchDataContent(idCategory: Int, title: String, currentCategory: String){
        loadingMovies = true
        var query=  fetchMoviesAndUpdateList().limit(12)
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
                    if(idCategory != 200){
                        movieList.add(createMoreItem(idCategory))
                    }
                    if(currentCategory == currentCategory){
                        UpdateListItem(movieList, title)
                    }
                }
                loadingMovies = false
            }
            .addOnFailureListener {
                loadingMovies = false
            }
    }

    private fun fatchDataContentWidthQuery(query:  Query, title: String, idCategory: Int, Category: String){
        loadingMovies = true
        var modifiedQuery = query
        if(lastSelectedCategory != "Home"){
            modifiedQuery = query.whereEqualTo("contentType", lastSelectedCategory)
        }
        modifiedQuery
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
                if(!movieList.isEmpty() && Category == lastSelectedCategory){
                    movieList.add(createMoreItem(idCategory))
                    UpdateListItem(movieList, title)
                }
                loadingMovies = false
            }
            .addOnFailureListener {
                loadingMovies = false
            }
    }


    private fun UpdateListItem(movieList: MutableList<ListMovieModel.Movie>, title: String) {
            MoviesListFragment.bindData(ListMovieModel(movieList.toMutableList(), title))
    }
    private fun clearMainMovie() {
        binding.mainTitle.setText("")
        binding.infosMainMovie.text = ""
        binding.description.text = ""
        binding.infosMainMovie.setText("")
        Glide.with(this)
            .clear(binding.imgBanner)
    }

    private fun updateMainMovie(movie: ListMovieModel.Movie, ownUrl: Boolean?){
        try {
            if(movie?.isNoPartContent != null) {
                return
            }
            var date = ""
            if(movie?.contentType == "Serie" && movie?.first_air_date != null){
                binding.mainTitle.setText(movie?.name)
                date = movie.first_air_date
            }else{
                binding.mainTitle.setText(movie?.title)
                date = movie.release_date
            }

            val genresList = genres.filter { it.id in movie.genre_ids }
            val genresNames = genresList.joinToString(" ● ") { "<font color='#08B44E'> ${it.name}</font>" }
            if(genresNames != null){
                try{
                    binding.infosMainMovie.text = HtmlCompat.fromHtml("$genresNames - ${date.slice(0..3)}", HtmlCompat.FROM_HTML_MODE_LEGACY)
                }
                catch (e: Exception){
                    binding.infosMainMovie.text = ""
                }
            }

            binding.mainTitle.maxLines = 2
            binding.description.maxLines = 3

            binding.description.setText(movie?.overview)
            var urlImage = "https://image.tmdb.org/t/p/w1280" + movie?.backdrop_path
            if(ownUrl == true){
                urlImage = movie.backdrop_path
            }
            try {
                Glide.with(this)
                    .load(urlImage)
                    .into(binding.imgBanner)
            }catch (e: Exception){
                Toast.makeText(this,"Erro: Load image", Toast.LENGTH_LONG).show()
            }

        }catch (e: Exception){
            Toast.makeText(this,"Erro: Focus item", Toast.LENGTH_LONG).show()

        }
    }

    private fun fetchMoviesAndUpdateList(): CollectionReference {
        val db = Firebase.firestore
        val docRef = db.collection("catalog")
        return docRef
    }

    private fun fetchGetLinkUrlTv(): CollectionReference {
        val db = Firebase.firestore
        val docRef = db.collection("onlineTvLinks")
        return docRef
    }
    private fun addFragment(fragment: Fragment, containerId: Int) {
        supportFragmentManager.beginTransaction()
            .replace(containerId, fragment)
            .commit()
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
//        binding.btnFavs.text = "Minha lista"
        binding.btnGenres.text = "Categorias"
        binding.btnTv.text = "Tv Online"
        binding.btnSettings.text = "Conta"

        navBar.requestLayout()
        navBar.setBackgroundResource(R.drawable.banner_gradient)
        navBar.layoutParams.width = Common.getWidthInPercent(this, 13)

    }

    fun closeMenu() {
        binding.btnHome.text = ""
        binding.btnSearch.text = ""
        binding.btnMovies.text = ""
        binding.btnSeries.text = ""
//        binding.btnFavs.text = ""
        binding.btnGenres.text = ""
        binding.btnTv.text = ""
        binding.btnSettings.text = ""
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

    private fun createListMovieModelFromTvItem(itemTv: ChannelTVModel): ListMovieModel.Movie {
        val formattedDate = "2024-11-11"
        return ListMovieModel.Movie(
            adult = false,
            backdrop_path = itemTv?.tvgLogoUrl ?: "",
            first_air_date = formattedDate,
            genre_ids = listOf(2024),
            id = 5,
            name = itemTv.tvgName ?: "",
            origin_country = listOf(),
            original_language = "",
            original_name = "",
            original_title = "",
            overview = "",
            popularity = 0.0,
            poster_path = itemTv?.tvgLogoUrl ?: "",
            release_date = formattedDate,
            title = itemTv?.tvgName ?: "",
            vote_average = 0.0,
            vote_count = 0,
            url = itemTv?.streamUrl ?: "",
            original_title_lowcase = "",
            contentType = "Movie",
            subtitles = listOf(),
            urls_subtitle = listOf()
        )
    }

    private fun createMovieModelFromTvItem(itemTv: ChannelTVModel): MovieModel {
        val formattedDate = "2024-11-11"
        return MovieModel(
            adult = false,
            backdrop_path = itemTv.tvgLogoUrl ?: "",
            first_air_date =formattedDate,
            genre_ids = listOf(2024),
            id = 5,
            name = itemTv?.tvgName ?: "",
            origin_country = listOf(),
            original_language = "",
            original_name = "",
            original_title = "",
            overview = "",
            popularity = 0.0,
            poster_path = itemTv.tvgLogoUrl ?: "",
            release_date = formattedDate,
            title = itemTv.tvgName ?: "",
            vote_average = 0.0,
            vote_count = 0,
            url = itemTv.streamUrl ?: "",
            original_title_lowcase = "",
            contentType = "Movie",
            subtitles = listOf(),
            urls_subtitle = listOf(),
            isTvLink = true
        )
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