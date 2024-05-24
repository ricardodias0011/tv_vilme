package com.nest.nestplay

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
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
            if(!email.isEmpty() && !password.isEmpty()){
                AuthLoginWithEmailAndPassword(email, password, date)
            }else{
                Toast.makeText(applicationContext, "Preencha todos os campos", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun AuthLoginWithEmailAndPassword(email: String, password: String, today: Date) {
        binding.authLoginEnter.setText("Carregando...")
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener{task ->
            if(task.isSuccessful){
                val db = Firebase.firestore
                val docRef = db.collection("users")
                task.result.user?.uid?.let {
                    docRef
                        .document(it)
                        .get()
                        .addOnSuccessListener {document ->
                            val user = document.toObject(UserModel::class.java)
                            println(user)
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
                                    val i = Intent(this, HomeActivity::class.java)
                                    i.putExtra("data_extra", "")
                                    startActivity(i)
                                    finish()
                                }
                            }
                        }
                }
            }else {
                binding.authLoginEnter.setText("Entrar")
                Toast.makeText(applicationContext, "Email ou senha invalídos", Toast.LENGTH_LONG).show()
            }
                task.addOnFailureListener {
                    println(it)
                    binding.authLoginEnter.setText("Entrar")
                }
        }.addOnFailureListener {
                binding.authLoginEnter.setText("Entrar")
                Toast.makeText(applicationContext, "Erro de conexão, tente novamente!", Toast.LENGTH_LONG).show()
            }
    }
}