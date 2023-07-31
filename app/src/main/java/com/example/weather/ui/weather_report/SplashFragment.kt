package com.example.weather.ui.weather_report

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.example.weather.R
import com.example.weather.core.BaseFragment
import com.example.weather.databinding.FragmentSplashBinding
import com.example.weather.models.Main
import com.example.weather.ui.MainActivity
import com.example.weather.utils.Constants.BUNDLE_WEATHER
import com.example.weather.utils.Constants.UNIT_METRIC
import com.example.weather.utils.Resource
import com.example.weather.utils.Tools.convertToJsonString
import com.example.weather.utils.Tools.getLastKnownLocation
import com.example.weather.utils.Tools.isInternetAvailable
import com.example.weather.utils.Tools.isLocationEnabled
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class SplashFragment : BaseFragment<FragmentSplashBinding>(FragmentSplashBinding::inflate) {

    private val viewModel by viewModels<WeatherReportViewModel>()



    override fun initView() {
        requestLocationPermission.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
    }


    override fun initListener() {

        binding.ivRetry.setOnClickListener {
            requestLocationPermission.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
        }

    }

    override fun addObserver() {
        viewModel.weatherReportLiveData.observe(viewLifecycleOwner){
            if (it != null){

                binding.pb.isVisible = it is Resource.Loading
                when(it){

                    is Resource.Loading -> {
                        binding.ivRetry.isVisible = false
                    }

                    is Resource.Success -> {
                        navigate(R.id.action_splashFragment_to_weatherReportFragment, Bundle().apply {
                            putString(BUNDLE_WEATHER, it.data?.convertToJsonString())
                        })
                    }
                    is Resource.Error -> {
                        binding.ivRetry.isVisible = true
                        showErrorMessage(it.message)
                    }
                    else -> {}
                }
            }
        }

    }

    @SuppressLint("SuspiciousIndentation")
    private val requestLocationPermission =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {

            if (it?.get(Manifest.permission.ACCESS_FINE_LOCATION) == true) {

                //Start the location listener.
                when(val parentActivity = activity){
                    is MainActivity -> {
                        parentActivity.startLocationMonitoring()
                    }
                }

                if (context?.isLocationEnabled() == false){
                    showErrorMessage("Please turn on location.")
                    binding.ivRetry.isVisible = true
                    return@registerForActivityResult
                }

                if (context?.isInternetAvailable() == false){
                    viewModel.getLatestReport()
                    return@registerForActivityResult
                }
               val location = context?.getLastKnownLocation()
                       if (location != null){
                           viewModel.getWeatherReport(location.latitude.toString(), location.longitude.toString(), UNIT_METRIC)
                       }else {
                           binding.ivRetry.isVisible = true
                           showErrorMessage("Unable to get the location, try again later.")
                       }


            } else {
                binding.ivRetry.isVisible = true
                showErrorMessage("Please grant location permission.")
            }

        }


}