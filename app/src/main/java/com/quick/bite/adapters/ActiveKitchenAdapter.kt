package com.quick.bite.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.quick.bite.model.ActiveKitchen
import com.quick.bite.R

class ActiveKitchenAdapter(
    private var kitchens: List<ActiveKitchen>,
    private val onKitchenClick: (Int) -> Unit
) : RecyclerView.Adapter<ActiveKitchenAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.ic_kitchen_icon)
        val name: TextView = view.findViewById(R.id.tv_kitchen_name)
        val category: TextView = view.findViewById(R.id.tv_kitchen_category)
        val rating: TextView = view.findViewById(R.id.tv_kitchen_rating)
        val deliveryTime: TextView = view.findViewById(R.id.tv_delivery_time)
        val deliveryFee: TextView = view.findViewById(R.id.tv_delivery_fee)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_active_kitchen, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val kitchen = kitchens[position]

        // Set icon (using placeholder for now)
        holder.icon.setImageResource(R.drawable.ic_soup_kitchen_128)
        holder.name.text = kitchen.name
        holder.category.text = kitchen.category
        holder.rating.text = kitchen.rating
        holder.deliveryTime.text = kitchen.deliveryTime
        holder.deliveryFee.text = kitchen.deliveryFee

        holder.itemView.setOnClickListener {
            onKitchenClick(kitchen.restaurantId)
        }
    }

    override fun getItemCount() = kitchens.size

    /**
     * Replaces the current data set with new items and refreshes the UI.
     * This is called directly by the Fragment when the Repository Flow emits new data.
     */
    fun updateData(newKitchens: List<ActiveKitchen>) {
        this.kitchens = newKitchens
        notifyDataSetChanged()
    }
}