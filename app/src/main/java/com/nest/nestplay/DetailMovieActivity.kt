package com.nest.nestplay

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.GridLayout
import android.widget.Toast
import androidx.core.text.HtmlCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentContainerView
import com.bumptech.glide.Glide
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import com.google.gson.Gson
import com.nest.nestplay.databinding.ActivityDetailMovieBinding
import com.nest.nestplay.model.Genres
import com.nest.nestplay.model.ListEpisodesModel
import com.nest.nestplay.model.MovieModel
import com.nest.nestplay.model.TimeModel
import com.nest.nestplay.model.UserModel
import com.nest.nestplay.player.VideoPlayActivity2
import com.nest.nestplay.utils.Common
import com.nest.nestplay.utils.Constants
import java.util.Date
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.math.ceil

class DetailMovieActivity: FragmentActivity() {
    init {
        System.loadLibrary("api-keys")
    }
    external fun getKeys() : String

    lateinit var binding: ActivityDetailMovieBinding
    lateinit var loadingDialog: Dialog

    var loadInfosContentFavsMovies = true

    val SimilarFragment = ListFragment()
//    val EpisodesListFragment = ListEpisodesFragment()
    var detailsResponse: MovieModel? = null
    var lastEpisode: ListEpisodesModel? = null
    var firstEpisode: ListEpisodesModel? = null

    var wasEncrypted: Boolean = false
    //    btn_tv
    var movieId: Int = 0

    val episodesList = mutableListOf<ListEpisodesModel>()

