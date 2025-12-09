package com.example.walkingtourguide.tracklist

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.example.walkingtourguide.R
import com.example.walkingtourguide.data.BookmarkItem
import com.example.walkingtourguide.data.FileItem
import com.example.walkingtourguide.database.AppDatabase
import com.example.walkingtourguide.database.Bookmark
import com.example.walkingtourguide.settings.Settings
import com.example.walkingtourguide.trackplayer.TrackPlayer
import java.io.File

private const val MY_PERMISSIONS_REQUEST_POST_NOTIFICATIONS = 2
private const val MY_PERMISSIONS_REQUEST_READ_MEDIA_AUDIO = 1

class TrackList : AppCompatActivity() {
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_track_list)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<Button>(R.id.playerBtn).setOnClickListener {
            startActivity(Intent(this, TrackPlayer::class.java))
        }
        findViewById<Button>(R.id.settingsBtn).setOnClickListener {
            startActivity(Intent(this, Settings::class.java))
        }

        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, getString(R.string.database_name)
        ).allowMainThreadQueries().build()

        handleReadMediaAudioPermissions()
        handlePostNotificationsPermissions()
    }

    override fun onResume() {
        super.onResume()
        listTracks()
        listBookmarks()
    }

    //Modified from android developers wiki: https://developer.android.com/training/permissions/requesting
    private fun handleReadMediaAudioPermissions() {
        when {
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.READ_MEDIA_AUDIO
            ) == PackageManager.PERMISSION_GRANTED -> {
                listTracks()
                listBookmarks()
            } else -> {
                ActivityCompat.requestPermissions(
                    this@TrackList,
                    arrayOf(Manifest.permission.READ_MEDIA_AUDIO),
                    MY_PERMISSIONS_REQUEST_READ_MEDIA_AUDIO
                )
            }
        }
    }

    //Modified from android developers wiki: https://developer.android.com/training/permissions/requesting
    private fun handlePostNotificationsPermissions() {
        when {
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED -> {
                createNotificationChannel()
            } else -> {
                ActivityCompat.requestPermissions(
                    this@TrackList,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    MY_PERMISSIONS_REQUEST_POST_NOTIFICATIONS
                )
            }
        }
    }

    //Modified from android developers wiki: https://developer.android.com/training/permissions/requesting
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_READ_MEDIA_AUDIO -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    listTracks()
                    listBookmarks()
                    handlePostNotificationsPermissions()
                } else {
                    Toast.makeText(this, "Permission DENIED", Toast.LENGTH_SHORT)
                        .show()
                }
                return
            }
            MY_PERMISSIONS_REQUEST_POST_NOTIFICATIONS -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    createNotificationChannel()
                } else {
                    Toast.makeText(this, "Permission DENIED", Toast.LENGTH_SHORT)
                        .show()
                }
                return
            }
        }
    }

    private fun listTracks() {
        val audioFiles = getAudioFiles(getString(R.string.audio_path))
        val onClickListener = View.OnClickListener { view -> onTrackClick(view) }

        val recyclerView: RecyclerView = findViewById(R.id.file_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val fileRecyclerViewAdapter = FileRecyclerViewAdapter(audioFiles, onClickListener)
        recyclerView.adapter = fileRecyclerViewAdapter
    }

    private fun listBookmarks() {
        val bookmarks = getBookmarks()
        val itemClickListener = View.OnClickListener { view -> onBookmarkClick(view) }
        val deleteClickListener = View.OnClickListener { view -> onBookmarkDelete(view) }

        val recyclerView: RecyclerView = findViewById(R.id.bookmark_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val bookmarkRecyclerViewAdapter = BookmarkRecyclerViewAdapter(bookmarks, itemClickListener, deleteClickListener)
        recyclerView.adapter = bookmarkRecyclerViewAdapter
    }

    private fun getAudioFiles(path: String) : List<FileItem> {
        val files = File(path).listFiles()

        if (files == null) {
            return listOf()
        }

        val audioFiles: List<FileItem> = files
            .filter { file -> file.isFile() }
            .map { file -> FileItem(file.name, getAudioDuration(file)) }

        audioFiles.forEachIndexed { index, file ->
            file.prevFile = audioFiles.getOrNull(index - 1)
            file.nextFile = audioFiles.getOrNull(index + 1)
        }

        return audioFiles
    }

    private fun getBookmarks() : List<BookmarkItem> {
        val bookmarkDao = db.bookmarkDao()
        val bookmarks: List<Bookmark> = bookmarkDao.getAll()
        val audioFiles: List<FileItem> = getAudioFiles(getString(R.string.audio_path))

        val bookmarkItems: List<BookmarkItem> = bookmarks.mapNotNull { bookmark ->
            val audioFile = audioFiles.find { it.filename == bookmark.filename }
            if (audioFile != null) {
                BookmarkItem(bookmark.uid , audioFile, bookmark.timestamp)
            } else {
                bookmarkDao.delete(bookmark)
                null
            }
        }

        return bookmarkItems
    }

    private fun getAudioDuration(audioFile: File): Int {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(audioFile.absolutePath)
        val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        return durationStr?.toIntOrNull() ?: -1
    }

    private fun onTrackClick(view: View) {
        val intent = Intent(this, TrackPlayer::class.java)
        val audioFile: FileItem = getAudioFiles(getString(R.string.audio_path))[view.tag as Int]
        intent.putExtra("file_item", audioFile)
        startActivity(intent)
    }

    private fun onBookmarkClick(view: View) {
        val intent = Intent(this, TrackPlayer::class.java)
        val bookmark: BookmarkItem = getBookmarks()[view.tag as Int]
        intent.putExtra("file_item", bookmark.fileItem)
        intent.putExtra("timestamp", bookmark.timeStamp)
        startActivity(intent)
    }

    private fun onBookmarkDelete(view: View) {
        val bookmarkDao = db.bookmarkDao()
        val bookmark = bookmarkDao.getAll().find { it.uid == getBookmarks()[view.tag as Int].uid }
        if (bookmark != null) {
            bookmarkDao.delete(bookmark)
            listBookmarks()
        }
    }

    private fun createNotificationChannel() {
        val name = getString(R.string.channel_id)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(getString(R.string.channel_id), name, importance)

        val notificationManager: NotificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}

