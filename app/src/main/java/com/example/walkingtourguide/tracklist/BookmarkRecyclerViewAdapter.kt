package com.example.walkingtourguide.tracklist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.walkingtourguide.R
import com.example.walkingtourguide.data.BookmarkItem
import kotlin.math.round

class BookmarkRecyclerViewAdapter(
    private val list: List<BookmarkItem>,
    private val itemClickListener: View.OnClickListener,
    private val deleteClickListener: View.OnClickListener
) : RecyclerView.Adapter<BookmarkRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_item_bookmark, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.textView.text = round(list[position].timeStamp.toFloat() / 1000).toString() + " : " + list[position].fileItem.filename
        holder.textView.tag = position
        holder.textView.setOnClickListener(itemClickListener)
        holder.btnDelete.tag = position
        holder.btnDelete.setOnClickListener(deleteClickListener)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.textView)
        val btnDelete: Button = view.findViewById(R.id.bookmarkDeleteBtn)
    }

}