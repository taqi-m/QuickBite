package com.quick.bite.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.quick.bite.R
import com.quick.bite.models.Restaurant
import java.util.Locale

class RestaurantAdapter(
    restaurants: List<Restaurant>,
    private val onItemClick: (Restaurant) -> Unit
) : RecyclerView.Adapter<RestaurantAdapter.ViewHolder>() {

    private val originalItems = restaurants.toList()
    private val visibleItems = restaurants.toMutableList()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.ic_kitchen_icon)
        val name: TextView = view.findViewById(R.id.tv_kitchen_name)
        val category: TextView = view.findViewById(R.id.tv_kitchen_category)
        val rating: TextView = view.findViewById(R.id.tv_kitchen_rating)
        val deliveryTime: TextView = view.findViewById(R.id.tv_delivery_time)
        val deliveryFee: TextView = view.findViewById(R.id.tv_delivery_fee)
        val action: MaterialButton = view.findViewById(R.id.btn_kitchen_action)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_active_kitchen, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val restaurant = visibleItems[position]
        holder.icon.setImageResource(R.drawable.ic_soup_kitchen_128)
        holder.name.text = restaurant.name
        holder.category.text = restaurant.category
        holder.rating.text = restaurant.rating
        holder.deliveryTime.text = restaurant.deliveryTime
        holder.deliveryFee.text = restaurant.deliveryFee

        holder.itemView.setOnClickListener { onItemClick(restaurant) }
        holder.action.setOnClickListener { onItemClick(restaurant) }
    }

    override fun getItemCount(): Int = visibleItems.size

    fun filter(query: String) {
        val keyword = query.trim().lowercase(Locale.getDefault())
        visibleItems.clear()

        if (keyword.isEmpty()) {
            visibleItems.addAll(originalItems)
        } else {
            visibleItems.addAll(
                originalItems.filter { item ->
                    item.name.lowercase(Locale.getDefault()).contains(keyword) ||
                        item.category.lowercase(Locale.getDefault()).contains(keyword)
                }
            )
        }

        notifyDataSetChanged()
    }
}

