package com.vendtech.app.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.vendtech.app.R
import com.vendtech.app.models.meter.PosListModel
import kotlinx.android.synthetic.main.item_pos_list.view.*
import java.text.NumberFormat
import java.util.*
import kotlin.collections.ArrayList

class PosListAdapter(val posList: ArrayList<PosListModel.Result>) : RecyclerView.Adapter<PosListAdapter.ViewHolder>() {
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view)


    var test=0;
    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        val view = LayoutInflater.from(p0.context).inflate(R.layout.item_pos_list, p0, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return posList.size
    }

    override fun onBindViewHolder(p0: ViewHolder, p1: Int) {
        val pos = posList[p1]
        p0.itemView.apply {
            item_pos_balance.text = NumberFormat.getNumberInstance(Locale.US).format(pos.balance.toInt())
            item_pos_cellNo.text = pos.phone
            item_pos_enabled.setText(pos.enabled.toString())
            item_pos_id.setText(pos.serialNumber)
            item_pos_vendor_name.setText(pos.vendorName)
            item_pos_type.setText(pos.vendorType)

        }

        var temp=p0.itemView.item_pos_balance.text.toString();

            temp=temp.replace(",","");


        test=(test+temp.toInt());
        Log.d("TotalBalance-:::","---"+test+"***********");

    }
}