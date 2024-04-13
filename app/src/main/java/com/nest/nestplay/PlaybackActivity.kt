package com.nest.nestplay

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.leanback.app.VideoSupportFragment
import androidx.leanback.app.VideoSupportFragmentGlueHost
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.nest.nestplay.model.MovieModel
import com.nest.nestplay.model.TimeModel
import com.nest.nestplay.player.BasicMediaPlayerAdpter
import com.nest.nestplay.player.CustomTransportControlGlue

class PlaybackFragment : VideoSupportFragment() {

    private lateinit var transporGlue: CustomTransportControlGlue
    private val handler = Handler(Looper.getMainLooper())

    private var runnable: Runnable? = null

    private val jumpHandler = Handler(Looper.getMainLooper())
    private var jumpRunnable: Runnable? = null
    private var tojump = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        val data = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable("movie", MovieModel::class.java)
        } else {
            arguments?.getParcelable<MovieModel>("movie")
        }

        val playerAdapter = BasicMediaPlayerAdpter(requireContext())
        playerAdapter.setOnPreparedListener {
            if (data != null) {
                GeteCurrentTime(null, data, true)
            } else {
            }
        }


        transporGlue = CustomTransportControlGlue(
            context = requireContext(),
            playerAdpter = BasicMediaPlayerAdpter(requireContext())
        )


        transporGlue.host = VideoSupportFragmentGlueHost(this)

        transporGlue.playerAdapter.setDataSource(Uri.parse(data?.url))
        println(data?.url)
        transporGlue.loadMovieInfo(data)

        transporGlue.playerAdapter.play()

        runnable = object : Runnable {
            val currentPosition = transporGlue.getCurrentPosition()
            override fun run() {
                if(data != null && currentPosition > 30000){
                    println("ENABLE ${currentPosition}")
                    GeteCurrentTime(currentPosition, data, null)
                    handler.postDelayed(this, 5 * 60 * 1000)
                }
            }
        }
        handler.post(runnable!!)

        jumpRunnable = object : Runnable {
            val currentPosition = transporGlue.getCurrentPosition()
            override fun run() {
                if(data != null && currentPosition > 1){
                    if (!tojump) {
                        GeteCurrentTime(null, data, true)
                        jumpHandler.postDelayed(this, 3000)
                        tojump = true
                    }
                }
            }
        }
        jumpHandler.post(jumpRunnable!!)

        setOnKeyInterceptListener { view, keyCode, event ->
            val currentPosition = transporGlue.getCurrentPosition()
            if(isControlsOverlayVisible || event.repeatCount > 0 ){
                if(data != null && currentPosition > 30000){
                    println("TESTE GeteCurrentTime chamado")
//                    GeteCurrentTime(currentPosition, data, null)
                }
            } else when(keyCode){

            }
            transporGlue.onKey(view, keyCode, event)
        }
    }

    fun GeteCurrentTime(time: Long?, movie: MovieModel, only_get: Boolean?){
        val db = Firebase.firestore
        val currentUser = Firebase.auth.currentUser



        currentUser?.let { user ->
            var query =  db.collection("content_watch")
                .whereEqualTo("user_id", user.uid)
                .whereEqualTo("content_id", movie.id)

            if (movie?.contentType == "Serie") {
                query = query
                    .whereEqualTo("episode", movie?.current_ep)
                    .whereEqualTo("season", movie?.season)
            }
            query
                .get()
                .addOnSuccessListener { documents ->
                    val firstDocument = documents.firstOrNull()
                    if(firstDocument != null && only_get == true){
                        val firstContent = firstDocument.toObject(TimeModel::class.java)
                        if(firstContent?.current_time != null){
                            transporGlue.startVideoAtTime(firstContent?.current_time)
                        }
                    }
                    if (firstDocument != null && only_get != true) {
                        if(firstDocument != null &&  firstDocument?.id != null){
                            updateCurrentTime(user.uid, movie, time, firstDocument.id)
                        }
                    }
                    if(firstDocument == null && only_get != true){
                        updateCurrentTime(user.uid, movie, time, null)
                    }
                }
                .addOnFailureListener {
                    updateCurrentTime(user.uid, movie, time, null)
                }

        }
    }

    fun updateCurrentTime(userId: String, movie: MovieModel, currentTime: Long?, id: String?){
        println("Atualizando o tempo atual para o usuário $userId e filme ${movie.id}.")

        val db = Firebase.firestore
        val docRef = db.collection("content_watch")
        val now = Timestamp.now()
        if(id != null){
            docRef.document(id)
                .update(
                    mapOf(
                        "current_time" to currentTime,
                        "updatedAt" to now
                    )
                )
                .addOnSuccessListener {
                    println("Tempo atualizado com sucesso para o documento com ID: $id")
                }
                .addOnFailureListener { exception ->
                    println("Erro ao atualizar tempo atual para o documento com ID: $id. Exceção: $exception")
                }
        } else {
            val newItem = hashMapOf(
                "user_id" to userId,
                "content_id" to movie.id,
                "current_time" to currentTime,
                "createdAt" to now,
                "updatedAt" to now
            )

            if (movie?.contentType == "Serie") {
                newItem["episode"] = movie?.current_ep
                newItem["season"] = movie?.season
            }

            docRef
                .add(newItem)
                .addOnSuccessListener { documentReference ->
                    println("Novo item adicionado com sucesso com ID: ${documentReference.id}")
                }
                .addOnFailureListener { exception ->
                    println("Erro ao adicionar novo item. Exceção: $exception")
                }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        jumpRunnable?.let {
            jumpHandler.removeCallbacks(it)
        }
        runnable?.let {
            handler.removeCallbacks(it)
        }
    }
}
