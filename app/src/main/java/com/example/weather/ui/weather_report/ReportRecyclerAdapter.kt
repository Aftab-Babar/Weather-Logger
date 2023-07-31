package com.example.weather.ui.weather_report

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.weather.databinding.ReportListLayoutBinding
import com.example.weather.db.entities.ReportEntity
import com.example.weather.utils.Tools.utcToLocal

class ReportRecyclerAdapter : ListAdapter<ReportEntity, ReportRecyclerAdapter.ReportViewHolder>(object : DiffUtil.ItemCallback<ReportEntity>(){
    override fun areItemsTheSame(oldItem: ReportEntity, newItem: ReportEntity): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: ReportEntity, newItem: ReportEntity): Boolean {
        return oldItem.id == newItem.id
    }

}) {

    inner class ReportViewHolder(val binding : ReportListLayoutBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder = ReportViewHolder(
        ReportListLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        val report = getItem(position)
        val binding = holder.binding

        binding.tvLocationName.text = report.report.name ?: "-"
        binding.tvWeatherType.text = report.report.weather?.firstOrNull()?.main ?: "-"
        binding.tvTemperature.text = "${report.report.main?.temp?.toInt()?.toString() ?: "-"}\u00B0"
        binding.tvDay.text = report.storedOn.utcToLocal("EEEE")
        binding.tvDate.text = report.storedOn.utcToLocal("dd/mm/yyyy hh:mm a")
    }
}