package com.example.mobileappscoursework.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mobileappscoursework.R
import com.example.mobileappscoursework.model.BadgeEntry

class BadgeReyclerAdapter(private val badges: List<BadgeEntry>) : RecyclerView.Adapter<BadgeReyclerAdapter.BadgeViewHolder>() {

    class BadgeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val badgeName: TextView = itemView.findViewById(R.id.badge_type)
        private val badgeNoNeeded: TextView = itemView.findViewById(R.id.no_needed)
        private val badgeStatus: TextView = itemView.findViewById(R.id.achieved)
        private val frameLayout: FrameLayout = itemView.findViewById(R.id.badge_background)

        fun bind(badge: BadgeEntry) {
            badgeName.text = badge.name
            badgeNoNeeded.text = "Requirement: ${badge.requirement}"
            badgeStatus.text = if (badge.isAchieved) "Achieved" else "Not Achieved"

            if (badge.isAchieved) {
                frameLayout.setBackgroundResource(R.drawable.badge_done_gradient)
            } else {
                frameLayout.setBackgroundResource(R.drawable.shadow)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BadgeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.badge_layout, parent, false)
        return BadgeViewHolder(view)
    }

    override fun onBindViewHolder(holder: BadgeViewHolder, position: Int) {
        holder.bind(badges[position])
    }

    override fun getItemCount() = badges.size
}
