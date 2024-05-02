package com.example.mobileappscoursework.ui.leaderboard

import LeaderboardAdapter
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mobileappscoursework.LeaderboardEntry
import com.example.mobileappscoursework.LoggingChunksActivity
import com.example.mobileappscoursework.R
import com.example.mobileappscoursework.databinding.FragmentLeaderboardBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar

class LeaderboardFragment : Fragment() {

    private var _binding: FragmentLeaderboardBinding? = null

    private var db = FirebaseFirestore.getInstance()
    private lateinit var currentWeekTextView: TextView
    private lateinit var helloMessageTextView: TextView

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val dashboardViewModel =
            ViewModelProvider(this)[LeaderboardViewModel::class.java]

        _binding = FragmentLeaderboardBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val loggingButton: Button = binding.logItButton
        loggingButton.setOnClickListener {
            val intent = Intent(requireContext(), LoggingChunksActivity::class.java)
            startActivity(intent)
        }

        // Initialize the RecyclerView
        binding.leaderboardRecyclerView.layoutManager = LinearLayoutManager(context)
        loadLeaderboardData()

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        currentWeekTextView = view.findViewById(R.id.leaderboard_current_week_text_view)
        val displayStartDate = getStartOfWeek()
        val displayEndDate = getEndOfWeek()
        currentWeekTextView.text = getString(R.string.date_range, displayStartDate, displayEndDate)

        helloMessageTextView = view.findViewById(R.id.hello_text)
    }

    private fun loadLeaderboardData() {
        this.lifecycleScope.launch {
            try {
                val userTotals = mutableMapOf<String, Int>()
                val usersCollection = db.collection("users")
                val users = usersCollection.get().await()

                val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
                var currentUserFirstName = ""
                var currentUserHours = 0

                val formatter = SimpleDateFormat("yyyy-MM-dd")
                val calendar = Calendar.getInstance()

                // Gets mondays date for current week.
                calendar.firstDayOfWeek = Calendar.MONDAY
                calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                val startOfWeek = formatter.format(calendar.time)

                // Gets sunday as well for end of week.
                calendar.add(Calendar.DATE, 6)
                val endOfWeek = formatter.format(calendar.time)

                for (user in users) {
                    val userId = user.id
                    val userFirstName = user.getString("firstName") ?: "User"
                    val logsCollection = usersCollection.document(userId).collection("logs")
                    val logs = logsCollection
                        .whereGreaterThanOrEqualTo("sortableDate", startOfWeek)
                        .whereLessThanOrEqualTo("sortableDate", endOfWeek)
                        .get()
                        .await()

                    var totalHours = 0
                    logs.forEach { log ->
                        totalHours += log.getLong("hours")?.toInt() ?: 0
                    }

                    if (userId == currentUserId) {
                        currentUserFirstName = userFirstName
                        currentUserHours = totalHours
                    }

                    if (totalHours > 0) {
                        userTotals[user.getString("firstName") ?: userId] = totalHours
                    }
                }
                val sortedEntries = userTotals.toList().sortedByDescending { it.second }
                val topEntries = sortedEntries.take(10)
                val leaderboardAdapterEntries = topEntries.mapIndexed { index, entry ->
                    LeaderboardEntry(index + 1, entry.first, entry.second)
                }

                binding.leaderboardRecyclerView.adapter = LeaderboardAdapter(leaderboardAdapterEntries)

                updateUserPositionMessage(currentUserFirstName, currentUserHours, topEntries)

            } catch (e: Exception) {
            }
        }
    }

    private fun updateUserPositionMessage(firstName: String, userHours: Int, sortedEntries: List<Pair<String, Int>>) {
        if (sortedEntries.isEmpty()) return

        val currentUserIndex = sortedEntries.indexOfFirst { it.first == firstName }
        if (currentUserIndex == -1) {
            helloMessageTextView.text = getString(R.string.leaderboard_position_na, firstName)
            return
        }

        if (currentUserIndex == 0) {
            helloMessageTextView.text = getString(R.string.leaderboard_position_win, firstName)
        } else {
            val nextHigherUser = sortedEntries[currentUserIndex - 1]
            val hoursBehind = nextHigherUser.second - userHours
            helloMessageTextView.text = getString(R.string.leaderboard_position_below, firstName, hoursBehind.toString(), nextHigherUser.first)
        }
    }

    private fun getStartOfWeek(): String {
        val today = LocalDate.now()
        val startOfWeek = today.with(DayOfWeek.MONDAY)
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

        return startOfWeek.format(formatter)
    }

    private fun getEndOfWeek(): String {
        val today = LocalDate.now()
        val endOfWeek = today.with(DayOfWeek.SUNDAY)
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

        return endOfWeek.format(formatter)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}