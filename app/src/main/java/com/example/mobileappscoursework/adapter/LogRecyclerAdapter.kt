package com.example.mobileappscoursework.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mobileappscoursework.R
import com.example.mobileappscoursework.model.LogEntry

class LogRecyclerAdapter(private val logs: List<LogEntry>) : RecyclerView.Adapter<LogRecyclerAdapter.LogViewHolder>() {

    class LogViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val dateTextView: TextView = view.findViewById(R.id.log_date)
        val logTextView: TextView = view.findViewById(R.id.log_text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.log_item_layout, parent, false)
        return LogViewHolder(view)
    }

    override fun onBindViewHolder(holder: LogViewHolder, position: Int) {
        val logEntry = logs[position]
        holder.dateTextView.text = logEntry.date
        holder.logTextView.text = logEntry.title
    }

    override fun getItemCount() = logs.size
}
