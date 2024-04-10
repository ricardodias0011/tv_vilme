package com.nest.nestplay

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.nest.nestplay.databinding.ActivityMainBinding

class MainActivity : FragmentActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        binding.authLoginEnter.setOnClickListener {
            val email = binding.authLoginEmail.text.toString().trim()
            val password = binding.authLoginPassword.text.toString().trim()
            if(!email.isEmpty() && !password.isEmpty()){
                AuthLoginWithEmailAndPassword(email, password)
            }else{
                Toast.makeText(applicationContext, "Preencha todos os campos", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun AuthLoginWithEmailAndPassword(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener{task ->
            if(task.isSuccessful){
                val i = Intent(this, HomeActivity::class.java)
                i.putExtra("data_extra", "data bem legal")
                startActivity(i)
                finish()
            }else {
                Toast.makeText(applicationContext, "Email ou senha invalídos", Toast.LENGTH_LONG).show()
            }
        }
    }
}