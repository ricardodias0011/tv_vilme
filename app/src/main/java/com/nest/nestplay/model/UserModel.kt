package com.nest.nestplay.model

import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale

class UserModel (
    var name: String = "",
    val createdAt: Timestamp? = null,
    val expirePlanDate: Timestamp? = null,
    val status: String = "",
    val type: String = "",
){
    companion object {
        private const val DATE_FORMAT = "yyyy-MM-dd HH:mm:ss"

        fun timestampToString(timestamp: Timestamp?): String {

            val date = timestamp?.toDate()
            val dateFormat = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
            return dateFormat.format(date)
        }
    }
}