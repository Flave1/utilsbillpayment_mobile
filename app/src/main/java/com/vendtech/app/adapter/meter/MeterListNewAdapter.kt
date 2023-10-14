package com.vendtech.app.adapter.meter

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import androidx.recyclerview.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageButton
import com.google.gson.Gson
import com.vendtech.app.R
import com.vendtech.app.adapter.meter.MeterListAdapter
import com.vendtech.app.helper.SharedHelper
import com.vendtech.app.models.meter.DeleteMeterModel
import com.vendtech.app.models.meter.MeterListResults
import com.vendtech.app.network.Uten
import com.vendtech.app.ui.activity.meter.EditMeterActivity
import com.vendtech.app.ui.activity.meter.MeterListActivity
import com.vendtech.app.ui.fragment.BillPaymentActivity
import com.vendtech.app.utils.Constants
import com.vendtech.app.utils.CustomDialog
import com.vendtech.app.utils.Utilities
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MeterListNewAdapter(internal var meterListModels: MutableList<MeterListResults>, internal var context: Context, var itemEditable:Boolean, var itemClickListeners: MeterListAdapter.ItemClickListener, var passCode:String, var meterListActivity: MeterListActivity): RecyclerView.Adapter<MeterListNewAdapter.ViewHolder>() {


    lateinit var itemClickListener: MeterListAdapter.ItemClickListener;
    var itemEditables = false;

   override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_meter_list, parent, false);

       this.itemClickListener = itemClickListeners;
       this.itemEditables=itemEditable;

       return ViewHolder(view);
   }

   override fun onBindViewHolder(holder: ViewHolder, position: Int) {

             if (itemEditables) {
                holder.delete.visibility = View.VISIBLE;
             } else {
                 holder.delete.visibility = View.GONE;
             }


             holder.meterNo.text = "Meter #: "+meterListModels[position].number;
             //holder.meterName.text = meterListModels[position].name
             holder.meterAddress.text = meterListModels[position].address;
             holder.meterDate.text = meterListModels[position].meterMake;


             holder.itemView.setOnClickListener {

                 if (itemEditables) {
                     val `is` = Intent(context, EditMeterActivity::class.java);
                     `is`.putExtra("mnumber", meterListModels[position].number);
                     `is`.putExtra("mname", meterListModels[position].name);
                     `is`.putExtra("maddress", meterListModels[position].address);
                     `is`.putExtra("mdate", meterListModels[position].meterMake);
                     `is`.putExtra("mid", meterListModels[position].meterId);
                     `is`.putExtra("malias", meterListModels[position].alias?: "".orEmpty());
                     `is`.putExtra("misVerified", meterListModels[position].isVerified.toString());
                     `is`.putExtra("mnumberType", meterListModels[position].numberType);
                     context.startActivity(`is`);
                 }


             }


             holder.iv_recharge.setOnClickListener {

                 var selectMeter = meterListModels[position];
                 if(selectMeter.platformDisabled){
                     Utilities.longToast("Service Disabled.",context)
                     return@setOnClickListener
                 }
                 val `is` = Intent(context, BillPaymentActivity::class.java)

                 `is`.putExtra("mnumber", meterListModels[position].number);
                 `is`.putExtra("mname", meterListModels[position].name);
                     `is`.putExtra("maddress", meterListModels[position].address);
                 `is`.putExtra("mdate", meterListModels[position].meterMake);
                 `is`.putExtra("mid", meterListModels[position].meterId);
                 `is`.putExtra("malias", meterListModels[position].alias?: "".orEmpty());
                 `is`.putExtra("misVerified", meterListModels[position].isVerified.toString());
                 `is`.putExtra("mnumberType", meterListModels[position].numberType);

                 `is`.putExtra("data", selectMeter);
                 context.startActivity(`is`);


                 if (meterListActivity!=null){
                     meterListActivity.setOnclickIntent(selectMeter);
                 }


             }

             holder.delete.setOnClickListener(View.OnClickListener {

                 AskDeleteDialog(meterListModels[position].meterId);
             })



   }

   override fun getItemCount(): Int {
        return meterListModels.size
       //return 10

   }

    fun AskDeleteDialog(meterId:String){

        val builder = AlertDialog.Builder(context)
        //set title for alert dialog
        builder.setTitle(R.string.app_name)
        //set message for alert dialog
        builder.setMessage("Are you sure to delete this meter?")
        builder.setIcon(R.drawable.appicon)

        //performing positive action
        builder.setPositiveButton("Yes"){dialogInterface, which ->

            if(Uten.isInternetAvailable(context)){
                DeleteMeterApi(meterId)
            }else{
                Utilities.shortToast("No internet connection. Please check your network connectivity.",context)
            }

        }
        //performing negative action
        builder.setNegativeButton("No"){dialogInterface, which ->
        }
        // Create the AlertDialog
        val alertDialog: AlertDialog = builder.create()
        // Set other dialog properties
        alertDialog.setCancelable(false)
        alertDialog.show()

    }

    fun DeleteMeterApi(meterId: String){

        var customDialog: CustomDialog
        customDialog= CustomDialog(context)
        customDialog.show()

        val call: Call<DeleteMeterModel> = Uten.FetchServerData().delete_meter(SharedHelper.getString(context, Constants.TOKEN),meterId)
        call.enqueue(object : Callback<DeleteMeterModel> {
            override fun onResponse(call: Call<DeleteMeterModel>, response: Response<DeleteMeterModel>) {

                if(customDialog.isShowing){
                    customDialog.dismiss()
                }
                val  g = Gson()
                g.toJson(response.body())

                var data=response.body()
                if(data!=null){

                    Utilities.shortToast(data.message,context)

                    if(data.status.equals("true")){

                        if(itemClickListener!=null){

                            itemClickListener.itemDeleted()
                        }
                    }
                }
                Log.v("VerifyOTP","vERIFY response: "+g.toJson(response.body()))

            }

            override fun onFailure(call: Call<DeleteMeterModel>, t: Throwable) {
                val  gs = Gson()
                gs.toJson(t.localizedMessage)
                if(customDialog.isShowing){
                    customDialog.dismiss()
                }
            }

        })

    }

    interface ItemClickListener {
        fun itemDeleted()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {


         internal var meterNo: TextView
        // internal var meterName: TextView
         internal var meterAddress: TextView
         internal var meterDate: TextView
         internal var delete: ImageView
         internal var iv_recharge: AppCompatImageButton
        // internal var ll_delete: LinearLayout
         //internal var meterItem: TableRow

        init {
               meterNo = itemView.findViewById<View>(R.id.meterNumber) as TextView
               // meterName = itemView.findViewById<View>(R.id.userName) as TextView
               meterAddress = itemView.findViewById<View>(R.id.address) as TextView
               meterDate = itemView.findViewById<View>(R.id.date) as TextView
               delete = itemView.findViewById<View>(R.id.delete) as ImageView
               iv_recharge = itemView.findViewById<View>(R.id.iv_recharge) as AppCompatImageButton

              // ll_delete = itemView.findViewById<View>(R.id.ll_delete) as LinearLayout
               // meterItem = itemView.findViewById<View>(R.id.meterItem) as TableRow
        }

    }

    fun clear() {

        if (meterListModels.size > 0) {

            meterListModels.clear()
        }

    }









}
