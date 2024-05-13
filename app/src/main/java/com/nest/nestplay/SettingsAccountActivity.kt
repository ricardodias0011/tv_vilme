package com.nest.nestplay

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import com.google.gson.Gson
import com.nest.nestplay.databinding.ActivitySettingsAccountBinding
import com.nest.nestplay.model.FavsMovie
import com.nest.nestplay.model.ListMovieModel
import com.nest.nestplay.model.TimeModel
import com.nest.nestplay.model.UserModel
import com.nest.nestplay.utils.Common
import java.text.SimpleDateFormat
import java.util.Date

class SettingsAccountActivity : FragmentActivity() {
    private lateinit var binding: ActivitySettingsAccountBinding
    val MoviesListFragment = ListFragment()
    var URLPATHIMAGE = "https://image.tmdb.org/t/p/w500"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportFragmentManager.beginTransaction()
            .replace(R.id.historicMoviesListSettings, MoviesListFragment)
            .commit()


        val firebaseAuth = FirebaseAuth.getInstance()
        val user = firebaseAuth.currentUser
        if (user != null) {
            val db = Firebase.firestore
            val docRef = db.collection("users")
            binding.settingsUserName.text = user.email
            user.uid?.let {
                docRef
                    .document(it)
                    .get()
                    .addOnSuccessListener { document ->
                        var currentUser = document.toObject(UserModel::class.java)
                        if (currentUser != null) {
                            binding.settingsUserName.text = "Olá, ${currentUser.name}"
                                currentUser.expirePlanDate?.let { timestamp ->
                                    val expirePlanDateTime = Date(timestamp.seconds * 1000)
                                    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
                                    val expirePlanDateString = sdf.format(expirePlanDateTime)
                                    var textExpirePlan = "Plano expira"
                                    if (expirePlanDateTime < Date()){
                                        textExpirePlan = "Plano expirou"
                                    }
                                    binding.validAccountDate.text = "$textExpirePlan em ${expirePlanDateString}"
                                }
                        }

                    }
            }


        }
        GetRecentsWatchList()
        GetMovieListFavs()
        binding.authLogout.setOnClickListener {
            authLogout()
        }

    }

    private fun authLogout(){
        val firebaseAuth = FirebaseAuth.getInstance()
        val user = firebaseAuth.currentUser
        user?.let { currentUser ->
            currentUser.getIdToken(true)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        firebaseAuth.signOut()
                        val intent = Intent(this, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    } else {
                    }
                }
        }
    }

    private fun GetMovieListFavs() {
        val db = Firebase.firestore
        val docRef = db.collection("content_favs")
        val currentUser = Firebase.auth.currentUser

        docRef
            .whereEqualTo("user_id", currentUser?.uid)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { querySnapshots ->
                val FavList = mutableListOf<Int>()
                val FavDates = mutableListOf<Timestamp>()
                for (document in querySnapshots) {
                    if (document != null) {
                        val fav = document.toObject(FavsMovie::class.java)
                        FavList.add(fav.content_id)
                        FavDates.add(fav.createdAt)
                    }
                }
                val docRefMovieMyList = db.collection("catalog")
                docRefMovieMyList
                    .whereIn("id", FavList)
                .get()
                    .addOnSuccessListener { querySnapshots ->
                        val moviesMylist = mutableListOf<ListMovieModel.Movie>()
                        for (document in querySnapshots) {
                            if (document != null) {
                                val movie = document.toObject(ListMovieModel.Movie::class.java)
                                movie.poster_path = URLPATHIMAGE + movie.poster_path
                                moviesMylist.add(movie)
                            }
                        }
                        moviesMylist.sortByDescending { movie ->
                            val index = FavList.indexOf(movie.id)
                            if (index != -1 && index < FavDates.size) {
                                FavDates[index].toDate()
                            } else {
                                Date(0)
                            }
                        }
                        MoviesListFragment.bindData(ListMovieModel(moviesMylist.toMutableList(), "Minha lista"))
                    }
            }
            .addOnFailureListener { it
                if(it.message == Common.msgPermissionDENIED){
                    println("msgPermissionDENIED")
                    accessDenied()
                }
            }
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
                    if(movieList.isNotEmpty()){
                        GetRecentsWatchMovies(movieList, movieListDate)
                    }
                }
        }
    }
    private fun GetRecentsWatchMovies(list: List<Int>, dates: List<Timestamp>) {
        val db = Firebase.firestore
        val query = db.collection("catalog")
        query
            .whereIn("id", list)
            .limit(20)
            .get()
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

                MoviesListFragment.bindData(ListMovieModel(movieDatePairs.toMutableList(), "Histórico"))
            }
            .addOnFailureListener { it
                if(it.message == Common.msgPermissionDENIED){
                    accessDenied()
                }
            }
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