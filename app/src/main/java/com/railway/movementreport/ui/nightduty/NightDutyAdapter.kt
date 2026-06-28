package com.railway.movementreport.ui.nightduty

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.railway.movementreport.R
import com.railway.movementreport.data.entity.NightDutyEntry
import com.railway.movementreport.utils.DateUtils

class NightDutyAdapter(
    private val onEdit: (NightDutyEntry) -> Unit,
    private val onDelete: (NightDutyEntry) -> Unit
) : ListAdapter<NightDutyEntry, NightDutyAdapter.VH>(Diff()) {

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val tvDate: TextView     = v.findViewById(R.id.tvNdDate)
        val tvStations: TextView = v.findViewById(R.id.tvNdStations)
        val tvHours: TextView    = v.findViewById(R.id.tvNdHours)
        val tvRemark: TextView   = v.findViewById(R.id.tvNdRemark)
        val btnEdit: ImageButton = v.findViewById(R.id.btnEdit)
        val btnDelete: ImageButton = v.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(p: ViewGroup, t: Int) =
        VH(LayoutInflater.from(p.context).inflate(R.layout.item_night_duty_entry, p, false))

    override fun onBindViewHolder(h: VH, pos: Int) {
        val e = getItem(pos)
        h.tvDate.text     = DateUtils.dbToDisplay(e.fromDate)
        val trainPart = if (e.trainNumber.isNotEmpty()) "Train: ${e.trainNumber}  •  " else ""
        h.tvStations.text = "${trainPart}${e.stationFrom} → ${e.stationTo}  |  Night: ${e.nightDutyFrom}–${e.nightDutyTo}"
        h.tvHours.text    = "Total: ${e.totalNightHrs} hrs  |  To: ${DateUtils.dbToDisplay(e.toDate)}"
        h.tvRemark.text   = e.remark.ifEmpty { "—" }
        h.btnEdit.setOnClickListener { onEdit(e) }
        h.btnDelete.setOnClickListener { onDelete(e) }
    }

    class Diff : DiffUtil.ItemCallback<NightDutyEntry>() {
        override fun areItemsTheSame(a: NightDutyEntry, b: NightDutyEntry) = a.id == b.id
        override fun areContentsTheSame(a: NightDutyEntry, b: NightDutyEntry) = a == b
    }
}
