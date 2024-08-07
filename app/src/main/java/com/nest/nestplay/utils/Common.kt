package com.nest.nestplay.utils

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Handler
import android.provider.Settings
import android.util.Base64
import android.view.Window
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import com.nest.nestplay.R
import com.nest.nestplay.model.UserModel
import java.net.InetAddress
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
data class Caption(val startTime: Long, val endTime: Long, val text: String)
class Common {
    companion object {

        var msgPermissionDENIED = "PERMISSION_DENIED: Missing or insufficient permissions."

        var imgDefaultMainBg = "https://media.istockphoto.com/id/1332097112/pt/foto/the-black-and-silver-are-light-gray-with-white-the-gradient-is-the-surface-with-templates.jpg?s=612x612&w=0&k=20&c=9XnsxOC-p39iiiL-BgBvMsxUfQ5TaB5eXF8UpcYB_dg="
        fun getWidthInPercent(context: Context, percent: Int): Int {
            val width = context.resources.displayMetrics.widthPixels ?: 0
            return (width * percent) / 100
        }

        fun getHeightInPercent(context: Context, percent: Int): Int {
            val height = context.resources.displayMetrics.heightPixels ?: 0
            return (height * percent) / 100
        }

        private fun hexStringToByteArray(s: String): ByteArray {
            val len = s.length
            val data = ByteArray(len / 2)
            var i = 0
            while (i < len) {
                data[i / 2] = ((Character.digit(s[i], 16) shl 4)
                        + Character.digit(s[i + 1], 16)).toByte()
                i += 2
            }
            return data
        }
        fun decrypt(msg: String, password: String = Constants.KEY_D): String {
            var urlDecodeString = ""
                try {
                    val key32Char = hexStringToByteArray(password)
                    val iv32Char = hexStringToByteArray(password)
                    val secretKeySpec = SecretKeySpec(key32Char, "AES")
                    val ivParameterSpec = IvParameterSpec(iv32Char)
                    val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
                    cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec)
                    val dstBuff = cipher.doFinal(Base64.decode(msg, Base64.DEFAULT))
                    urlDecodeString = String(dstBuff)
                }catch (ex: Exception) {
                    ex.printStackTrace()
                }
            return urlDecodeString
        }

        fun parseBaseUrlAndEndpoint(url: String): Pair<String, String> {
            val cleanedUrl = url.trimEnd('/')
            val firstSlashIndex = cleanedUrl.indexOf('/', startIndex = "https://".length)

            if (firstSlashIndex == -1) {
                return Pair(cleanedUrl, "")
            } else {
                val baseUrl = cleanedUrl.substring(0, firstSlashIndex)
                val endpoint = cleanedUrl.substring(firstSlashIndex)

                return Pair(baseUrl, endpoint)
            }
        }


        fun getDeviceInformation(context: Context): String {
            val deviceInfo = mutableMapOf<String, String>()
            deviceInfo["Model"] = Build.MODEL
            deviceInfo["Manufacturer"] = Build.MANUFACTURER
            deviceInfo["Android Version"] = Build.VERSION.RELEASE
            deviceInfo["SDK Version"] = Build.VERSION.SDK_INT.toString()
            deviceInfo["Device Name"] = Settings.Global.getString(context.contentResolver, "device_name") ?: "Unknown"
            deviceInfo["Brand"] = Build.BRAND
            deviceInfo["Hardware"] = Build.HARDWARE
            deviceInfo["Host"] = Build.HOST
            deviceInfo["Product"] = Build.PRODUCT
            deviceInfo["device id"] = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            return "${Build.MODEL} - ${Build.VERSION.RELEASE} - ${deviceInfo["Device Name"]} - ${Build.VERSION.SDK_INT.toString()} - ${deviceInfo["device id"]}"
        }

