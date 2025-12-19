package com.example.trainn

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class  TrainAdapter_train(
    private val trains: List<train1>,
    private val onEdit: (train1) -> Unit,
    private val onDelete: (Int) -> Unit
) : RecyclerView.Adapter<TrainAdapter_train.TrainViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrainViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.iteam_train_train, parent, false)
        return TrainViewHolder(view)
    }

    override fun onBindViewHolder(holder: TrainViewHolder, position: Int) {
        val train1 = trains[position]
        holder.trainNumber.text = train1.trainNumber
        holder.trainName.text = train1.trainName

        holder.btnEdit.setOnClickListener { onEdit(train1) }
        holder.btnDelete.setOnClickListener { onDelete(train1.trainId) }
    }


    override fun getItemCount() = trains.size

    class TrainViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val trainNumber: TextView = view.findViewById(R.id.textTrainNumber)
        val trainName: TextView = view.findViewById(R.id.textTrainName)
        val btnEdit: ImageView = view.findViewById(R.id.btnEdit)
        val btnDelete: ImageView = view.findViewById(R.id.btnDelete)
    }
}
