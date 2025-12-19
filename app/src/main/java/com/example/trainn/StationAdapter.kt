package com.example.trainn

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class StationAdapter(
    private val stations: List<Station>,
    private val onEdit: (Station) -> Unit,
    private val onDelete: (Int) -> Unit
) : RecyclerView.Adapter<StationAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtName: TextView = view.findViewById(R.id.station_name)
        val txtCode: TextView = view.findViewById(R.id.station_code)
        val btnEdit: ImageView = view.findViewById(R.id.btn_edit)
        val btnDelete: ImageView = view.findViewById(R.id.btn_delete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_station, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val station = stations[position]
        holder.txtName.text = station.stationName
        holder.txtCode.text = station.stationCode
        holder.btnEdit.setOnClickListener { onEdit(station) }
        holder.btnDelete.setOnClickListener { onDelete(station.id) }
    }

    override fun getItemCount() = stations.size
}
