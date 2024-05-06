package com.nest.nestplay.fragments

import android.os.Bundle
import android.view.View
import androidx.leanback.app.RowsSupportFragment
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.FocusHighlight
import androidx.leanback.widget.ListRow
import androidx.leanback.widget.ListRowPresenter
import androidx.leanback.widget.OnItemViewClickedListener
import androidx.leanback.widget.OnItemViewSelectedListener
import androidx.leanback.widget.Presenter
import androidx.leanback.widget.Row
import androidx.leanback.widget.RowPresenter
import com.nest.nestplay.R
import com.nest.nestplay.model.ListEpisodesModel
import com.nest.nestplay.presenter.EpisodeosPresenter

class ListEpisodesFragment : RowsSupportFragment() {

    private var itemSelectedListener: ((ListEpisodesModel) -> Unit)? = null
    private var itemClickListener: ((ListEpisodesModel) -> Unit)? = null

    private var itemSelectListener: ((ListEpisodesModel) -> Unit)? = null

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
                    itemView.setBackgroundResource(R.drawable.btn_selector_keybord)
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
    private val gridPresenter = EpisodeosPresenter()

    fun findItemRowAdapter(episode: ListEpisodesModel): Row? {
        for (i in 0 until rootAdapter.size()) {
            val row = rootAdapter[i] as? ListRow
            row?.let {
                val adapter = row.adapter
                for (j in 0 until adapter.size()) {
                    val item = adapter.get(j) as? ListEpisodesModel
                    if (item != null && item == episode) {
                        val viewHolder = (gridPresenter as? EpisodeosPresenter)?.getViewHolder(item, requireContext())
                        viewHolder?.let { holder ->
                            val itemView = holder.view
                            itemView.setBackgroundResource(R.drawable.btn_selector_keybord)
                            println("Assim porra do caralho ${itemView.background}")
                            itemView.invalidate()
                        }
                        return row
                    }
                }
            }
        }
        return null
    }



    fun bindData(list: List<ListEpisodesModel>, title: String) {
        val arrayObjectAdapter = ArrayObjectAdapter(EpisodeosPresenter())

        list.forEach { content ->
            arrayObjectAdapter.add(content)
        }

        val headerItem = null
        val listRow = ListRow(headerItem, arrayObjectAdapter)
        rootAdapter.add(listRow)
    }


    fun setOnContentSelectedListener(listener: (ListEpisodesModel) -> Unit) {
        this.itemSelectedListener = listener
    }

    fun setOnItemClickListener(listener: (ListEpisodesModel) -> Unit) {
        this.itemClickListener = listener
    }

    inner class ItemViewSelectedListener : OnItemViewSelectedListener {
        override fun onItemSelected(
            itemViewHolder: Presenter.ViewHolder?,
            item: Any?,
            rowViewHolder: RowPresenter.ViewHolder?,
            row: Row?
        ) {
            if (item is ListEpisodesModel) {
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
            if (item is ListEpisodesModel) {
                itemClickListener?.invoke(item)
            }
        }
    }

    fun clearAll() {
        rootAdapter.clear()
    }

    fun requestFocus(): View {
        val view = view
        view?.requestFocus()
        return view!!
    }

}
