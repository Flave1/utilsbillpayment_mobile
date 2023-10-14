package com.vendtech.app.helper

import android.content.Context
import android.content.SharedPreferences
import com.vendtech.app.utils.Constants

class SharedHelper {


    companion object{


        lateinit var sharedPreference: SharedPreferences
        lateinit var editor:SharedPreferences.Editor

        fun putString(context: Context,key: String,value: String?){
            sharedPreference=context.getSharedPreferences("VendTechSP",Context.MODE_PRIVATE)
            editor=sharedPreference.edit()
            editor.putString(key,value)
            editor.apply()
        }
        fun putBoolean(context: Context,key: String,value: Boolean){
            sharedPreference=context.getSharedPreferences("VendTechSP",Context.MODE_PRIVATE)
            editor=sharedPreference.edit()
            editor.putBoolean(key,value)
            editor.apply()
        }
        fun getString(context: Context,key: String) :String{
            sharedPreference=context.getSharedPreferences("VendTechSP",Context.MODE_PRIVATE)
            return sharedPreference.getString(key,"")!!
        }
        fun getBoolean(context: Context,key: String):Boolean{
            sharedPreference=context.getSharedPreferences("VendTechSP",Context.MODE_PRIVATE)
            return sharedPreference.getBoolean(key,false)
        }
        fun removeUserData(context: Context){
            putString(context,Constants.USER_FNAME,"")
            putBoolean(context,Constants.IS_LOGGEDIN,false)
            putString(context,Constants.TOKEN,"")
            putString(context,Constants.USER_LNAME,"")
            putString(context,Constants.USER_EMAIL,"")
            putString(context,Constants.USERNAME,"")
            putString(context,Constants.USER_ADDRESS,"")
            putString(context,Constants.USER_AVATAR,"")
            putString(context,Constants.USER_AVATAR,"")
            putString(context,Constants.USER_CITY,"")
            putString(context,Constants.USER_COUNTRY,"")
            putString(context,Constants.USER_PHONE,"")
            putString(context,Constants.USER_ID,"")
            putString(context,Constants.USER_TYPE,"")
            putString(context,Constants.PASS_CODE_VALUE,"")
            putString(context,Constants.BALANCE,"")
            if(!getBoolean(context,Constants.IS_REMEMBER_ME)){
                putString(context,Constants.REMEMBER_PASS,"")
                putString(context,Constants.REMEMBER_EMAIL,"")
            }
        }
    }

}