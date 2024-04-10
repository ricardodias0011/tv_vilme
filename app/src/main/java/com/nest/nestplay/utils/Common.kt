package com.nest.nestplay.utils

import android.app.Dialog
import android.content.Context
import android.view.Window
import android.widget.TextView
import com.nest.nestplay.R
import com.nest.nestplay.model.UserModel

class Common {
    companion object {
        fun getWidthInPercent(context: Context, percent: Int): Int {
            val width = context.resources.displayMetrics.widthPixels ?: 0
            return (width * percent) / 100
        }

        fun getHeightInPercent(context: Context, percent: Int): Int {
            val height = context.resources.displayMetrics.heightPixels ?: 0
            return (height * percent) / 100
        }

        fun playmentRequiredDialog(context: Context, user: UserModel) {
            val dialog = Dialog(context, R.style.Theme_NestPlay)
            dialog.window?.setBackgroundDrawableResource(R.color.transparent)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setContentView(R.layout.dialog_payment_required)

            val expireString = UserModel.timestampToString(user.expirePlanDate)

            dialog.findViewById<TextView>(R.id.paymentRequiredTitle).text = "Olá ${user.name}"
            dialog.findViewById<TextView>(R.id.paymentRequiredSubtitle).text = "Seu plano básico venceu no dia $expireString, realize o pagamento e continue curtindo seus filmes e series prefiridos"

            dialog.findViewById<TextView>(R.id.closeBtn).setOnClickListener {
                System.exit(0)
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
    }
}