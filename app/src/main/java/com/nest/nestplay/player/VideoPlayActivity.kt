package com.nest.nestplay.player

import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Button
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.DefaultTimeBar
import com.google.android.exoplayer2.ui.PlayerView
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.nest.nestplay.R
import com.nest.nestplay.fragments.ListEpisodesFragment
import com.nest.nestplay.model.Genres
import com.nest.nestplay.model.ListEpisodesModel
import com.nest.nestplay.model.MovieModel
import com.nest.nestplay.model.TimeModel
import com.nest.nestplay.model.UserModel
import com.nest.nestplay.utils.Common
import kotlin.math.ceil

class VideoPlayActivity2: FragmentActivity() {
    private val handler = Handler(Looper.getMainLooper())

    private var runnable: Runnable? = null

    private var movieDate:  MovieModel? = null
    private var sesonMovieId = ""

    private val handlerScreenVerif = Handler(Looper.getMainLooper())
    private var runnableScreenVerify: Runnable? = null

    private lateinit var playerView: PlayerView
    private lateinit var exoPlayer: ExoPlayer
    private lateinit var videoTitle: TextView
    private lateinit var videoCover: ImageView
    private lateinit var pauseVideoPlayImage: ImageView
    private lateinit var videoSubtitle: TextView
    private lateinit var loadingSpinner: LinearLayout
    private lateinit var conteinerplayvideoview: LinearLayout

    private lateinit var btnViewFit: Button
    private lateinit var btnViewFill: Button
    private lateinit var btnViewZoom: Button

    private lateinit var fastForwardIndicatorView: View
    private lateinit var rewindIndicatorView: View
    private lateinit var currentMovie: MovieModel
    private lateinit var mediaItemBuilder: MediaItem.Builder
    private lateinit var timeBar: DefaultTimeBar
    var episodesList = mutableListOf<ListEpisodesModel>()

    private lateinit var bottomSettinsg: ConstraintLayout
    var isVisiblebottomSettinsg = false


    private var isFirstLoad = true
    private var tojumpLong = true

