package com.quick.bite.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.quick.bite.model.others.DashboardWidget
import com.quick.bite.ui.activities.MainActivity
import com.quick.bite.R

// Simple RecyclerView Adapters for demo purposes
class DashboardWidgetAdapter(private val widgets: List<DashboardWidget>) :
    RecyclerView.Adapter<DashboardWidgetAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.tv_widget_icon)
        val title: TextView = view.findViewById(R.id.tv_widget_title)
        val mainText: TextView = view.findViewById(R.id.tv_widget_main_text)
        val subText: TextView = view.findViewById(R.id.tv_widget_sub_text)
        val actionButton: MaterialButton = view.findViewById(R.id.btn_widget_action)
        val background: View = view.findViewById(R.id.view_widget_background)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_dashboard_widget, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val widget = widgets[position]

        // Set icon (for now using placeholder)
        holder.icon.setImageResource(R.drawable.ic_local_fire_department_128)
        holder.title.text = widget.title
        holder.mainText.text = widget.mainText
        holder.subText.text = widget.subText
        holder.actionButton.text = widget.actionText

        // Show/hide action button based on actionText
        if (widget.actionText.isEmpty()) {
            holder.actionButton.visibility = View.GONE
        } else {
            holder.actionButton.visibility = View.VISIBLE
        }

        // Apply background based on widget type
        when (widget.backgroundType) {
            MainActivity.WidgetBackgroundType.GRADIENT -> {
                holder.background.setBackgroundResource(R.drawable.bg_promotion_gradient)
            }
            MainActivity.WidgetBackgroundType.DARK -> {
                holder.background.setBackgroundResource(R.drawable.dark_background)
            }
            else -> {
                holder.background.setBackgroundResource(R.drawable.normal_background)
            }
        }
    }

    override fun getItemCount() = widgets.size
}