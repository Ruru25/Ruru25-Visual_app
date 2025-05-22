package com.example.myapp

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.location.Location
import android.location.LocationManager
import android.provider.Settings
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LocationActivity : AppCompatActivity() {



    private lateinit var bBackToMain: Button
    private lateinit var tvLat: TextView
    private lateinit var tvLon: TextView
    private lateinit var tvAlt: TextView
    private lateinit var tvTime: TextView

    private lateinit var myFusedLocationProviderClient: FusedLocationProviderClient

    companion object {

        private const val PERMISSION_REQUEST_ACCESS_LOCATION = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_location)


        bBackToMain = findViewById(R.id.back_to_main)
        myFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        tvLat = findViewById(R.id.tv_lat)
        tvLon = findViewById(R.id.tv_lon)
        tvAlt = findViewById(R.id.textView)
        tvTime = findViewById(R.id.textView2)
    }


    override fun onResume() {
        super.onResume()

        bBackToMain.setOnClickListener {
            val backToMain = Intent(this, MainActivity::class.java)
            startActivity(backToMain)
        }

        getCurrentLocation()
    }

    fun setCurentTime(ms: Long) : String{
        val seconds = (ms / 1000) % 60
        val minutes = (ms / 60000) % 60
        val hours = (ms / 3600000) % 24
        val timeNow = "${hours.toString()} ${minutes.toString()} ${seconds.toString()}"
        return (timeNow)
    }

    private fun getCurrentLocation() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    requestPermissions()
                    return
                }

                myFusedLocationProviderClient.lastLocation.addOnCompleteListener(this) { task ->
                    val location: Location? = task.result
                    if (location == null) {
                        Toast.makeText(applicationContext, "Проблемы с сигналом", Toast.LENGTH_SHORT).show()
                    } else {
                        tvLat.text = "Широта: ${location.latitude}"
                        tvLon.text = "Долгота: ${location.longitude}"
                        tvAlt.text = "Высота: ${"%.2f".format(location.altitude)} метров"
                        tvTime.text = "Время: ${setCurentTime(location.time)}"
                    }
                }
            } else {
                Toast.makeText(applicationContext, "Включите геолокацию в настройках", Toast.LENGTH_SHORT).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {

            tvLat.text = "Разрешение не предоставлено"
            tvLon.text = "Разрешение не предоставлено"
            tvAlt.text = "Разрешение не предоставлено"
            tvTime.text = "Разрешение не предоставлено"
            requestPermissions()
        }
    }


    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            PERMISSION_REQUEST_ACCESS_LOCATION
        )
    }

    private fun checkPermissions(): Boolean {
        if( ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED )
        {
            return true
        } else {
            return false
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_ACCESS_LOCATION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(applicationContext, "Разрешение получено", Toast.LENGTH_SHORT).show()
                getCurrentLocation()
            } else {
                Toast.makeText(applicationContext, "Пользователь отказал в разрешении", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager:LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }
}