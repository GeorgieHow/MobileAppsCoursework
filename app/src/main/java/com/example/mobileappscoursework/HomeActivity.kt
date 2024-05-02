package com.example.mobileappscoursework

import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.viewpager2.widget.ViewPager2
import com.example.mobileappscoursework.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.example.mobileappscoursework.adapter.NavigationAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton

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

        setupBottomNavigationView()
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
