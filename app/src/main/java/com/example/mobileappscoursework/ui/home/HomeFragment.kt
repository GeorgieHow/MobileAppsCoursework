package com.example.mobileappscoursework.ui.home

import NumberPickerDialogFragment
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.mobileappscoursework.LoggingChunksActivity
import com.example.mobileappscoursework.databinding.FragmentHomeBinding
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.LayerDrawable
import android.util.Log
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.example.mobileappscoursework.R
import com.example.mobileappscoursework.api.PokemonListResponse
import com.example.mobileappscoursework.api.RetrofitClient
import com.example.mobileappscoursework.model.Pokemon
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.w3c.dom.Text
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    private var mAuth = FirebaseAuth.getInstance()
    private var db = FirebaseFirestore.getInstance()
    private var uid = mAuth.currentUser?.uid

    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val loggingButton: Button = binding.logItButton
        loggingButton.setOnClickListener {
            val intent = Intent(requireContext(), LoggingChunksActivity::class.java)
            startActivity(intent)
        }

        val lineChart: LineChart = binding.graphLineChart

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val welcomeMessage = view.findViewById<TextView>(R.id.welcome_text_view)
        uid?.let {

            val userDocument = db.collection("users").document(uid!!)

            userDocument.get().addOnSuccessListener { document ->
                if (document.exists()) {

                    val userFirstName = document.getString("firstName") ?: "User"
                    welcomeMessage.text = getString(R.string.home_welcome_message, userFirstName)

                } else {
                    welcomeMessage.text = getString(R.string.welcome_user)
                }
            }.addOnFailureListener {
                welcomeMessage.text = getString(R.string.welcome_user)
            }
        } ?: run {
            welcomeMessage.text = getString(R.string.welcome_user)
        }

        loadAndDisplayUserRank()
        loadAndDisplayTopTags()
        loadAndDisplayTodayHours()
        fetchRandomPokemon()
        loadWeeklyGoal()

        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout)
        swipeRefreshLayout.setOnRefreshListener {
            loadAndDisplayUserRank()
            loadAndDisplayTopTags()
            loadAndDisplayTodayHours()
            fetchRandomPokemon()
            loadWeeklyGoal()
            fetchWeeklyHours { dailyHours ->
                setupGraph(dailyHours)
            }
            swipeRefreshLayout.isRefreshing = false
        }

        val weeklyGoalText = view.findViewById<TextView>(R.id.weekly_goal_text)
        val changeWeeklyGoalButton = view.findViewById<Button>(R.id.change_weekly_goal_button)
        changeWeeklyGoalButton.setOnClickListener {
            fetchNumberDoneThisWeekFromFirebase { totalHoursThisWeek ->
                val numberPickerDialog = NumberPickerDialogFragment().apply {
                    listener = object : NumberPickerDialogFragment.NumberPickerDialogListener {
                        override fun onNumberPicked(number: Int) {
                            weeklyGoalText.text = "Weekly Goal:\n $totalHoursThisWeek / $number"
                            updateUserWeeklyGoal(number)
                        }
                    }
                }
                numberPickerDialog.show(parentFragmentManager, "numberPicker")
            }
        }

        fetchWeeklyHours { dailyHours ->
            setupGraph(dailyHours)
        }

    }

    private fun updateUserWeeklyGoal(weeklyGoal: Int) {
        uid?.let { userId ->
            val userDocument = FirebaseFirestore.getInstance().collection("users").document(userId)
            userDocument.update("weeklyGoal", weeklyGoal)
                .addOnSuccessListener {
                }
                .addOnFailureListener { e ->
                }
        }
    }
    private fun loadWeeklyGoal() {
        uid?.let { userId ->
            val userDocument = FirebaseFirestore.getInstance().collection("users").document(userId)
            userDocument.get().addOnSuccessListener { document ->
                if (document.exists()) {
                    val weeklyGoal = document.getLong("weeklyGoal")?.toInt()
                    fetchNumberDoneThisWeekFromFirebase { totalHoursThisWeek ->
                        if (weeklyGoal != null) {
                            binding.weeklyGoalText.text = "Weekly Goal:\n $totalHoursThisWeek / $weeklyGoal"
                        } else {
                            binding.weeklyGoalText.text = "Set a goal!"
                        }
                    }
                } else {
                    binding.weeklyGoalText.text = "Set a goal!"
                }
            }.addOnFailureListener { e ->
                binding.weeklyGoalText.text = "Set a goal!"
            }
        }
    }

    private fun loadAndDisplayUserRank() {
        val usersCollection = db.collection("users")
        usersCollection.get().addOnSuccessListener { documents ->
            val userTotals = documents.mapNotNull { doc ->
                doc.id to (doc.getLong("totalHours") ?: 0)
            }.toMap()

            val sortedTotals = userTotals.toList().sortedByDescending { it.second }
            val currentUserRank = sortedTotals.indexOfFirst { it.first == uid } + 1

            updateLeaderboardPositionText(currentUserRank)
        }.addOnFailureListener {
            updateLeaderboardPositionText(null)
        }
    }

    private fun updateLeaderboardPositionText(rank: Int?) {
        val rankText = if (rank != null) {
            "You are currently ${ordinal(rank)} in the leaderboard!"
        } else {
            "Unable to retrieve leaderboard position."
        }
        binding.leaderboardText.text = rankText
    }
    private fun ordinal(number: Int): String {
        val suffixes = arrayOf("th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th")
        return when (number % 100) {
            11, 12, 13 -> number.toString() + "th"
            else -> number.toString() + suffixes[number % 10]
        }
    }

    private fun loadAndDisplayTopTags() {
        val tagsCount = mutableMapOf<String, Int>()
        uid?.let { userId ->
            val logsCollection = db.collection("users").document(userId).collection("logs")
            logsCollection.get().addOnSuccessListener { documents ->
                for (document in documents) {
                    val tags = document.get("tags") as List<String>?
                    tags?.forEach { tag ->
                        tagsCount[tag] = tagsCount.getOrDefault(tag, 0) + 1
                    }
                }
                val topTags = tagsCount.entries.sortedByDescending { it.value }.map { it.key }.take(3)
                val filledTopTags = if (topTags.size < 3) {
                    topTags + List(3 - topTags.size) { "N/A" }
                } else {
                    topTags
                }
                updateTagsView(filledTopTags)
            }.addOnFailureListener {
                updateTagsView(listOf("N/A", "N/A", "N/A"))
            }
        }
    }

    private fun updateTagsView(tags: List<String>) {
        val tagsText = "Your popular tags:\n${tags.joinToString("\n")}"
        binding.popularTagsDisplayText.text = tagsText
    }

    private fun loadAndDisplayTodayHours() {
        uid?.let { userId ->
            val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val todayString = formatter.format(Date())

            val logsCollection = db.collection("users").document(userId).collection("logs")
            logsCollection.whereEqualTo("date", todayString).get().addOnSuccessListener { documents ->
                var totalHoursToday = 0
                for (document in documents) {
                    val hours = document.getLong("hours")?.toInt() ?: 0
                    totalHoursToday += hours
                }
                updateHoursTodayView(totalHoursToday)
            }.addOnFailureListener {
                updateHoursTodayView(0)
            }
        }
    }
    private fun updateHoursTodayView(hours: Int) {
        val hoursText = "Hours done today: $hours hours."
        binding.hoursDoneTextView.text = hoursText
    }

    private fun fetchRandomPokemon() {
        val maxPokemonId = 800
        val randomPokemonId = (1..maxPokemonId).random()

        fetchPokemonDetailsById(randomPokemonId)
    }

    private fun fetchPokemonDetailsById(pokemonId: Int) {
        RetrofitClient.service.getPokemonById(pokemonId).enqueue(object : Callback<Pokemon> {
            override fun onResponse(call: Call<Pokemon>, response: Response<Pokemon>) {
                if (response.isSuccessful) {
                    response.body()?.let { pokemon ->
                        updatePokemonDetails(pokemon)
                    }
                } else {
                    binding.pokemonNameTextView.text = "Error Fetching Pokemon."
                }
            }

            override fun onFailure(call: Call<Pokemon>, t: Throwable) {
            }
        })
    }

    private fun updatePokemonDetails(pokemon: Pokemon) {
        binding.pokemonNameTextView.text = pokemon.name
        binding.pokemonWeightTextView.text = "${pokemon.weight} kg"
        binding.pokemonTypeTextView.text = pokemon.types.joinToString { it.type.name.capitalize() }
        binding.pokemonImageView.loadImage(pokemon.sprites.frontDefault)
    }
    private fun ImageView.loadImage(url: String) {
        Glide.with(this.context)
            .load(url)
            .into(this)
    }

    private fun fetchNumberDoneThisWeekFromFirebase(callback: (Int) -> Unit) {
        val calendar = Calendar.getInstance()
        calendar.firstDayOfWeek = Calendar.MONDAY
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.clear(Calendar.MINUTE)
        calendar.clear(Calendar.SECOND)
        calendar.clear(Calendar.MILLISECOND)


        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        val startOfWeek = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)

        calendar.add(Calendar.WEEK_OF_YEAR, 1)
        calendar.add(Calendar.DATE, -1)
        val endOfWeek = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)

        Log.d("Start", " $startOfWeek")
        Log.d("End", " $endOfWeek")

        if (uid != null) {
            val userLogsRef = FirebaseFirestore.getInstance()
                .collection("users").document(uid!!)
                .collection("logs")

            userLogsRef.whereGreaterThanOrEqualTo("sortableDate", startOfWeek)
                .whereLessThan("sortableDate", endOfWeek)
                .get()
                .addOnSuccessListener { documents ->
                    var totalHours = 0
                    for (document in documents) {
                        val hours = document.getLong("hours") ?: 0
                        totalHours += hours.toInt()
                    }
                    Log.d("FirestoreQuery", "Total hours this week: $totalHours")
                    callback(totalHours)
                }
                .addOnFailureListener { exception ->
                    Log.e("FirestoreQuery", "Error fetching hours: $exception")
                    callback(0)
                }
        } else {
            Log.e("FirestoreQuery", "User ID is null, cannot fetch logs.")
            callback(0)
        }
    }

    private fun fetchWeeklyHours(callback: (Map<String, Float>) -> Unit) {
        val calendar = Calendar.getInstance()
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dayNameFormatter = SimpleDateFormat("EEEE", Locale.UK)

        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        val startDate = formatter.format(calendar.time)

        calendar.add(Calendar.DATE, 6)
        val endDate = formatter.format(calendar.time)

        val dailyHours = mutableMapOf<String, Float>()

        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        for (i in 0 until 7) {
            val dayDate = calendar.time
            val dayName = dayNameFormatter.format(dayDate)
            dailyHours[dayName] = 0f
            calendar.add(Calendar.DATE, 1)
        }

        uid?.let { userId ->
            FirebaseFirestore.getInstance().collection("users").document(userId).collection("logs")
                .whereGreaterThanOrEqualTo("sortableDate", startDate)
                .whereLessThanOrEqualTo("sortableDate", endDate)
                .get()
                .addOnSuccessListener { documents ->
                    documents.forEach { doc ->
                        val date = doc.getString("sortableDate")
                        val hours = doc.getLong("hours")?.toFloat() ?: 0f
                        if (date != null) {
                            val dayOfWeek = dayNameFormatter.format(formatter.parse(date)!!)
                            dailyHours[dayOfWeek] = dailyHours.getOrDefault(dayOfWeek, 0f) + hours
                        }
                    }
                    callback(dailyHours)
                }.addOnFailureListener {
                    callback(dailyHours)
                }
        }
    }

    private fun setupGraph(dailyHours: Map<String, Float>) {
        val daysOfWeek = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
        val entries = daysOfWeek.mapIndexed { index, day ->
            Entry(index.toFloat(), dailyHours.getOrElse(day) { 0f })
        }

        val dataSet = LineDataSet(entries, "Hours Spent")
        dataSet.color = Color.BLUE
        dataSet.valueTextColor = Color.BLACK
        dataSet.lineWidth = 2f
        dataSet.setCircleColor(Color.BLUE)

        val lineData = LineData(dataSet)
        binding.graphLineChart.apply {
            data = lineData
            description.text = "Hours logged each day"
            xAxis.valueFormatter = IndexAxisValueFormatter(daysOfWeek)
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.granularity = 1f
            xAxis.setLabelCount(daysOfWeek.size, true)
            xAxis.labelRotationAngle = -45f

            axisRight.isEnabled = false
            axisLeft.axisMinimum = 0f
            axisLeft.granularity = 1f
            invalidate()
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}