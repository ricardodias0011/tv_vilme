package com.nest.nestplay

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.firestore
import com.google.gson.Gson
import com.nest.nestplay.adpters.MoviesListAdpter
import com.nest.nestplay.databinding.ActivityMovieFavBinding
import com.nest.nestplay.model.FavsMovie
import com.nest.nestplay.model.ListMovieModel
import com.nest.nestplay.model.UserModel
import com.nest.nestplay.utils.Common

class MovieFavActivity : FragmentActivity() {

    var URLPATHIMAGE = "https://image.tmdb.org/t/p/w500"

    private lateinit var binding: ActivityMovieFavBinding
    private lateinit var adpterMovie: MoviesListAdpter
    private var listMovies: MutableList<ListMovieModel.Movie> = mutableListOf()

    lateinit var loadingDialog: Dialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMovieFavBinding.inflate(layoutInflater)
        setContentView(binding.root)
        loadingDialog = Common.loadingDialog(this)
        loadingDialog.show()

        val recyclerViewMoviesList = binding.favsMoviesList
        recyclerViewMoviesList.layoutManager = GridLayoutManager(this, 6)
        adpterMovie = MoviesListAdpter(this, listMovies)
        recyclerViewMoviesList.adapter = adpterMovie

//        val recyclerViewGenreList = binding.categoryList
//        recyclerViewGenreList.layoutManager = LinearLayoutManager(this)
//        val genreList = Genres.genres
//        adpterLintGenre = GenreListAdpter(this, genreList)
//        recyclerViewGenreList.adapter = adpterLintGenre

//        adpterLintGenre.onItemClickChangerListener = object : GenreListAdpter.OnItemClickChangerListener {
//            override fun onItemClicked(item: Genre) {
//                GetMovieListFavs(item.id)
//            }
//        }

        GetMovieListFavs()
    }

    private fun GetMovieListFavs() {
        listMovies.clear()
        val db = Firebase.firestore
        val docRef = db.collection("content_favs")
        val currentUser = Firebase.auth.currentUser

        docRef
            .whereEqualTo("user_id", currentUser?.uid)
            .get()
            .addOnSuccessListener { querySnapshots ->
                val FavList = mutableListOf<Int>()
                for (document in querySnapshots) {
                    if (document != null) {
                        val fav = document.toObject(FavsMovie::class.java)
                        FavList.add(fav.content_id)
                    }
                }
                var query = fetchMoviesList().whereIn("id", FavList)
                query.get()
                    .addOnSuccessListener { querySnapshots ->
                        listMovies.clear()
                        for (document in querySnapshots) {
                            if (document != null) {
                                val movie = document.toObject(ListMovieModel.Movie::class.java)
                                movie.poster_path = URLPATHIMAGE + movie.poster_path
                                listMovies.add(movie)
                            }
                        }
                        adpterMovie.notifyDataSetChanged()
                        loadingDialog.dismiss()
                    }
            }
            .addOnFailureListener { it
                if(it.message == Common.msgPermissionDENIED){
                    println("msgPermissionDENIED")
                    accessDenied()
                }
            }
    }
    override fun onResume() {
        super.onResume()
        GetMovieListFavs()
    }
    private fun fetchMoviesList(): CollectionReference {
        val db = Firebase.firestore
        val docRef = db.collection("catalog")
        return docRef
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
}