    var epsodesIsLoader = false
    var similarListIsLoader = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_detail_movie)
        setContentView(binding.root)
        movieId = intent.getIntExtra("id", 0)

        Constants.KEY_D = getKeys()

        val user = getCurrentUser()
        var today = Date()
        if(user?.expirePlanDate?.toDate()?.before(today) == true){
            navigateFromAccessDenied(user)
        }

        addFragment(SimilarFragment)
        wasEncrypted = false

        loadingDialog = Common.loadingDialog(this)

        SimilarFragment.setOnItemDetailClickListener {movie ->
            val intent = Intent(this, DetailMovieActivity::class.java)
            intent.putExtra("id", movie.id)
            startActivity(intent)
        }


        ContentInFavs(movieId)
        binding.playBeginning.visibility = View.VISIBLE
        binding.horizontalScrollViewEpsodes.visibility = View.GONE
        binding.spinnerSelectTemp.visibility = View.GONE
        binding.spinnerSelectEpNumber.visibility = View.GONE
        binding.showMore.setOnClickListener {

            val subTitle = binding.subtitleDetail.text.toString()
            val descripton = binding.descriptionDetail.text.toString()

            val titleVideo = binding.titleDetail.text.toString()

            Common.descriptionDialog(this, titleVideo,subTitle, descripton )
        }

        binding.playBeginning.setOnClickListener {
            onClickPlayMovie(true)
        }

        binding.play.setOnClickListener {
            onClickPlayMovie(false)
        }

        binding.moreLikeThis.setOnClickListener {
            addOrRemoveContentInFavs(detailsResponse!!.id)
        }

    }

    fun onClickPlayMovie(beginning: Boolean?) {
        if (detailsResponse?.contentType == "Serie"){
            wasEncrypted = false
        }
        if(lastEpisode != null && detailsResponse?.contentType == "Serie"){
            detailsResponse?.url = decrypt(lastEpisode!!.url, Constants.KEY_D)
            detailsResponse?.current_ep = lastEpisode!!.ep_number
            detailsResponse?.idEpsode = lastEpisode!!.idEpsode
            detailsResponse?.listEpsodes = episodesList
            detailsResponse?.season = lastEpisode?.season
        }else {
            if(detailsResponse?.url != null){
                detailsResponse?.url =  decrypt(detailsResponse!!.url, Constants.KEY_D)
            }
        }
        if(beginning === true || (lastEpisode == null && detailsResponse?.contentType == "Serie")){
            if(detailsResponse?.contentType == "Serie"){
                wasEncrypted = false
                detailsResponse?.url = decrypt(firstEpisode!!.url, Constants.KEY_D)
                detailsResponse?.current_ep = firstEpisode!!.ep_number
                detailsResponse?.idEpsode = firstEpisode!!.idEpsode
                detailsResponse?.season = firstEpisode?.season
                detailsResponse?.listEpsodes = episodesList
            }
            detailsResponse?.beginningStart  = true
        }
        if(detailsResponse?.url == null || detailsResponse?.url?.length!! < 2){
            Toast.makeText(this,"Não foi possivel reproduzir conteúdo", Toast.LENGTH_LONG).show()
        }else{
//            val intent = Intent(this, VideoPlayActivity::class.java)
            val intent = Intent(this, VideoPlayActivity2::class.java)
            intent.putExtra("movie", detailsResponse)
            startActivity(intent)
        }
    }

    fun addHeightViewListMovie() {
        val fragmentContainer = findViewById<FragmentContainerView>(R.id.similarMoviesDetaillist)
        val contentHeight = SimilarFragment?.view?.measuredHeight ?: 0
        val params = fragmentContainer.layoutParams
        params.height = contentHeight + 100

        fragmentContainer.layoutParams = params
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
    fun decrypt(msg: String, password: String): String {
        var urlDecodeString = ""
        if(wasEncrypted == false){
            try {
                val key32Char = hexStringToByteArray(password)
                val iv32Char = hexStringToByteArray(password)
                val secretKeySpec = SecretKeySpec(key32Char, "AES")
                val ivParameterSpec = IvParameterSpec(iv32Char)
                val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
                cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec)
                val dstBuff = cipher.doFinal(Base64.decode(msg, Base64.DEFAULT))
                urlDecodeString = String(dstBuff)
                wasEncrypted = true
            }catch (ex: Exception) {
                ex.printStackTrace()
            }
        }else {
            urlDecodeString = msg
        }
        return urlDecodeString
    }

    private fun GetMovie(id_movie: Int) {
        loadingDialog.show()
        val db =  Firebase.firestore
        val docRef = db.collection("catalog")
        docRef
            .whereEqualTo("id", id_movie)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents){
                    if(document != null){
                        val movie = document.toObject(MovieModel::class.java)
                        GeteCurrentTime(movie)
                        if(movie?.contentType == "Serie"){
                            binding.moreLikeThis.nextFocusForwardId = binding.spinnerSelectEpNumber.id
                            binding.horizontalScrollViewEpsodes.visibility = View.VISIBLE
                            binding.spinnerSelectEpNumber.visibility = View.VISIBLE
                            binding.spinnerSelectTemp.visibility = View.VISIBLE
                        }
                        movie.poster_path = "https://image.tmdb.org/t/p/w1280" + movie.backdrop_path
                        var date = ""

                        if(movie?.contentType == "Serie" && movie.first_air_date != null){
                            binding.titleDetail.setText(movie?.name)
                            date = movie.first_air_date
                        }else{
                            binding.titleDetail.setText(movie?.title)
                            date = movie.release_date
                        }
                        binding.descriptionDetail.setText(movie.overview)
                        val genresList = Genres.genres.filter { it.id in movie.genre_ids }
                        val genresNames = genresList.joinToString(" ● ") { "<font color='#08B44E'> ${it.name}</font>" }

                        binding.subtitleDetail.text = HtmlCompat.fromHtml("$genresNames - ${date.slice(0..3)}", HtmlCompat.FROM_HTML_MODE_LEGACY)
                        detailsResponse = movie
                        binding.urlLinkVideo.setText(movie?.url)
                        loadingDialog.dismiss()
                        Glide.with(this)
                            .load(movie.poster_path)
                            .into(binding.imgBannerDetail)
                        if(!similarListIsLoader){
                            GetSimilar(movie.genre_ids.get(0),id_movie)
                            similarListIsLoader = true
                        }


                    }
                }
            }
            .addOnFailureListener { it
                if(it.message == Common.msgPermissionDENIED){
                    accessDenied()
                }
            }
    }


    private fun GetSeassons(movie: MovieModel?, season: Int?) {
        val totalSeasons = movie?.total_seasons
        val listTemps = mutableListOf<String>()

        if (totalSeasons != null) {
            for (item in 1..totalSeasons) {
                listTemps.add("Temporada $item")
            }
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, listTemps)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerSelectTemp.adapter = adapter

        if (season != null && season >= 1 && season <= totalSeasons!!) {
            binding.spinnerSelectTemp.setSelection(season - 1)
        }

        binding.spinnerSelectTemp.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedSeason = position + 1
                if (movie != null) {
                    GetEpsodeos(movie.id, selectedSeason)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Implemente conforme necessário
            }
        }
    }


    private fun GetEpsodeos(id_movie: Int, seasseon: Number?){
        loadingDialog.show()
        episodesList.clear()
//        EpisodesListFragment.clearAll()
        val db =  Firebase.firestore
        val docRef = db.collection("epsodes_series")
        docRef
            .whereEqualTo("id_content", id_movie)
            .whereEqualTo("season", seasseon)
            .orderBy("ep_number", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener {documents ->
                val firstDocument = documents.firstOrNull()
                val getfirstMovie = firstDocument?.toObject(ListEpisodesModel::class.java)
                if (getfirstMovie != null) {
                    firstEpisode = getfirstMovie
                    firstEpisode?.idEpsode = firstDocument.id
                }
                for (document in documents){
                    if(document != null){
                        val epsode = document.toObject(ListEpisodesModel::class.java)
                        epsode?.idEpsode = document.id
                        episodesList.add(epsode)
                    }
                }
                loadingDialog.hide()

                renderEpides(episodesList)

                getEpsodesListForCount(documents.count())
            }
            .addOnFailureListener {
                loadingDialog.hide()
                binding.horizontalScrollViewEpsodes.visibility = View.GONE
                binding.similarMoviesDetaillist.setPadding(0, 0, 0, 0)
                if(it.message == Common.msgPermissionDENIED){
                    accessDenied()
                }
            }
    }

    private fun renderEpides(episodes: List<ListEpisodesModel>) {
        var total_items = 0
        val EpisodesLists = findViewById<GridLayout>(R.id.details_episodes_series)
        EpisodesLists.removeAllViews()

        for (epsode in episodes){
            val episode = epsode
            val button = Button(this)
            button.id = epsode.ep_number + epsode?.season!! * 1000
            button.text = episode.ep_number.toString()
            val params = GridLayout.LayoutParams().apply {
                setMargins(3,3,3,3)
            }
            button.layoutParams = params
            button.setBackgroundResource(R.drawable.btn_selector_keybord)
            if(episode.ep_number == lastEpisode?.ep_number && episode.season == lastEpisode?.season){
                button.setBackgroundResource(R.drawable.selected_active_btn)
                button.setTextColor(Color.WHITE)
            }else{
                button.setBackgroundResource(R.drawable.btn_selector_keybord)
            }
            button.setOnFocusChangeListener() { v, hasFocus ->
                if(hasFocus){
                    button.setBackgroundResource(R.drawable.btn_selector_keybord)
                    button.setTextColor(Color.BLACK)
                }else{
                    if(episode.ep_number == lastEpisode?.ep_number && episode.season == lastEpisode?.season){
                        button.setTextColor(Color.WHITE)
                        button.setBackgroundResource(R.drawable.selected_active_btn)
                    }else{
                        button.setTextColor(Color.WHITE)
                        button.setBackgroundResource(R.drawable.btn_selector_keybord)
                    }
                }
            }
            button.setOnClickListener {
                if(detailsResponse?.current_ep == episode.ep_number){
                    detailsResponse?.beginningStart = true
                }

                val intent = Intent(this, VideoPlayActivity2::class.java)
                wasEncrypted = false
                detailsResponse?.url = decrypt(epsode.url, Constants.KEY_D)
                detailsResponse?.current_ep = epsode?.ep_number
                detailsResponse?.season = epsode?.season
                detailsResponse?.urls_subtitle = epsode?.urls_subtitle
                detailsResponse?.subtitles = epsode?.subtitles
                detailsResponse?.idEpsode = epsode?.idEpsode
                detailsResponse?.listEpsodes = episodesList
                if(detailsResponse?.url == null || detailsResponse?.url?.length!! < 2){
                    Toast.makeText(this,"Não foi possivel reproduzir conteúdo", Toast.LENGTH_LONG).show()
                }else{
                    intent.putExtra("movie", detailsResponse)
                    startActivity(intent)
                }
            }
            total_items += 1
            EpisodesLists.addView(button)
        }
        EpisodesLists.columnCount = total_items
    }

    private fun getEpsodesListForCount(totalepInSeason: Int,) {

        val listEpNumbers = mutableListOf<String>()

        if (totalepInSeason > 0) {
            val episodesPerBlock = 20
            val renderEpsodeNumbers = ceil(totalepInSeason.toDouble() / episodesPerBlock).toInt()
            for (blockNumber in 1..renderEpsodeNumbers) {
                val initialEp = (blockNumber - 1) * episodesPerBlock + 1
                var lastEp = blockNumber * episodesPerBlock
                if (lastEp > totalepInSeason) {
                    lastEp = totalepInSeason
                }
                listEpNumbers.add("Episódios $initialEp - $lastEp")
            }
        }
        val adapterSelectSpinnerEp_numbers = ArrayAdapter(this, android.R.layout.simple_spinner_item, listEpNumbers)
        adapterSelectSpinnerEp_numbers.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        binding.spinnerSelectEpNumber.adapter = adapterSelectSpinnerEp_numbers

        binding.spinnerSelectEpNumber.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedRange = listEpNumbers[position]
                val rangeValues = selectedRange.substringAfter("Episódios ").split(" - ")
                val startEp = rangeValues[0].toInt()
                val endEp = rangeValues[1].toInt()

//                EpisodesListFragment.clearAll()
                    val episodesListRender = episodesList.subList(startEp - 1, endEp)
                if (episodesListRender.isEmpty()) {
                    binding.horizontalScrollViewEpsodes.visibility = View.GONE
                } else {
                    binding.horizontalScrollViewEpsodes.visibility = View.VISIBLE
                    renderEpides(episodesListRender.toList())
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

        }
    }

    private fun GetSimilar(genre:Int, id_movie: Int){
        if( loadInfosContentFavsMovies == false){
            return
        }
        val db =  Firebase.firestore
        val docRef = db.collection("catalog")
        var query = docRef.whereArrayContains("genre_ids", genre)
        query
            .limit(14)
            .get()
            .addOnSuccessListener {documents ->
                val movieList = mutableListOf<MovieModel>()
                for (document in documents){
                    if(document != null){
                        val movie = document.toObject(MovieModel::class.java)
                        movie.poster_path = "https://image.tmdb.org/t/p/w500" + movie.poster_path
                        if (movie.id != id_movie) {
                            movieList.add(movie)
                        }
                    }
                }
                if (SimilarFragment != null) {
                    SimilarFragment.bindMovieData(movieList,"Você também pode gostar")
                    addHeightViewListMovie()
                    loadInfosContentFavsMovies = false
                } else {
                    Log.e("GetSimilar", "SimilarFragment is null")
                }
            }
            .addOnFailureListener {
                if(it.message == Common.msgPermissionDENIED){
                    accessDenied()
                }
            }
    }

    fun GeteCurrentTime(movie: MovieModel){
        val db = Firebase.firestore
        val currentUser = Firebase.auth.currentUser
        val docRef = db.collection("content_watch")
        currentUser?.let { user ->
            docRef
                .whereEqualTo("user_id", user.uid)
                .whereEqualTo("content_id", movieId)
                .orderBy("updatedAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener { documents ->
                    val firstDocument = documents.firstOrNull()
                    if(firstDocument != null){
                        binding.playBeginning.visibility = View.VISIBLE
                        val time = firstDocument.toObject(TimeModel::class.java)
                        binding.play.text = time?.episode?.takeIf { it != 0 }?.let { "Continuar assistindo E: $it | T: ${time?.season}" } ?: "Continuar assistindo"
                        if(time?.episode != null) {
                            detailsResponse?.current_ep = time.episode
                            detailsResponse?.current_ep = time.episode
                            if(!epsodesIsLoader){
                                GetSeassons(movie, time?.season)
                                epsodesIsLoader = true
                            }
                            val docRef = db.collection("epsodes_series")
                            docRef
                                .whereEqualTo("id_content", movieId)
                                .whereEqualTo("ep_number", time.episode)
                                .whereEqualTo("season", time?.season)
                                .get()
                                .addOnSuccessListener { documents ->
                                    val firstDocument = documents.firstOrNull()
                                    val getLastEpsode =  firstDocument?.toObject(ListEpisodesModel::class.java)
                                    if (getLastEpsode != null) {
                                        try {
                                            val EpisodesLists = findViewById<GridLayout>(R.id.details_episodes_series)
                                            if(lastEpisode != null){
                                                val idbtn2 = lastEpisode!!.ep_number + lastEpisode!!.season * 1000
                                                for (i in 0 until EpisodesLists?.childCount!!) {
                                                    val button = EpisodesLists.getChildAt(i) as Button
                                                    if (button.id == idbtn2) {
                                                        button.setBackgroundResource(R.drawable.btn_selector_keybord)
                                                        break
                                                    }
                                                }
                                            }

                                            lastEpisode = getLastEpsode
                                            val idbtn = lastEpisode!!.ep_number + lastEpisode!!.season * 1000  // Correspondendo ao valor da tag
                                            for (i in 0 until EpisodesLists?.childCount!!) {
                                                val button = EpisodesLists.getChildAt(i) as Button
                                                if (button.id == idbtn) {
                                                    button.setBackgroundResource(R.drawable.selected_active_btn)
                                                    break
                                                }
                                            }
                                        }catch (e: Exception){
                                            lastEpisode = getLastEpsode
                                        }
                                    }
                                }
                        }
                    }
                    else{
                        binding.playBeginning.visibility = View.GONE
                        binding.play.setText("Assistir")
                        if(!epsodesIsLoader && movie.contentType == "Serie"){
                            GetSeassons(movie, 1)
                            epsodesIsLoader = true
                        }
                    }
                }
                .addOnFailureListener {
                    if(it.message == Common.msgPermissionDENIED){
                        accessDenied()
                    }
                }

        }
    }


    fun addOrRemoveContentInFavs(movieId: Int){
        val db = Firebase.firestore
        val currentUser = Firebase.auth.currentUser
        val docRef = db.collection("content_favs")
        currentUser?.let { user ->
            docRef
                .whereEqualTo("user_id", user.uid)
                .whereEqualTo("content_id", movieId)
                .get()
                .addOnSuccessListener { documents ->
                    val firstDocument = documents.firstOrNull()
                    if(firstDocument != null){

                        docRef
                            .document(firstDocument.id).delete()
                            .addOnSuccessListener { documentReference ->
                                Toast.makeText(applicationContext, "Conteúdo removido da Minha lista", Toast.LENGTH_LONG).show()
                                binding.moreLikeThis.setText("Adicionar à Minha lista")
                            }
                            .addOnFailureListener { exception ->
                                binding.moreLikeThis.setText("Remover da Minha lista")
                            }
                    }else{
                        val newItem = hashMapOf(
                            "user_id" to user.uid,
                            "content_id" to movieId.toInt(),
                            "createdAt" to Date()
                        )
                        docRef
                            .add(newItem)
                            .addOnSuccessListener { documentReference ->
                                Toast.makeText(applicationContext, "Conteúdo adicionado à Minha lista", Toast.LENGTH_LONG).show()
                                binding.moreLikeThis.setText("Remover da Minha lista")
                            }
                            .addOnFailureListener { exception ->
                                binding.moreLikeThis.setText("Adicionar à Minha lista")
                            }
                    }
                }
                .addOnFailureListener {
                }

        }
    }


    private fun ContentInFavs(movieId: Int){
        val db = Firebase.firestore
        val currentUser = Firebase.auth.currentUser
        val docRef = db.collection("content_favs")
        currentUser?.let { user ->
            docRef
                .whereEqualTo("user_id", user.uid)
                .whereEqualTo("content_id", movieId)
                .get()
                .addOnSuccessListener { documents ->
                    val firstDocument = documents.firstOrNull()
                    if(firstDocument != null){
                        binding.moreLikeThis.setText("Remover da Minha lista")
                    }else{
                        binding.moreLikeThis.setText("Adicionar à Minha lista")
                    }
                }
                .addOnFailureListener {
                    binding.moreLikeThis.setText("Adicionar à Minha lista")
                }

        }
    }

    private fun addFragment(similarFragment: ListFragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.add(R.id.similarMoviesDetaillist, similarFragment)
        transaction.commit()
    }
//    private fun addFragmentEpisodeos(similarFragment: ListEpisodesFragment) {
//        val transaction = supportFragmentManager.beginTransaction()
//        transaction.add(R.id.more_episodes_serie, similarFragment)
//        transaction.commit()
//    }
    override fun onDestroy() {
        super.onDestroy()
        wasEncrypted = false
        loadInfosContentFavsMovies = true
        loadingDialog.hide()
    }

    fun getCurrentUser(): UserModel? {
        val sharedPreferences = applicationContext.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val gson = Gson()
        val userJson = sharedPreferences.getString("user", null)
        val user = gson.fromJson(userJson, UserModel::class.java)

        return user
    }

    fun navigateFromAccessDenied(user: UserModel) {
        val i = Intent(this, PaymentRequiredActivity::class.java)
        i.putExtra("user", user)
        startActivity(i)
        finish()
    }

    fun accessDenied () {
        val user = getCurrentUser()
        if(user != null){
            navigateFromAccessDenied(user)
        }
    }

    override fun onResume() {
        super.onResume()
        wasEncrypted = false
        ContentInFavs(movieId)
        GetMovie(movieId)
        if(detailsResponse?.contentType == "Serie"){
            Handler(Looper.getMainLooper()).postDelayed({
                GeteCurrentTime(detailsResponse!!)
            }, 1000)
        }
    }
}