package com.example.trainn

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MenuItem
import android.widget.FrameLayout
import android.widget.ImageSwitcher
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView

class adminhome : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle
    private lateinit var imageSwitcher: ImageSwitcher
    private val images = arrayOf(
        R.drawable.dd,  // Add your drawable images here
        R.drawable.bb
    )
    private var currentIndex = 0
    private val handler = Handler(Looper.getMainLooper())
    private val switchInterval = 3000L  // 3 seconds interval

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.admin_home)

        // Initialize Views
        drawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        imageSwitcher = findViewById(R.id.image_switcher)

        setSupportActionBar(toolbar)

        // Setup Drawer Toggle
        actionBarDrawerToggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()
        actionBarDrawerToggle.drawerArrowDrawable.color = resources.getColor(R.color.white)

        // Load Shared Preferences
        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)

        // Set up Navigation Header
        val headerView = navView.getHeaderView(0)
        val adminNameTextView = headerView.findViewById<TextView>(R.id.userNameTextView)
        val adminEmailTextView = headerView.findViewById<TextView>(R.id.userEmailTextView)

        // Retrieve and set stored admin details
        val adminName = sharedPreferences.getString("username", "Admin")
        val adminEmail = sharedPreferences.getString("email", "admin@gmail.com")

        adminNameTextView.text = adminName
        adminEmailTextView.text = adminEmail

        // Handle Navigation Item Clicks
        navView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_manage_stations -> startActivity(Intent(this, HomeActivity::class.java))
                R.id.nav_manage_trains -> startActivity(Intent(this, Trainopration::class.java))
                R.id.nav_manage_routes -> startActivity(Intent(this, TrainRouteActivity::class.java))
                R.id.All_Bookings -> startActivity(Intent(this, AdminActivity::class.java))
                R.id.nav_logout -> logout()
            }
            drawerLayout.closeDrawers()
            true
        }

        // Initialize ImageSwitcher
        setupImageSwitcher()
        startImageSlideshow()
    }
    private fun setupImageSwitcher() {
        imageSwitcher.setFactory {
            val imageView = ImageView(this)
            imageView.scaleType = ImageView.ScaleType.CENTER_CROP
            imageView.layoutParams = FrameLayout.LayoutParams(  // Use FrameLayout.LayoutParams here
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            imageView
        }
        imageSwitcher.setImageResource(images[currentIndex])
    }


    private fun startImageSlideshow() {
        val runnable = object : Runnable {
            override fun run() {
                currentIndex = (currentIndex + 1) % images.size
                imageSwitcher.setImageResource(images[currentIndex])
                handler.postDelayed(this, switchInterval)
            }
        }
        handler.post(runnable)
    }

    // Logout Function
    private fun logout() {
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            true
        } else super.onOptionsItemSelected(item)
    }
}
