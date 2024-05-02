package com.example.mobileappscoursework.ui.profile

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mobileappscoursework.R
import com.example.mobileappscoursework.model.BadgeEntry
import com.example.mobileappscoursework.adapter.BadgeReyclerAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

class ProfileFragment: Fragment() {

    private lateinit var recyclerViewBadges: RecyclerView
    private lateinit var badgeAdapter: BadgeReyclerAdapter
    private var badges: MutableList<BadgeEntry> = mutableListOf()
    private var db = FirebaseFirestore.getInstance()
    private var mAuth = FirebaseAuth.getInstance()
    private var uid = mAuth.currentUser?.uid

    private lateinit var userNameTextView: TextView
    private lateinit var userWinsTextView: TextView
    private lateinit var totalHoursLogged: TextView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        recyclerViewBadges = view.findViewById(R.id.badges_recycler_view)
        userNameTextView = view.findViewById(R.id.users_name_text_view)
        userWinsTextView = view.findViewById(R.id.wins_text_view)
        totalHoursLogged = view.findViewById(R.id.hours_logged_text_view)

        fetchUserDataAndLogs()
        uid?.let { fetchLogsAndUpdateBadges(it, 0) }

        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout)
        swipeRefreshLayout.setOnRefreshListener {
            fetchUserDataAndLogs()
        }

        return view
    }

    private fun fetchLogsAndUpdateBadges(userId: String, totalWins: Int) {
        val userLogsRef = db.collection("users").document(userId).collection("logs")
        userLogsRef.get().addOnSuccessListener { documents ->
            var totalHours = 0
            for (document in documents) {
                totalHours += document.getLong("hours")?.toInt() ?: 0
            }

            totalHoursLogged.text = totalHours.toString()
            swipeRefreshLayout.isRefreshing = false
            updateBadgeAchievements(totalHours, totalWins)
            initializeBadgeDisplay()
        }.addOnFailureListener {
            swipeRefreshLayout.isRefreshing = false
            Log.e("ProfileFragment", "Error fetching logs")
        }
    }

    private fun updateBadgeAchievements(totalHours: Int, totalWins: Int) {
        badges = createBadges().map { badge ->
            when (badge.name) {
                "Victory Royal", "Weekly Winner", "On Fire", "Champion" ->
                    badge.copy(isAchieved = totalWins >= badge.requirement)
                else ->
                    badge.copy(isAchieved = totalHours >= badge.requirement)
            }
        }.toMutableList()
    }

    private fun createBadges(): List<BadgeEntry> {
        return listOf(
            BadgeEntry("Log Beginner", 10, false),
            BadgeEntry("Log Novice", 50, false),
            BadgeEntry("Advanced Logger", 100, false),
            BadgeEntry("Logging Expert", 500, false),
            BadgeEntry("Master Logger", 1000, false),
            BadgeEntry("Logging Monarch", 2000, false),
            BadgeEntry("Victory Royal", 1, false),
            BadgeEntry("Weekly Winner", 5, false),
            BadgeEntry("On Fire", 10, false),
            BadgeEntry("Champion", 25, false)
        )
    }

    private fun fetchUserDataAndLogs() {
        uid?.let { userId ->
            val userDocRef = db.collection("users").document(userId)

            userDocRef.get().addOnSuccessListener { document ->
                if (document.exists()) {
                    val totalWins = document.getLong("winCount")?.toInt() ?: 0
                    val userName = document.getString("firstName") ?: "No Name"

                    userNameTextView.text = userName
                    userWinsTextView.text = totalWins.toString()

                    fetchLogsAndUpdateBadges(userId, totalWins)
                }
            }.addOnFailureListener { exception ->
                Log.d("ProfileFragment", "Error getting user details: ", exception)
            }
        }
    }

    private fun initializeBadgeDisplay() {
        badgeAdapter = BadgeReyclerAdapter(badges)
        recyclerViewBadges.adapter = badgeAdapter
        recyclerViewBadges.layoutManager = LinearLayoutManager(context)
    }
}