package com.nest.nestplay

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.google.firebase.Firebase
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.gson.Gson
import com.nest.nestplay.databinding.ActivityMainBinding
import com.nest.nestplay.model.UserModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class MainActivity : FragmentActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth

    private var loading: Boolean = false

    private var failedAttempts = 0
    private lateinit var countdownTimer: CountDownTimer
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        var today = intent.getIntExtra("date", 0)
        val date = if (today != 0) {
            Date(today.toLong())
        } else {
            Date()
        }
        auth = Firebase.auth
        binding.authLoginEnter.setOnClickListener {
            val email = binding.authLoginEmail.text.toString().trim()
            val password = binding.authLoginPassword.text.toString().trim()
            if (!email.isEmpty() && !password.isEmpty()) {
                if (loading == false) {
                    AuthLoginWithEmailAndPassword(email, password, date)
                }
            } else {
                Toast.makeText(applicationContext, "Preencha todos os campos", Toast.LENGTH_LONG)
                    .show()
            }
        }
    }

    private fun AuthLoginWithEmailAndPassword(email: String, password: String, today: Date) {
        binding.authLoginEnter.setText("Carregando...")
        loading = true
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val db = Firebase.firestore
                val docRef = db.collection("users")
                task.result.user?.uid?.let {
                    docRef
                        .document(it)
                        .get()
                        .addOnSuccessListener { document ->
                            failedAttempts = 0
                            val user = document.toObject(UserModel::class.java)
                            println(user)
                            val sharedPreferences = applicationContext.getSharedPreferences(
                                "MyPrefs",
                                Context.MODE_PRIVATE
                            )
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
                                } else {
                                    val i = Intent(this, HomeActivity::class.java)
                                    i.putExtra("data_extra", "")
                                    startActivity(i)
                                    finish()
                                }
                            }
                        }
                }
                loading = false
            } else {
                binding.authLoginEnter.setText("Entrar")
                loading = false
            }
        }.addOnFailureListener {
            failedAttempts++
            loading = false
            binding.authLoginEnter.setText("Entrar")
            if (failedAttempts > 3) {
                lockLoginButton()
            }
            println(it)
            when (it) {
                is FirebaseAuthInvalidCredentialsException -> {
                    Toast.makeText(this, "Email ou senha incorretos.", Toast.LENGTH_LONG).show()
                }

                is FirebaseTooManyRequestsException -> {
                    Toast.makeText(
                        this,
                        "Muitas tentativas de login. Tente novamente mais tarde.",
                        Toast.LENGTH_LONG
                    ).show()
                }

                else -> {
                    Toast.makeText(this, "Erro de autenticação:  ${it.message}", Toast.LENGTH_LONG)
                        .show()
                }
            }
        }
    }

    fun lockLoginButton() {
        binding.authLoginEnter.isEnabled = false
        countdownTimer = object : CountDownTimer(5000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                binding.authLoginEnter.text =
                    "Tente novamente em ${millisUntilFinished / 1000} segundos"
            }

            override fun onFinish() {
                binding.authLoginEnter.isEnabled = true
                binding.authLoginEnter.text = "Entrar"
            }
        }.start()

    }
}
