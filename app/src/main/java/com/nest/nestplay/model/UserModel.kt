package com.nest.nestplay.model

import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.parcelize.Parcelize
import java.text.SimpleDateFormat
import java.util.Locale
@Parcelize
class UserModel (
    var name: String = "",
    val createdAt: Timestamp? = null,
    val expirePlanDate: Timestamp? = null,
    val status: String = "",
    val type: String = "",
    val currentScreens : List<String>? = null,
    val screensAvailables : Int = 0,
    val activeOnlineTv: Boolean? = false,
    val iptv_list_link: String? = "",
    val use_default_list_tv: Boolean? = true,
    val userRegisteredBy: String? = "",
    var id: String? = "",
    ): Parcelable {
    companion object {
        private const val DATE_FORMAT = "dd/MM/yyyy HH:mm:ss"
        fun timestampToString(timestamp: Timestamp?): String {
            val date = timestamp?.toDate()
            val dateFormat = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
            return dateFormat.format(date)
        }
    }
}