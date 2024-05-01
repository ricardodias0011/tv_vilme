package com.nest.nestplay

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.text.HtmlCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentContainerView
import com.bumptech.glide.Glide
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import com.nest.nestplay.databinding.ActivityDetailMovieBinding
import com.nest.nestplay.fragments.ListEpisodesFragment
import com.nest.nestplay.model.Genres
import com.nest.nestplay.model.ListEpisodesModel
import com.nest.nestplay.model.MovieModel
import com.nest.nestplay.utils.Common
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class DetailMovieActivity: FragmentActivity() {

    lateinit var binding: ActivityDetailMovieBinding
    lateinit var loadingDialog: Dialog

    val SimilarFragment = ListFragment()
    val EpisodesListFragment = ListEpisodesFragment()
    var detailsResponse: MovieModel? = null
    var lastEpisode: ListEpisodesModel? = null
    var firstEpisode: ListEpisodesModel? = null

    var wasEncrypted: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_detail_movie)
        setContentView(binding.root)

        addFragment(SimilarFragment)
        addFragmentEpisodeos(EpisodesListFragment)
        wasEncrypted = false

        loadingDialog = Common.loadingDialog(this)

        EpisodesListFragment.setOnItemClickListener { epsode ->
            val intent = Intent(this, VideoPlayActivity::class.java)
            wasEncrypted = false
            detailsResponse?.url = decrypt(epsode.url)
            detailsResponse?.current_ep = epsode?.ep_number
            detailsResponse?.season = epsode?.season
            detailsResponse?.urls_subtitle = epsode?.urls_subtitle
            detailsResponse?.subtitles = epsode?.subtitles
            detailsResponse?.idEpsode = epsode?.idEpsode
            if(detailsResponse?.url == null || detailsResponse?.url?.length!! < 2){
                Toast.makeText(this,"Não foi possivel reproduzir conteúdo", Toast.LENGTH_LONG)
            }else{
                intent.putExtra("movie", detailsResponse)
                startActivity(intent)
            }
        }

        SimilarFragment.setOnItemDetailClickListener {movie ->
            val intent = Intent(this, DetailMovieActivity::class.java)
            intent.putExtra("id", movie.id)
            startActivity(intent)
        }

        val movieId = intent.getIntExtra("id", 0)

        GetMovie(movieId)
        GeteCurrentTime(movieId)
        ContentInFavs(movieId)
        binding.playBeginning.visibility = View.VISIBLE
        binding.moreEpisodesSerie.visibility = View.GONE
        binding.spinnerSelectTemp.visibility = View.GONE
        binding.episodesText.visibility = View.GONE
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
            detailsResponse?.url = decrypt(lastEpisode!!.url)
            detailsResponse?.current_ep = lastEpisode!!.ep_number
            detailsResponse?.idEpsode = lastEpisode!!.idEpsode
        }else {
            if(detailsResponse?.url != null){
                detailsResponse?.url = decrypt(detailsResponse!!.url)
            }
        }
        if(beginning === true){
            if(detailsResponse?.contentType == "Serie"){
                wasEncrypted = false
                detailsResponse?.url = decrypt(firstEpisode!!.url)
                detailsResponse?.current_ep = firstEpisode!!.ep_number
                detailsResponse?.idEpsode = firstEpisode!!.idEpsode
            }
            detailsResponse?.beginningStart  = true
        }
        if(detailsResponse?.url == null || detailsResponse?.url?.length!! < 2){
            Toast.makeText(this,"Não foi possivel reproduzir conteúdo", Toast.LENGTH_LONG)
        }else{
            val intent = Intent(this, VideoPlayActivity::class.java)
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
    fun decrypt(msg: String, password: String = "770E75DC61635CCC61A1D7D8FFF9D1B0"): String {
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
                        if(movie.contentType == "Serie"){
                            GetSeassons(movie)
                            binding.moreLikeThis.nextFocusForwardId = binding.similarMoviesDetaillist.id
                            binding.moreEpisodesSerie.visibility = View.VISIBLE
                            binding.episodesText.visibility = View.VISIBLE
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

                        GetSimilar(movie.genre_ids.get(0),id_movie)


                    }
                }
            }

    }


    private fun GetSeassons(movie: MovieModel?) {
        val totalSeasons = movie?.total_seasons
        val listTemps = mutableListOf<String>()
        if(totalSeasons != null){
            for (item in 1..totalSeasons) {
                listTemps.add("Temporada $item")
            }
        }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, listTemps)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerSelectTemp.adapter = adapter
        binding.spinnerSelectTemp.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedItem = position + 1
                if(movie != null){
                    GetEpsodeos(movie.id, selectedItem)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }
    }

    private fun GetEpsodeos(id_movie: Int, seasseon: Number?){
        EpisodesListFragment.clearAll()
        val db =  Firebase.firestore
        val docRef = db.collection("epsodes_series")
        docRef
            .whereEqualTo("id_content", id_movie)
            .whereEqualTo("season", seasseon)
            .orderBy("ep_number", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener {documents ->
                val episodesList = mutableListOf<ListEpisodesModel>()
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
                val episodioMaisRecente = episodesList.maxByOrNull { it.last_seen?.seconds ?: 0 }
                if(episodioMaisRecente != null){
                    lastEpisode = episodioMaisRecente
                }else {
                    if (getfirstMovie != null) {
                        lastEpisode = getfirstMovie
                    }
                }
                if(episodesList?.isEmpty()!!){
                    binding.moreEpisodesSerie.visibility = View.GONE
                    binding.episodesText.visibility = View.GONE
                }
                if (EpisodesListFragment != null) {
                    binding.moreEpisodesSerie.visibility = View.VISIBLE
                    binding.episodesText.visibility = View.VISIBLE
                    EpisodesListFragment.bindData(episodesList,"Episódios")
                } else {
                }
            }
            .addOnFailureListener {
                binding.moreEpisodesSerie.visibility = View.GONE
                binding.similarMoviesDetaillist.setPadding(0, 0, 0, 0)
            }
    }

    private fun GetSimilar(genre:Int, id_movie: Int){
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
                } else {
                    Log.e("GetSimilar", "SimilarFragment is null")
                }
            }
    }

    fun GeteCurrentTime(movieId: Int){
        val db = Firebase.firestore
        val currentUser = Firebase.auth.currentUser
        val docRef = db.collection("content_watch")
        currentUser?.let { user ->
            docRef
                .whereEqualTo("user_id", user.uid)
                .whereEqualTo("content_id", movieId)
                .get()
                .addOnSuccessListener { documents ->
                    val firstDocument = documents.firstOrNull()
                    if(firstDocument != null){
                        binding.playBeginning.visibility = View.VISIBLE
                        binding.play.setText("Continuar assistindo")
                    }else{
                        binding.playBeginning.visibility = View.GONE
                        binding.play.setText("Assistir")
                    }
                }
                .addOnFailureListener {
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
                            "createdAt" to Timestamp.now()
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
    private fun addFragmentEpisodeos(similarFragment: ListEpisodesFragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.add(R.id.more_episodes_serie, similarFragment)
        transaction.commit()
    }
    override fun onDestroy() {
        super.onDestroy()
        wasEncrypted = false
        loadingDialog.hide()
    }
}