package com.nest.nestplay

import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.leanback.app.VideoSupportFragment
import androidx.leanback.app.VideoSupportFragmentGlueHost
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.ClassPresenterSelector
import androidx.leanback.widget.HeaderItem
import androidx.leanback.widget.ListRow
import androidx.leanback.widget.ListRowPresenter
import androidx.leanback.widget.PlaybackControlsRow.PlayPauseAction
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.nest.nestplay.model.ListEpisodesModel
import com.nest.nestplay.model.MovieModel
import com.nest.nestplay.model.TimeModel
import com.nest.nestplay.model.UserModel
import com.nest.nestplay.player.BasicMediaPlayerAdpter
import com.nest.nestplay.player.CustomTransportControlGlue
import com.nest.nestplay.presenter.EpisodeosPresenter
import com.nest.nestplay.utils.Common
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL

class PlaybackFragment : VideoSupportFragment() {

    private lateinit var transporGlue: CustomTransportControlGlue
    private lateinit var fastForwardIndicatorView: View
    private lateinit var rewindIndicatorView: View

    private val handler = Handler(Looper.getMainLooper())

    private var runnable: Runnable? = null
    private var sesonMovieId = ""
    private val handlerScreenVerif = Handler(Looper.getMainLooper())
    private var runnableScreenVerify: Runnable? = null
    private var toVerify = false

    private val jumpHandler = Handler(Looper.getMainLooper())
    private var jumpRunnable: Runnable? = null
    private var tojump = false
    private var movieDate:  MovieModel? = null

    private var tojumpLong = true

