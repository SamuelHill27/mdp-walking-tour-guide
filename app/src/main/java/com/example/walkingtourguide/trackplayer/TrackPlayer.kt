package com.example.walkingtourguide.trackplayer

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.example.cwk_mwe.AudioPlayer
import com.example.walkingtourguide.R
import com.example.walkingtourguide.database.AppDatabase
import com.example.walkingtourguide.database.Bookmark
import com.example.walkingtourguide.data.FileItem
import com.example.walkingtourguide.trackplayer.AudioService.LocalBinder
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch


class TrackPlayer : AppCompatActivity() {
    private lateinit var trackTextView: TextView
    private lateinit var audioService: AudioService
    private var isBound: Boolean = false
    private lateinit var db: AppDatabase

    // Used framework from android developers: https://developer.android.com/develop/background-work/services/bound-services
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(
            name: ComponentName?,
            service: IBinder?
        ) {
            val binder = service as LocalBinder
            audioService = binder.getService()
            isBound = true

            if (intent.hasExtra("file_item")) {
                audioService.loadTrack(intent.getSerializableExtra("file_item", FileItem::class.java))
            }
            if (intent.hasExtra("timestamp")) {
                audioService.setTrackProgress(intent.getIntExtra("timestamp", 0))
            }

            db = Room.databaseBuilder(
                applicationContext,
                AppDatabase::class.java, getString(R.string.database_name)
            ).allowMainThreadQueries().build()

            trackTextView.text = audioService.audioFile?.filename
            updateProgressBar()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_track_player)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<View>(R.id.trackPlayerBackBtn).setOnClickListener {
            finish()
        }

        trackTextView = findViewById(R.id.trackPlayerTrackTitle)

        val sharedPref = getSharedPreferences("settings", MODE_PRIVATE)
        val playbackSpeed = sharedPref.getFloat(
            "playback_speed",
            getString(R.string.playback_speed_default).toFloat()
        )
        findViewById<TextView>(R.id.playbackSpeedTextView).text =
            getString(R.string.playback_speed_display, playbackSpeed.toString())

        initPlayerControls()
    }

    private fun initPlayerControls() {
        findViewById<View>(R.id.playBtn).setOnClickListener {
            audioService.play()
        }
        findViewById<View>(R.id.pauseBtn).setOnClickListener {
            audioService.pause()
        }
        findViewById<View>(R.id.stopBtn).setOnClickListener {
            audioService.stop()
            trackTextView.text = ""
        }
        findViewById<View>(R.id.prevBtn).setOnClickListener {
            if (audioService.audioFile?.prevFile != null &&
                (audioService.getPlayerState() == AudioPlayer.AudioPlayerState.PLAYING ||
                        audioService.getPlayerState() == AudioPlayer.AudioPlayerState.PAUSED)) {
                audioService.loadTrack(audioService.audioFile?.prevFile)
                trackTextView.text = audioService.audioFile?.filename
            }
        }
        findViewById<View>(R.id.nextBtn).setOnClickListener {
            if (audioService.audioFile?.nextFile != null &&
                (audioService.getPlayerState() == AudioPlayer.AudioPlayerState.PLAYING ||
                        audioService.getPlayerState() == AudioPlayer.AudioPlayerState.PAUSED)) {
                audioService.loadTrack(audioService.audioFile?.nextFile)
                trackTextView.text = audioService.audioFile?.filename
            }
        }
        findViewById<View>(R.id.bookmarkBtn).setOnClickListener {
            if (audioService.audioFile != null) {
                db.bookmarkDao().insertAll(
                    Bookmark(
                        filename = audioService.audioFile!!.filename,
                        timestamp = audioService.getTrackProgress()
                    )
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        Intent(this, AudioService::class.java).also { intent ->
            startService(intent)
            bindService(intent, connection, BIND_AUTO_CREATE)
        }
    }

    override fun onStop() {
        super.onStop()
        unbindService(connection)
        isBound = false
    }

    fun updateProgressBar() {
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        progressBar.max = audioService.audioFile?.duration ?: 0

        lifecycleScope.launch() {
            while (isActive) {
                progressBar.progress = audioService.getTrackProgress()
                delay(100)
            }
        }
    }
}