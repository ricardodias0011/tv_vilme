package com.nest.nestplay

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.fragment.app.FragmentActivity
import com.nest.nestplay.databinding.ActivityPaymentRequiredBinding
import com.nest.nestplay.model.UserModel

class PaymentRequiredActivity : FragmentActivity() {
    private lateinit var binding: ActivityPaymentRequiredBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentRequiredBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.makePaymentBtn.visibility = View.GONE

        val user = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            intent.getParcelableExtra<UserModel>("user")
        } else {
            intent.getParcelableExtra("user")
        }
        val expireString = UserModel.timestampToString(user?.expirePlanDate)
        println(user?.userRegisteredBy)
        if(user?.userRegisteredBy == "admin"){
            binding.makePaymentBtn.visibility = View.VISIBLE
            binding.paymentRequiredTitle.text = "Olá, ${user?.name}"
            binding.paymentRequiredSubtitle.text = "Seu plano venceu no dia $expireString. Realize o pagamento para continuar curtindo seus filmes e séries favoritos."
          }else{
            binding.makePaymentBtn.requestFocus()
            binding.paymentRequiredTitle.text = "Olá, ${user?.name}"
            binding.paymentRequiredSubtitle.text = "Seu plano venceu no dia $expireString. Contate o revendedor e realize o pagamento para continuar curtindo seus filmes e séries favoritos."
        }
        binding.makePaymentBtn.setOnClickListener {
            val i = Intent(this, MakePaymentActivity::class.java)
            startActivity(i)
            finish()
        }
        binding.closeBtn.setOnClickListener {
            finish()
        }
    }
}