        fun getIpAddress(context: Context): String? {
            try {
                val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
                val wifiInfo = wifiManager.connectionInfo
                val ipAddress = wifiInfo.ipAddress
                try{
                    return InetAddress.getByAddress(
                        byteArrayOf(
                            (ipAddress and 0xFF).toByte(),
                            (ipAddress shr 8 and 0xFF).toByte(),
                            (ipAddress shr 16 and 0xFF).toByte(),
                            (ipAddress shr 24 and 0xFF).toByte()
                        )
                    ).hostAddress
                }catch (e: Exception) {
                    e.printStackTrace()
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
            return null
        }

        fun playmentRequiredDialog(context: Context, user: UserModel) {
            val dialog = Dialog(context, R.style.Theme_NestPlay)
            dialog.window?.setBackgroundDrawableResource(R.color.transparent)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setContentView(R.layout.dialog_payment_required)

            val expireString = UserModel.timestampToString(user.expirePlanDate)

            dialog.findViewById<TextView>(R.id.paymentRequiredTitle).text = "Olá ${user.name}"
            dialog.findViewById<TextView>(R.id.paymentRequiredSubtitle).text = "Seu plano venceu no dia $expireString, Contate o vendedor e realize o pagamento e continue curtindo seus filmes e series prefiridos"

            dialog.findViewById<TextView>(R.id.closeBtn).setOnClickListener {
                System.exit(0)
            }

            dialog.show()
        }

        fun loadingDialog(context: Context): Dialog {
            val dialog = Dialog(context, R.style.Theme_NestPlay)
            dialog.window?.setBackgroundDrawableResource(R.color.transparent)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setContentView(R.layout.dialog_loading)
            return dialog
        }


        fun changeQuality(context: Context) {
            val dialog = Dialog(context, R.style.Theme_NestPlay)
            dialog.window?.setBackgroundDrawableResource(R.color.transparent)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setContentView(R.layout.dialog_quality)
            dialog.findViewById<TextView>(R.id.closeBtnQuality).setOnClickListener {
                dialog.dismiss()
            }
            dialog.show()
        }

        fun errorModal(context: Context, title: String, msg: String?) {
            val dialog = Dialog(context, R.style.Theme_NestPlay)
            dialog.window?.setBackgroundDrawableResource(R.color.transparent)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setContentView(R.layout.content_erro)

            dialog.findViewById<TextView>(R.id.erroTitleContent).text = title
            dialog.findViewById<TextView>(R.id.mainTextErro).text = msg

            dialog.findViewById<TextView>(R.id.closeBtnErro).setOnClickListener {
                if (context is Activity) {
                    (context as Activity).finish()
                } else {
                    dialog.dismiss()
                }

            }
            dialog.show()
        }

        fun newVersion(context: Context, title: String, msg: String?, url: String?) {
            val dialog = Dialog(context, R.style.Theme_NestPlay)
            dialog.window?.setBackgroundDrawableResource(R.color.transparent)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setContentView(R.layout.dialog_version)

            dialog.findViewById<TextView>(R.id.VersionTitleContent).text = title
            dialog.findViewById<TextView>(R.id.mainTextVersion).text = msg

            dialog.findViewById<TextView>(R.id.closeBtnVersion).setOnClickListener {
                    dialog.dismiss()
            }

            dialog.findViewById<TextView>(R.id.downloadBtn).setOnClickListener{
                val uri = Uri.parse(url)
                val intent = Intent(Intent.ACTION_VIEW, uri)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
            }

            dialog.show()
        }


        fun changeSubtitle(context: Context, subtitles: List<String>) {
            val dialog = Dialog(context, R.style.Theme_NestPlay)
            dialog.window?.setBackgroundDrawableResource(R.color.transparent)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setContentView(R.layout.dialog_subititles)

            val radioSubtitleGroup: RadioGroup = dialog.findViewById(R.id.radioSubtitleGroup)

            radioSubtitleGroup.removeAllViews()

            subtitles.forEach { subtitle ->
                val radioButton = RadioButton(context)
                radioButton.text = subtitle
                radioSubtitleGroup.addView(radioButton)
            }

            dialog.findViewById<TextView>(R.id.closeBtnSubtitle).setOnClickListener {
                dialog.dismiss()
            }
            dialog.show()
        }


        fun descriptionDialog(context: Context, title: String?, subtext: String, description: String) {
            val dialog = Dialog(context, R.style.Theme_NestPlay)
            dialog.window?.setBackgroundDrawableResource(R.color.transparent)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setContentView(R.layout.dialog_description)

            dialog.findViewById<TextView>(R.id.tvTitle).text = title
            dialog.findViewById<TextView>(R.id.tvSubTitle).text = subtext
            dialog.findViewById<TextView>(R.id.tv_description).text = description

            dialog.findViewById<TextView>(R.id.closeBtn).setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
        }

        fun showSubtitlesInTextView(textView: AppCompatTextView, subtitles: List<Caption>) {
            var currentPosition = 0
            val timerHandler = Handler()
            val timerRunnable = object : Runnable {
                override fun run() {
                    val currentTime = System.currentTimeMillis()
                    while (currentPosition < subtitles.size && subtitles[currentPosition].endTime < currentTime) {
                        currentPosition++
                    }
                    if (currentPosition < subtitles.size && subtitles[currentPosition].startTime <= currentTime) {
                        textView.text = subtitles[currentPosition].text
                    } else {
                        textView.text = ""
                    }
                    timerHandler.postDelayed(this, 100)
                }
            }

            textView.text = ""
            timerHandler.post(timerRunnable)
        }

    }
}