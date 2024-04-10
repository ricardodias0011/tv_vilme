package com.nest.nestplay

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.core.text.HtmlCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentActivity
import com.bumptech.glide.Glide
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.nest.nestplay.databinding.ActivityDetailMovieBinding
import com.nest.nestplay.model.Genres
import com.nest.nestplay.model.MovieModel
import com.nest.nestplay.utils.Common

class DetailMovieActivity: FragmentActivity() {

    lateinit var binding: ActivityDetailMovieBinding
    val SimilarFragment = ListFragment()

    var detailsResponse: MovieModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_detail_movie)
        setContentView(binding.root)

        addFragment(SimilarFragment)

        SimilarFragment.setOnItemDetailClickListener {movie ->
            val intent = Intent(this, DetailMovieActivity::class.java)
            intent.putExtra("id", movie.id)
            startActivity(intent)
        }

        val movieId = intent.getIntExtra("id", 0)

        GetMovie(movieId)

        binding.showMore.setOnClickListener {

            val subTitle = binding.subtitleDetail.text.toString()
            val descripton = binding.descriptionDetail.text.toString()

            val titleVideo = binding.titleDetail.text.toString()

            Common.descriptionDialog(this, titleVideo,subTitle, descripton )
        }

        binding.play.setOnClickListener {
            val urlVideo = binding.urlLinkVideo.text.toString()
            val urlOverview = binding.subtitleDetail.text.toString()
            val bannerVideo = binding.imgBannerDetail
            val titleVideo = binding.titleDetail.text.toString()

            val intent = Intent(this, VideoPlayActivity::class.java)
            intent.putExtra("movie", detailsResponse)
            startActivity(intent)
//
//            val playbackActivity = PlaybackFragment()
//            playbackActivity.setVideoUrl(PlayVideoModel(
//                url = urlVideo,
//                overview = urlOverview,
//                title = titleVideo,
//                backdrop_path = bannerVideo.tag as? String ?: ""
//            ))
//            supportFragmentManager.beginTransaction()
//                .replace(android.R.id.content, playbackActivity)
//                .commit()
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

    private fun addFragment(similarFragment: ListFragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.add(R.id.similarMoviesDetaillist, similarFragment)
        transaction.commit()
    }
}