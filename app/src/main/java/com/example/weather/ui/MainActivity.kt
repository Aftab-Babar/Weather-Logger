package com.example.weather.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.navigation.fragment.NavHostFragment
import com.example.weather.R
import com.example.weather.core.BaseActivity
import com.example.weather.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : BaseActivity<MainViewModel>(), LocationListener {

    private var binding: ActivityMainBinding? = null
    private var navHostFragment: NavHostFragment? = null

    private var locationManager: LocationManager? = null

    private var isMonitoringLocation = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        navHostFragment = supportFragmentManager.findFragmentById(R.id.container) as NavHostFragment
    }

    override fun getViewModel(): Class<MainViewModel> = MainViewModel::class.java

    override fun addObserver() {
    }


    override fun onLocationChanged(p0: Location) {
    }

    override fun onDestroy() {
        locationManager?.removeUpdates(this)
        super.onDestroy()
    }

    fun startLocationMonitoring() {
        if (!isMonitoringLocation){
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // here to request the missing permissions, and then overriding
                // public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            locationManager?.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, 30000 /*3 Seconds*/, 0F, this
            )
            isMonitoringLocation = true
        }
    }
}