    private var resizeModes = listOf(
        AspectRatioFrameLayout.RESIZE_MODE_FIT,
        AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH,
        AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT,
        AspectRatioFrameLayout.RESIZE_MODE_FILL,
        AspectRatioFrameLayout.RESIZE_MODE_ZOOM
    )
    private var firstSelectSepareteItem = true
    private var firstRequestMovie = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_play)


        videoSubtitle = findViewById(R.id.video_subtitle)
        videoTitle = findViewById(R.id.video_title)
        videoCover = findViewById(R.id.video_cover)
        playerView = findViewById(R.id.player_view)
        loadingSpinner = findViewById(R.id.animation_loading_video_view)
        bottomSettinsg = findViewById(R.id.bottom_view_settings_video)
        pauseVideoPlayImage = findViewById(R.id.playimage_video)
        conteinerplayvideoview = findViewById(R.id.playvideo_view)

        timeBar = findViewById(R.id.exo_progress)

        btnViewFit = findViewById(R.id.fit_video)
        btnViewFill = findViewById(R.id.fill_video)
        btnViewZoom = findViewById(R.id.zoom_video)

        fastForwardIndicatorView = findViewById(R.id.fast_forward_indicator)
        rewindIndicatorView = findViewById(R.id.rewind_indicator)
        updateAndVerifyScreens(false)
        val separateItemsEpsides = findViewById<GridLayout>(R.id.video_episodes_separete_series)

        val data = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("movie", MovieModel::class.java )
        } else {
            intent.getParcelableExtra<MovieModel>("movie")
        }
        sesonMovieId = Common.getDeviceInformation(this)

        movieDate = data
        playerView.resizeMode = resizeModes[0]
        btnViewFit.isActivated = true

        btnViewFit.setOnClickListener {
            playerView.resizeMode = resizeModes[0]
            btnViewFit.isActivated = true
            btnViewFill.isActivated = false
            btnViewZoom.isActivated = false
        }
        btnViewFill.setOnClickListener {
            playerView.resizeMode = resizeModes[3]
            btnViewFit.isActivated = false
            btnViewFill.isActivated = true
            btnViewZoom.isActivated = false
        }

        btnViewZoom.setOnClickListener {
            playerView.resizeMode = resizeModes[4]
            btnViewFit.isActivated = false
            btnViewFill.isActivated = false
            btnViewZoom.isActivated = true
        }



        data?.let {
//            initializePlayer(data)
            loadMovieInfo(it)

            if(data?.listEpsodes != null){
                episodesList = data.listEpsodes!!
                playerView.hideController()
                val totalepInSeason =  movieDate?.listEpsodes!!.size
                var total_items_separetes = 0
                if (totalepInSeason > 0) {
                    val episodesPerBlock = 20
                    val renderEpsodeNumbers = ceil(totalepInSeason.toDouble() / episodesPerBlock).toInt()
                    for (blockNumber in 1..renderEpsodeNumbers) {

                        val initialEp = (blockNumber - 1) * episodesPerBlock + 1
                        var lastEp = blockNumber * episodesPerBlock
                        if (lastEp > totalepInSeason) {
                            lastEp = totalepInSeason
                        }
                        val button = Button(this)
                        button.text = "$initialEp - $lastEp"
                        val params = GridLayout.LayoutParams().apply {
                            setMargins(3,3,3,3)
                        }
                        button.layoutParams = params
                        button.setBackgroundResource(R.drawable.btn_selector_keybord)
                        button.setTextColor(Color.WHITE)
                        button.setOnFocusChangeListener { v, hasFocus ->
                            if(hasFocus){
                                try {
                                    handleButtonClick("$initialEp - $lastEp")
                                    button.setTextColor(Color.BLACK)
                                } catch (e: Exception) {
                                    println(e)
                                }
                            }else{
                                button.setTextColor(Color.WHITE)
                            }
                        }
                        total_items_separetes = total_items_separetes + 1
                        separateItemsEpsides.addView(button)
                        if(blockNumber == 1 && firstSelectSepareteItem){
                            button.requestFocus()
                            firstSelectSepareteItem = false
                        }
                    }
                }
                separateItemsEpsides.columnCount = total_items_separetes
                handleButtonClick("1 - 20")
            }else{
                separateItemsEpsides.visibility = View.GONE
                val textEpisoes = findViewById<TextView>(R.id.epidoesTextVide)
                val EpisodesList = findViewById<GridLayout>(R.id.video_episodes_series)
                textEpisoes.visibility = View.GONE
                EpisodesList.visibility = View.GONE
            }
        }
        runnableScreenVerify = object : Runnable {
            override fun run() {
                if(data != null){
                    updateAndVerifyScreens(false)
                    handlerScreenVerif.postDelayed(this, 7 * 60 * 1000)
                }
            }
        }
        handlerScreenVerif.post(runnableScreenVerify!!)
    }

    private fun initializePlayer(content: MovieModel?) {
        println("INICIOU PLAYER")
        try {
            exoPlayer = ExoPlayer.Builder(this).build()
            playerView.player = exoPlayer
                val mediaItem = MediaItem.fromUri(Uri.parse(content?.url))

                exoPlayer.setMediaItem(mediaItem)
                exoPlayer.prepare()
                exoPlayer.playWhenReady = true

            timeBar.setKeyTimeIncrement(15000)
            println(playerView.defaultArtwork)
            exoPlayer.addListener(object : Player.Listener {
                override fun onIsLoadingChanged(isLoading: Boolean) {
//                    loadingSpinner.visibility = if (isLoading) View.VISIBLE else View.GONE
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    loadingSpinner.visibility = when (playbackState) {
                        Player.STATE_BUFFERING -> View.VISIBLE
                        Player.STATE_READY, Player.STATE_ENDED -> View.GONE
                        else -> loadingSpinner.visibility
                    }
                    if(playbackState == Player.EVENT_PLAYER_ERROR){

                    }
                    if (playbackState == Player.STATE_ENDED) {
                        val nextEpisode = getNextEpisode()
                        if (nextEpisode != null) {
                            movieDate?.current_ep = nextEpisode.ep_number
                            movieDate?.url = Common.decrypt(nextEpisode.url)
                            movieDate?.subtitles = nextEpisode?.subtitles
                            playerView.hideController()
                            exoPlayer.release()
                            loadingSpinner.visibility = View.VISIBLE
                            loadMovieInfo(movieDate)
                            initializePlayer(movieDate)
                            bottomSettinsg.visibility = View.GONE
                            conteinerplayvideoview.visibility = View.VISIBLE
                            playerView.showController()
                            isVisiblebottomSettinsg = false
                        }
                    }
                }

                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    if (isPlaying) {
                        playerView.hideController()
                    }
                }

                override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                    if (playbackState == Player.STATE_READY && isFirstLoad) {
                        if (isFirstLoad && content?.isTvLink == false) {
                            isFirstLoad = false
                            GeteCurrentTime(null, content, true)
                            runnable = object : Runnable {
                                override fun run() {
                                    val currentPosition = exoPlayer.currentPosition
                                    if(content?.isTvLink == false){
                                        if(currentPosition > 60000){
                                            GeteCurrentTime(currentPosition, content, null)
                                        }
                                        handler.postDelayed(this, 5 * 60 * 1000)
                                    }
                                }
                            }
                            handler.post(runnable!!)
                        }
                    }
                }

            })
        }catch (e: Exception){
            println(e)
            Common.errorModal(this, "Erro ao reproduzir conteudo", "Não foi possível reproduzir o conteúdo, tente novamente")
        }

    }
    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        val currentPosition = exoPlayer.currentPosition
        return when (keyCode) {
            KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                if(!isVisiblebottomSettinsg) {
                    togglePlayPause()
                }
                true
            }
            KeyEvent.KEYCODE_DPAD_RIGHT -> {
                if(!isVisiblebottomSettinsg) {
                    skipForward(currentPosition)
                }
                true
            }
            KeyEvent.KEYCODE_DPAD_LEFT -> {
                if(!isVisiblebottomSettinsg){
                    skiRewind(currentPosition)
                }
                true
            }
            KeyEvent.KEYCODE_DPAD_UP -> {
                if(!isVisiblebottomSettinsg) {
                    playerView.showController()
                }
                true
            }
            KeyEvent.KEYCODE_DPAD_DOWN -> {
                    conteinerplayvideoview.visibility = View.GONE
                    bottomSettinsg.visibility = View.VISIBLE
                    if(movieDate?.contentType == "Movie" && firstRequestMovie){
                        btnViewFit.requestFocus()
                        firstRequestMovie = false
                    }
                    isVisiblebottomSettinsg = true
                    true
                }
            KeyEvent.KEYCODE_BACK -> {
                if(isVisiblebottomSettinsg) {
                    bottomSettinsg.visibility = View.GONE
                    conteinerplayvideoview.visibility = View.VISIBLE
                    playerView.showController()
                    isVisiblebottomSettinsg = false
                    firstSelectSepareteItem = true
                    firstRequestMovie = true
                    true
                }
                if (playerView.isControllerVisible) {
                    playerView.hideController()
                    true
                } else {
                    super.onKeyUp(keyCode, event)
                }
            }
            else -> super.onKeyUp(keyCode, event)
        }
    }


    fun loadMovieInfo(movie: MovieModel?) {
        if(movie != null){
            currentMovie = movie
        }

        if(movie?.contentType == "Serie"){
            videoTitle.text = movie?.name
            if (movie?.current_ep != 0 || movie?.current_ep != null){
                videoTitle.text  = videoTitle.text  as String? + " Ep: ${movie.current_ep}"
            }
        }else{
            videoTitle.text  = movie?.title
        }
        if(movie != null) {
            val genresList = Genres.genres.filter { it?.id in movie.genre_ids }
            val genresNames = genresList.joinToString(" ● ") { it.name }
            var date_release = movie?.release_date
            if(movie?.contentType == "Serie"){
                date_release = movie?.first_air_date
            }
            videoSubtitle.text = "$genresNames - ${date_release?.slice(0..3)}"
            if(movie?.season != null && movie?.season != 0 && movie?.contentType == "Serie"){
                videoTitle.text = videoTitle.text as String + " (Temp: ${movie?.season})"
            }
        }
        videoTitle.text
        var urlImageBG: String?

        if(movie?.backdrop_path?.startsWith("http") == true){
            urlImageBG = movie?.backdrop_path.toString()
        }
        else{
            urlImageBG = "https://image.tmdb.org/t/p/w780" + movie?.backdrop_path
        }

        println(urlImageBG)
        Glide.with(this)
            .asBitmap()
            .load(urlImageBG)
            .into(videoCover)
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

    private fun loadSubtitle(subtitleUrl: String) {
        val subtitleMediaItem = MediaItem.fromUri(subtitleUrl)
        exoPlayer.addMediaItem(subtitleMediaItem)
        exoPlayer.prepare()
        if (!exoPlayer.isPlaying) {
            exoPlayer.play()
        }
    }
    private fun disableSubtitle() {
        exoPlayer.clearVideoSurface()
    }
    fun getUrlSubtitleByIndex(index: Int, urlsSubtitles: List<String>): String? {
        if (index in 0 until urlsSubtitles.size) {
            return urlsSubtitles[index]
        }
        return null
    }

    private fun togglePlayPause() {
        val currentPosition = exoPlayer.currentPosition
        if (exoPlayer.isPlaying) {
            if(movieDate != null && currentPosition > 60000 && tojumpLong){
                tojumpLong = false
                ResetToJumLong()
                GeteCurrentTime(currentPosition, movieDate!!, null)
            }
            pauseVideoPlayImage.visibility = View.VISIBLE
            exoPlayer.pause()
        } else {
            pauseVideoPlayImage.visibility = View.GONE
            playerView.hideController()
            exoPlayer.play()
        }
    }

    private fun getNextEpisode(): ListEpisodesModel? {
        val currentIndex = episodesList.indexOfFirst { it.ep_number == movieDate?.current_ep }
        return if (currentIndex != -1 && currentIndex < episodesList.size - 1) {
            episodesList[currentIndex + 1]
        } else {
            null
        }
    }

    private fun handleButtonClick(item: String) {
        val EpisodesList = findViewById<GridLayout>(R.id.video_episodes_series)
        EpisodesList.removeAllViews()
        val rangeValues = item.split(" - ")
        val startEp = rangeValues[0].toInt()
        val endEp = rangeValues[1].toInt()
        var total_items = 0
        val episodesListRender = episodesList.subList(startEp - 1, endEp.coerceAtMost(episodesList.size))
        try{
            for (item in 1..episodesListRender.size) {
                val episode = episodesListRender[item - 1]
                val button = Button(this)
                button.text = episode.ep_number.toString()
                val params = GridLayout.LayoutParams().apply {
                    setMargins(3,3,3,3)
                }
                button.layoutParams = params
                button.setBackgroundResource(R.drawable.btn_selector_keybord)
                if(episode.ep_number == currentMovie?.current_ep){
                    button.setBackgroundResource(R.drawable.selected_active_btn)
                }else{
                    button.setBackgroundResource(R.drawable.btn_selector_keybord)
                }
                button.setOnFocusChangeListener() { v, hasFocus ->
                    if(hasFocus){
                        button.setBackgroundResource(R.drawable.btn_selector_keybord)
                        button.setTextColor(Color.BLACK)
                    }else{
                        if(episode.ep_number == currentMovie?.current_ep){
                            button.setTextColor(Color.WHITE)
                            button.setBackgroundResource(R.drawable.selected_active_btn)
                        }else{
                            button.setTextColor(Color.WHITE)
                            button.setBackgroundResource(R.drawable.btn_selector_keybord)
                        }
                    }
                }
                button.setOnClickListener {
                    if(movieDate?.current_ep != episode.ep_number){
                        movieDate?.current_ep = episode.ep_number
                        movieDate?.url = Common.decrypt(episode.url)
                        movieDate?.subtitles = episode?.subtitles
                        playerView.hideController()
                        exoPlayer.release()
                        loadingSpinner.visibility = View.VISIBLE
                        loadMovieInfo(movieDate)
                        initializePlayer(movieDate)
                        bottomSettinsg.visibility = View.GONE
                        conteinerplayvideoview.visibility = View.VISIBLE
                        playerView.showController()
                        isVisiblebottomSettinsg = false
                    }
                }
                total_items += 1
                EpisodesList.addView(button)
            }
            EpisodesList.columnCount = total_items
        }catch (e: Exception){
            println(e)
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
                        println(user?.currentScreens)

                        val currentScreens = user?.currentScreens?.filter { it != sesonMovieId } ?: emptyList()
                        val csSize = currentScreens.size
                        val screensAvailables = user?.screensAvailables ?: 0

                        var listSeassonDate = mutableListOf<String>()

                        for (screen in currentScreens){
                            if(sesonMovieId != screen){
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
                                Toast.makeText(this, "Máximo de telas simultâneas excedido", Toast.LENGTH_LONG).show()
                                this.finish()
                            }
                        }
                    }

            }
        }
    }

    fun ResetToJumLong() {
        Handler(Looper.getMainLooper()).postDelayed({
            tojumpLong = true
        }, 60000)
    }

    private fun skipForward(currentPosition: Long) {
        animatedIndicator(fastForwardIndicatorView)
        val seekPosition = currentPosition + 15_000L
        exoPlayer.seekTo(seekPosition)
    }

    private fun skiRewind(currentPosition: Long) {
        animatedIndicator(rewindIndicatorView)
        val seekPosition = currentPosition - 15_000L
        exoPlayer.seekTo(seekPosition)
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
                            exoPlayer.seekTo(firstContent?.current_time)
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

    private fun addFragmentEpisodeos(similarFragment: ListEpisodesFragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.add(R.id.video_episodes_series, similarFragment)
        transaction.commit()
    }

    override fun onStart() {
        super.onStart()
        initializePlayer(movieDate)
    }


    override fun onDestroy() {
        super.onDestroy()
        exoPlayer.release()
        isFirstLoad = true
        runnable?.let {
            handler.removeCallbacks(it)
        }
        runnable = null
        tojumpLong = true
        runnableScreenVerify?.let {
            handlerScreenVerif.removeCallbacks(it)
        }

        runnableScreenVerify = null
    }
    override fun onStop() {
        super.onStop()
        if(tojumpLong && exoPlayer.currentPosition > 60000){
            GeteCurrentTime(exoPlayer.currentPosition, movieDate!!, null)
        }
        exoPlayer.release()
        isFirstLoad = true
        runnable?.let {
            handler.removeCallbacks(it)
        }
        runnable = null
        tojumpLong = true
        runnableScreenVerify?.let {
            handlerScreenVerif.removeCallbacks(it)
        }

        runnableScreenVerify = null
        updateAndVerifyScreens(true)
    }

}