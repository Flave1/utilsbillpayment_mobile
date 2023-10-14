package com.vendtech.app.ui.activity.authentication

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.LinearLayout
import android.widget.TextView
import com.chaos.view.PinView

import com.vendtech.app.R
import com.vendtech.app.base.BaseActivity
import com.vendtech.app.helper.SharedHelper
import com.vendtech.app.models.authentications.SignInResponse
import com.vendtech.app.models.profile.GetProfileModel
import com.vendtech.app.network.Uten
import com.vendtech.app.ui.activity.home.HomeActivity
import com.vendtech.app.utils.Constants
import com.vendtech.app.utils.CustomDialog
import com.vendtech.app.utils.Utilities
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_login.emailET
import kotlinx.android.synthetic.main.activity_login.passwordET
import kotlinx.android.synthetic.main.activity_passcode.*
import kotlinx.android.synthetic.main.activity_passcode.rememberMeCB
import kotlinx.android.synthetic.main.activity_sign_up.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception

class PassCodeActivity : BaseActivity() {

    lateinit var layoutSignUp: LinearLayout
    lateinit var txtForgotPassword: TextView
    lateinit var loginResetPasscode: TextView
    lateinit var txtLogin: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_passcode)
        layoutSignUp = findViewById(R.id.layoutSignUp)
        txtLogin = findViewById(R.id.txtLogin)
        txtForgotPassword = findViewById(R.id.txtForgotPassword)
        loginResetPasscode = findViewById(R.id.loginResetPasscode)

        openKeyPad()
        layoutSignUp.setOnClickListener { v ->

            //val intent = Intent(this@LoginActivity, SignUpActivityUsername::class.java)
            val intent = Intent(this@PassCodeActivity, SignUpActivity::class.java)
            startActivity(intent)
            //finish()
            overridePendingTransition(R.anim.slide_in, R.anim.slide_out)
        }

        txtForgotPassword.setOnClickListener { v ->

            //launchActivity(ForgotPasswordActivity::class.java)
            GotoForgotPassword()
        }

        loginResetPasscode.setOnClickListener { v ->
            //launchActivity(ForgotPasswordActivity::class.java)
            GotoForgotPassword()
        }

        txtLogin.setOnClickListener { v -> processLogin() }

        if (!TextUtils.isEmpty(SharedHelper.getString(this, Constants.REMEMBER_EMAIL))) {
            emailET.setText(SharedHelper.getString(this, Constants.REMEMBER_EMAIL))
            passwordET.setText(SharedHelper.getString(this, Constants.REMEMBER_PASS))
        }

        //emailET.setText("vblell@gmail.com")
        //passwordET.setText("lauratu1")

    }

    private fun openKeyPad(){
        val pinView = findViewById<PinView>(R.id.newPinView)
        pinView.requestFocus()
    }
    fun processLogin() {

//        if (TextUtils.isEmpty(emailET.text)) {
//            Utilities.shortToast("Enter your email address or username", this);
//        } else if (TextUtils.isEmpty(passwordET.text)) {
//            Utilities.shortToast("Enter your password", this);
//        } else if (passwordET.text.toString().trim().length < 6) {
//            Utilities.shortToast(resources.getString(R.string.pass_length), this)
//        } else {
//            if (Uten.isInternetAvailable(this)) {
//                performLogin()
//            } else {
//                Utilities.shortToast("No internet connection. Please check your network connectivity.", this)
//            }
//        }

        if(TextUtils.isEmpty(newPinView.text.toString().trim())){
            Utilities.shortToast("Please enter PASSCODE",this)
        }else if (newPinView.text.toString().trim().length<5){
            Utilities.shortToast("Please enter complete PASSCODE",this)
        }else{
            performLoginNew(newPinView.text.toString().trim())

        }

    }




    fun GotoForgotPassword() {

        val intent = Intent(this@PassCodeActivity, ForgotPasswordActivity::class.java)
        startActivity(intent)

        overridePendingTransition(R.anim.slide_in, R.anim.slide_out)
    }

    fun GotoAppUpdate() {

        val intent = Intent(this@PassCodeActivity, UpdateAppVersion::class.java)
        startActivity(intent)

        overridePendingTransition(R.anim.slide_in, R.anim.slide_out)
    }

    private fun performLoginNew(pscode: String) {
        var customDialog = CustomDialog(this)
        customDialog.setCancelable(false)
        customDialog.show()
        val userId =SharedHelper.getString(this,Constants.USER_ID);

         val call: Call<SignInResponse> = Uten.FetchServerData().sign_in_new_passcode( pscode, SharedHelper.getString(this, Constants.DEVICE_TOKEN), Constants.DEVICE_TYPE, userId)

        call.enqueue(object : Callback<SignInResponse> {
            override fun onResponse(call: Call<SignInResponse>, response: Response<SignInResponse>) {
                customDialog.dismiss()
                var data = response.body()
                Log.d("dasffsdafasdf", "onResponse: $data")
                if (data != null) {
                    Utilities.shortToast(data.message, this@PassCodeActivity)
                    if (data.status.equals("true")) {

                        if(data.message == "APP VERSION IS OUT OF DATE, PLEASE UPDATE APP FROM PLAYSTORE"){
                            GotoAppUpdate()
                        }else{
                            SharedHelper.putBoolean(this@PassCodeActivity, Constants.IS_LOGGEDIN, true)
                            SharedHelper.putString(this@PassCodeActivity, Constants.TOKEN, data.result.token)
                            SharedHelper.putString(this@PassCodeActivity, Constants.USER_FNAME, data.result.firstName)
                            SharedHelper.putString(this@PassCodeActivity, Constants.USER_LNAME, data.result.lastName)
                            SharedHelper.putString(this@PassCodeActivity, Constants.USER_ID, data.result.userId)
                            SharedHelper.putString(this@PassCodeActivity, Constants.USER_EMAIL, data.result.email)
                            SharedHelper.putString(this@PassCodeActivity, Constants.USER_ACCOUNT_STATUS, data.result.accountStatus)
                            SharedHelper.putString(this@PassCodeActivity, Constants.POS_NUMBER, data.result.posNumber)
                            SharedHelper.putString(this@PassCodeActivity, Constants.MIN_VEND, data.result.minVend)
                            SharedHelper.putString(this@PassCodeActivity, Constants.COMMISSION_PERCENTAGE, data.result.percentage)
                            SharedHelper.putString(this@PassCodeActivity, Constants.PASS_CODE_VALUE,loginFirstPinView.text.toString().trim())
                            SharedHelper.putString(this@PassCodeActivity, Constants.VENDOR, data.result.vendor)
                            //var vv=SharedHelper.getString(this@LoginActivity, Constants.PASS_CODE_VALUE)
                            if (data.result.phone != null || data.result.phone == "null") {
                                SharedHelper.putString(this@PassCodeActivity, Constants.USER_PHONE, data.result.phone)
                            } else {
                                SharedHelper.putString(this@PassCodeActivity, Constants.USER_PHONE, "")
                            }
                            SharedHelper.putString(this@PassCodeActivity, Constants.USER_TYPE, data.result.userType)
                            if (rememberMeCB.isChecked) {
                                SharedHelper.putBoolean(this@PassCodeActivity, Constants.IS_REMEMBER_ME, true)
                                SharedHelper.putString(this@PassCodeActivity, Constants.REMEMBER_EMAIL, emailET.text.toString().trim())
                                SharedHelper.putString(this@PassCodeActivity, Constants.REMEMBER_PASS, passwordET.text.toString().trim())
                            } else {
                                SharedHelper.putBoolean(this@PassCodeActivity, Constants.IS_REMEMBER_ME, false)
                            }
                            GetProfile();
                        }

                    }else{
                        Utilities.shortToast(data.message, this@PassCodeActivity)
                    }
                } else {
                    Utilities.shortToast("Something went wrong!", this@PassCodeActivity)
                }
            }
            override fun onFailure(call: Call<SignInResponse>, t: Throwable) {
                Log.d("LoginError","---"+t.localizedMessage)
                customDialog.dismiss()
                Utilities.shortToast("Something went wrong!", this@PassCodeActivity)
            }
        })
    }

    /*fun performLogin() {

        var customDialog = CustomDialog(this)
        customDialog.setCancelable(false)
        customDialog.show()

        val call: Call<SignInResponse> = Uten.FetchServerData().sign_in(emailET.text.trim().toString(), passwordET.text.trim().toString(), SharedHelper.getString(this, Constants.DEVICE_TOKEN), Constants.DEVICE_TYPE)

        call.enqueue(object : Callback<SignInResponse> {
            override fun onResponse(call: Call<SignInResponse>, response: Response<SignInResponse>) {
                customDialog.dismiss()
                var data = response.body()
                Log.d("dasffsdafasdf", "onResponse: $data")
                if (data != null) {
                    Utilities.shortToast(data.message, this@LoginActivity)
                    if (data.status.equals("true")) {
                        SharedHelper.putBoolean(this@LoginActivity, Constants.IS_LOGGEDIN, true)
                        SharedHelper.putString(this@LoginActivity, Constants.TOKEN, data.result.token)
                        SharedHelper.putString(this@LoginActivity, Constants.USER_FNAME, data.result.firstName)
                        SharedHelper.putString(this@LoginActivity, Constants.USER_LNAME, data.result.lastName)
                        SharedHelper.putString(this@LoginActivity, Constants.USER_ID, data.result.userId)
                        SharedHelper.putString(this@LoginActivity, Constants.USER_EMAIL, data.result.email)
                        SharedHelper.putString(this@LoginActivity, Constants.USER_ACCOUNT_STATUS, data.result.accountStatus)
                        SharedHelper.putString(this@LoginActivity, Constants.POS_NUMBER, data.result.posNumber)
                        SharedHelper.putString(this@LoginActivity, Constants.COMMISSION_PERCENTAGE, data.result.percentage)
                        if (data.result.phone != null || data.result.phone == "null") {
                            SharedHelper.putString(this@LoginActivity, Constants.USER_PHONE, data.result.phone)
                        } else {
                            SharedHelper.putString(this@LoginActivity, Constants.USER_PHONE, "")
                        }
                        SharedHelper.putString(this@LoginActivity, Constants.USER_TYPE, data.result.userType)
                        if (rememberMeCB.isChecked) {
                            SharedHelper.putBoolean(this@LoginActivity, Constants.IS_REMEMBER_ME, true)
                            SharedHelper.putString(this@LoginActivity, Constants.REMEMBER_EMAIL, emailET.text.toString().trim())
                            SharedHelper.putString(this@LoginActivity, Constants.REMEMBER_PASS, passwordET.text.toString().trim())
                        } else {
                            SharedHelper.putBoolean(this@LoginActivity, Constants.IS_REMEMBER_ME, false)
                        }
                        GetProfile()
                    }
                } else {
                    Utilities.shortToast("Something went wrong!", this@LoginActivity)
                }
            }
            override fun onFailure(call: Call<SignInResponse>, t: Throwable) {
                Log.d("LoginError","---"+t.localizedMessage)
                customDialog.dismiss()
                Utilities.shortToast("Something went wrong!", this@LoginActivity)
            }
        })
    }*/

    fun GetProfile() {

        var customDialog = CustomDialog(this)
        customDialog.setCancelable(false)
        customDialog.show()

        val call: Call<GetProfileModel> = Uten.FetchServerData().get_user_profile(SharedHelper.getString(this, Constants.TOKEN))

        call.enqueue(object : Callback<GetProfileModel> {
            override fun onResponse(call: Call<GetProfileModel>, response: Response<GetProfileModel>) {
                customDialog.dismiss()
                var data = response.body()

                if (data != null) {
                    //Utilities.shortToast(data.message,this@LoginActivity)
                    if (data.status.equals("true")) {

                        try {
                            SharedHelper.putBoolean(this@PassCodeActivity, Constants.IS_LOGGEDIN, true)
                            SharedHelper.putString(this@PassCodeActivity, Constants.USER_FNAME, data.result.name)
                            SharedHelper.putString(this@PassCodeActivity, Constants.USER_LNAME, data.result.surName)
                            SharedHelper.putString(this@PassCodeActivity, Constants.USER_ID, data.result.userId)
                            SharedHelper.putString(this@PassCodeActivity, Constants.USER_EMAIL, data.result.email)
                            SharedHelper.putString(this@PassCodeActivity, Constants.USER_PHONE, data.result.phone)
                            SharedHelper.putString(this@PassCodeActivity, Constants.USER_CITY, data.result.city)
                            SharedHelper.putString(this@PassCodeActivity, Constants.USER_COUNTRY, data.result.country)
                            SharedHelper.putString(this@PassCodeActivity, Constants.USER_AVATAR, data.result.profilePic)
                            SharedHelper.putString(this@PassCodeActivity, Constants.USERNAME, data.result.userName)
                            SharedHelper.putString(this@PassCodeActivity, Constants.USER_ADDRESS, data.result.address)
                            SharedHelper.putString(this@PassCodeActivity, Constants.BALANCE, data.result.balance)

                        } catch (e: Exception) {

                        }

                        if (SharedHelper.getString(this@PassCodeActivity, Constants.USER_ACCOUNT_STATUS).equals(Constants.STATUS_PASSWORD_NOT_RESET)) {

                            Utilities.PleaseResetPassword(this@PassCodeActivity, false, this@PassCodeActivity)

                        } else {

                            GotoHome()
                        }
                    } else {
                        Utilities.CheckSessionValid(data.message, this@PassCodeActivity, this@PassCodeActivity)

                    }
                } else {
                    Utilities.shortToast("Something went wrong!", this@PassCodeActivity)
                }
            }

            override fun onFailure(call: Call<GetProfileModel>, t: Throwable) {
                customDialog.dismiss()
                Utilities.shortToast("Something went wrong!", this@PassCodeActivity)
            }
        })
    }


    fun GotoHome() {

        val intent = Intent(this@PassCodeActivity, HomeActivity::class.java)
        startActivity(intent)
        finish()
        overridePendingTransition(R.anim.slide_in, R.anim.slide_out)
    }
}
