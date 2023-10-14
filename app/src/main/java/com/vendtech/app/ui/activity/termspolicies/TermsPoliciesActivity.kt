package com.vendtech.app.ui.activity.termspolicies

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.google.gson.Gson

import com.vendtech.app.R
import com.vendtech.app.models.termspolicies.TermsPoliciesModel
import com.vendtech.app.network.Uten
import com.vendtech.app.utils.CustomDialog
import com.vendtech.app.utils.Utilities
import kotlinx.android.synthetic.main.activity_change_password.*
import kotlinx.android.synthetic.main.activity_terms_policy.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TermsPoliciesActivity : Activity(), View.OnClickListener {


    lateinit var back: ImageView
    lateinit var title: TextView
    internal var titleIS = ""
    internal var type=""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_terms_policy)

        titleIS = intent.getStringExtra("title")!!
        type=intent.getStringExtra("type")!!
        initViews()

        if(Uten.isInternetAvailable(this)){

            GetPrivacyPOlicies()

        }else{
            Utilities.shortToast("No internet connection. Please check your network connectivity.",this)
        }

    }


    fun initViews() {

        back = findViewById<View>(R.id.imgBack) as ImageView
        title = findViewById<View>(R.id.Title) as TextView
        title.text = titleIS
        back.setOnClickListener(this)

    }

    override fun onClick(v: View) {


        when (v.id) {

            R.id.imgBack -> {
                finish()
                overridePendingTransition(R.anim.activity_back_in, R.anim.activity_back_out)
            }
        }

    }


    fun GetTermsPolicies(){


        var customDialog: CustomDialog
        customDialog= CustomDialog(this)
        customDialog.show()

        val call: Call<TermsPoliciesModel> = Uten.FetchServerData().get_terms()
        call.enqueue(object : Callback<TermsPoliciesModel> {
            override fun onResponse(call: Call<TermsPoliciesModel>, response: Response<TermsPoliciesModel>) {

                if(customDialog.isShowing){
                    customDialog.dismiss()
                }

                var data=response.body()
                if(data!=null){
                    if(data.status.equals("true")){
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            //datatextTV.setText(Html.fromHtml(data.result.html, Html.FROM_HTML_MODE_COMPACT));
                        } else {
                           // datatextTV.setText(Html.fromHtml(data.result.html));
                        }
                    }else {
                        Utilities.CheckSessionValid(data.message,this@TermsPoliciesActivity,this@TermsPoliciesActivity)
                    }
                }
            }

            override fun onFailure(call: Call<TermsPoliciesModel>, t: Throwable) {
                val  gs = Gson()
                gs.toJson(t.localizedMessage)
                Utilities.shortToast("Something went wrong",this@TermsPoliciesActivity)
                if(customDialog.isShowing){
                    customDialog.dismiss()
                }
            }
        })
    }

    fun GetPrivacyPOlicies(){


        var customDialog: CustomDialog
        customDialog= CustomDialog(this)
        customDialog.show()

        val call: Call<TermsPoliciesModel> = Uten.FetchServerData().get_policies()
        call.enqueue(object : Callback<TermsPoliciesModel> {
            override fun onResponse(call: Call<TermsPoliciesModel>, response: Response<TermsPoliciesModel>) {

                if(customDialog.isShowing){
                    customDialog.dismiss()
                }

                var data=response.body()
                if(data!=null){

                    if(data.status.equals("true")){

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            datatextTV.setText(Html.fromHtml(data.result.termsHtml, Html.FROM_HTML_MODE_COMPACT));
                            datatextPolicyTV.setText(Html.fromHtml(data.result.privacyPolicyHtml, Html.FROM_HTML_MODE_COMPACT));

                        } else {
                            datatextTV.setText(Html.fromHtml(data.result.termsHtml));
                            datatextPolicyTV.setText(Html.fromHtml(data.result.privacyPolicyHtml));
                        }
                    }else {
                        Utilities.CheckSessionValid(data.message,this@TermsPoliciesActivity,this@TermsPoliciesActivity)
                    }
                }
            }

            override fun onFailure(call: Call<TermsPoliciesModel>, t: Throwable) {
                val  gs = Gson()
                gs.toJson(t.localizedMessage)
                Utilities.shortToast("Something went wrong",this@TermsPoliciesActivity)
                if(customDialog.isShowing){
                    customDialog.dismiss()
                }
            }
        })
    }
}