    private var CanJumpTime = true
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val data = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable("movie", MovieModel::class.java)
        } else {
            arguments?.getParcelable<MovieModel>("movie")
        }
        updateAndVerifyScreens(false)
        val playerAdapter = BasicMediaPlayerAdpter(requireContext())
        playerAdapter.setOnPreparedListener {
            if (data != null) {
                GeteCurrentTime(null, data, true)
            } else {
            }
        }
        view?.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                CanJumpTime = true
            }
        }
        movieDate = data

        sesonMovieId = Common.getIpAddress(requireContext()).toString()

        transporGlue = CustomTransportControlGlue(
            context = requireContext(),
            playerAdpter = BasicMediaPlayerAdpter(requireContext())
        )
        transporGlue.host = VideoSupportFragmentGlueHost(this)

        transporGlue.playerAdapter.setDataSource(Uri.parse(data?.url))
        transporGlue.loadMovieInfo(data)
        transporGlue.playerAdapter.play()

        var repeatError = 0

        transporGlue.playerAdapter.mediaPlayer.setOnErrorListener { mp, what, extra ->
            repeatError = repeatError + 1
            if(data?.isTvLink == false && repeatError > 2){
                Common.errorModal(
                    requireContext(),
                    "Erro ao reproduzir conteúdo",
                    "Não foi possível reproduzir o conteúdo devido a um erro interno.")
            }
            true
        }
        transporGlue.playerAdapter.setOnPreparedListener{
            repeatError = 0
        }

        transporGlue.playerAdapter.mediaPlayer.setOnCompletionListener {
            if (movieDate?.contentType == "Serie" && data?.isTvLink == false){
                var nextEpisode = data?.listEpsodes?.find { it.ep_number == data.current_ep?.plus(1) }

                if (nextEpisode != null) {
                    println(nextEpisode)
                    movieDate?.current_ep = nextEpisode.ep_number
                    movieDate?.url = Common.decrypt(nextEpisode.url)
                    movieDate?.subtitles = nextEpisode?.subtitles
                    movieDate?.listEpsodes = movieDate?.listEpsodes
                    movieDate?.contentType = "Serie"
                    try {
                        transporGlue.playerAdapter.reset()
                        transporGlue.playerAdapter.setDataSource(Uri.parse(movieDate?.url))
                        transporGlue.loadMovieInfo(movieDate)
                        transporGlue.playerAdapter.play()
                    }catch (e: Exception){
                        println(e)
                    }

                } else {

                }
            }
        }
        runnable = object : Runnable {
            override fun run() {
                val currentPosition = transporGlue.getCurrentPosition()
                if(data != null && data?.isTvLink == false){
                    if(currentPosition > 5000){
                        println("Tempo loop update time: current position ${currentPosition}")
                        GeteCurrentTime(currentPosition, data, null)
                    }
                    handler.postDelayed(this, 4 * 60 * 1000)
                }
            }
        }
        handler.post(runnable!!)
        jumpRunnable = object : Runnable {
            override fun run() {
                val currentPosition = transporGlue.getCurrentPosition()
                if(data != null){
                    if (!tojump && data?.isTvLink == false) {
                        if(currentPosition > 300){
//                            LoadSubtitlesTask().execute("x_e_1.vtt")
                            GeteCurrentTime(null, data, true)
                            tojump = true
                        }
                        jumpHandler.postDelayed(this, 400)
                    }
                }
            }
        }
        jumpHandler.post(jumpRunnable!!)


        runnableScreenVerify = object : Runnable {
            override fun run() {
                if(data != null){
                    if(!toVerify && data?.isTvLink == false) {
                        updateAndVerifyScreens(false)
                        handlerScreenVerif.postDelayed(this, 7 * 60 * 1000)
                    }
                }
            }
        }
        handlerScreenVerif.post(runnable!!)

        setOnKeyInterceptListener { view, keyCode, event ->
            if (CanJumpTime) {
                val currentPosition = transporGlue.getCurrentPosition()
                if(isControlsOverlayVisible || event.repeatCount > 0 ){
                    if(data != null && currentPosition > 30000 && tojumpLong){
                        isShowOrHideControlsOverlayOnUserInteraction = true
                        tojumpLong = false
                        println("Avanço")
                        ResetToJumPLogn()
                        GeteCurrentTime(currentPosition, data, null)
                    }
//                    when(keyCode){
//                        KeyEvent.KEYCODE_DPAD_RIGHT -> {
//                            val newPosition = currentPosition + 10_000
//                            transporGlue.playerAdapter.seekTo(newPosition)
//                        }
//                        KeyEvent.KEYCODE_DPAD_LEFT -> {
//                            val newPosition = currentPosition - 10_000
//                            transporGlue.playerAdapter.seekTo(newPosition)
//                        }
//                    }
                }
                else {
                    when(keyCode){
                        KeyEvent.KEYCODE_DPAD_RIGHT -> {
                            isShowOrHideControlsOverlayOnUserInteraction = event.action != KeyEvent.ACTION_DOWN
                            if(event.action == KeyEvent.ACTION_DOWN){
                                animatedIndicator(fastForwardIndicatorView)
                            }
                        }
                        KeyEvent.KEYCODE_DPAD_LEFT -> {
                            isShowOrHideControlsOverlayOnUserInteraction = event.action != KeyEvent.ACTION_DOWN
                            if(event.action == KeyEvent.ACTION_DOWN){
                                animatedIndicator(rewindIndicatorView)
                            }
                        }
                    }
                }
            }
            transporGlue.onKey(view, keyCode, event)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setOnItemViewSelectedListener { itemViewHolder, item, rowViewHolder, row ->
            println(row)
            try {
                println(item.javaClass.simpleName)
            }catch (e: Exception){
                println(e)
            }

            CanJumpTime = when (item) {
                is ListEpisodesModel, is PlayPauseAction -> false
                else -> true
            }
        }

        setOnItemViewClickedListener { itemViewHolder, item, rowViewHolder, row ->
            if(item is ListEpisodesModel && movieDate?.current_ep != item.ep_number){
                movieDate?.current_ep = item.ep_number
                movieDate?.url = Common.decrypt(item.url)
                movieDate?.subtitles = item?.subtitles
                try {
                    transporGlue.hideControlsOverlay(true)
                    transporGlue.playerAdapter.reset()
                    transporGlue.playerAdapter.setDataSource(Uri.parse(movieDate?.url))
                    transporGlue.loadMovieInfo(movieDate)
                    transporGlue.playerAdapter.play()
                }catch (e: Exception){
                    println(e)
                }
            }
        }

        if(movieDate?.listEpsodes != null){
            (adapter.presenterSelector as ClassPresenterSelector).addClassPresenter(
                ListRow::class.java,
                ListRowPresenter()
            )
            val presenterSelector = ArrayObjectAdapter(EpisodeosPresenter())
            movieDate?.listEpsodes?.forEach {
                presenterSelector.add(it)
            }

            val row = ListRow(1L, HeaderItem("Episódios"), presenterSelector)
            (adapter as ArrayObjectAdapter).add(row)
        }
        false
    }

    private inner class LoadSubtitlesTask() : AsyncTask<String, Void, List<Pair<Long, String>>>() {

        override fun doInBackground(vararg params: String?): List<Pair<Long, String>> {
            val subtitleList = mutableListOf<Pair<Long, String>>()

            try {
                val url = URL(params[0])
                val inputStream = url.openStream()
                val reader = BufferedReader(InputStreamReader(inputStream))

                var line: String?
                var timestamp: Long = 0

                while (reader.readLine().also { line = it } != null) {
                    if (line!!.matches(Regex("\\d{2}:\\d{2}:\\d{2}.\\d{3} --> \\d{2}:\\d{2}:\\d{2}.\\d{3}"))) {
                        val timestamps = line!!.split(" --> ")
                        val startTime = parseTimestamp(timestamps[0])
                        val endTime = parseTimestamp(timestamps[1])
                        timestamp = startTime
                    } else if (line!!.isNotEmpty()) {
                        subtitleList.add(Pair(timestamp, line!!))
                    }
                }

                inputStream.close()
            } catch (e: Exception) {
                e.printStackTrace()
                println(e)
            }

            return subtitleList
        }

        override fun onPostExecute(result: List<Pair<Long, String>>?) {
            result?.let {
                transporGlue.playerAdapter.mediaPlayer.setOnTimedTextListener { mp, text ->
                    val currentPosition = mp.currentPosition.toLong()
                    val currentSubtitle = it.find { it.first <= currentPosition && it.first + 5000 >= currentPosition }
                    currentSubtitle?.let { transporGlue.subtitle = it.second }
                }
            }
        }
    }


    private fun parseTimestamp(timestamp: String): Long {
        val timeComponents = timestamp.split(":")
        val hours = timeComponents[0].toLong() * 3600000
        val minutes = timeComponents[1].toLong() * 60000
        val seconds = (timeComponents[2].substring(0, 2).toLong() * 1000) + timeComponents[2].substring(3).toLong()
        return hours + minutes + seconds
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState) as ViewGroup

        fastForwardIndicatorView = inflater.inflate(R.layout.view_forward, view, false)
        view.addView(fastForwardIndicatorView)

        rewindIndicatorView = inflater.inflate(R.layout.view_rewind, view, false)
        view.addView(rewindIndicatorView)
        return view
    }

    fun animatedIndicator(indicator: View) {
        indicator.animate()
            .withEndAction{
                indicator.isVisible = false
                indicator.alpha = 1F
                indicator.scaleX = 1F
                indicator.scaleY = 1F
            }
            .withStartAction {
                indicator.isVisible = true
            }
            .alpha(0.2F)
            .scaleX(2f)
            .scaleY(2f)
            .setDuration(400)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()
    }

    fun ResetToJumPLogn() {
        Handler(Looper.getMainLooper()).postDelayed({
            tojumpLong = true
        }, 60000)
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
//                    .whereEqualTo("episode", movie?.current_ep)
                    .whereEqualTo("season", movie?.season)
            }
            query
                .get()
                .addOnSuccessListener { documents ->
                    val firstDocument = documents.firstOrNull()
                    if(firstDocument != null && only_get == true){
                        val firstContent = firstDocument.toObject(TimeModel::class.java)
                        val isWorkProxy = "workerproxy"
                        if(firstContent?.current_time != null &&
                            !movie.url.contains(isWorkProxy) &&
                            movie.beginningStart === false &&
                            movie.current_ep == firstContent.episode){
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
        val updateItem = hashMapOf(
            "current_time" to currentTime,
            "updatedAt" to now,
        )
        if (movie?.contentType == "Serie") {
            updateItem["episode"] = movie?.current_ep
        }

        if(id != null){
            docRef.document(id)
                .update(updateItem.toMap())
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
                "updatedAt" to now,
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

    fun updateAndVerifyScreens(remove: Boolean?) {
        val auth = Firebase.auth
        if (auth.currentUser != null) {
            val db = Firebase.firestore
            val docRef = db.collection("users")
            auth.uid?.let {id ->
                docRef
                    .document(id)
                    .get()
                    .addOnSuccessListener{document ->
                        val user = document.toObject(UserModel::class.java)
                        val currentScreens = user?.currentScreens ?: emptyList()
                        val csSize = currentScreens.size
                        val screensAvailables = user?.screensAvailables ?: 0

                        var listSeassonDate = mutableListOf<String>()

                        for (screen in currentScreens){
                            if(sesonMovieId != screen){
                                println(sesonMovieId)
                                println(screen)
                                listSeassonDate.add(screen)
                            }
                        }
                        if(remove == true){
                            val updatedScreens = listSeassonDate.toMutableList().apply {
                                remove(sesonMovieId)
                            }
                            docRef
                                .document(id)
                                .update(
                                    mapOf(
                                        "currentScreens" to updatedScreens
                                    )
                                )
                        }else{
                            if (csSize < screensAvailables) {
                                val updatedScreens = listSeassonDate.toMutableList().apply {
                                    add(sesonMovieId)
                                }
                                docRef
                                    .document(id)
                                    .update(
                                        mapOf(
                                            "currentScreens" to updatedScreens
                                        )
                                    )
                            }else {
                                Toast.makeText(requireContext(), "Máximo de telas simultâneas excedido", Toast.LENGTH_LONG).show()
                                requireActivity().finish()
                            }
                        }
                    }

            }
        }
    }
    override fun onResume() {
        super.onResume()
        updateAndVerifyScreens(false)
    }
    override fun onStop() {
        super.onStop()
        updateAndVerifyScreens(true)
    }
    override fun onDestroy() {
        super.onDestroy()
        updateAndVerifyScreens(true)
        transporGlue.playerAdapter.stop()
        transporGlue.removePlayerCallback(null)
        jumpRunnable?.let {
            jumpHandler.removeCallbacks(it)
        }
        runnable?.let {
            handler.removeCallbacks(it)
        }

        runnableScreenVerify?.let {
            handlerScreenVerif.removeCallbacks(it)
        }

        runnableScreenVerify = null
        runnable = null
        jumpRunnable = null
        tojumpLong = true
        tojump = false
        toVerify = false
    }
}
