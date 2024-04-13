package com.nest.nestplay

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.text.HtmlCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentActivity
import com.bumptech.glide.Glide
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import com.nest.nestplay.databinding.ActivityDetailMovieBinding
import com.nest.nestplay.fragments.ListEpisodesFragment
import com.nest.nestplay.model.Genres
import com.nest.nestplay.model.ListEpisodesModel
import com.nest.nestplay.model.MovieModel
import com.nest.nestplay.utils.Common

class DetailMovieActivity: FragmentActivity() {

    lateinit var binding: ActivityDetailMovieBinding
    val SimilarFragment = ListFragment()
    val EpisodesListFragment = ListEpisodesFragment()

    var detailsResponse: MovieModel? = null
    var lastEpisode: ListEpisodesModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_detail_movie)
        setContentView(binding.root)

        addFragment(SimilarFragment)
        addFragmentEpisodeos(EpisodesListFragment)


        EpisodesListFragment.setOnItemClickListener { movie ->
            val intent = Intent(this, VideoPlayActivity::class.java)
            detailsResponse?.url = movie.url
            detailsResponse?.current_ep = movie?.ep_number
            detailsResponse?.season = movie?.season
            intent.putExtra("movie", detailsResponse)
            startActivity(intent)
        }

        SimilarFragment.setOnItemDetailClickListener {movie ->
            val intent = Intent(this, DetailMovieActivity::class.java)
            intent.putExtra("id", movie.id)
            startActivity(intent)
        }

        val movieId = intent.getIntExtra("id", 0)

        GetMovie(movieId)
        GeteCurrentTime(movieId)
        binding.moreEpisodesSerie.visibility = View.GONE
        binding.showMore.setOnClickListener {

            val subTitle = binding.subtitleDetail.text.toString()
            val descripton = binding.descriptionDetail.text.toString()

            val titleVideo = binding.titleDetail.text.toString()

            Common.descriptionDialog(this, titleVideo,subTitle, descripton )
        }

        binding.play.setOnClickListener {
            if(lastEpisode != null && detailsResponse?.contentType == "Serie"){
                if(lastEpisode != null){
                    detailsResponse?.url = lastEpisode!!.url
                    detailsResponse?.current_ep = lastEpisode!!.ep_number
                }
            }
            val intent = Intent(this, VideoPlayActivity::class.java)
            intent.putExtra("movie", detailsResponse)
            startActivity(intent)
        }

    }

    private fun GetMovie(id_movie: Int) {
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
                            GetEpsodeos(movie.id)
                            binding.moreLikeThis.nextFocusForwardId = binding.similarMoviesDetaillist.id
                            binding.moreEpisodesSerie.visibility = View.VISIBLE
                            binding.similarMoviesDetaillist.setPadding(0, 10, 0, 0)
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
                        Glide.with(this)
                            .load(movie.poster_path)
                            .into(binding.imgBannerDetail)

                        GetSimilar(movie.genre_ids.get(0), id_movie)
                    }
                }
            }

    }

    private fun GetEpsodeos(id_movie: Int){
        val db =  Firebase.firestore
        val docRef = db.collection("epsodes_series")
        docRef
            .whereEqualTo("id_content", id_movie)
            .orderBy("ep_number", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener {documents ->
                val episodesList = mutableListOf<ListEpisodesModel>()
                val firstDocument = documents.firstOrNull()
                if (firstDocument != null) {
                    val firstMovie = firstDocument.toObject(ListEpisodesModel::class.java)
                    lastEpisode = firstMovie
                }
                for (document in documents){
                    if(document != null){
                        val epsode = document.toObject(ListEpisodesModel::class.java)
                        episodesList.add(epsode)
                        println(document)
                    }
                }
                if(episodesList?.isEmpty()!!){
                    binding.moreEpisodesSerie.visibility = View.GONE
                    binding.similarMoviesDetaillist.setPadding(0, 0, 0, 0)
                }
                if (EpisodesListFragment != null) {
                    EpisodesListFragment.bindData(episodesList,"Episódios")
                } else {
                }
            }
            .addOnFailureListener {
                println(it)
                binding.moreEpisodesSerie.visibility = View.GONE
                binding.similarMoviesDetaillist.setPadding(0, 0, 0, 0)
            }
    }

    private fun GetSimilar(genre:Int, id_movie: Int){
        val db =  Firebase.firestore
        val docRef = db.collection("catalog")
        docRef
            .whereArrayContains("genre_ids", genre)
            .limit(13)
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
                } else {
                    Log.e("GetSimilar", "SimilarFragment is null")
                }
            }
    }

    fun GeteCurrentTime(movieId: Int){
        val db = Firebase.firestore
        val currentUser = Firebase.auth.currentUser
        val docRef = db.collection("content_watch")
        println("GeteCurrentTime em execução")
        currentUser?.let { user ->
            docRef
                .whereEqualTo("user_id", user.uid)
                .whereEqualTo("content_id", movieId)
                .get()
                .addOnSuccessListener { documents ->
                    val firstDocument = documents.firstOrNull()
                    if(firstDocument != null){
                        binding.play.setText("Continuar assistindo")
                    }else{
                        binding.play.setText("Assistir")
                    }
                }
                .addOnFailureListener {
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
}