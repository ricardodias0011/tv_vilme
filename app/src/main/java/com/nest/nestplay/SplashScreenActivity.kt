package com.nest.nestplay

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.FragmentActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.gson.Gson
import com.nest.nestplay.databinding.ActivitySplashScreenBinding
import com.nest.nestplay.model.UserModel
import com.nest.nestplay.utils.Common
import com.nest.nestplay.utils.Constants
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class SplashScreenActivity: FragmentActivity() {
    init {
        System.loadLibrary("api-keys")
    }
//    external fun getKeys() : String
    external fun getKeys(): String
    lateinit var loadingDialog: Dialog
    private val READ_EXTERNAL_STORAGE_REQUEST_CODE = 123
    private val STORAGE_PERMISSION_REQUEST_CODE = 123
    private lateinit var binding: ActivitySplashScreenBinding


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)


        Constants.KEY_D = getKeys()

        loadingDialog = Common.loadingDialog(this)
        try{
            Glide.with(this)
                .load("https://playnestvilme.s3.us-east-2.amazonaws.com/banner-tv.jpg")
                .transition(DrawableTransitionOptions.withCrossFade())
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(binding.imageCoverSplash)
        }catch (e:Exception){
            println(e)
        }

        var today = Date()
        val ntpServerUrl = "https://worldtimeapi.org/api/timezone/America/Sao_Paulo"

        try {
            val url = URL(ntpServerUrl)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                val inputStream = conn.inputStream
                val textResponse = inputStream.bufferedReader().use { it.readText() }
                val currentTimeJson = textResponse
                val currentTime = parseDateTimeFromJson(currentTimeJson)
                if(currentTime != null){
                    today = currentTime
                }
                verifyUser(currentTime)
            }
        } catch (e: Exception) {
            verifyUser(today)
        }

    }

    fun verifyUser(today: Date) {
        val auth = Firebase.auth
        if (auth.currentUser != null) {
            val db = Firebase.firestore
            val docRef = db.collection("users")
            auth.uid?.let {
                docRef
                    .document(it)
                    .get()
                    .addOnSuccessListener { document ->
                        try{
                            val user = document.toObject(UserModel::class.java)
                            val sharedPreferences = applicationContext.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                            val editor = sharedPreferences.edit()
                            val gson = Gson()
                            val userJson = gson.toJson(document.toObject(UserModel::class.java))
                            if (userJson != null) {
                                val userJsonMap = gson.fromJson(userJson, Map::class.java) as MutableMap<String, Any>
                                userJsonMap["id"] = document.id
                                val modifiedUserJson = gson.toJson(userJsonMap)
                                editor.putString("user", modifiedUserJson)
                                editor.apply()
                            }
                            val expirePlanDate = user?.expirePlanDate?.toDate()

                            if (expirePlanDate != null) {
                                val timeZone = TimeZone.getTimeZone("America/Sao_Paulo")
                                val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                                sdf.timeZone = timeZone
                                val expirePlanDateString = sdf.format(expirePlanDate)
                                val todayString = sdf.format(today)

                                if (user != null && expirePlanDateString < todayString) {
                                    val i = Intent(this, PaymentRequiredActivity::class.java)
                                    i.putExtra("user", user)
                                    startActivity(i)
                                    finish()
                                } else{
                                    Handler(Looper.getMainLooper()).postDelayed({
                                        val i = Intent(this, HomeActivity::class.java)
                                        startActivity(i)
                                        finish()
                                    }, 5000)
                                }
                            }
                        }catch (e:Exception){
                        }

                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "", Toast.LENGTH_LONG).show()
                    }
            }


        } else {
            Handler(Looper.getMainLooper()).postDelayed({
                val i = Intent(this, MainActivity::class.java)
                i.putExtra("date", today)
                startActivity(i)
                finish()
            }, 3000)
        }
    }

    fun parseDateTimeFromJson(json: String): Date {
        val jsonObject = JSONObject(json)
        val dateTimeString = jsonObject.getString("utc_datetime")

        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        return sdf.parse(dateTimeString) ?: Date()
    }
}