package com.vendtech.app.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.vendtech.app.helper.SharedHelper
import com.vendtech.app.ui.activity.authentication.LoginActivity
import com.vendtech.app.ui.activity.profile.ChangePasswordActivity
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.*

class Utilities {


    companion object {

        fun shortToast(data: String, context: Context) {

            Toast.makeText(context, data, Toast.LENGTH_SHORT).show()
        }

        fun longToast(data: String, context: Context) {

            Toast.makeText(context, data, Toast.LENGTH_LONG).show()
        }

fun formatToUtc(date:String):String{
    try {
        var date = date
        var spf = SimpleDateFormat("dd/MM/yyyy hh:mm")
        spf.setTimeZone(TimeZone.getTimeZone("UTC"));
        val newDate = spf.parse(date)
        spf.timeZone = TimeZone.getDefault()
        spf = SimpleDateFormat("dd/MM/yyyy hh:mm")
        date = spf.format(newDate)

        return date

    } catch (e: Exception) {

        return "N/A"
    }
}
        fun changeDateFormat(context: Context, dates: String): String {


            try {
                var date = dates
                var spf = SimpleDateFormat("dd/MM/yyyy hh:mm")
                spf.setTimeZone(TimeZone.getTimeZone("UTC"));
                val newDate = spf.parse(date)
                spf.timeZone = TimeZone.getDefault()
                spf = SimpleDateFormat("MMM dd, yyyy")
                date = spf.format(newDate)

                return date

            } catch (e: Exception) {

                return "N/A"
            }

        }
        fun changeDateFormatWithAmPm(context: Context, dates: String): String {


            Log.d("BeforechangeDate",""+dates);


            try {
                var date = dates
                var spf = SimpleDateFormat("MM/dd/yyyy hh:mm:ss aa")
               // spf.setTimeZone(TimeZone.getTimeZone("UTC"));
                val newDate = spf.parse(date)
                spf.timeZone = TimeZone.getDefault()
                spf = SimpleDateFormat("dd/MM/yyyy : hh:mm aa")
                date = spf.format(newDate)

                return date

            } catch (e: Exception) {

                return "N/A"
            }

        }

       fun changeDateFormat(dates: String): String {


            try {
                var date = dates
                var spf = SimpleDateFormat("MM/dd/yyyy hh:mm:ss aa")
                spf.setTimeZone(TimeZone.getTimeZone("UTC"));
                val newDate = spf.parse(date)
                spf.timeZone = TimeZone.getDefault()
                spf = SimpleDateFormat("MMM dd, yyyy")
                date = spf.format(newDate)

                return date

            } catch (e: Exception) {

                return "N/A"
            }

        }

        fun DateFormatNotificationList(dates: String): String {
            try {
                var date = dates
                var spf = SimpleDateFormat("MM/dd/yyyy hh:mm:ss aa")
                spf.setTimeZone(TimeZone.getTimeZone("UTC"));
                val newDate = spf.parse(date)
                spf.timeZone = TimeZone.getDefault()
                spf = SimpleDateFormat("MMM dd, yyyy '-' hh:mm aa")
                date = spf.format(newDate)

                return date

            } catch (e: Exception) {

                return "N/A"
            }
        }


        fun changeTimeFormat(context: Context, dates: String): String {


            try {
                var date = dates
                var spf = SimpleDateFormat("MM/dd/yyyy hh:mm")
               // spf.setTimeZone(TimeZone.getTimeZone("UTC"));
                val newDate = spf.parse(date)
                spf.timeZone = TimeZone.getDefault()
                spf = SimpleDateFormat("hh:mm")
                date = spf.format(newDate)

                return date

            } catch (e: Exception) {

                return "N/A"
            }

        }


        fun GenerateInviteCode(): String {

            val chars1 = "ABCDEF012GHIJKL345MNOPQR678STUVWXYZ9".toCharArray()
            val sb1 = StringBuilder()
            val random1 = Random()
            for (i in 0..5) {
                val c1 = chars1[random1.nextInt(chars1.size)]
                sb1.append(c1)
            }
            val random_string = sb1.toString()

            return random_string
        }

        fun CheckSessionValid(string: String, context: Context, activity: Activity) {

            if (string.contentEquals(Constants.SESSION_EXPIRE_MESSAGE)) {

                longToast("Your session is expired. Please login again", context)
                SharedHelper.removeUserData(context)
                SharedHelper.putBoolean(context, Constants.IS_LOGGEDIN, false)
                val i = Intent(context, LoginActivity::class.java)
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                context.startActivity(i)
                activity.finish()

            }else if (string.contentEquals(Constants.ACCOUNT_DISABLE_MSG)) {

                longToast(string, context)
                SharedHelper.removeUserData(context)
                SharedHelper.putBoolean(context, Constants.IS_LOGGEDIN, false)
                val i = Intent(context, LoginActivity::class.java)
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                context.startActivity(i)
                activity.finish()

            } else {
                longToast(string, context)
            }

        }


        fun changeDateFormat2(context: Context, dates: String): String {
            try {
                var date = dates
                var spf = SimpleDateFormat("EEEE,dd MMMM yyyy")
                spf.setTimeZone(TimeZone.getTimeZone("UTC"));
                val newDate = spf.parse(date)
                spf.timeZone = TimeZone.getDefault()
                spf = SimpleDateFormat("MMM dd, yyyy")
                date = spf.format(newDate)

                return date


            } catch (e: Exception) {

                return "N/A"
            }
        }


        fun getSaveDir(context: Context): String {
            return context.filesDir.toString() + "/VendTech"
        }


        fun PleaseResetPassword(context: Context, isFinish: Boolean, activity: Activity) {

            val dialog = AlertDialog.Builder(context)
            dialog.setCancelable(false)
            dialog.setMessage("Please set a new password")
                    .setPositiveButton("Ok") { paramDialogInterface, paramInt ->
                        //  permissionsclass.requestPermission(type,code);
                        if (isFinish) {
                            val i = Intent(context, ChangePasswordActivity::class.java)
                            context.startActivity(i)
                            activity.finish()
                        } else {
                            val i = Intent(context, ChangePasswordActivity::class.java)
                            context.startActivity(i)

                        }

                    }

            dialog.show()

        }

        fun formatCurrencyValue(valueIn: String): String {
            Log.d("TAG", valueIn)
//
            if (valueIn.toDouble() == 0.0)
                return "00,00"

            val symbols = DecimalFormatSymbols()
            symbols.groupingSeparator = ','
            symbols.decimalSeparator = ','
            val decimalFormat = DecimalFormat("#,###.00", symbols)
            return decimalFormat.format(valueIn.toDouble())
        }

    }


}
