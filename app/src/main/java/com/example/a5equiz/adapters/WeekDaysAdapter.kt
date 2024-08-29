package com.example.a5equiz.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.a5equiz.R

class WeekDaysAdapter(
    private val context: Context,
    private var daysList: List<Pair<String, String>>,
    private val onDaySelected: (String) -> Unit
) : RecyclerView.Adapter<WeekDaysAdapter.DayViewHolder>() {

    private var selectedPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_date, parent, false)
        return DayViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: DayViewHolder,
        @SuppressLint("RecyclerView") position: Int
    ) {
        val (day, weekday) = daysList[position]
        holder.bind(day, weekday, position == selectedPosition)

        holder.itemView.setOnClickListener {
            selectedPosition = position
            notifyDataSetChanged()
            onDaySelected(day)
        }
    }

    override fun getItemCount(): Int = daysList.size

    fun updateDaysList(newDaysList: List<Pair<String, String>>) {
        daysList = newDaysList
        notifyDataSetChanged()
    }

    inner class DayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nbreDayMounth: TextView = itemView.findViewById(R.id.nbreDayMounth)
        private val titleToDay: TextView = itemView.findViewById(R.id.titleToDay)
        private val container: LinearLayout = itemView.findViewById(R.id.container)

        @SuppressLint("ResourceType")
        fun bind(day: String, weekday: String, isSelected: Boolean) {
            nbreDayMounth.text = day
            titleToDay.text = weekday

            if (isSelected) {
                nbreDayMounth.setTextColor(ContextCompat.getColor(context, android.R.color.white))
                titleToDay.setTextColor(ContextCompat.getColor(context, android.R.color.white))
                container.setBackgroundColor(
                    ContextCompat.getColor(
                        context,
                        R.drawable.custom_date_selected
                    )
                )
            } else {
                nbreDayMounth.setTextColor(ContextCompat.getColor(context, R.color.black_primary))
                titleToDay.setTextColor(ContextCompat.getColor(context, R.color.black_primary))
                container.setBackgroundColor(
                    ContextCompat.getColor(
                        context,
                        R.drawable.custom_input
                    )
                )
            }
        }
    }
}
