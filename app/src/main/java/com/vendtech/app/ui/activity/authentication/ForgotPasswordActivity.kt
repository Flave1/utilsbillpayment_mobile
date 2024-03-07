package com.vendtech.app.ui.activity.authentication

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.view.View
import android.widget.EditText
import android.widget.TextView
import com.google.gson.Gson

import com.vendtech.app.R
import com.vendtech.app.base.BaseActivity
import com.vendtech.app.helper.SharedHelper
import com.vendtech.app.models.authentications.ForgotPasswordModel
import com.vendtech.app.models.authentications.SignInResponse
import com.vendtech.app.network.Uten
import com.vendtech.app.utils.Constants
import com.vendtech.app.utils.CustomDialog
import com.vendtech.app.utils.Utilities
import kotlinx.android.synthetic.main.activity_forgot_password.*
import kotlinx.android.synthetic.main.activity_forgot_password.emailET
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ForgotPasswordActivity : BaseActivity() {

    lateinit var txtSubmit: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)
        txtSubmit = findViewById(R.id.txtSubmit)
        openKeyPad()

        //disable email and phno
        frgtPwdPhoneET.isEnabled=false
        //emailET.isEnabled=false


        txtSubmit.setOnClickListener { v ->
//            if (TextUtils.isEmpty(frgtPwdPhoneET.text.toString().trim())) {
//                Utilities.shortToast("Enter phone number", this);
//            } else if (frgtPwdPhoneET.text.toString().trim().length > 10) {
//                Utilities.shortToast("Enter a valid phone number", this);
//            }

             if (TextUtils.isEmpty(emailET.text.toString().trim())) {
                Utilities.longToast("Enter your Email address", this);
            } else if (!emailET.text.matches(Patterns.EMAIL_ADDRESS.toRegex())) {
                Utilities.longToast("Enter a valid Email", this);
            } else {
                if (Uten.isInternetAvailable(this)) {
                    // ForgotPassApi(emailET.text.toString().trim())
                    resetPasscodeApi(emailET.text.toString().trim());
                } else {
                    Utilities.longToast("No internet connection. Please check your network connectivity.", this);
                }
            }

        }

        layoutSignIn.setOnClickListener(View.OnClickListener {
            GotoLogin()
        })

       // getPosUserDetails();
    }

    fun getPosUserDetails() {

        var customDialog: CustomDialog
        customDialog = CustomDialog(this)
        customDialog.show()
        var vv=SharedHelper.getString(this, Constants.POS_NUMBER)

        val call: Call<SignInResponse> = Uten.FetchServerData().getPosUserDetails(SharedHelper.getString(this, Constants.POS_NUMBER))
        call.enqueue(object : Callback<SignInResponse> {
            override fun onResponse(call: Call<SignInResponse>, response: Response<SignInResponse>) {

                if (customDialog.isShowing) {
                    customDialog.dismiss()
                }
                var data = response.body()
                if (data != null) {

                    if (data.status.equals("true")) {

                        /* totalBalanceTV.setText("SLL : " + data.result.balance)
                        tickerViewBalance.setText("SLL : " + data.result.balance)
                        tickerViewBalance.setText(NumberFormat.getNumberInstance(Locale.US).format(data.result.balance.toDouble().toInt()))
                        //tickerViewBalance.setText(Utilities.formatCurrencyValue(data.result.balance))
                        totalAvlblBalance = data.result.balance.toDouble()
                        countInterface?.CountIs(data.result.unReadNotifications)*/

                        if (data.result!=null) {
                            ll_et.visibility = View.VISIBLE
                            frgtPwdPhoneET.setText(data.result.phone)
                            emailET.setText(data.result.email)


                        }else{
                            ll_et.visibility = View.INVISIBLE
                            Utilities.CheckSessionValid(data.message, this@ForgotPasswordActivity, this@ForgotPasswordActivity)
                        }

                    } else {
                        Utilities.CheckSessionValid(data.message, this@ForgotPasswordActivity, this@ForgotPasswordActivity)
                        ll_et.visibility=View.INVISIBLE;
                    }
                }
            }

            override fun onFailure(call: Call<SignInResponse>, t: Throwable) {
                val gs = Gson()
                gs.toJson(t.localizedMessage)
                if (customDialog.isShowing) {
                    customDialog.dismiss()
                }
                ll_et.visibility=View.INVISIBLE;
            }

        })
    }

    private fun openKeyPad(){
        val emView = findViewById<EditText>(R.id.emailET)
        emView.requestFocus()
    }
    fun resetPasscodeApi(email: String) {

        var customDialog: CustomDialog
        customDialog = CustomDialog(this)
        customDialog.show()

        var vvv=SharedHelper.getString(this, Constants.TOKEN)
        //SharedHelper.getString(this, Constants.TOKEN),
        val call: Call<ForgotPasswordModel> = Uten.FetchServerData().forgot_passcode( email, SharedHelper.getString(this, Constants.POS_NUMBER))
        call.enqueue(object : Callback<ForgotPasswordModel> {
            override fun onResponse(call: Call<ForgotPasswordModel>, response: Response<ForgotPasswordModel>) {

                if (customDialog.isShowing) {
                    customDialog.dismiss()
                }

                var data = response.body()
                if (data != null) {
                    if (data.status.equals("true")) {

                        Utilities.longToast("OTP has been sent to your registered number as SMS", this@ForgotPasswordActivity)
                        SharedHelper.putString(this@ForgotPasswordActivity, Constants.USER_ID, data.message)
                        SharedHelper.putString(this@ForgotPasswordActivity, Constants.USER_EMAIL, email)
                        GotoOTPScreen()
                    } else {
                        Utilities.CheckSessionValid(data.message, this@ForgotPasswordActivity, this@ForgotPasswordActivity)

                    }
                }
            }

            override fun onFailure(call: Call<ForgotPasswordModel>, t: Throwable) {
                val gs = Gson()
                gs.toJson(t.localizedMessage)
                if (customDialog.isShowing) {
                    customDialog.dismiss()
                }
            }
        })
    }

