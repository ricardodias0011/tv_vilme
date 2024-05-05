package com.nest.nestplay

import android.os.Bundle
import android.view.View
import androidx.leanback.app.RowsSupportFragment
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.FocusHighlight
import androidx.leanback.widget.HeaderItem
import androidx.leanback.widget.ListRow
import androidx.leanback.widget.ListRowPresenter
import androidx.leanback.widget.OnItemViewClickedListener
import androidx.leanback.widget.OnItemViewSelectedListener
import androidx.leanback.widget.Presenter
import androidx.leanback.widget.Row
import androidx.leanback.widget.RowPresenter
import com.nest.nestplay.model.ChannelTVModel
import com.nest.nestplay.model.ListChannelTVModel
import com.nest.nestplay.presenter.itemTvListPresenter

class ListLiveTvFragment: RowsSupportFragment() {

    private var itemSelectedListener: ((ChannelTVModel) -> Unit)? = null
    private var itemClickListener: ((ChannelTVModel) -> Unit)? = null

    private var itemClickDetailListener: ((ChannelTVModel) -> Unit)? = null
    private val itemPositionMap: MutableMap<Any, Int> = mutableMapOf()

    private val listRowPresenter = object : ListRowPresenter(FocusHighlight.ZOOM_FACTOR_MEDIUM) {
        override fun isUsingDefaultListSelectEffect(): Boolean {
            return false
        }

        override fun onSelectLevelChanged(holder: RowPresenter.ViewHolder?) {
            super.onSelectLevelChanged(holder)
            if (holder != null) {
                val itemView = holder.view
                if (itemView.isSelected) {
                    itemView.setBackgroundResource(R.drawable.item_selected_background)
                } else {
                    itemView.background = null
                }
            }
        }

    }.apply {
        shadowEnabled = false
    }

    private var rootAdapter: ArrayObjectAdapter = ArrayObjectAdapter(listRowPresenter)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = rootAdapter

        onItemViewSelectedListener = ItemViewSelectedListener()
        onItemViewClickedListener = ItemViewClickListener()
    }

    fun bindData(dataList: ListChannelTVModel) {
        val arrayObjectAdapter = ArrayObjectAdapter(itemTvListPresenter())
        dataList.list.forEach { movie ->
            arrayObjectAdapter.add(movie)
            itemPositionMap[movie] = itemPositionMap.size
        }
        val headerItem = HeaderItem(dataList.title)
        val listRow = ListRow(headerItem, arrayObjectAdapter)
        rootAdapter.add(listRow)
    }

    fun bindMovieData(list: List<ListChannelTVModel>, title: String) {
        val arrayObjectAdapter = ArrayObjectAdapter(MovieItemPresenter())

        list.forEach { content ->
            arrayObjectAdapter.add(content)
        }

        val headerItem = HeaderItem(title)
        val listRow = ListRow(headerItem, arrayObjectAdapter)
        rootAdapter.add(listRow)
    }

    fun setOnContentSelectedListener(listener: (ChannelTVModel) -> Unit) {
        this.itemSelectedListener = listener
    }

    fun setOnItemClickListener(listener: (ChannelTVModel) -> Unit) {
        this.itemClickListener = listener
    }

    inner class ItemViewSelectedListener : OnItemViewSelectedListener {
        override fun onItemSelected(
            itemViewHolder: Presenter.ViewHolder?,
            item: Any?,
            rowViewHolder: RowPresenter.ViewHolder?,
            row: Row?
        ) {
            if (item is ChannelTVModel) {
                itemSelectedListener?.invoke(item)
            }

        }
    }


    inner class ItemViewClickListener : OnItemViewClickedListener {
        override fun onItemClicked(
            itemViewHolder: Presenter.ViewHolder?,
            item: Any?,
            rowViewHolder: RowPresenter.ViewHolder?,
            row: Row?
        ) {
            if (item is ChannelTVModel) {
                itemClickListener?.invoke(item)
            }
        }
    }

    fun clearAll() {
        rootAdapter.clear()
    }
}