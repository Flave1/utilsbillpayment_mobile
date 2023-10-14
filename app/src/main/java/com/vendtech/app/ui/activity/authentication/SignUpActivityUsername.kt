package com.vendtech.app.ui.activity.authentication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import com.google.gson.Gson
import com.vendtech.app.R
import com.vendtech.app.helper.SharedHelper
import com.vendtech.app.models.authentications.CheckUsernameModel
import com.vendtech.app.models.authentications.ForgotPasswordModel
import com.vendtech.app.network.Uten
import com.vendtech.app.utils.Constants
import com.vendtech.app.utils.CustomDialog
import com.vendtech.app.utils.Utilities
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignUpActivityUsername : Activity(){


    lateinit var usernameET:EditText
    lateinit var passwordET:EditText
    lateinit var next:TextView
    lateinit var layoutSignIn:LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup_username)

        initViews()
    }


    fun initViews(){

        usernameET=findViewById<View>(R.id.usernamesET)as EditText
        passwordET=findViewById<View>(R.id.passwordET)as EditText
        next=findViewById<View>(R.id.txtNext)as TextView
        layoutSignIn=findViewById<View>(R.id.layoutSignIn)as LinearLayout

        layoutSignIn.setOnClickListener(View.OnClickListener {
            val intent = Intent(this@SignUpActivityUsername, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            overridePendingTransition(R.anim.activity_back_in, R.anim.activity_back_out)
        })


        next.setOnClickListener(View.OnClickListener {

            if(TextUtils.isEmpty(usernameET.text.toString().trim())){
                Utilities.shortToast("Enter username",this)
            }else if(usernameET.text.toString().trim().length<3){
                Utilities.shortToast(resources.getString(R.string.user_name_length),this)
            }else if(TextUtils.isEmpty(passwordET.text.toString().trim())){
                Utilities.shortToast("Enter password",this)
            }else if(passwordET.text.toString().trim().length<6){
                Utilities.shortToast(resources.getString(R.string.pass_length),this)
            }else {
                CheckUsernameExistance()
            }
        })
    }


    fun CheckUsernameExistance(){

        var customDialog: CustomDialog
        customDialog= CustomDialog(this)
        customDialog.show()

        val call: Call<CheckUsernameModel> = Uten.FetchServerData().check_username(usernameET.text.toString().trim())
        call.enqueue(object : Callback<CheckUsernameModel> {
            override fun onResponse(call: Call<CheckUsernameModel>, response: Response<CheckUsernameModel>) {
                if(customDialog.isShowing){
                    customDialog.dismiss()
                }
                var data=response.body()
                if(data!=null){
                    if(data.status.equals("true")){
                        GotoSignUpActivity()
                    }else {
                        Utilities.CheckSessionValid(data.message,this@SignUpActivityUsername,this@SignUpActivityUsername)
                    }
                }
            }
            override fun onFailure(call: Call<CheckUsernameModel>, t: Throwable) {
                val  gs = Gson()
                gs.toJson(t.localizedMessage)
                if(customDialog.isShowing){
                    customDialog.dismiss()
                }
            }
        })
    }

    fun GotoSignUpActivity(){

        val intent = Intent(this@SignUpActivityUsername, SignUpActivity::class.java)
        intent.putExtra("username",usernameET.text.toString().trim())
        intent.putExtra("password",passwordET.text.toString().trim())
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in, R.anim.slide_out)
    }
}