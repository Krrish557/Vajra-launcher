package com.vajra.launcher.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.vajra.launcher.R
import com.vajra.launcher.databinding.ItemAppRowBinding
import com.vajra.launcher.models.AppInfo

class AppAdapter(
    private var apps: List<AppInfo>,
    private val onAppClick: (AppInfo) -> Unit
) : RecyclerView.Adapter<AppAdapter.AppViewHolder>() {

    class AppViewHolder(val binding: ItemAppRowBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val binding = ItemAppRowBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AppViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        val app = apps[position]

        holder.binding.appLabel.text = app.label
        holder.binding.appPackage.text = app.packageName

        if (app.icon != null) {
            holder.binding.appIcon.setImageDrawable(app.icon)
        } else {
            holder.binding.appIcon.setImageResource(R.drawable.ic_nav_apps)
        }

        holder.itemView.setOnClickListener {
            onAppClick(app)
        }
    }

    override fun getItemCount(): Int = apps.size

    fun updateList(newApps: List<AppInfo>) {
        apps = newApps
        notifyDataSetChanged()
    }
}
