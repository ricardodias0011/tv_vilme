package com.nest.nestplay

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.view.View
import androidx.fragment.app.FragmentActivity
import com.google.gson.Gson
import com.nest.nestplay.databinding.ActivityMakePaymentBinding
import com.nest.nestplay.model.PaymentModel
import com.nest.nestplay.model.PaymentStatus
import com.nest.nestplay.model.UserIdRequest
import com.nest.nestplay.model.UserIdStatusRequest
import com.nest.nestplay.model.UserModel
import com.nest.nestplay.service.ApiClient
import com.nest.nestplay.utils.Common
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Timer
import java.util.TimerTask

class MakePaymentActivity : FragmentActivity() {
    private lateinit var binding: ActivityMakePaymentBinding
    private var finalizePayment = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMakePaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val user = getCurrentUser()
        if (user != null) {
            println(user.id)
        }
        binding.animationLoadingMakePayment.visibility = View.VISIBLE
        binding.successMakePayment.visibility = View.GONE

        binding.closeBtnMakePayment.setOnClickListener {
            val i = Intent(this, PaymentRequiredActivity::class.java)
            i.putExtra("user", user)
            startActivity(i)
            finish()
        }
        val NestApiService = ApiClient.NestApiClient()
        if (user != null) {
            NestApiService.generatePayment(UserIdRequest(user.id!!))
                .enqueue(object : Callback<PaymentModel> {
                    override fun onResponse(call: Call<PaymentModel>, response: Response<PaymentModel>) {
                        if (response.isSuccessful) {
                            val paymentModel = response.body()
                            if (paymentModel != null) {
                                val qrCodeByteArray: ByteArray = Base64.decode(paymentModel.qr_code_base64, Base64.DEFAULT)
                                val qrCodeBitmap: Bitmap = BitmapFactory.decodeByteArray(qrCodeByteArray, 0, qrCodeByteArray.size)
                                binding.QrCodePayment.setImageBitmap(qrCodeBitmap)
                                binding.animationLoadingMakePayment.visibility = View.GONE
                                binding.textTitlePaymentMake.text = "Efetue o Pagamento com PIX."
                                binding.textSubTitlePaymentMake.text = "Escaneie o QR Code abaixo para efetuar o pagamento. (válido por 30 minutos)"
                                checkPaymentStatusPeriodically(user.id!!, paymentModel.id)
                                binding.successMakePayment.visibility = View.GONE
                            } else {
                                binding.textTitlePaymentMake.text = "Falha ao gerar o pagamento"
                                binding.textSubTitlePaymentMake.text = ""
                                erroInPayment()
                                finalizePayment = true
                            }
                        } else {
                            binding.textTitlePaymentMake.text = "Falha ao gerar o pagamento"
                            binding.textSubTitlePaymentMake.text = ""
                            finalizePayment = true
                            erroInPayment()

                        }
                    }

                    override fun onFailure(call: Call<PaymentModel>, t: Throwable) {
                        println("Falha ao gerar o pagamento: ${t.message}")
                        erroInPayment()
                    }
                })
        }
    }

    fun erroInPayment(){
        finalizePayment = true
        Common.errorModal(this, "Falha ao gerar o pagamento", "Não foi possível gerar pagamento, tente novamente mais tarde.")
    }
    fun checkPaymentStatusPeriodically(userId: String, paymentId: Long) {
        val maxAttempts = 50
        var attemptCount = 0
        val timer = Timer()
        timer.schedule(object : TimerTask() {
            override fun run() {

                if (attemptCount < maxAttempts && finalizePayment == false) {
                    getPaymentStatus(userId, paymentId)
                    attemptCount++
                } else {
                    println("FINALIZADO COM SUCESSO!")
                    timer.cancel()
                }
            }
        }, 0, 7000)
    }
    fun getPaymentStatus(userId: String, paymentId: Long) {
        val NestApiService = ApiClient.NestApiClient()
        NestApiService.getStatusPayment(UserIdStatusRequest(userId, paymentId))
            .enqueue(object : Callback<PaymentStatus> {
                override fun onResponse(p0: Call<PaymentStatus>, response: Response<PaymentStatus>) {
                    if (response.isSuccessful) {
                        val reponseStatusPayment = response.body()
                        if(reponseStatusPayment?.success == true){
                            finalizePayment = true
                            binding.textTitlePaymentMake.text = "Pagamento confirmado"
                            if(reponseStatusPayment.message != null){
                                binding.textSubTitlePaymentMake.text = reponseStatusPayment.message
                                binding.QrCodePayment.visibility = View.GONE
                                binding.animationLoadingMakePayment.visibility = View.GONE
                                binding.successMakePayment.visibility = View.VISIBLE
                                binding.closeBtnMakePayment.visibility = View.GONE
                                binding.footerTextHelp.visibility = View.GONE
                            }
                            Handler(Looper.getMainLooper()).postDelayed({
                                val i = Intent(applicationContext, HomeActivity::class.java)
                                startActivity(i)
                                finish()
                            }, 3000)

                        }
                    }
                }
                override fun onFailure(p0: Call<PaymentStatus>, p1: Throwable) {

                }
            })

    }

    fun getCurrentUser(): UserModel? {
        val sharedPreferences = applicationContext.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val gson = Gson()
        val userJson = sharedPreferences.getString("user", null)
        val user = gson.fromJson(userJson, UserModel::class.java)

        return user
    }
}


