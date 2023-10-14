package com.vendtech.app.adapter.transactions

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.vendtech.app.R
import com.vendtech.app.models.transaction.RechargeTransactionNewListModel
import com.vendtech.app.ui.activity.transaction.RechargeTransactionDetails
import com.vendtech.app.utils.Utilities
import java.text.NumberFormat
import java.util.*


class RechargeTransactionAdapter(internal var meterListModels: MutableList<RechargeTransactionNewListModel.Result>, internal var context: Context, internal var activity: Activity) : RecyclerView.Adapter<RechargeTransactionAdapter.ViewHolder>(), View.OnClickListener {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        @SuppressLint("InflateParams") val view = LayoutInflater.from(parent.context).inflate(R.layout.item_transaction_history, null)

        val lp = RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        view.setLayoutParams(lp)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {


        holder.title.text = "Meter no: "+meterListModels[position].meterNumber
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
        }

        holder.rootView.setOnClickListener(View.OnClickListener {

            val i = Intent(context, RechargeTransactionDetails::class.java)
            i.putExtra("rechargeId",meterListModels[position].rechargeId)
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


        lateinit var transID: TextView
        lateinit var date: TextView
        lateinit var amount: TextView
        lateinit var title: TextView
        lateinit var rootView:LinearLayout

        init {

            transID = itemView.findViewById<View>(R.id.transactionidTV) as TextView
            date = itemView.findViewById<View>(R.id.dateTV) as TextView
            amount = itemView.findViewById<View>(R.id.amountTV) as TextView
            title = itemView.findViewById<View>(R.id.titleTV) as TextView
            rootView=itemView.findViewById<View>(R.id.rootView)as LinearLayout


        }

    }


}