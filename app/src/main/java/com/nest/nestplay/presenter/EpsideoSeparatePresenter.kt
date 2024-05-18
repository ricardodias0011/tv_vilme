package com.nest.nestplay.presenter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.leanback.widget.Presenter
import com.nest.nestplay.R
class EpsideoSeparatePresenter : Presenter() {

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {

        val view =
            LayoutInflater.from(parent?.context).inflate(R.layout.episodes_separete_item, parent, false)

        view.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                view.setBackgroundResource(R.drawable.item_selected_background)
            } else {
                view.background = null
            }
        }

        return ViewHolder(view)

    }

    fun getViewHolder(item: String, context: Context): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.episodes_separete_item, null, false)
        val viewHolder = ViewHolder(view)
        onBindViewHolder(viewHolder, item)

        return viewHolder
    }
    override fun onBindViewHolder(viewHolder: ViewHolder, item: Any?) {
        val content = item as String

        val textView = viewHolder!!.view.findViewById<TextView>(R.id.episode_separete_item)
        if(textView != null) {
            textView.setText(content)
        }

    }
    override fun onUnbindViewHolder(viewHolder: ViewHolder) {
    }
}

