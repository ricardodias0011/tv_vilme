package com.nest.nestplay.adpters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nest.nestplay.R
import com.nest.nestplay.databinding.GenreItemBinding
import com.nest.nestplay.model.Genre

class GenreListAdpter(private val context: Context, private val listGenre: List<Genre>):
    RecyclerView.Adapter<GenreListAdpter.GenreViewHolder>() {

    var onItemFocusChangeListener: OnItemFocusChangeListener? = null
    var onItemClickChangerListener: OnItemClickChangerListener? = null
    inner class GenreViewHolder(binding: GenreItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val title = binding.genreLabel
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GenreViewHolder {
        val itemList = GenreItemBinding.inflate(LayoutInflater.from(context), parent, false)
        return GenreViewHolder(itemList)
    }

    override fun getItemCount() = listGenre.size

    override fun onBindViewHolder(holder: GenreListAdpter.GenreViewHolder, position: Int) {
        holder.itemView.setBackgroundResource(R.drawable.no_selected_bg)
        holder.title.setTextColor(Color.GRAY)
        val item = listGenre[position]
        holder.itemView.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                holder.title.setTextColor(Color.WHITE)
                onItemFocusChangeListener?.onItemFocused(item)
                holder.itemView.setBackgroundResource(R.drawable.item_selected_genre)
            } else {
                holder.itemView.setBackgroundResource(R.drawable.no_selected_bg)
                holder.title.setTextColor(Color.GRAY)
            }
        }

        holder.title.setText(item.name)

        holder.itemView.setOnClickListener { view ->
            onItemClickChangerListener?.onItemClicked(item)
        }
    }


    interface OnItemFocusChangeListener {
        fun onItemFocused(item:Genre)
    }

    interface OnItemClickChangerListener {
        fun onItemClicked(item:Genre)
    }
}