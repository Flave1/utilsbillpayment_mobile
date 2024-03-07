package com.vendtech.app.ui.activity.authentication

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson

import com.vendtech.app.R
import com.vendtech.app.helper.SharedHelper
import com.vendtech.app.models.authentications.ForgotPasswordModel
import com.vendtech.app.models.authentications.ResendOTPModel
import com.vendtech.app.models.authentications.VerifyOTPModel
import com.vendtech.app.network.Uten
import com.vendtech.app.utils.Constants
import com.vendtech.app.utils.CustomDialog
import com.vendtech.app.utils.Utilities
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class VerificationCodeActivity : AppCompatActivity() {

    lateinit var etOne: EditText
    lateinit var etTwo: EditText
    lateinit var etThree: EditText
    lateinit var etFour: EditText
    lateinit var txtSubmit: TextView
    lateinit var resendOTPTV: TextView
    var screenSource = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verification_code)

        screenSource = intent.getStringExtra("source")!!
        etOne = findViewById(R.id.etOne)
        etTwo = findViewById(R.id.etTwo)
        etThree = findViewById(R.id.etThree)
        etFour = findViewById(R.id.etFour)
        txtSubmit = findViewById(R.id.txtSubmit)
        resendOTPTV=findViewById(R.id.resendOTPTV)

        onClickListners()
        openKeyPad()
    }
    private fun openKeyPad(){
        val pinView = findViewById<EditText>(R.id.etOne)
        pinView.requestFocus()
    }
    private fun onClickListners() {
        etOne.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            }
            override fun afterTextChanged(s: Editable) {
                if (etOne.text.length > 0)
                    etTwo.requestFocus()
            }
        })


        etTwo.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable) {
                if (etTwo.text.length > 0)
                    etThree.requestFocus()
                else
                    etOne.requestFocus()
            }
        })
        etThree.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            }
            override fun afterTextChanged(s: Editable) {
                if (etThree.text.length > 0)
                    etFour.requestFocus()
                else
                    etTwo.requestFocus()
            }
        })
        etFour.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable) {
                if (etFour.text.length > 0)
                    txtSubmit.requestFocus()
                else
                    etThree.requestFocus()
            }
        })



        txtSubmit.setOnClickListener(View.OnClickListener {


            if(TextUtils.isEmpty(etOne.text.toString().trim()) ||
                    TextUtils.isEmpty(etTwo.text.toString().trim()) ||
                    TextUtils.isEmpty(etThree.text.toString().trim()) ||
                    TextUtils.isEmpty(etFour.text.toString().trim()) ){
                Utilities.shortToast("Please enter valid OTP",this)
            }else{
                if(Uten.isInternetAvailable(this)){
                    var code=etOne.text.toString().trim()+etTwo.text.toString().trim()+etThree.text.toString().trim()+etFour.text.toString().trim()
                    SubmitOTP(code)
                }else{
                    Utilities.shortToast("No internet connection. Please check your network connectivity.",this)
                }
            }
        })


        resendOTPTV.setOnClickListener(View.OnClickListener {

            if(Uten.isInternetAvailable(this)){
//                ResendOTP()
                resetPasscodeApi()
            }else{
                Utilities.shortToast("No internet connection. Please check your network connectivity.",this)
            }
        })
    }


    fun resetPasscodeApi() {

        var customDialog: CustomDialog
        customDialog = CustomDialog(this)
        customDialog.show()

        var email = SharedHelper.getString(this, Constants.USER_EMAIL)
        val call: Call<ForgotPasswordModel> = Uten.FetchServerData().forgot_passcode( email, SharedHelper.getString(this, Constants.POS_NUMBER))
        call.enqueue(object : Callback<ForgotPasswordModel> {
            override fun onResponse(call: Call<ForgotPasswordModel>, response: Response<ForgotPasswordModel>) {

                if (customDialog.isShowing) {
                    customDialog.dismiss()
                }

                var data = response.body()
                if (data != null) {
                    if (data.status.equals("true")) {
                        Utilities.shortToast("OTP has been resent to your mobile number", this@VerificationCodeActivity)
                    } else {
                        Utilities.CheckSessionValid(data.message, this@VerificationCodeActivity, this@VerificationCodeActivity)

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



    fun SubmitOTP(code:String){

        val userId =SharedHelper.getString(this,Constants.USER_ID);
        var customDialog:CustomDialog
        customDialog= CustomDialog(this)
        customDialog.show()

        val call: Call<VerifyOTPModel> = Uten.FetchServerData().verify_otp(code,userId)
        call.enqueue(object : Callback<VerifyOTPModel> {
            override fun onResponse(call: Call<VerifyOTPModel>, response: Response<VerifyOTPModel>) {

                if(customDialog.isShowing){
                    customDialog.dismiss()
                }
                var data=response.body()
                if(data!=null){

                    Utilities.shortToast(data.message,this@VerificationCodeActivity)
                    if(data.status.equals("true")){
                        if(screenSource.equals("forgotPasscodeActivity")){
                            GotoPassCodeScreen();
                        }else{
                            GotoLogin()
                        }
                    }else{
                        Utilities.CheckSessionValid(data.message,this@VerificationCodeActivity,this@VerificationCodeActivity)
                    }
                }
            }

            override fun onFailure(call: Call<VerifyOTPModel>, t: Throwable) {
                val  gs = Gson()
                gs.toJson(t.localizedMessage)
                if(customDialog.isShowing){
                    customDialog.dismiss()
                }
            }
        })


    }


    fun ResendOTP(){

        var customDialog:CustomDialog
        customDialog= CustomDialog(this)
        customDialog.show()

        val call: Call<ResendOTPModel> = Uten.FetchServerData().resend_otp(SharedHelper.getString(this,Constants.USER_ID))
        call.enqueue(object : Callback<ResendOTPModel> {
            override fun onResponse(call: Call<ResendOTPModel>, response: Response<ResendOTPModel>) {

                if(customDialog.isShowing){
                    customDialog.dismiss()
                }
                var data=response.body()
                if(data!=null){
                    Utilities.shortToast(data.message,this@VerificationCodeActivity)
                }
            }

            override fun onFailure(call: Call<ResendOTPModel>, t: Throwable) {
                val  gs = Gson()
                gs.toJson(t.localizedMessage)
                Utilities.shortToast("Something went wrong.",this@VerificationCodeActivity)
                if(customDialog.isShowing){
                    customDialog.dismiss()
                }
            }

        })

    }

    fun GotoLogin(){

        val intent = Intent(this@VerificationCodeActivity, LoginActivity::class.java)
        startActivity(intent)
        finish()
        overridePendingTransition(R.anim.activity_back_in, R.anim.activity_back_out)

    }
    fun GotoPassCodeScreen(){

        val intent = Intent(this@VerificationCodeActivity, PassCodeActivity::class.java)
        startActivity(intent)
        finish()
        overridePendingTransition(R.anim.activity_back_in, R.anim.activity_back_out)

    }
}
