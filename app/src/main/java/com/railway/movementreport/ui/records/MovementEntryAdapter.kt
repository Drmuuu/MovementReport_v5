package com.railway.movementreport.ui.records

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.railway.movementreport.R
import com.railway.movementreport.data.entity.MovementEntry
import com.railway.movementreport.utils.DateUtils

class MovementEntryAdapter(
    private val onEdit: (MovementEntry) -> Unit,
    private val onDelete: (MovementEntry) -> Unit
) : ListAdapter<MovementEntry, MovementEntryAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDate: TextView = view.findViewById(R.id.tvDate)
        val tvTrainInfo: TextView = view.findViewById(R.id.tvTrainInfo)
        val tvRemarks: TextView = view.findViewById(R.id.tvRemarks)
        val btnEdit: ImageButton = view.findViewById(R.id.btnEdit)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDelete)
        val remarksBadge: View = view.findViewById(R.id.remarksBadge)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_movement_entry, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val entry = getItem(position)
        holder.tvDate.text = DateUtils.dbToDisplay(entry.date)
        holder.tvTrainInfo.text = when {
            entry.entryType == "DUTY" && entry.remarks == "OD" -> "Office Duty"
            entry.entryType == "DUTY" -> "Train: ${entry.trainNumber}  •  ${entry.stationFrom} → ${entry.stationTo}"
            entry.entryType == "LEAVE" -> when (entry.remarks) {
                "CL"   -> "Casual Leave"
                "LAP"  -> "Leave on Average Pay"
                "SICK" -> "Sick Leave"
                "SCL"  -> "Special Casual Leave"
                else   -> entry.remarks
            }
            else -> "—"
        }
        holder.tvRemarks.text = entry.remarks

        val ctx = holder.itemView.context
        val (bg, fg) = when (entry.remarks) {
            "Duty"   -> ContextCompat.getColor(ctx, R.color.duty_bg)  to ContextCompat.getColor(ctx, R.color.duty_text)
            "Rest"   -> ContextCompat.getColor(ctx, R.color.rest_bg)  to ContextCompat.getColor(ctx, R.color.rest_text)
            "C-Rest" -> ContextCompat.getColor(ctx, R.color.crest_bg) to ContextCompat.getColor(ctx, R.color.crest_text)
            "CL"     -> android.graphics.Color.parseColor("#FFF8E1")  to android.graphics.Color.parseColor("#E65100")
            "LAP"    -> android.graphics.Color.parseColor("#E8F5E9")  to android.graphics.Color.parseColor("#1B5E20")
            "SICK"   -> android.graphics.Color.parseColor("#FCE4EC")  to android.graphics.Color.parseColor("#880E4F")
            "SCL"    -> android.graphics.Color.parseColor("#E0F2F1")  to android.graphics.Color.parseColor("#004D40")
            "OD"     -> android.graphics.Color.parseColor("#E8EAF6")  to android.graphics.Color.parseColor("#1A237E")
            else     -> ContextCompat.getColor(ctx, R.color.rest_bg)  to ContextCompat.getColor(ctx, R.color.rest_text)
        }
        holder.remarksBadge.setBackgroundColor(bg)
        holder.tvRemarks.setTextColor(fg)
        holder.btnEdit.setOnClickListener { onEdit(entry) }
        holder.btnDelete.setOnClickListener { onDelete(entry) }
    }

    class DiffCallback : DiffUtil.ItemCallback<MovementEntry>() {
        override fun areItemsTheSame(oldItem: MovementEntry, newItem: MovementEntry) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: MovementEntry, newItem: MovementEntry) = oldItem == newItem
    }
}
