package com.example.soundrecorder

import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.SystemClock
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    lateinit var place: TextView
    lateinit var play: ImageView
    lateinit var stop: ImageView
    lateinit var startRecording: ImageView
    lateinit var stopRecordng: Button
    lateinit var playFile: Button
    lateinit var stopFile: Button
    lateinit var timer:Chronometer


    private var accept: Array<String> = arrayOf(
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
        android.Manifest.permission.RECORD_AUDIO
    )
    var pathofFile: String = ""
    private lateinit var recording: MediaRecorder
    private lateinit var player: MediaPlayer
    private var permissionNum: Int = 1000
    companion object{
        private const val timeFormating = "yyyyMMdd_HHmmss"
        fun newStance() = MainActivity()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //timer = recordTime
        if (!checkPermission()) {
            requestPermission()
        }
        setContentView(R.layout.activity_main)
        place = findViewById(R.id.textView2)
        play = findViewById(R.id.PlayButton)
        stop = findViewById(R.id.stop)
        startRecording = findViewById(R.id.RecordButton)
        stopRecordng = findViewById(R.id.StopRecording)
        playFile = findViewById(R.id.playRecFile)
        stopFile = findViewById(R.id.stopRecFile)
        timer = findViewById(R.id.recordTime)

        playFile.setOnClickListener {
            val file = "Livin' On a Prayer.m4a"
            val asset = assets.openFd(file)
            player = MediaPlayer()
            //reference:  https://www.codota.com/code/java/methods/android.content.res.AssetFileDescriptor/getFileDescriptor
            //reference: https://stackoverflow.com/questions/3289038/play-audio-file-from-the-assets-directory
            player.setDataSource(asset.fileDescriptor, asset.startOffset, asset.length)
            volumeControlStream = AudioManager.STREAM_MUSIC
            try{
                player.prepare()
                player.start()
                //https://stackoverflow.com/questions/10862845/how-to-set-android-chronometer-base-time-from-date-object
                timer.base = SystemClock.elapsedRealtime()
                timer.start()
                place.text = "Playing audio"
                playFile.isEnabled = false
            }catch (error:Exception){
                error.printStackTrace()
            }
        }

        stopFile.setOnClickListener {
            try{
                timer.stop()
                player.stop()
                player.prepare()
                player.release()
                playFile.isEnabled = true
            } catch (error:Exception){
                error.printStackTrace()
            }
        }

        startRecording.setOnClickListener {

            if (checkPermission()) {
                pathofFile = "${externalCacheDir?.absolutePath}/myRecording.3gp"
                setRecording()
                try {
                    recording.prepare()
                    recording.start()
                    timer.base = SystemClock.elapsedRealtime()
                    timer.start()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                play.isEnabled = false
                stop.isEnabled = false
                place.text = "Recording..."
            } else {
                requestPermission()
            }
        }

        stopRecordng.setOnClickListener {
            timer.stop()
            recording.stop()
            stopRecordng.isEnabled = false
            play.isEnabled = true
            stopRecordng.isEnabled = true
            stop.isEnabled = false
            place.text = "Stopping Recording "

        }

        play.setOnClickListener {
            stop.isEnabled = true
            stopRecordng.isEnabled = false
            startRecording.isEnabled = false
            player = MediaPlayer()
            try
            {
                player.setDataSource(pathofFile)
                player.prepare()
            } catch (e: IOException)
            {
                e.printStackTrace()
            }
            player.start()
            timer.base = SystemClock.elapsedRealtime()
            timer.start()
            place.text = "Playing...."
        }
        stop.setOnClickListener {
            stopRecordng.isEnabled = false
            startRecording.isEnabled = true
            stop.isEnabled = false
            play.isEnabled = true
            if(player != null)
            {
                timer.stop()
                player.stop()
                player.release()
                place.text="Stop Playing"
                setRecording()
            }
        }
    }

    private fun setRecording() {
        recording = MediaRecorder()
        recording.setAudioSource(MediaRecorder.AudioSource.MIC)
        recording.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
        recording.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB)
        recording.setOutputFile(pathofFile)
    }

    private fun getCurrentTime()= SimpleDateFormat(timeFormating).format(Calendar.getInstance().timeInMillis)

    private fun requestPermission() {
        ActivityCompat.requestPermissions(this, accept, permissionNum)
    }

    private fun checkPermission(): Boolean
    {
        val result: Int = ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE )
        val audiorecording: Int = ContextCompat.checkSelfPermission(this,
            android.Manifest.permission.RECORD_AUDIO)
        return result == PackageManager.PERMISSION_GRANTED &&
                audiorecording == PackageManager.PERMISSION_GRANTED
    }

    //https://stackoverflow.com/questions/49223343/asking-for-location-permission-grantresults-is-empty
    override fun onRequestPermissionsResult( requestCode: Int, permissions: Array<out String>, grantResults: IntArray){
        when (requestCode) {
            permissionNum -> if (grantResults.isNotEmpty() && grantResults[0]
                == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_LONG).show()
            } }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

}

