package com.vendtech.app.ui.fragment

import android.app.Activity
import android.os.Bundle

import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.vendtech.app.R
import com.vendtech.app.adapter.PosListAdapter
import com.vendtech.app.helper.SharedHelper
import com.vendtech.app.models.meter.PosListModel
import com.vendtech.app.network.Uten
import com.vendtech.app.utils.Constants
import com.vendtech.app.utils.CustomDialog
import com.vendtech.app.utils.Utilities
import kotlinx.android.synthetic.main.activity_notifications_list.*
import kotlinx.android.synthetic.main.fragment_pos_list.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.util.*
import kotlin.collections.ArrayList

class PosListActivity : Activity(){
    var pageNo=1
    val pageSize=10
    lateinit var posListAdapter: PosListAdapter
    var posList=ArrayList<PosListModel.Result>()
    var layoutManager: LinearLayoutManager?=null
    var isLastPage=false
    lateinit var recyclerView:RecyclerView;
    var totalBalance:Long=0;
    //ANIMATION
    lateinit var slide_in: Animation
    lateinit var slide_out: Animation


    companion object {
        fun newInstance(): PosListActivity {
            return PosListActivity()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_pos_list)
        layoutManager= LinearLayoutManager(this)
        posListAdapter= PosListAdapter(posList)
        posListRv.layoutManager=layoutManager
        posListRv.adapter=posListAdapter
        getPosList()
        setRecyclerViewScrollListener()
        val imgBack = findViewById<ImageView>(R.id.imgBack)
        imgBack.setOnClickListener{
            onBackPressed()
        }
        slide_in = AnimationUtils.loadAnimation(this, R.anim.slide_in)
        slide_out = AnimationUtils.loadAnimation(this, R.anim.activity_back_out)
    }

//    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
//      val view=LayoutInflater.from(context).inflate(R.layout.fragment_pos_list,container,false)
//        return view
//    }



//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        layoutManager= LinearLayoutManager(context)
//        posListAdapter= PosListAdapter(posList)
//        posListRv.layoutManager=layoutManager
//        posListRv.adapter=posListAdapter
//        getPosList()
//        setRecyclerViewScrollListener()
//    }

    private fun setRecyclerViewScrollListener() {
        posListRv.addOnScrollListener(object :RecyclerView.OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val visibleItemCount = layoutManager!!.childCount
                val totalItemCount = layoutManager!!.itemCount
                val firstVisibleItemPosition = layoutManager!!.findFirstVisibleItemPosition()

                if (!isLastPage) {
                    if (visibleItemCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition >= 0 && totalItemCount >= pageSize) {
                        pageNo++
                       getPosList()
                    }
                }
            }
        })
    }

    private fun getPosList() {
        var customDialog: CustomDialog
        customDialog = CustomDialog(this)
        customDialog.show()
        val call=Uten.FetchServerData().getPosList(SharedHelper.getString(this, Constants.TOKEN),pageNo,pageSize)
        call.enqueue(object :Callback<PosListModel>{
            override fun onFailure(call: Call<PosListModel>, t: Throwable) {
                val gs = Gson()
                gs.toJson(t.localizedMessage)
                if (customDialog.isShowing) {
                    customDialog.dismiss()
                }
            }
            override fun onResponse(call: Call<PosListModel>, response: Response<PosListModel>) {
                if(customDialog.isShowing){
                    customDialog.dismiss()
                }
                val data=response.body()
                if(data!=null){
                    if(data.status.equals("true")){
                        if(data.result.size>0){
                            setPosList(data)
                        }
                        else isLastPage=true
                    }else{
                        Utilities.CheckSessionValid(data.message, this@PosListActivity, this@PosListActivity)
                    }
                }
            }
        })
    }



    private fun setPosList(result: PosListModel) {
       posList.addAll(result.result)
       posListAdapter.notifyDataSetChanged()

      /* for (i in result.indices) {
           var temp=NumberFormat.getNumberInstance(Locale.US).format(result[i].balance.toLong());
           temp=temp.replace(",","");
           totalBalance=(2147483647+16988000);
           Log.d("TotalBalance","---"+totalBalance+"***********"+temp);
       }*/
       tv_total.setText(resources.getString(R.string.total_balance)+" "+NumberFormat.getNumberInstance(Locale.US).format(result.totalCount));

        if (result.totalCount.equals("0")){
            ll_total.visibility=View.GONE
        }


    }
}