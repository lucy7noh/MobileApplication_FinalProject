package com.example.fitnote.ui.exercise

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.fitnote.R
import com.example.fitnote.data.entity.ExerciseEntity

class ExerciseAdapter(
    private var items: List<ExerciseEntity>,
    private val onClick: (ExerciseEntity) -> Unit
) : RecyclerView.Adapter<ExerciseAdapter.ViewHolder>() {

    fun submitList(newItems: List<ExerciseEntity>) {
        items = newItems
        notifyDataSetChanged()
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvName: TextView = view.findViewById(R.id.tvName)
        private val tvInfo: TextView = view.findViewById(R.id.tvInfo)

        fun bind(item: ExerciseEntity) {
            tvName.text = item.name
            tvInfo.text = "${item.time}분 · ${item.calorie}kcal"

            itemView.setOnClickListener {
                onClick(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_exercise, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}
