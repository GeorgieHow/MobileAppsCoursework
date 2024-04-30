package com.example.mobileappscoursework.ui.logs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mobileappscoursework.R
import com.example.mobileappscoursework.adapter.LogRecyclerAdapter
import com.example.mobileappscoursework.databinding.FragmentLogsBinding
import com.example.mobileappscoursework.model.LogEntry
import com.example.mobileappscoursework.ui.leaderboard.LeaderboardViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class LogFragment : Fragment() {

    private var _binding: FragmentLogsBinding? = null
    private val binding get() = _binding!!

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

        binding.previousLogsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = LogRecyclerAdapter(emptyList())
        }

        loadLogs()

        return root
    }



    private fun loadLogs() {
        userId?.let { userId ->
            val logsCollection = db.collection("users").document(userId).collection("logs")
            logsCollection.orderBy("date", Query.Direction.DESCENDING).get()
                .addOnSuccessListener { documents ->
                    val logs = documents.mapNotNull { document ->
                        val date = document.getString("date")
                        val title = document.getString("title")
                        val description = document.getString("description") ?: ""
                        val hours = document.getLong("hours")?.toInt() ?: 0
                        val tags = document.get("tags") as? List<String> ?: emptyList()
                        val location = document.getString("location") ?: ""
                        val imageUri = document.getString("imageUri") ?: " "
                        if (date != null && title != null) {
                            LogEntry(title, description, date, hours, tags, location, imageUri)
                        } else {
                            null
                        }
                    }
                    if (logs.isNotEmpty()) {
                        binding.previousLogsRecyclerView.adapter = LogRecyclerAdapter(logs)
                    }
                }
                .addOnFailureListener { exception ->

                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

