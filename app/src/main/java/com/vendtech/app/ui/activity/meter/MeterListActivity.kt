package com.vendtech.app.ui.activity.meter

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.vendtech.app.R
import com.vendtech.app.adapter.meter.MeterListAdapter
import com.vendtech.app.adapter.meter.MeterListNewAdapter
import com.vendtech.app.helper.SharedHelper
import com.vendtech.app.models.meter.GetMetersModel
import com.vendtech.app.models.meter.MeterListResults
import com.vendtech.app.network.Uten
import com.vendtech.app.utils.Constants
import com.vendtech.app.utils.CustomDialog
import com.vendtech.app.utils.Utilities
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*


class MeterListActivity : Activity(), View.OnClickListener, MeterListAdapter.ItemClickListener {



    lateinit var back: ImageView
    lateinit var edit: ImageView
    lateinit var editDone: ImageView
    lateinit var fabAdd: FloatingActionButton
    internal var meterListModels: MutableList<MeterListResults> = ArrayList()
    internal var finalmeterListModels: MutableList<MeterListResults> = ArrayList()
    lateinit var recyclerViewList: RecyclerView
    lateinit var meterListAdapter: MeterListNewAdapter
    lateinit var customDialog:CustomDialog
    lateinit var mainLayout:RelativeLayout
    lateinit var errorLayout:LinearLayout
    lateinit var backPress:TextView
    lateinit var retry:TextView
    lateinit var nodataTV:TextView

    //ANIMATION
    lateinit var slide_in: Animation
    lateinit var slide_out: Animation


    var isLastPage: Boolean = false
    var isLoading: Boolean = false
    var itemEditables = true



    //Pagination
    var loadings = true
    var pastVisiblesItems = 0
    var visibleItemCount = 0
    var totalItemCount = 0
    var totalItemsNo=50


    var page = 1

    private lateinit var mLayoutManager: LinearLayoutManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meter_list)
        customDialog = CustomDialog(this)

