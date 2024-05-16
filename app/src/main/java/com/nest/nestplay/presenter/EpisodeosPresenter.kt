package com.nest.nestplay.presenter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.leanback.widget.Presenter
import com.nest.nestplay.R
import com.nest.nestplay.model.ListEpisodesModel
class EpisodeosPresenter : Presenter() {

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {

        val view =
            LayoutInflater.from(parent?.context).inflate(R.layout.epsodeos_item, parent, false)

        view.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                view.setBackgroundResource(R.drawable.item_selected_background)
            } else {
                view.background = null
            }
        }

        return ViewHolder(view)

    }

    fun getViewHolder(item: ListEpisodesModel, context: Context): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.epsodeos_item, null, false)
        val viewHolder = ViewHolder(view)
        onBindViewHolder(viewHolder, item)

        return viewHolder
    }
    override fun onBindViewHolder(viewHolder: ViewHolder, item: Any?) {
        val content = item as ListEpisodesModel

        val textView = viewHolder!!.view.findViewById<TextView>(R.id.episode_item)
        if(textView != null) {
            textView.setText(content?.ep_number?.toString())
        }

    }
    override fun onUnbindViewHolder(viewHolder: ViewHolder) {
    }
}

