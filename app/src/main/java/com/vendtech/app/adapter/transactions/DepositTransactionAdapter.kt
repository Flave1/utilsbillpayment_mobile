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
import com.vendtech.app.models.transaction.DepositTransactionNewListModel
import com.vendtech.app.ui.activity.transaction.DepositTransactionDetails
import com.vendtech.app.utils.Utilities
import java.text.NumberFormat
import java.util.*

class DepositTransactionAdapter(internal var depositListModels: MutableList<DepositTransactionNewListModel.Result>, internal var context: Context, internal var activity:Activity) : RecyclerView.Adapter<DepositTransactionAdapter.ViewHolder>(), View.OnClickListener {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DepositTransactionAdapter.ViewHolder {
        @SuppressLint("InflateParams") val view = LayoutInflater.from(parent.context).inflate(R.layout.item_deposit_transaction, null)

        val lp = RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        view.setLayoutParams(lp)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: DepositTransactionAdapter.ViewHolder, position: Int) {

        holder.title.text = "CHX/Slip no: "+depositListModels[position].chkNoOrSlipId
        holder.amount.text = "SLL: ${NumberFormat.getNumberInstance(Locale.US).format(depositListModels[position].amount.toDouble().toInt())}"
        //holder.amount.text = "SLL: ${depositListModels[position].amount.toDouble().toInt()}"
        holder.transID.text = depositListModels[position].status


        if(depositListModels[position].status.equals("Pending")){
            holder.transID.setTextColor(ContextCompat.getColor(context,R.color.colorred))
        }else if (depositListModels[position].status.equals("Rejected")){
            holder.transID.setTextColor(ContextCompat.getColor(context,R.color.colorred))
        }else if (depositListModels[position].status.equals("Approved")){
            holder.transID.setTextColor(ContextCompat.getColor(context,R.color.colorgreen))
        }else {
            holder.transID.setTextColor(ContextCompat.getColor(context,R.color.colororange))
        }

        holder.date.text = Utilities.formatToUtc(depositListModels[position].createdAt)


        holder.rootView.setOnClickListener(View.OnClickListener {

            val i = Intent(context, DepositTransactionDetails::class.java)
            i.putExtra("depositId",depositListModels[position].depositId)
            i.putExtra("type","")
            context.startActivity(i)
            activity.overridePendingTransition(R.anim.slide_in, R.anim.slide_out)


        })

    }



    override fun getItemCount(): Int {
        return depositListModels.size
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