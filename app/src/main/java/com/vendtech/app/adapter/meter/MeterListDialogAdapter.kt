package com.vendtech.app.adapter.meter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.vendtech.app.R
import com.vendtech.app.models.meter.MeterListResults

class MeterListDialogAdapter (internal var meterListModels: MutableList<MeterListResults>, internal var context: Context, var itemClickListeners:ItemClickListener) : RecyclerView.Adapter<MeterListDialogAdapter.ViewHolder>(), View.OnClickListener {


    lateinit var itemClickListener: ItemClickListener
    var itemEditables = false


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        @SuppressLint("InflateParams") val view = LayoutInflater.from(parent.context).inflate(R.layout.item_dialog_meter, null)
        this.itemClickListener = itemClickListeners

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.meterName.text = meterListModels[position].name + " "+ "("+meterListModels[position].number+")"

        holder.itemViews.setOnClickListener {

            if(itemClickListener!=null){

                itemClickListener.meterId(meterListModels[position].meterId,meterListModels[position].number)
            }

        }

    }

    override fun getItemCount(): Int {
        return meterListModels.size
    }

    override fun onClick(v: View) {

    }

    interface ItemClickListener {
        fun meterId(id: String,name:String)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {


        internal var meterName: AppCompatTextView
        internal var itemViews: LinearLayout

        init {


            meterName = itemView.findViewById<View>(R.id.meterDetailTV) as AppCompatTextView
            itemViews = itemView.findViewById<View>(R.id.itemView) as LinearLayout

        }

    }

    fun clear() {

        if (meterListModels.size > 0) {

            meterListModels.clear()
        }

    }


}