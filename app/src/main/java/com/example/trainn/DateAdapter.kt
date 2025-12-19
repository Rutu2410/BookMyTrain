package com.example.trainn

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class DateAdapter(private var dateList: List<DateModel>, private val onDateSelected: (DateModel) -> Unit) :
    RecyclerView.Adapter<DateAdapter.DateViewHolder>() {

    private var selectedPosition = -1  // To track the selected date

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DateViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_date, parent, false)
        return DateViewHolder(view)
    }

    override fun onBindViewHolder(holder: DateViewHolder, position: Int) {
        val date = dateList[position]

        // Extract month and day separately
        val formattedMonth = formatToMonth(date.date)  // "Feb"
        val formattedDay = formatToDay(date.date)      // "1"

        holder.monthTextView.text = formattedMonth
        holder.dateTextView.text = formattedDay

        // Highlight selected item
        holder.itemView.setBackgroundResource(
            if (position == selectedPosition) R.drawable.selected_date_background else R.drawable.default_date_background
        )

        holder.itemView.setOnClickListener {
            selectedPosition = position
            notifyDataSetChanged()
            onDateSelected(date)
        }
    }

    override fun getItemCount(): Int = dateList.size

    class DateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val monthTextView: TextView = itemView.findViewById(R.id.monthTextView)
        val dateTextView: TextView = itemView.findViewById(R.id.dateTextView)
    }

    // Function to get month (MMM)
    private fun formatToMonth(dateString: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormat = SimpleDateFormat("MMM", Locale.getDefault()) // "Feb"
            val date = inputFormat.parse(dateString)
            outputFormat.format(date!!)
        } catch (e: Exception) {
            ""
        }
    }

    // Function to get day (d)
    private fun formatToDay(dateString: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormat = SimpleDateFormat("d", Locale.getDefault()) // "1"
            val date = inputFormat.parse(dateString)
            outputFormat.format(date!!)
        } catch (e: Exception) {
            ""
        }
    }
}
