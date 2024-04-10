package com.nest.nestplay

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
import com.nest.nestplay.model.UserModel
import com.nest.nestplay.utils.Common
import java.util.Date

class SplashScreenActivity: FragmentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
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
                        val expirePlanDate = user?.expirePlanDate?.toDate()?.let { Date(it.time) }
                        val today = Date()
                        if (expirePlanDate != null) {
                            if (user != null && expirePlanDate.before(today)) {
                                Common.playmentRequiredDialog(this, user)
                            } else{
                                Handler(Looper.getMainLooper()).postDelayed({
                                    val i = Intent(this, HomeActivity::class.java)
                                    startActivity(i)
                                    finish()
                                }, 3000)
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
                startActivity(i)
                finish()
            }, 3000)
        }

    }
}