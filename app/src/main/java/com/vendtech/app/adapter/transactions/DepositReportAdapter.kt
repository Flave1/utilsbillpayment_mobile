package com.vendtech.app.adapter.transactions

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.recyclerview.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.vendtech.app.R
import com.vendtech.app.models.transaction.DepositTransactionNewListModel
import com.vendtech.app.ui.activity.transaction.DepositTransactionDetails
import com.vendtech.app.utils.Utilities
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*

class DepositReportAdapter(internal var depositListModels: MutableList<DepositTransactionNewListModel.Result>, internal var context: Context, internal var activity:Activity) : RecyclerView.Adapter<DepositReportAdapter.ViewHolder>(), View.OnClickListener {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DepositReportAdapter.ViewHolder {
        @SuppressLint("InflateParams") val view = LayoutInflater.from(parent.context).inflate(R.layout.item_deposit_report, null)

        val lp = RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        view.setLayoutParams(lp)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: DepositReportAdapter.ViewHolder, position: Int) {

        /*holder.title.text = "CHX/Slip no: "+depositListModels[position].chkNoOrSlipId
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

        holder.date.text = Utilities.formatToUtc(depositListModels[position].createdAt)*/

        holder.tv_date.text=(""+Utilities.formatToUtc(depositListModels[position].createdAt));
        var formatter: DecimalFormat = NumberFormat.getInstance(Locale.US) as DecimalFormat
        formatter.applyPattern("#,###,###,###")
        var formattedString = formatter.format(depositListModels[position].newBalance.toLong())

        holder.tv_new_balance.text=("New Bal: "+formattedString);

        formattedString = formatter.format(depositListModels[position].percentageAmount.toLong())

        holder.tv_deposite.text=("Deposit: "+formattedString);
        holder.tv_bank_deposite_ref.text=("Deposit Ref#: "+depositListModels[position].chkNoOrSlipId);


        Log.d("chkNoOrSlipId","--"+depositListModels[position].chkNoOrSlipId)


        holder.rootView.setOnClickListener(View.OnClickListener {
            val i = Intent(context, DepositTransactionDetails::class.java)
            i.putExtra("depositId",depositListModels[position].depositId)
            i.putExtra("type","")
            context.startActivity(i)
            activity.overridePendingTransition(R.anim.slide_in, R.anim.slide_out)
        });


    }

    override fun getItemCount(): Int {
        return depositListModels.size
    }

    override fun onClick(v: View) {

    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {


        lateinit var tv_date: TextView
        lateinit var tv_deposite: TextView
        lateinit var tv_bank_deposite_ref: TextView
        lateinit var tv_new_balance: TextView
        lateinit var rootView:LinearLayout

        init {


            tv_date = itemView.findViewById<View>(R.id.tv_date) as TextView
            tv_deposite = itemView.findViewById<View>(R.id.tv_deposite) as TextView
            tv_bank_deposite_ref = itemView.findViewById<View>(R.id.tv_bank_deposite_ref) as TextView
            tv_new_balance = itemView.findViewById<View>(R.id.tv_new_balance) as TextView
            rootView=itemView.findViewById<View>(R.id.rootView)as LinearLayout


        }

    }


}