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
import com.nest.nestplay.model.ListMovieModel
import com.nest.nestplay.model.MovieModel
import com.nest.nestplay.presenter.MovieListItemPresenter
import com.nest.nestplay.presenter.itemTvListPresenter

class ListFragment : RowsSupportFragment() {

    private var itemSelectedListener: ((ListMovieModel.Movie, String) -> Unit)? = null
    private var itemClickListener: ((ListMovieModel.Movie) -> Unit)? = null
    private var itemTvOnlineClickListener: ((ChannelTVModel) -> Unit)? = null
    private var itemTvOnlineSelectedListener: ((ChannelTVModel) -> Unit)? = null
    private var itemClickDetailListener: ((MovieModel) -> Unit)? = null
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

    fun bindData(dataList: ListMovieModel) {
        val arrayObjectAdapter = ArrayObjectAdapter(MovieListItemPresenter())
        dataList.list.forEach { movie ->
            arrayObjectAdapter.add(movie)
            itemPositionMap[movie] = itemPositionMap.size
        }
        val headerItem = HeaderItem(dataList.title)
        val listRow = ListRow(headerItem, arrayObjectAdapter)
        rootAdapter.add(listRow)
    }

    fun bindDataTvOnline(dataList: ListChannelTVModel) {
        val arrayObjectAdapter = ArrayObjectAdapter(itemTvListPresenter())
        dataList.list.forEach { movie ->
            arrayObjectAdapter.add(movie)
            itemPositionMap[movie] = itemPositionMap.size
        }
        val headerItem = HeaderItem(dataList.title)
        val listRow = ListRow(headerItem, arrayObjectAdapter)
        rootAdapter.add(listRow)
    }


    fun bindMovieData(list: List<MovieModel>, title: String) {
        val arrayObjectAdapter = ArrayObjectAdapter(MovieItemPresenter())

        list.forEach { content ->
            arrayObjectAdapter.add(content)
        }

        val headerItem = HeaderItem(title)
        val listRow = ListRow(headerItem, arrayObjectAdapter)
        rootAdapter.add(listRow)
    }

    fun setOnContentSelectedListener(listener: (ListMovieModel.Movie, String) -> Unit) {
        this.itemSelectedListener = listener
    }

    fun setOnItemClickListener(listener: (ListMovieModel.Movie) -> Unit) {
        this.itemClickListener = listener
    }

    fun setOnItemTvOnlineClickListener(listener: (ChannelTVModel) -> Unit) {
        this.itemTvOnlineClickListener = listener
    }

    fun setOnContentTvOnlineSelectedListener(listener: (ChannelTVModel) -> Unit) {
        this.itemTvOnlineSelectedListener = listener
    }


    fun setOnItemDetailClickListener(listener: (MovieModel) -> Unit) {
        this.itemClickDetailListener = listener
    }

    inner class ItemViewSelectedListener : OnItemViewSelectedListener {
        override fun onItemSelected(
            itemViewHolder: Presenter.ViewHolder?,
            item: Any?,
            rowViewHolder: RowPresenter.ViewHolder?,
            row: Row?
        ) {
            var headerItemText = ""
            if (row is ListRow) {
                headerItemText = row.headerItem?.name ?: ""
                println("Header Item Text: $headerItemText")
            }
            if (item is ListMovieModel.Movie) {
                itemSelectedListener?.invoke(item, headerItemText)
            }
            if (item is ChannelTVModel) {
                itemTvOnlineSelectedListener?.invoke(item)
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
            if (item is ListMovieModel.Movie) {
                itemClickListener?.invoke(item)
            }
            if (item is MovieModel) {
                itemClickDetailListener?.invoke(item)
            }
            if (item is ChannelTVModel) {
                itemTvOnlineClickListener?.invoke(item)
            }
        }
    }

    fun clearAll() {
        rootAdapter.clear()
        rootAdapter.notifyArrayItemRangeChanged(0, rootAdapter.size())
    }

    fun requestFocus(): View {
        val view = view
        view?.requestFocus()
        return view!!
    }

}