package com.example.mobileappscoursework.ui.profile

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mobileappscoursework.R
import com.example.mobileappscoursework.model.BadgeEntry
import com.example.mobileappscoursework.adapter.BadgeReyclerAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileFragment: Fragment() {

    private lateinit var recyclerViewBadges: RecyclerView
    private lateinit var badgeAdapter: BadgeReyclerAdapter
    private var badges: MutableList<BadgeEntry> = mutableListOf()
    private var db = FirebaseFirestore.getInstance()
    private var mAuth = FirebaseAuth.getInstance()
    private var uid = mAuth.currentUser?.uid


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        recyclerViewBadges = view.findViewById(R.id.badges_recycler_view)
        fetchLogsAndUpdateBadges()
        return view
    }

    private fun fetchLogsAndUpdateBadges() {
        val userLogsRef = uid?.let {
            db.collection("users").document(it)
                .collection("logs")
        }

        userLogsRef?.get()?.addOnSuccessListener { documents ->
            var totalHours = 0
            for (document in documents) {
                totalHours += document.getLong("hours")?.toInt() ?: 0
            }
            updateBadgeAchievements(totalHours)
            initializeBadgeDisplay()
        }?.addOnFailureListener {
        }
    }

    private fun updateBadgeAchievements(totalHours: Int) {
        badges = createBadges().map { badge ->
            badge.copy(isAchieved = totalHours >= badge.requirement)
        }.toMutableList()

        Log.d("Help", "$badges")
    }
    private fun createBadges(): List<BadgeEntry> {
        return listOf(
            BadgeEntry("Log Beginner", 10, false),
            BadgeEntry("Log Novice", 50, false),
            BadgeEntry("Advanced Logger", 100, false),
            BadgeEntry("Logging Expert", 500, false),
            BadgeEntry("Master Logger", 1000, false),
            BadgeEntry("Logging Monarch", 2000, false)
        )
    }

    private fun initializeBadgeDisplay() {
        badgeAdapter = BadgeReyclerAdapter(badges)
        recyclerViewBadges.adapter = badgeAdapter
        recyclerViewBadges.layoutManager = LinearLayoutManager(context)
    }
}