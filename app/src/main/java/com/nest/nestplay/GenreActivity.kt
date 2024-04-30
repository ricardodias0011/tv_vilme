package com.nest.nestplay

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.Firebase
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.firestore
import com.nest.nestplay.adpters.GenreListAdpter
import com.nest.nestplay.adpters.MoviesListAdpter
import com.nest.nestplay.databinding.ActivityGenreBinding
import com.nest.nestplay.model.Genre
import com.nest.nestplay.model.Genres
import com.nest.nestplay.model.ListMovieModel
import com.nest.nestplay.utils.Common

class GenreActivity : FragmentActivity() {
    private lateinit var binding: ActivityGenreBinding
    var URLPATHIMAGE = "https://image.tmdb.org/t/p/w500"
    private var idGenreSelected: Int = 0
    private lateinit var adpterMovie: MoviesListAdpter
    private lateinit var adpterLintGenre: GenreListAdpter
    private var listMovies: MutableList<ListMovieModel.Movie> = mutableListOf()

    lateinit var loadingDialog: Dialog

    var lastVisible: Int? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGenreBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadingDialog = Common.loadingDialog(this)

        binding.btnSearchGenre.setOnClickListener {
            val intent = Intent(this, SearchMovie::class.java)
            startActivity(intent)
        }

        val recyclerViewMoviesList = binding.favsMoviesList
        recyclerViewMoviesList.layoutManager = GridLayoutManager(this, 5)
        adpterMovie = MoviesListAdpter(this, listMovies)
        recyclerViewMoviesList.adapter = adpterMovie

        adpterMovie.onLastItemFocusChangeListener  = object : MoviesListAdpter.OnLastItemFocusChangeListener {
            override fun onLastItemFocused(movie: ListMovieModel.Movie) {
                GetMovieListFavs(idGenreSelected, false)
            }
        }


        val recyclerViewGenreList = binding.categoryList
        recyclerViewGenreList.layoutManager = LinearLayoutManager(this)
        val genreList = Genres.genres
        adpterLintGenre = GenreListAdpter(this, genreList)
        recyclerViewGenreList.adapter = adpterLintGenre

        adpterLintGenre.onItemClickChangerListener = object : GenreListAdpter.OnItemClickChangerListener {
            override fun onItemClicked(item: Genre) {
                lastVisible = null
                GetMovieListFavs(item.id, true)
                idGenreSelected = item.id
            }
        }

        GetMovieListFavs(0, true)
    }

    private fun GetMovieListFavs(gere: Int, clear: Boolean?) {
        loadingDialog.show()

        if (clear == true) {
            listMovies.clear()
        }
        println(gere)


        var query = fetchMoviesList().limit(10).orderBy("vote_count")
        if (gere != 0) {
            query = query.whereArrayContains("genre_ids", gere)
        }

        if (lastVisible != null && clear == false) {
            query = query.startAfter(lastVisible)
        }

        query.get()
            .addOnSuccessListener { querySnapshots ->
                    if (clear == true) {
                        listMovies.clear()
                    }
                    for (document in querySnapshots) {
                        val movie = document.toObject(ListMovieModel.Movie::class.java)
                        lastVisible = movie.vote_count
                        movie.poster_path = URLPATHIMAGE + movie.poster_path
                        listMovies.add(movie)
                    }
                    adpterMovie.notifyDataSetChanged()
                    println("$lastVisible $clear")
                    if(lastVisible != null && clear == false){
                        adpterMovie.onFocusItem()
                    }
                    loadingDialog.dismiss()
            }
            .addOnFailureListener { e ->
                loadingDialog.dismiss()
            }
    }

    override fun onResume() {
        super.onResume()
        GetMovieListFavs(0, true)
    }
    private fun fetchMoviesList(): CollectionReference {
        val db = Firebase.firestore
        val docRef = db.collection("catalog")
        return docRef
    }
}