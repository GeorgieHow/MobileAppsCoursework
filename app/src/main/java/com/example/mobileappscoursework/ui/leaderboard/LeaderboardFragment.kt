package com.example.mobileappscoursework.ui.leaderboard

import LeaderboardAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mobileappscoursework.LeaderboardEntry
import com.example.mobileappscoursework.databinding.FragmentLeaderboardBinding

class LeaderboardFragment : Fragment() {

    private var _binding: FragmentLeaderboardBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val dashboardViewModel =
            ViewModelProvider(this).get(LeaderboardViewModel::class.java)

        _binding = FragmentLeaderboardBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Example dummy data
        val dummyData = listOf(
            LeaderboardEntry(1, "Natasha", 50),
            LeaderboardEntry(2, "Bob", 48),
            LeaderboardEntry(3,"Dave", 42),
            LeaderboardEntry(4, "Zachary", 36),
            LeaderboardEntry(5, "Beth", 30),
            LeaderboardEntry(6,"Hector", 27),
            LeaderboardEntry(7,"Lily", 24),
            LeaderboardEntry(8, "Gwen", 19),
            LeaderboardEntry(9, "Julian", 18),
            LeaderboardEntry(10,"Curtis",7),
            LeaderboardEntry(11, "John", 4),
            LeaderboardEntry(12, "Stacy", 2)
                // Add more entries as needed
        )

        // Initialize the RecyclerView
        binding.leaderboardRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = LeaderboardAdapter(dummyData)
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}