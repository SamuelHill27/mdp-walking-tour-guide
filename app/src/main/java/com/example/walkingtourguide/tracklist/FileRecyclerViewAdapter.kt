package com.example.walkingtourguide.tracklist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.walkingtourguide.R
import com.example.walkingtourguide.data.FileItem

class FileRecyclerViewAdapter(
    private val list: List<FileItem>,
    private val itemClickListener: View.OnClickListener
) : RecyclerView.Adapter<FileRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_item_file, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.textView.text = list[position].filename
        holder.itemView.tag = position
        holder.itemView.setOnClickListener(itemClickListener)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.textView)
    }

}