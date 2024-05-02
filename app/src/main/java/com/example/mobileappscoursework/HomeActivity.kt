package com.example.mobileappscoursework

import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.viewpager2.widget.ViewPager2
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.mobileappscoursework.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.example.mobileappscoursework.adapter.NavigationAdapter
import com.example.mobileappscoursework.ui.leaderboard.LeaderboardWorker
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.Calendar
import java.util.concurrent.TimeUnit

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var mAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val adapter = NavigationAdapter(this)
        binding.navHostFragmentActivityMain.adapter = adapter

        val logOutButton = findViewById<FloatingActionButton>(R.id.log_out_button)
        logOutButton.setOnClickListener{
            mAuth.signOut()
            finish()
        }

        scheduleLeaderboardUpdate()
        setupBottomNavigationView()
    }

    private fun scheduleLeaderboardUpdate() {
        val currentDay = Calendar.getInstance()
        var daysUntilSunday = Calendar.SUNDAY - currentDay.get(Calendar.DAY_OF_WEEK)
        if (daysUntilSunday <= 0) {
            daysUntilSunday += 7
        }
        val initialDelay = daysUntilSunday * 24 * 60 * 60 * 1000L
        val workRequest = PeriodicWorkRequestBuilder<LeaderboardWorker>(7, TimeUnit.DAYS)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(this).enqueue(workRequest)
    }

    private fun setupBottomNavigationView() {
        binding.navView.setOnItemSelectedListener { item ->
            binding.navHostFragmentActivityMain.currentItem = when (item.itemId) {
                R.id.navigation_home -> 0
                R.id.navigation_leaderboard -> 1
                //R.id.navigation_groups -> 2
                R.id.navigation_logs -> 2
                R.id.navigation_profile -> 3
                else -> 0
            }
            true
        }

        binding.navHostFragmentActivityMain.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                binding.navView.menu.getItem(position).isChecked = true
            }
        })
    }
}
