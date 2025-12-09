package com.example.walkingtourguide.settings

import android.widget.EditText

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.walkingtourguide.R

class Settings : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<View>(R.id.settingsBackBtn).setOnClickListener {
            Log.d("SettingsActivity", "Button ${R.id.settingsBackBtn} pressed, returning to parent Activity")
            finish()
        }

        initThemeControl()
        initPlaybackSpeedControl()
    }

    private fun initThemeControl() {
        findViewById<View>(R.id.darkBtn).setOnClickListener {
            val sharedPref = getSharedPreferences("settings", MODE_PRIVATE)
            sharedPref.edit { putBoolean("is_dark_theme", true) }
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }

        findViewById<View>(R.id.lightBtn).setOnClickListener {
            val sharedPref = getSharedPreferences("settings", MODE_PRIVATE)
            sharedPref.edit { putBoolean("is_dark_theme", false) }
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }

    private fun initPlaybackSpeedControl() {
        val sharedPref = getSharedPreferences("settings", MODE_PRIVATE)

        val editTextPlaybackSpeed = findViewById<EditText>(R.id.editTextNumberDecimal)
        val playbackSpeed = sharedPref.getFloat("playback_speed", getString(R.string.playback_speed_default).toFloat())
        editTextPlaybackSpeed.setText(playbackSpeed.toString())

        findViewById<View>(R.id.setPlaybackSpeedBtn).setOnClickListener {
            sharedPref.edit { putFloat("playback_speed", editTextPlaybackSpeed.text.toString().toFloat()) }
        }
    }
}