//        meterNumber.setOnClickListener {
//            val intent = Intent(this, RechargeTransactionDetails::class.java)
//            startActivity(intent)
//            overridePendingTransition(R.anim.slide_in, R.anim.slide_out)
//        }
        initView()
        //GetMeterList()

    }

    fun setOnclickIntent(data:MeterListResults){

        var intent=Intent();
        intent.putExtra("data",data);
        setResult(RESULT_OK,intent);
        finish();
    }



    fun initView() {

        back = findViewById<View>(R.id.imgBack) as ImageView
        edit = findViewById<View>(R.id.imgEdit) as ImageView
        editDone = findViewById<View>(R.id.imgEditDone) as ImageView
        fabAdd = findViewById<View>(R.id.fabAdd) as FloatingActionButton
        recyclerViewList = findViewById<View>(R.id.recyclerviewList) as RecyclerView
        mainLayout = findViewById<View>(R.id.mainlayout) as RelativeLayout
        errorLayout = findViewById<View>(R.id.error_layout) as LinearLayout
        backPress = findViewById<View>(R.id.backPress) as TextView
        retry=findViewById<View>(R.id.retry)as TextView
        nodataTV = findViewById<View>(R.id.nodataTV)as TextView


        back.setOnClickListener(this)
        edit.setOnClickListener(this)
        fabAdd.setOnClickListener(this)
        editDone.setOnClickListener(this)
        backPress.setOnClickListener(this)
        retry.setOnClickListener(this)


        slide_in = AnimationUtils.loadAnimation(this, R.anim.slide_in)
        slide_out = AnimationUtils.loadAnimation(this, R.anim.activity_back_out)

        //Set pagination for meter list

        recyclerViewList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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
                            Log.v("MeterListActivity", "-------------------------------------------Last Item Wow !--------------------")
                            page++
                            GetMeterList()
                        }
                    }
                }
            }
        })

    }


    fun GetMeterList(){

        customDialog.show()


        val call: Call<GetMetersModel> = Uten.FetchServerData().get_meters(SharedHelper.getString(this,Constants.TOKEN), page.toString(),totalItemsNo.toString())

        call.enqueue(object : Callback<GetMetersModel> {
            override fun onResponse(call: Call<GetMetersModel>, response: Response<GetMetersModel>) {


                mainLayout.visibility=View.VISIBLE
                errorLayout.visibility=View.GONE
                var data=response.body()
                    if(data!=null) {
                        if (data.status.equals("true")) {
                            if (data.result.size > 0) {
                                //   meterListModels.clear()
                                recyclerViewList.visibility = View.VISIBLE
                                nodataTV.visibility = View.GONE

                                if(itemEditables){
                                    edit.visibility = View.GONE
                                    editDone.visibility = View.VISIBLE

                                }else{
                                    edit.visibility = View.VISIBLE
                                    editDone.visibility = View.GONE
                                }

                                if(data.result.size<totalItemsNo){
                                    //last item reached. so no more pagination required
                                    loadings=false
                                }else{
                                    loadings=true
                                }

                                if(page==1){
                                    meterListModels.clear()
                                    meterListModels.addAll(data.result)
                                }else{
                                    meterListModels.addAll(data.result)
                                }
                                if(page==1){
                                    setData()
                                }else{
                                    meterListAdapter.notifyDataSetChanged()
                                }
                            } else {
                                recyclerViewList.visibility = View.GONE
                                nodataTV.visibility = View.VISIBLE
                                edit.visibility = View.GONE
                                editDone.visibility = View.GONE
                            }
                        }else {
                            Utilities.CheckSessionValid(data.message,this@MeterListActivity,this@MeterListActivity)
                        }
                }
                if(customDialog.isShowing){
                    customDialog.dismiss()
                }
            }

            override fun onFailure(call: Call<GetMetersModel>, t: Throwable) {

                isLoading = false
                val  gs = Gson()
                gs.toJson(t.localizedMessage)
                if(customDialog.isShowing){
                    customDialog.dismiss()
                }
                errorLayout.visibility =View.VISIBLE
                mainLayout.visibility = View.GONE
            }
        })
    }

    override fun itemDeleted() {

        GetMeterList()

    }
    fun setData() {
       Log.v("BeforeEdit",Gson().toJson(meterListModels));

       meterListAdapter = MeterListNewAdapter(meterListModels, this,itemEditables,this,SharedHelper.getString(this, Constants.PASS_CODE_VALUE),this);
       /* val mLayoutManager = LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
       recyclerViewList.adapter = meterListAdapter;
       recyclerViewList.layoutManager = mLayoutManager;
       //recyclerViewList.setHasFixedSize(true);
       meterListAdapter.notifyDataSetChanged();*/

       val llm = LinearLayoutManager(applicationContext);
       llm.orientation = LinearLayoutManager.VERTICAL;
       recyclerViewList!!.layoutManager = llm;
       recyclerViewList!!.adapter = meterListAdapter;
       meterListAdapter.notifyDataSetChanged();

    }
    fun ShowMainLayout(){

        mainLayout.visibility==View.VISIBLE
        errorLayout.visibility==View.GONE
    }
    override fun onClick(v: View) {


        when (v.id) {

            R.id.imgBack -> {

                finish()
                overridePendingTransition(R.anim.activity_back_in, R.anim.activity_back_out)
            }

            R.id.backPress -> {

                finish()
                overridePendingTransition(R.anim.activity_back_in, R.anim.activity_back_out)
            }

            R.id.retry -> {

                ShowMainLayout()
                GetMeterList()
            }

            R.id.imgEdit -> {


            /*    if (meterListAdapter != null) {
                    meterListAdapter!!.clear()
                }*/
              //  customData2()
                itemEditables = true
                editDone.visibility=View.VISIBLE
                edit.visibility=View.GONE

                setData()
                HideFabButton()

            }

            R.id.imgEditDone -> {


                /*if (meterListAdapter != null) {
                    meterListAdapter!!.clear()
                }*/
              //  customData3()

                itemEditables = false
                editDone.visibility=View.GONE
                edit.visibility=View.VISIBLE

                setData()
                ShowFabButton()

            }


            R.id.fabAdd -> {

                val i = Intent(this@MeterListActivity, AddMeterActivity::class.java)
                startActivity(i)
                overridePendingTransition(R.anim.slide_in, R.anim.slide_out)
            }
        }

    }
    override fun onResume() {
        super.onResume()
        GetMeterList()
    }
    @SuppressLint("RestrictedApi")
    fun ShowFabButton(){

        if (fabAdd.visibility == View.GONE) {
            fabAdd.startAnimation(slide_in)
        }
        fabAdd.visibility = View.VISIBLE

    }
    @SuppressLint("RestrictedApi")
    fun HideFabButton(){

        if (fabAdd.visibility == View.VISIBLE) {
            fabAdd.startAnimation(slide_out)
        }
        fabAdd.visibility = View.GONE


    }
}
