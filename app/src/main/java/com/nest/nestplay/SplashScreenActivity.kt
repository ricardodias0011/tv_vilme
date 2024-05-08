package com.nest.nestplay

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.FragmentActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.gson.Gson
import com.nest.nestplay.model.UserModel
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class SplashScreenActivity: FragmentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        var today = Date()
        val ntpServerUrl = "http://worldtimeapi.org/api/timezone/America/Sao_Paulo"

        try {
            val url = URL(ntpServerUrl)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                val inputStream = conn.inputStream
                val textResponse = inputStream.bufferedReader().use { it.readText() }
                val currentTimeJson = textResponse
                val currentTime = parseDateTimeFromJson(currentTimeJson)
                today = currentTime
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
                        val user = document.toObject(UserModel::class.java)
                        val sharedPreferences = applicationContext.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                        val editor = sharedPreferences.edit()
                        val gson = Gson()
                        val userJson = gson.toJson(document.toObject(UserModel::class.java))
                        editor.putString("user", userJson)
                        editor.apply()
                        val expirePlanDate = user?.expirePlanDate?.toDate()
                        val timeZone = TimeZone.getTimeZone("America/Sao_Paulo")
                        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                        sdf.timeZone = timeZone
                        val expirePlanDateString = sdf.format(expirePlanDate)
                        val todayString = sdf.format(today)

                        if (expirePlanDate != null) {
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
                                }, 1500)
                            }
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Erro", Toast.LENGTH_LONG)
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
        val dateTimeString = jsonObject.getString("datetime")

        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        return sdf.parse(dateTimeString) ?: Date()
    }
}