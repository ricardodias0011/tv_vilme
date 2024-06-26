package com.nest.nestplay

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.GridLayout
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Firebase
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.firestore
import com.google.gson.Gson
import com.nest.nestplay.adpters.MoviesListAdpter
import com.nest.nestplay.databinding.ActivitySearchMovieBinding
import com.nest.nestplay.model.ListMovieModel
import com.nest.nestplay.model.UserModel
import com.nest.nestplay.utils.Common

class SearchMovie : FragmentActivity() {


    var URLPATHIMAGE = "https://image.tmdb.org/t/p/w500"

    private lateinit var binding: ActivitySearchMovieBinding
    private lateinit var adpterMovie: MoviesListAdpter
    private var listMovies: MutableList<ListMovieModel.Movie> = mutableListOf()
    lateinit var loadingDialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchMovieBinding.inflate(layoutInflater)
        setContentView(binding.root)
        loadingDialog = Common.loadingDialog(this)

        val recyclerViewMoviesList = binding.searchMoviesList
        recyclerViewMoviesList.layoutManager = GridLayoutManager(this, 5)
        adpterMovie = MoviesListAdpter(this, listMovies)
        recyclerViewMoviesList.adapter = adpterMovie

        val keyboardBottomLayout = findViewById<GridLayout>(R.id.keyboard_bottom)
        val keyboardLayout = findViewById<GridLayout>(R.id.keyboard)
        val keyboardButtonsBottom = listOf(
            "ESPAÇO", "APAGAR", "BUSCAR"
        )
        val keyboardButtons = listOf(
            "a", "b", "c", "d", "e", "f",
            "g", "h", "i", "j", "k", "l",
            "m", "n", "o", "p", "q", "r",
            "s", "t", "u", "v", "w", "x",
            "y", "z", "1", "2", "3", "4",
            "5", "6", "7", "8", "9", "0"
        )
        for (buttonTopLabel in keyboardButtonsBottom) {
            val button = Button(this)
            button.text = buttonTopLabel
            val params = GridLayout.LayoutParams().apply {
                setMargins(3,3,3,3)
            }
            button.layoutParams = params
            button.setBackgroundResource(R.drawable.btn_selector_keybord)
            button.setTextColor(Color.WHITE)
            button.setOnFocusChangeListener { v, hasFocus ->
                if(hasFocus){
                    button.setTextColor(Color.BLACK)
                }else{
                    button.setTextColor(Color.WHITE)
                }
            }
            button.setOnClickListener {
                handleButtonClick(buttonTopLabel)
            }
            keyboardBottomLayout.addView(button)
        }

        for (buttonLabel in keyboardButtons) {
            val button = Button(this)
            button.text = buttonLabel.uppercase()
            val params = GridLayout.LayoutParams().apply {
                width = resources.getDimensionPixelSize(R.dimen.button_width)
                height = resources.getDimensionPixelSize(R.dimen.button_height)
                setMargins(3,3,3,3)
            }
            button.isAllCaps = false
            button.layoutParams = params
            button.setBackgroundResource(R.drawable.btn_selector_keybord)
            button.setTextColor(Color.WHITE)
            button.setOnFocusChangeListener { v, hasFocus ->
                if(hasFocus){
                    button.setTextColor(Color.BLACK)
                }else{
                    button.setTextColor(Color.WHITE)
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                button.isFocusedByDefault = true
            }
            if(buttonLabel == "a"){
                button.requestFocus()
            }
            button.setOnClickListener {
                handleButtonClick(buttonLabel)
            }
            keyboardLayout.addView(button)
        }
        GetSearchMovies("")
    }

    private fun toCapitalize(string: String): String{
            return string.split(" ")
                .joinToString(" ") { it.capitalize() }
    }

    private fun GetSearchMovies(query: String) {
        try{
            loadingDialog.show()
        val queryCapitalizeCase = toCapitalize(query)
        println(queryCapitalizeCase)

        val tasks = mutableListOf<Task<QuerySnapshot>>().apply {
            add(fetchMoviesAndUpdateList().whereGreaterThanOrEqualTo("title", queryCapitalizeCase)
                .whereLessThanOrEqualTo("title", queryCapitalizeCase + "\uf8ff").limit(20).get())
            add(fetchMoviesAndUpdateList().whereGreaterThanOrEqualTo("name", queryCapitalizeCase)
                .whereLessThanOrEqualTo("name", queryCapitalizeCase + "\uf8ff").limit(20).get())
            add(fetchMoviesAndUpdateList().whereGreaterThanOrEqualTo("original_title", queryCapitalizeCase)
                .whereLessThanOrEqualTo("original_title", queryCapitalizeCase + "\uf8ff").limit(10).get())
            add(fetchMoviesAndUpdateList().whereGreaterThanOrEqualTo("original_name", queryCapitalizeCase)
                .whereLessThanOrEqualTo("original_name", queryCapitalizeCase + "\uf8ff").limit(10).get())

        }

        listMovies.clear()
        Tasks.whenAllSuccess<QuerySnapshot>(tasks)
            .addOnSuccessListener { querySnapshots ->
                val combinedResults = mutableListOf<DocumentSnapshot>()
                for (snapshot in querySnapshots) {
                    combinedResults.addAll(snapshot.documents)
                }
                listMovies.clear()
                for (document in combinedResults) {
                    if (document != null) {
                        val movie = document.toObject(ListMovieModel.Movie::class.java)
                        if(movie != null){
                            movie.poster_path = URLPATHIMAGE + movie.poster_path
                            if (listMovies.find { _movie -> _movie.id == movie.id } == null) {
                                listMovies.add(movie)
                            }
                        }
                    }
                }
                adpterMovie.notifyDataSetChanged()
                loadingDialog.hide()
            }
            .addOnFailureListener { it
                loadingDialog.hide()
                if(it.message == Common.msgPermissionDENIED){
                    println("msgPermissionDENIED")
                    accessDenied()
                }
            }
        }catch (e:Exception){}
    }

    private fun fetchMoviesAndUpdateList(): CollectionReference {
        val db = Firebase.firestore
        val docRef = db.collection("catalog")
        return docRef
    }

    private fun handleButtonClick(buttonLabel: String) {
        val editText = findViewById<TextView>(R.id.editText)
        val text = editText.text.toString()
        val newText = when (buttonLabel) {
            "APAGAR" -> if (text.isNotEmpty()) text.substring(0, text.length - 1) else ""
            "ESPAÇO" -> text + " "
            "BUSCAR" -> text
            else -> text + buttonLabel
        }
        when (buttonLabel) {
            "BUSCAR" ->  GetSearchMovies(newText)
        }
        editText.setText(newText)
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