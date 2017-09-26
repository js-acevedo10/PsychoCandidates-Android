package com.moviles.psychoapp.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.moviles.psychoapp.R
import com.moviles.psychoapp.world.BriefExam
import kotlinx.android.synthetic.main.item_my_exams.view.*

/**
 * Adapter for My Exams recycler
 */

class MyExamsAdapter(val items: List<BriefExam>, val layout: Int) : RecyclerView.Adapter<MyExamsAdapter.ViewHolder>() {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bindItem(items[position])

    override fun getItemCount(): Int = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(LayoutInflater.from(parent.context).inflate(layout, parent, false))

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindItem(item: BriefExam) {
            itemView.item_me_txt_date.text = itemView.context.getString(R.string.item_me_date, item.date)
            itemView.item_me_txt_score.text = itemView.context.getString(R.string.item_me_score, "${item.score}%")
            itemView.item_me_txt_title.text = item.name
        }
    }
}