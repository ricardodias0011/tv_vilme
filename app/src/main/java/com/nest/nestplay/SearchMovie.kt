package com.nest.nestplay

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.GridLayout
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.Firebase
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.firestore
import com.nest.nestplay.adpters.MoviesListAdpter
import com.nest.nestplay.databinding.ActivitySearchMovieBinding
import com.nest.nestplay.model.ListMovieModel

class SearchMovie : FragmentActivity() {


    var URLPATHIMAGE = "https://image.tmdb.org/t/p/w500"

    private lateinit var binding: ActivitySearchMovieBinding
    private lateinit var adpterMovie: MoviesListAdpter
    private var listMovies: MutableList<ListMovieModel.Movie> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchMovieBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val recyclerViewMoviesList = binding.searchMoviesList
        recyclerViewMoviesList.layoutManager = GridLayoutManager(this, 4)
        recyclerViewMoviesList.setHasFixedSize(true)
        adpterMovie = MoviesListAdpter(this, listMovies)
        recyclerViewMoviesList.adapter = adpterMovie

        val keyboardTopLayout = findViewById<GridLayout>(R.id.keyboard_top)
        val keyboardLayout = findViewById<GridLayout>(R.id.keyboard)
        val keyboardButtonsTop = listOf(
            "ESPAÇO", "APAGAR"
        )
        val keyboardButtons = listOf(
            "a", "b", "c", "d", "e", "f",
            "g", "h", "i", "j", "k", "l",
            "m", "n", "o", "p", "q", "r",
            "s", "t", "u", "v", "w", "x",
            "y", "z", "1", "2", "3", "4",
            "5", "6", "7", "8", "9", "0"
        )
        for (buttonTopLabel in keyboardButtonsTop) {
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
            keyboardTopLayout.addView(button)
        }

        for (buttonLabel in keyboardButtons) {
            val button = Button(this)
            button.text = buttonLabel
            val params = GridLayout.LayoutParams().apply {
                width = resources.getDimensionPixelSize(R.dimen.button_width)
                height = resources.getDimensionPixelSize(R.dimen.button_height)
                setMargins(3,3,3,3)
            }
            button.isAllCaps = false
            button.layoutParams = params
            button.setBackgroundResource(R.drawable.btn_selector_keybord)
            button.setTextColor(Color.WHITE)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                button.isFocusedByDefault = true
            }
            button.setOnFocusChangeListener { v, hasFocus ->
                if(hasFocus){
                    button.setTextColor(Color.BLACK)
                }else{
                    button.setTextColor(Color.WHITE)
                }
            }

            button.setOnClickListener {
                handleButtonClick(buttonLabel)
            }
            keyboardLayout.addView(button)
        }
        GetSearchMovies("")
    }

    private fun GetSearchMovies(query: String) {
        val queryLowerCase = query.capitalize()
        listMovies.clear()
        fetchMoviesAndUpdateList()
            .whereGreaterThanOrEqualTo("title", queryLowerCase)
            .whereLessThanOrEqualTo("title", queryLowerCase + "\uf8ff")
            .limit(12)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    if(document != null){
                        val movie = document.toObject(ListMovieModel.Movie::class.java)
                        movie.poster_path = URLPATHIMAGE + movie.poster_path
                        listMovies.add(movie)
                    }
                }
                adpterMovie.notifyDataSetChanged()
            }
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
            else -> text + buttonLabel
        }
        GetSearchMovies(newText)
        editText.setText(newText)
    }


}