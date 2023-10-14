package com.vendtech.app.adapter.transactions

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vendtech.app.R
import com.vendtech.app.models.transaction.RechargeTransactionNewListModel
import com.vendtech.app.ui.activity.transaction.RechargeTransactionDetails
import com.vendtech.app.utils.Utilities
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*


class SalesReportAdapter(internal var meterListModels: MutableList<RechargeTransactionNewListModel.Result>, internal var context: Context, internal var activity: Activity) : RecyclerView.Adapter<SalesReportAdapter.ViewHolder>(), View.OnClickListener {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        @SuppressLint("InflateParams") val view = LayoutInflater.from(parent.context).inflate(R.layout.item_sales_report, null)

        val lp = RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        view.setLayoutParams(lp)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {


       /* holder.title.text = "Meter no: "+meterListModels[position].meterNumber
        holder.amount.text = "SLL: ${NumberFormat.getNumberInstance(Locale.US).format(meterListModels[position].amount.toDouble().toInt())}"
       //  holder.date.text = /*Utilities.changeDateFormat(context,*/meterListModels[position].createdAt
        holder.date.text = Utilities.formatToUtc(meterListModels[position].createdAt)

        holder.transID.text = meterListModels[position].status


        if(meterListModels[position].status.equals("Pending")){
            holder.transID.setTextColor(ContextCompat.getColor(context,R.color.colorred))
        }else if (meterListModels[position].status.equals("Rejected")){
            holder.transID.setTextColor(ContextCompat.getColor(context,R.color.colorred))
        }else if (meterListModels[position].status.equals("Success")){
            holder.transID.setTextColor(ContextCompat.getColor(context,R.color.colorgreen))
        }else {
            holder.transID.setTextColor(ContextCompat.getColor(context,R.color.colororange))
        }*/


        var data= meterListModels.get(position);

        holder.tv_date.text =(""+Utilities.formatToUtc(data.createdAt));
        holder.tv_meter.text =("Meter#: "+data.meterNumber);

        var longval:Long=data.amount.toLong()
        var formatter: DecimalFormat = NumberFormat.getInstance(Locale.US) as DecimalFormat
        formatter.applyPattern("#,###,###,###")
        var formattedString = formatter.format(longval)
        holder.tv_amount.text =("Amount: "+formattedString)

        holder.tv_transaction_id.text =("Trans ID: "+data.transactionId);




        holder.rootView.setOnClickListener(View.OnClickListener {

            val i = Intent(context, RechargeTransactionDetails::class.java)
            i.putExtra("rechargeId",data.rechargeId)
            i.putExtra("type","")
            context.startActivity(i)
            activity.overridePendingTransition(R.anim.slide_in, R.anim.slide_out)


        })



    }

    override fun getItemCount(): Int {
        return meterListModels.size
    }

    override fun onClick(v: View) {

    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {


        lateinit var tv_date: TextView
        lateinit var tv_meter: TextView
        lateinit var tv_amount: TextView
        lateinit var tv_transaction_id: TextView
        lateinit var rootView:LinearLayout

        init {

            tv_date = itemView.findViewById<View>(R.id.tv_date) as TextView
            tv_meter = itemView.findViewById<View>(R.id.tv_meter) as TextView
            tv_amount = itemView.findViewById<View>(R.id.tv_amount) as TextView
            tv_transaction_id = itemView.findViewById<View>(R.id.tv_transaction_id) as TextView
            rootView=itemView.findViewById<View>(R.id.rootView)as LinearLayout


        }

    }


}