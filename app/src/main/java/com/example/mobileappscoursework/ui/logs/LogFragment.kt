package com.example.mobileappscoursework.ui.logs

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mobileappscoursework.LogDetailsActivity
import com.example.mobileappscoursework.R
import com.example.mobileappscoursework.adapter.LogRecyclerAdapter
import com.example.mobileappscoursework.databinding.FragmentLogsBinding
import com.example.mobileappscoursework.model.LogEntry
import com.example.mobileappscoursework.ui.leaderboard.LeaderboardViewModel
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

class LogFragment : Fragment() {

    private var _binding: FragmentLogsBinding? = null
    private val binding get() = _binding!!
    private lateinit var logRecyclerAdapter: LogRecyclerAdapter
    private lateinit var dateRangeTextView: TextView

    private val userId by lazy { FirebaseAuth.getInstance().currentUser?.uid }
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val dashboardViewModel =
            ViewModelProvider(this)[LogViewModel::class.java]

        _binding = FragmentLogsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        logRecyclerAdapter = LogRecyclerAdapter(mutableListOf(), this::handleLogClick)
        binding.previousLogsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = logRecyclerAdapter
        }

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val today = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val formattedDate = today.format(formatter)

        dateRangeTextView = view.findViewById<TextView>(R.id.date_range_text_view)

        val refreshButton = view.findViewById<FloatingActionButton>(R.id.refresh_button)
        refreshButton.setOnClickListener{
            loadLogs("1970-01-01", formattedDate)
            dateRangeTextView.text = getString(R.string.currently_showing_all)
        }

        val rangeDatePickerButton = view.findViewById<Button>(R.id.range_date_button)
        rangeDatePickerButton.setOnClickListener {
            val dateRangePicker = MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText("Select dates")
                .build()

            dateRangePicker.addOnPositiveButtonClickListener { datePair ->
                val startDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(datePair.first))
                val endDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(datePair.second))

                val displayFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val displayStartDate = displayFormat.format(Date(datePair.first))
                val displayEndDate = displayFormat.format(Date(datePair.second))

                loadLogs(startDate, endDate)
                dateRangeTextView.text = getString(R.string.date_range, displayStartDate.toString(), displayEndDate.toString())
            }

            dateRangePicker.show(childFragmentManager, "date_range_picker")
        }

        loadLogs("1970-01-01", formattedDate)
    }


    private fun loadLogs(startDate: String, endDate: String) {
        userId?.let { userId ->
            val logsCollection = db.collection("users").document(userId).collection("logs")
            logsCollection
                .whereGreaterThanOrEqualTo("sortableDate", startDate)
                .whereLessThanOrEqualTo("sortableDate", endDate)
                .orderBy("sortableDate", Query.Direction.DESCENDING).get()
                .addOnSuccessListener { documents ->
                    val logs = documents.mapNotNull { document ->
                        val date = document.getString("date")
                        val title = document.getString("title")
                        val description = document.getString("description") ?: ""
                        val hours = document.getLong("hours")?.toInt() ?: 0
                        val tags = document.get("tags") as? List<String> ?: emptyList()
                        val location = document.getString("location") ?: ""
                        val imageUri = document.getString("imageUrl") ?: " "
                        if (date != null && title != null) {
                            LogEntry(document.id,title, description, date, hours, tags, location, imageUri)
                        } else {
                            null
                        }
                    }
                    if (logs.isNotEmpty()) {
                        logRecyclerAdapter.updateData(logs)
                    }
                }
                .addOnFailureListener { exception ->

                }
        }
    }

    private fun handleLogClick(logEntry: LogEntry) {
        val intent = Intent(activity, LogDetailsActivity::class.java).apply {
            putExtra("logID", logEntry.id)
            putExtra("logDate", logEntry.date)
            putExtra("logTitle", logEntry.title)
            putExtra("logDescription", logEntry.description)
            putExtra("logHours", logEntry.hours)
            putExtra("logTags", ArrayList(logEntry.tags))
            putExtra("logLocation", logEntry.location)
            putExtra("logImage", logEntry.imageUri)
        }
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

