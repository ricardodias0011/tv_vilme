package com.nest.nestplay.presenter

import android.graphics.Color
import android.view.ViewGroup
import android.widget.TextView
import androidx.leanback.widget.ListRowPresenter
import androidx.leanback.widget.RowPresenter

class CustomListRowPresenter : ListRowPresenter() {
    override fun onBindRowViewHolder(holder: RowPresenter.ViewHolder?, item: Any?) {
        super.onBindRowViewHolder(holder, item)
        val headerView = holder?.headerViewHolder?.view as? TextView
        headerView?.setTextColor(Color.WHITE)
    }

    override fun createRowViewHolder(parent: ViewGroup): RowPresenter.ViewHolder {
        return super.createRowViewHolder(parent)
    }
}