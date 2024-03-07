package com.vendtech.app.ui.activity.profile

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.chaos.view.PinView
import com.google.gson.Gson

import com.vendtech.app.R
import com.vendtech.app.helper.SharedHelper
import com.vendtech.app.models.profile.ChangePasswordModel
import com.vendtech.app.models.profile.ChangePasswordOTPModel
import com.vendtech.app.network.Uten
import com.vendtech.app.ui.activity.home.HomeActivity
import com.vendtech.app.utils.Constants
import com.vendtech.app.utils.CustomDialog
import com.vendtech.app.utils.Utilities
import kotlinx.android.synthetic.main.activity_change_password.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChangePasswordActivity : Activity(), View.OnClickListener {

    lateinit var back: ImageView
    lateinit var saveNow: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)
        initViews()
    }


    fun initViews() {
        back = findViewById<View>(R.id.imgBack) as ImageView
        saveNow = findViewById<View>(R.id.saveTV)as TextView
        back.setOnClickListener(this)
        saveNow.setOnClickListener(this)
    }

    override fun onClick(v: View) {

        when (v.id) {

            R.id.imgBack -> {
                finish()
                overridePendingTransition(R.anim.activity_back_in, R.anim.activity_back_out)
            }

            R.id.saveTV -> {

                if(TextUtils.isEmpty(oldpassET.text.toString().trim())){
                    Utilities.shortToast("Enter current password",this)
                }else if(oldpassET.text.toString().trim().length<6) {
                    Utilities.shortToast(resources.getString(R.string.old_pass_length),this)
                } else if(TextUtils.isEmpty(npET.text.toString().trim())){
                    Utilities.shortToast("Enter new password",this)
                }else if(npET.text.toString().trim().length<6) {
                    Utilities.shortToast(resources.getString(R.string.new_pass_length),this)
                } else if(TextUtils.isEmpty(cnpET.text.toString().trim())){
                    Utilities.shortToast("Enter confirm password",this)
                }else if(!npET.text.toString().trim().equals(cnpET.text.toString().trim())){
                    Utilities.shortToast("Password doesn't match",this)
                }else {
                    if(Uten.isInternetAvailable(this)){
                        ChangePassword()
                    }else{
                        Utilities.shortToast("No internet connection. Please check your network connectivity.",this)
                    }
                }
            }
        }

    }



    fun ChangePassword(){

        var customDialog: CustomDialog
        customDialog= CustomDialog(this)
        customDialog.show()

        val call: Call<ChangePasswordModel> = Uten.FetchServerData().change_password(SharedHelper.getString(this, Constants.TOKEN),oldpassET.text.toString().trim(),npET.text.toString().trim(),cnpET.text.toString().trim())
        call.enqueue(object : Callback<ChangePasswordModel> {
            override fun onResponse(call: Call<ChangePasswordModel>, response: Response<ChangePasswordModel>) {

                if(customDialog.isShowing){
                    customDialog.dismiss()
                }

                var data=response.body()
                if(data!=null){
                    Utilities.shortToast(data.message,this@ChangePasswordActivity)
                    if(data.status.equals("true")){
                        showOTPDialog()
                    }else {
                        Utilities.CheckSessionValid(data.message,this@ChangePasswordActivity,this@ChangePasswordActivity)


                    }
                }
            }

            override fun onFailure(call: Call<ChangePasswordModel>, t: Throwable) {
                val  gs = Gson()
                gs.toJson(t.localizedMessage)
                Utilities.shortToast("Something went wrong",this@ChangePasswordActivity)
                if(customDialog.isShowing){
                    customDialog.dismiss()
                }
            }
        })
    }


    private fun showOTPDialog() {
        val dialog = Dialog(this)
        dialog .requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog .setCancelable(true)
        dialog .setContentView(R.layout.dialog_enter_otp)
        val cancel = dialog .findViewById(R.id.submitDialog) as AppCompatTextView
        val firstPinView=dialog.findViewById(R.id.firstPinView)as PinView
        val mLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        cancel.setOnClickListener {
            if(TextUtils.isEmpty(firstPinView.text.toString().trim())){
                Utilities.shortToast("Please enter OTP",this)
            }else if (firstPinView.text.toString().trim().length<4){
                Utilities.shortToast("Please enter complete OTP",this)
            }else{
                VerifyOTP(firstPinView.text.toString().trim(),dialog)
            }
        }
        dialog .show()
    }


    fun VerifyOTP(otp:String,dialog: Dialog){

        var customDialog: CustomDialog
        customDialog= CustomDialog(this)
        customDialog.show()

        val call: Call<ChangePasswordOTPModel> = Uten.FetchServerData().change_password_OTP_verification(SharedHelper.getString(this, Constants.TOKEN),oldpassET.text.toString().trim(),npET.text.toString().trim(),cnpET.text.toString().trim(),otp)
        call.enqueue(object : Callback<ChangePasswordOTPModel> {
            override fun onResponse(call: Call<ChangePasswordOTPModel>, response: Response<ChangePasswordOTPModel>) {

                if(customDialog.isShowing){
                    customDialog.dismiss()
                }
                var data=response.body()

                if(data!=null){
                    Utilities.shortToast(data.message,this@ChangePasswordActivity)
                    if(data.status.equals("true")){
                        dialog.dismiss()
                        SharedHelper.putString(this@ChangePasswordActivity,Constants.USER_ACCOUNT_STATUS,data.accountStatus);
                        GotoHome()
                    }else {
                        Utilities.CheckSessionValid(data.message,this@ChangePasswordActivity,this@ChangePasswordActivity)

                    }
                }
            }

            override fun onFailure(call: Call<ChangePasswordOTPModel>, t: Throwable) {
                val  gs = Gson()
                gs.toJson(t.localizedMessage)
                dialog.dismiss()
                Utilities.shortToast("Something went wrong",this@ChangePasswordActivity)
                if(customDialog.isShowing){
                    customDialog.dismiss()
                }
            }
        })
    }

    fun GotoHome(){
        val intent = Intent(this@ChangePasswordActivity, HomeActivity::class.java)
        intent.flags =  Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
        overridePendingTransition(R.anim.slide_in, R.anim.slide_out)
    }

    override fun onBackPressed() {
        super.onBackPressed()

        finish()
        overridePendingTransition(R.anim.activity_back_in, R.anim.activity_back_out)

    }
}
