package com.vendtech.app.ui.activity.termspolicies

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.google.gson.Gson
import com.vendtech.app.R
import com.vendtech.app.helper.SharedHelper
import com.vendtech.app.models.meter.RechargeMeterModel
import com.vendtech.app.models.termspolicies.ContactUsModel
import com.vendtech.app.models.termspolicies.TermsPoliciesModel
import com.vendtech.app.network.Uten
import com.vendtech.app.utils.Constants
import com.vendtech.app.utils.CustomDialog
import com.vendtech.app.utils.Utilities
import kotlinx.android.synthetic.main.activity_terms_policy.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ContactUsActivity : Activity(), View.OnClickListener {


    lateinit var back: ImageView
    lateinit var subjectET: EditText
    lateinit var messageET: EditText
    lateinit var sendTV: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contactus)
        initViews()

    }


    fun initViews() {

        back = findViewById<View>(R.id.imgBack) as ImageView
        subjectET = findViewById<View>(R.id.subjectET) as EditText
        messageET = findViewById<View>(R.id.commentET) as EditText
        sendTV = findViewById<View>(R.id.sendTV) as TextView

        back.setOnClickListener(this)
        sendTV.setOnClickListener(this)

    }

    override fun onClick(v: View) {


        when (v.id) {

            R.id.imgBack -> {
                finish()
                overridePendingTransition(R.anim.activity_back_in, R.anim.activity_back_out)
            }

            R.id.sendTV -> {

             if(TextUtils.isEmpty(subjectET.text.toString().trim())){
                    Utilities.shortToast("Enter subject",this)
                }else if(subjectET.text.toString().trim().length<20){
                    Utilities.shortToast("Entered subject is too short",this)
                }else if(TextUtils.isEmpty(messageET.text.toString().trim())){
                    Utilities.shortToast("Enter message",this)
                }else if(messageET.text.toString().trim().length<40){
                    Utilities.shortToast("Entered message is too short",this)
                }else {
                    SendMessage()
                }
            }
        }
    }


    fun SendMessage(){

        var customDialog: CustomDialog
        customDialog= CustomDialog(this)
        customDialog.show()

        val call: Call<ContactUsModel> = Uten.FetchServerData().contact_us(SharedHelper.getString(this, Constants.TOKEN),subjectET.text.toString().trim(),messageET.text.toString().trim())
        call.enqueue(object : Callback<ContactUsModel> {
            override fun onResponse(call: Call<ContactUsModel>, response: Response<ContactUsModel>) {

                if(customDialog.isShowing){
                    customDialog.dismiss()
                }
                var data=response.body()
                if(data!=null){
                    Utilities.shortToast(data.message,this@ContactUsActivity)
                    if(data.status.equals("true")){
                        finish()
                        overridePendingTransition(R.anim.activity_back_in, R.anim.activity_back_out)
                    }else{
                        Utilities.CheckSessionValid(data.message,this@ContactUsActivity,this@ContactUsActivity)
                    }
                }
            }

            override fun onFailure(call: Call<ContactUsModel>, t: Throwable) {
                val  gs = Gson()
                gs.toJson(t.localizedMessage)
                if(customDialog.isShowing){
                    customDialog.dismiss()
                }
                Utilities.shortToast("Something went wrong",this@ContactUsActivity)
            }
        })
    }
}