//    fun resetPasscodeApi(email: String, phno: String) {
//
//        var customDialog: CustomDialog
//        customDialog = CustomDialog(this)
//        customDialog.show()
//
//        var vvv=SharedHelper.getString(this, Constants.TOKEN)
//        //SharedHelper.getString(this, Constants.TOKEN),
//        val call: Call<ForgotPasswordModel> = Uten.FetchServerData().forgot_passcode( email, phno,SharedHelper.getString(this, Constants.POS_NUMBER))
//        call.enqueue(object : Callback<ForgotPasswordModel> {
//            override fun onResponse(call: Call<ForgotPasswordModel>, response: Response<ForgotPasswordModel>) {
//
//                if (customDialog.isShowing) {
//                    customDialog.dismiss()
//                }
//
//                var data = response.body()
//                if (data != null) {
//                    // Utilities.shortToast(data.message, this@ForgotPasswordActivity)
//                    if (data.status.equals("true")) {
//                        GotoLogin()
//                    } else {
//                        Utilities.CheckSessionValid(data.message, this@ForgotPasswordActivity, this@ForgotPasswordActivity)
//
//                    }
//                }
//            }
//
//            override fun onFailure(call: Call<ForgotPasswordModel>, t: Throwable) {
//                val gs = Gson()
//                gs.toJson(t.localizedMessage)
//                if (customDialog.isShowing) {
//                    customDialog.dismiss()
//                }
//            }
//        })
//    }



    /*fun ForgotPassApi(email:String){

        var customDialog: CustomDialog
        customDialog= CustomDialog(this)
        customDialog.show()

        val call: Call<ForgotPasswordModel> = Uten.FetchServerData().forgot_password(SharedHelper.getString(this,Constants.TOKEN),email)
        call.enqueue(object : Callback<ForgotPasswordModel> {
            override fun onResponse(call: Call<ForgotPasswordModel>, response: Response<ForgotPasswordModel>) {

                if(customDialog.isShowing){
                    customDialog.dismiss()
                }

                var data=response.body()
                if(data!=null){
                    Utilities.shortToast(data.message,this@ForgotPasswordActivity)
                    if(data.status.equals("true")){
                        GotoLogin()
                    }else{
                        Utilities.CheckSessionValid(data.message,this@ForgotPasswordActivity,this@ForgotPasswordActivity)

                    }
                }
            }

            override fun onFailure(call: Call<ForgotPasswordModel>, t: Throwable) {
                val  gs = Gson()
                gs.toJson(t.localizedMessage)
                if(customDialog.isShowing){
                    customDialog.dismiss()
                }
            }
        })
    }*/

    fun GotoLogin() {

        val intent = Intent(this@ForgotPasswordActivity, LoginActivity::class.java)
        startActivity(intent)
        finish()
        overridePendingTransition(R.anim.activity_back_in, R.anim.activity_back_out)
    }
    fun GotoOTPScreen() {

        val intent = Intent(this@ForgotPasswordActivity, VerificationCodeActivity::class.java)
        intent.putExtra("source", "forgotPasscodeActivity")
        startActivity(intent)
        finish()
        overridePendingTransition(R.anim.activity_back_in, R.anim.activity_back_out)
    }
}
