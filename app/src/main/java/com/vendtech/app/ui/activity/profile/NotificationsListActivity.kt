package com.vendtech.app.ui.activity.profile

import android.app.Activity
import android.app.NotificationManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.vendtech.app.R
import com.vendtech.app.adapter.profile.NotificationListAdapter
import com.vendtech.app.helper.SharedHelper
import com.vendtech.app.models.profile.NotificationListModel
import com.vendtech.app.models.profile.NotificationListResult
import com.vendtech.app.network.Uten
import com.vendtech.app.utils.Constants
import com.vendtech.app.utils.CustomDialog
import com.vendtech.app.utils.Utilities
import kotlinx.android.synthetic.main.activity_notifications_list.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class NotificationsListActivity :Activity(){

    lateinit var recyclerView:RecyclerView;
    var pageNumber=1;
    lateinit var notificationListAdapter:NotificationListAdapter;
    internal var notificationList: MutableList<NotificationListResult> = ArrayList();
    internal var combinedList: MutableList<NotificationListResult> = ArrayList();


    //Pagination
    var loadings = true;
    var pastVisiblesItems = 0;
    var visibleItemCount = 0;
    var totalItemCount = 0;
    var totalItemsNo=10;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications_list)
        initView()
    }


    fun initView(){

        recyclerView=findViewById<View>(R.id.recyclerviewNotificationsList)as RecyclerView

        imgBack.setOnClickListener(View.OnClickListener {
            finish()
            overridePendingTransition(R.anim.activity_back_in, R.anim.activity_back_out)

        })

        GetNotificationsList()

    }

    fun GetNotificationsList(){

        var customDialog: CustomDialog
        customDialog= CustomDialog(this)
        customDialog.show()

        val call: Call<NotificationListModel> = Uten.FetchServerData().get_notifications(SharedHelper.getString(this, Constants.TOKEN),pageNumber.toString(),totalItemsNo.toString())
        call.enqueue(object : Callback<NotificationListModel> {
            override fun onResponse(call: Call<NotificationListModel>, response: Response<NotificationListModel>) {
                if(customDialog.isShowing){
                    customDialog.dismiss()
                }
                var data=response.body()
                if(data!=null){
                    if(data.status.equals("true")){
                        combinedList.clear();
                        try {
                            combinedList.addAll(response.body()!!.result.result1);
                        }catch (exception:Exception){

                        }
                        try{
                            combinedList.addAll(response.body()!!.result.result2);

                            /*for (items in response.body()!!.result.result2){
                               // var notificationListResult=NotificationListResult("","",items.type,items.userName,items.sentOn,items.id,items.rechargeId,items.meterNumber,items.productShortName,items.meterRechargeId,items.posId,items.vendorName,items.vendorId,items.amount,items.valueDate,items.status,items.transactionId,items.meterRechargeId,items.chkNoOrSlipId,items.comments,items.bank,items.posNumber,items.balance,items.newBalance,items.percentageAmount,items.depositId,items.payer,items.issuingBank,items.valueDate);
                                //combinedList.add(notificationListResult);
                                items.createdAt=items.createdAt;
                                combinedList.add(items);
                            }*/
                        }catch (exception:Exception){}
                        try {
                            //Log.d("BeforeSorting","----"+combinedList);
                            // combinedList.sortByDescending {it.valueDate}
                            /*Collections.sort(combinedList, Comparator { o1, o2 ->
                                o1.createdAt.compareTo(o2.createdAt)
                            })*/

                            for (items in combinedList){
                                // var notificationListResult=NotificationListResult("","",items.type,items.userName,items.sentOn,items.id,items.rechargeId,items.meterNumber,items.productShortName,items.meterRechargeId,items.posId,items.vendorName,items.vendorId,items.amount,items.valueDate,items.status,items.transactionId,items.meterRechargeId,items.chkNoOrSlipId,items.comments,items.bank,items.posNumber,items.balance,items.newBalance,items.percentageAmount,items.depositId,items.payer,items.issuingBank,items.valueDate);
                                //combinedList.add(notificationListResult);
                                val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm");
                                val date = formatter.parse(items.createdAt);
                                items.date=date;
                            }



                            //combinedList.sortByDescending { it.date }
                           // combinedList.sortBy { it.date.time}


                            /*var tempList=ArrayList<Date>();
                            val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm");
                            val date1 = formatter.parse("24/07/2021 12:00");
                            val date2 = formatter.parse("24/07/2021 02:07");
                            val date3 = formatter.parse("24/07/2021 01:54");

                            tempList.add(date1);
                            tempList.add(date2);
                            tempList.add(date3);

                            Log.d("BeforeSorting","----"+tempList);
                            tempList.sortBy { it.time}
                            Log.d("AfterSorting","----"+tempList);*/

                            // Log.d("AfterSorting","----"+combinedList);
                            //val cmp = compareBy<String> { LocalDateTime.parse(it, DateTimeFormatter.ofPattern("dd-MM-yyyy | HH:mm")) }
                            //  combinedList.sortedWith(cmp).forEach(::println);

                        }catch (exception:java.lang.Exception){
                            Log.d("Exception","---"+exception)
                        }
                        //sort list
                        if (combinedList.size>0){
                            if(combinedList.size<totalItemsNo){
                                //last item reached. so no more pagination required
                                loadings=false;
                            }else{
                                loadings=true;
                            }
                            if(pageNumber==1){

                                notificationList.clear();
                                notificationList.addAll(combinedList)
                                notificationList.sortByDescending { it.date }
                                SetUpAdapter(notificationList);
                            }else {
                                notificationList.addAll(combinedList)
                                notificationList.sortByDescending { it.date }
                                notificationListAdapter.notifyDataSetChanged();
                            }
                            //new code for sorting
                            //---------------
                            nodataTV.visibility=View.GONE;
                            recyclerView.visibility=View.VISIBLE;
                        }else{
                            if(notificationList.size<1) {
                                nodataTV.visibility=View.VISIBLE
                                recyclerView.visibility=View.GONE
                            }
                        }
                        //combinedList.addAll(response.body().result.)
                    }else{
                        Utilities.CheckSessionValid(data.message,this@NotificationsListActivity,this@NotificationsListActivity)

                    }
                }
            }
            override fun onFailure(call: Call<NotificationListModel>, t: Throwable) {
                val  gs = Gson()
                gs.toJson(t.localizedMessage)
                if(customDialog.isShowing){
                    customDialog.dismiss()
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        // Clear all notification
        GetNotificationsList()
        val nMgr = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nMgr.cancelAll()
    }

    fun SetUpAdapter(data:MutableList<NotificationListResult>){

        var mLayoutManager= LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        notificationListAdapter = NotificationListAdapter(this, data)
        recyclerView.adapter = notificationListAdapter
        recyclerView.layoutManager = mLayoutManager
        //recyclerView.setHasFixedSize(true)
        notificationListAdapter.notifyDataSetChanged()


        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0)
                //check for scroll down
                {
                    visibleItemCount = mLayoutManager.childCount
                    totalItemCount = mLayoutManager.itemCount
                    pastVisiblesItems = mLayoutManager.findFirstVisibleItemPosition()

                    if (loadings) {

                        if (visibleItemCount + pastVisiblesItems >= totalItemCount) {
                            loadings = false
                            Log.v("NotificationList", "-------------------------------------------Last Item Wow !--------------------")
                            pageNumber++
                            GetNotificationsList()
                        }
                    }
                }
            }
        })


    }
}