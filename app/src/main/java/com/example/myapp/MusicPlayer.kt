package com.example.myapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.io.File

class MusicPlayer : AppCompatActivity() {


    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var SongName: TextView
    private lateinit var TrackDuration: TextView
    private lateinit var CurTime: TextView
    private lateinit var volumeSeekBar: SeekBar
    private lateinit var timerSeekBar: SeekBar
    private lateinit var pauseButton: Button
    private lateinit var nextButton: Button
    private lateinit var prevButton: Button
    private lateinit var volumePercent: TextView


    private val handler = Handler(Looper.getMainLooper())
    private val trackList = mutableListOf<Uri>()
    private var currentTrackIndex = 0
    private var isPlaying = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_music_player)


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initializeUI()

        setupButtonListeners()

        requestStoragePermission()
    }


    private fun initializeUI() {
        SongName = findViewById(R.id.SongName)
        volumeSeekBar = findViewById(R.id.volumeSeekBar)
        timerSeekBar = findViewById(R.id.timerSeekBar)
        pauseButton = findViewById(R.id.B_Pause)
        nextButton = findViewById(R.id.B_NEXT)
        prevButton = findViewById(R.id.B_PREV)
        TrackDuration = findViewById(R.id.TrackDuration)
        CurTime = findViewById(R.id.CurTime)
        volumePercent = findViewById(R.id.volumePercent)
    }


    private fun setupButtonListeners() {
        pauseButton.setOnClickListener { togglePlayPause() }
        nextButton.setOnClickListener { playNextTrack() }
        prevButton.setOnClickListener { playPreviousTrack() }
    }

    private fun requestStoragePermission() {
        if (checkSelfPermission(Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            loadStaticFolderTracks()
        } else {
            requestPermissions(arrayOf(Manifest.permission.READ_MEDIA_AUDIO), 1)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Разрешение получено", Toast.LENGTH_SHORT).show()
                loadStaticFolderTracks()
            } else {
                Toast.makeText(this, "Разрешение отклонено", Toast.LENGTH_SHORT).show()
            }
        }
    }


    @SuppressLint("Range")
    private fun loadStaticFolderTracks() {

        val folderPath = "/storage/emulated/0/Music/"

        val collection = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)


        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.DATA
        )

        val selection = "${MediaStore.Audio.Media.DATA} LIKE ?"
        val selectionArgs = arrayOf("%$folderPath%")

        contentResolver.query(collection, projection, selection, selectionArgs, null)?.use { cursor ->

            while (cursor.moveToNext()) {

                val dataColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DATA)
                val displayNameColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME)

                val filePath = cursor.getString(dataColumn)
                val displayName = cursor.getString(displayNameColumn)
                val file = File(filePath)

                val uri = Uri.fromFile(file)

                trackList.add(uri)
            }
        }

        if (trackList.isNotEmpty()) {
            currentTrackIndex = 0
            playCurrentTrack()
        } else {
            Toast.makeText(this, "Нет треков в указанной папке", Toast.LENGTH_LONG).show()
        }
    }

    private fun playCurrentTrack() {
        try {
            if (::mediaPlayer.isInitialized) mediaPlayer.release()

            val uri = trackList[currentTrackIndex]
            mediaPlayer = MediaPlayer.create(this, uri)
            mediaPlayer.setVolume(0.5f, 0.5f)

            updateTrackInfo(uri)
            mediaPlayer.start()
            isPlaying = true
            startUpdatingUI()

            mediaPlayer.setOnCompletionListener {
                playNextTrack()
            }

            setupVolumeSeekBar()
            setupTimerSeekBar()

        } catch (e: Exception) {
            Toast.makeText(this, "Ошибка воспроизведения", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateTrackInfo(uri: Uri) {
        val file = File(uri.path ?: "")
        SongName.text = file.nameWithoutExtension

        mediaPlayer.duration.takeIf { it > 0 }?.let {
            TrackDuration.text = formattedDuration(it)
            timerSeekBar.max = it
        }
    }

    private fun setupVolumeSeekBar() {
        volumeSeekBar.progress = 50
        volumePercent.text = "50%"

        volumeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    val volume = progress / 100f
                    mediaPlayer.setVolume(volume, volume)
                    volumePercent.text = "$progress%"
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun setupTimerSeekBar() {
        timerSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress)
                    CurTime.text = formattedDuration(progress)
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun togglePlayPause() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.pause()
            isPlaying = false
            handler.removeCallbacks(updateSeekBar)
        } else {
            mediaPlayer.start()
            isPlaying = true
            startUpdatingUI()
        }
    }

    private fun playNextTrack() {
        if (trackList.isNotEmpty()) {
            currentTrackIndex = (currentTrackIndex + 1) % trackList.size
            playCurrentTrack()
        }
    }

    private fun playPreviousTrack() {
        if (trackList.isNotEmpty()) {
            currentTrackIndex = (currentTrackIndex - 1 + trackList.size) % trackList.size
            playCurrentTrack()
        }
    }

    private val updateSeekBar: Runnable = object : Runnable {
        override fun run() {
            if (::mediaPlayer.isInitialized && mediaPlayer.isPlaying) {
                val currentPosition = mediaPlayer.currentPosition
                timerSeekBar.progress = currentPosition
                CurTime.text = formattedDuration(currentPosition)
            }
            handler.postDelayed(this, 1000)
        }
    }

    private fun startUpdatingUI() {
        handler.post(updateSeekBar)
    }

    private fun formattedDuration(duration: Int): String {
        val minutes = (duration / 1000) / 60
        val seconds = (duration / 1000) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::mediaPlayer.isInitialized) {
            mediaPlayer.release()
        }
        handler.removeCallbacks(updateSeekBar)
    }
}