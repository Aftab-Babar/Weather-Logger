package com.example.weather.ui.weather_report

import android.Manifest
import android.content.pm.PackageManager
import android.icu.util.Calendar
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.example.weather.core.BaseFragment
import com.example.weather.databinding.FragmentWeatherReportBinding
import com.example.weather.models.WeatherReport
import com.example.weather.utils.Constants.BUNDLE_WEATHER
import com.example.weather.utils.Constants.UNIT_METRIC
import com.example.weather.utils.Resource
import com.example.weather.utils.Tools.getLastKnownLocation
import com.example.weather.utils.Tools.isLocationEnabled
import com.example.weather.utils.Tools.localToGMT
import com.google.android.material.color.MaterialColors
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import dagger.hilt.android.AndroidEntryPoint
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

@AndroidEntryPoint
class WeatherReportFragment :
    BaseFragment<FragmentWeatherReportBinding>(FragmentWeatherReportBinding::inflate) {

    private val viewModel by viewModels<WeatherReportViewModel>()
    private val adapter by lazy { ReportRecyclerAdapter() }

    //private var report: WeatherReport? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.getWeatherReports()


    }

    override fun initView() {

        binding.rvReport.adapter = adapter

        arguments?.getString(BUNDLE_WEATHER)?.let {
            try {
                val report = Gson().fromJson(it, WeatherReport::class.java)
                viewModel.setWeatherReport(report)
            } catch (jse: JsonSyntaxException) {
                showErrorMessage(jse.message)
            }

        }

        view?.let {
            binding.swipeContainer.setColorSchemeColors(
                MaterialColors.getColor(
                    it,
                    androidx.appcompat.R.attr.colorPrimary
                ), MaterialColors.getColor(it, androidx.appcompat.R.attr.colorPrimaryDark)
            )
        }


    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun initListener() {

        binding.ivBack.setOnClickListener {
            activity?.onBackPressed()
        }

        binding.ivSave.setOnClickListener {
            val date = DateTimeFormat.forPattern("dd/MM/yyyy hh:mm a")
                .print(DateTime(Calendar.getInstance().time))

            viewModel.insertReport(date.localToGMT() ?: date)
        }

        binding.swipeContainer.setOnRefreshListener {
            context?.let { itContext ->

                if (ActivityCompat.checkSelfPermission(
                        itContext,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_DENIED
                ) {
                    showErrorMessage("Please grant location permission.")
                    binding.swipeContainer.isRefreshing = false
                    return@setOnRefreshListener
                }

                if (!itContext.isLocationEnabled()) {
                    showErrorMessage("Please turn on location.")
                    binding.swipeContainer.isRefreshing = false
                    return@setOnRefreshListener
                }

            }

            val location = context?.getLastKnownLocation()
            if (location == null) {
                showErrorMessage("Unable to get the location, try again later.")
                binding.swipeContainer.isRefreshing = false
                return@setOnRefreshListener
            }
            viewModel.getWeatherReport(
                location.latitude.toString(),
                location.longitude.toString(),
                UNIT_METRIC
            )

        }
    }

    override fun addObserver() {
        viewModel.weatherReportLiveData.observe(viewLifecycleOwner) {
            if (it != null) {
                binding.swipeContainer.isRefreshing = it is Resource.Loading
                when (it) {
                    is Resource.Success -> {

                        it.data?.let { report -> setWeatherReport(report) }
                        binding.ivSave.performClick()
                    }
                    is Resource.Error -> {
                        showErrorMessage(it.message)
                    }
                    else -> {}
                }
            }
        }

        viewModel.weatherReportsLiveData.observe(viewLifecycleOwner) {
            if (it != null) {
                binding.pbReport.isVisible = it is Resource.Loading
                when (it) {
                    is Resource.Success -> {

                        binding.cvReport.isVisible = !it.data.isNullOrEmpty()

                        if (!it.data.isNullOrEmpty()){
                            adapter.submitList(it.data.toMutableList())
                        }

                    }
                    is Resource.Error -> {
                        showErrorMessage(it.message)
                    }
                    else -> {}
                }
            }
        }

        viewModel.insertReportLiveData.observe(viewLifecycleOwner) {
            if (it != null) {

                when (it) {
                    is Resource.Success -> {
                        showErrorMessage("Report saved successfully.")
                    }
                    is Resource.Error -> {
                        showErrorMessage(it.message)
                    }
                    else -> {}
                }
            }
        }
    }

    private fun setWeatherReport(report: WeatherReport) {
        binding.tvTitle.text = report.name
        binding.tvTemperature.text = "${report.main?.temp?.toInt()?.toString() ?: "-"}\u00B0"
        binding.tvWeatherType.text = report.weather?.firstOrNull()?.main ?: "-"

        binding.tvHigh.text = "${report.main?.tempMax?.toInt()?.toString() ?: "-"}\u00B0"
        binding.tvLow.text = "${report.main?.tempMin?.toInt()?.toString() ?: "-"}\u00B0"

        binding.tvHumidity.text = report.main?.humidity?.toString() ?: "-"



    }
}