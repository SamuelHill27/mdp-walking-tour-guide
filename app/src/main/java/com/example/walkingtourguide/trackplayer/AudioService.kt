package com.example.walkingtourguide.trackplayer

import android.Manifest.permission.POST_NOTIFICATIONS
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.cwk_mwe.AudioPlayer
import com.example.walkingtourguide.R
import com.example.walkingtourguide.data.FileItem

class AudioService : Service() {
    private val binder = LocalBinder()
    private var audioPlayer: AudioPlayer = AudioPlayer()
    var audioFile: FileItem? = null
    private lateinit var sharedPref: SharedPreferences
    private val sharedPrefListener =
        SharedPreferences.OnSharedPreferenceChangeListener { prefs, key ->
            if (key == "playback_speed") {
                audioPlayer.setPlaybackSpeed(
                    prefs.getFloat("playback_speed", getString(R.string.playback_speed_default).toFloat()))
            }
        }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        sharedPref = getSharedPreferences("settings", MODE_PRIVATE)
        sharedPref.registerOnSharedPreferenceChangeListener(sharedPrefListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        sharedPref.unregisterOnSharedPreferenceChangeListener(sharedPrefListener)
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {
        return START_STICKY
    }

    fun loadTrack(track: FileItem?) {
        val playbackSpeed = sharedPref.getFloat("playback_speed", getString(R.string.playback_speed_default).toFloat())

        if (audioFile != null && audioFile?.filename.equals(track?.filename)) {
            return
        }

        audioFile = track
        audioPlayer.stop()
        audioPlayer.load(getString(R.string.audio_path) + audioFile?.filename, playbackSpeed)
        play()
    }

    fun play() {
        audioPlayer.play()
        with(NotificationManagerCompat.from(this)) {
            if (checkSelfPermission(POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                startForeground(1, buildMediaNotification())
            }
        }
    }

    fun pause() {
        audioPlayer.pause()
    }

    fun stop() {
        audioPlayer.stop()
        stopForeground(STOP_FOREGROUND_REMOVE)
        audioFile = null
        stopSelf();
    }

    fun getPlayerState(): AudioPlayer.AudioPlayerState {
        return audioPlayer.state ?: AudioPlayer.AudioPlayerState.ERROR
    }

    fun getTrackProgress(): Int {
        return audioPlayer.progress
    }

    fun setTrackProgress(time: Int) {
        audioPlayer.skipTo(time)
    }

    // Modified from android developers: https://developer.android.com/develop/ui/views/notifications/build-notification
    fun buildMediaNotification(): Notification {
        val intent = Intent(this, TrackPlayer::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(this, getString(R.string.channel_id))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Playing Track:")
            .setContentText(audioFile?.filename)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setSilent(true)
            .build()
    }

    inner class LocalBinder : Binder() {
        fun getService(): AudioService = this@AudioService
    }
}