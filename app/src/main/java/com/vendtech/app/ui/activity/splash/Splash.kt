package com.vendtech.app.ui.activity.splash

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.util.Log
import android.view.Window
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.vendtech.app.R
import com.vendtech.app.base.BaseActivity
import com.vendtech.app.helper.SharedHelper
import com.vendtech.app.models.profile.GetProfileModel
import com.vendtech.app.network.Uten
import com.vendtech.app.ui.Print.StringAlignUtils
import com.vendtech.app.ui.activity.authentication.LoginActivity
import com.vendtech.app.ui.activity.home.HomeActivity
import com.vendtech.app.utils.Constants
import com.vendtech.app.utils.Utilities
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Splash : BaseActivity() {


    private val TAG = "SplashActivty"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)



       // if(checkAndRequestPermissions()){
           GoInsideApp()
        //}


        var tv_address=
            ("Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt "
                    + "ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris "
                    + "nisi ut aliquip ex ea commodo consequat.")
        var rightALign= StringAlignUtils(tv_address.length, StringAlignUtils.Alignment.CENTER);
        Log.d("---",""+rightALign.format(tv_address))



    }

    private fun checkAndRequestPermissions(): Boolean {
        //val camerapermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        val writepermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        val permissionRecordAudio = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
        val listPermissionsNeeded = ArrayList<String>()

      /*  if (camerapermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CAMERA)
        }*/
        if (writepermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if (permissionRecordAudio != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        if (!listPermissionsNeeded.isEmpty()) {
            showPermissionDetailDialog(listPermissionsNeeded)
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        Log.d(TAG, "Permission callback called-------")
        when (requestCode) {
            REQUEST_ID_MULTIPLE_PERMISSIONS -> {

                val perms = HashMap<String, Int>()
                // Initialize the map with both permissions
              //  perms[Manifest.permission.CAMERA] = PackageManager.PERMISSION_GRANTED
                perms[Manifest.permission.WRITE_EXTERNAL_STORAGE] = PackageManager.PERMISSION_GRANTED
                perms[Manifest.permission.READ_EXTERNAL_STORAGE] = PackageManager.PERMISSION_GRANTED
                // Fill with actual results from user
                if (grantResults.size > 0) {
                    for (i in permissions.indices)
                        perms[permissions[i]] = grantResults[i]
                    // Check for both permissions
                    if (/*perms[Manifest.permission.CAMERA] == PackageManager.PERMISSION_GRANTED
                            && */perms[Manifest.permission.WRITE_EXTERNAL_STORAGE] == PackageManager.PERMISSION_GRANTED
                            && perms[Manifest.permission.READ_EXTERNAL_STORAGE] == PackageManager.PERMISSION_GRANTED
                    ) {
                        Log.d(TAG, "sms & location services permission granted")
                        // process the normal flow
                        GoInsideApp()
                        //else any one or both the permissions are not granted
                    } else {
                        Log.d(TAG, "Some permissions are not granted ask again ")
                        //permission is denied (this is the first time, when "never ask again" is not checked) so ask again explaining the usage of permission
                        //                        // shouldShowRequestPermissionRationale will return true
                        //show the dialog or snackbar saying its necessary and try again otherwise proceed with setup.
                        if (/*ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)
                                ||*/ ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)
                                /*|| ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)*/) {
                            showDialogOK("Service Permissions are required for this app",
                                    DialogInterface.OnClickListener { dialog, which ->
                                        when (which) {
                                            DialogInterface.BUTTON_POSITIVE -> checkAndRequestPermissions()
                                            DialogInterface.BUTTON_NEGATIVE ->
                                                // proceed with logic by disabling the related features or quit the app.
                                                //finish()
                                                GoInsideApp()
                                        }
                                    })
                        } else {
                            explain("You need to give some mandatory permissions to continue. Do you want to go to app settings?")
                            //                            //proceed with logic by disabling the related features or quit the app.
                        }//permission is denied (and never ask again is  checked)
                        //shouldShowRequestPermissionRationale will return false
                    }
                }
            }
        }

    }

    private fun showDialogOK(message: String, okListener: DialogInterface.OnClickListener) {
        AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", okListener)
                .create()
                .show()
    }

    private fun explain(msg: String) {
        val dialog = AlertDialog.Builder(this)
        dialog.setMessage(msg)
                .setPositiveButton("Yes") { paramDialogInterface, paramInt ->
                    //  permissionsclass.requestPermission(type,code);
                    startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:com.vendtech.app")))
                }
                .setNegativeButton("Cancel") { paramDialogInterface, paramInt -> finish() }
        dialog.show()
    }

    companion object {

        val REQUEST_ID_MULTIPLE_PERMISSIONS = 1
        private val SPLASH_TIME_OUT = 2000
    }


    fun GetProfile(){

        val call:Call<GetProfileModel> = Uten.FetchServerData().get_user_profile(SharedHelper.getString(this,Constants.TOKEN))
        call.enqueue(object : Callback<GetProfileModel> {
            override fun onResponse(call: Call<GetProfileModel>, response: Response<GetProfileModel>) {
                var data=response.body()

                if(data!=null){
                    //Utilities.shortToast(data.message,this@LoginActivity)
                    if(data.status.equals("true")){

                        SharedHelper.putBoolean(this@Splash,Constants.IS_LOGGEDIN,true)
                        SharedHelper.putString(this@Splash,Constants.USER_FNAME,data.result.name)
                        SharedHelper.putString(this@Splash,Constants.USER_LNAME,data.result.surName)
                        SharedHelper.putString(this@Splash,Constants.USER_ID,data.result.userId)
                        SharedHelper.putString(this@Splash,Constants.USER_EMAIL,data.result.email)
                        SharedHelper.putString(this@Splash,Constants.USER_PHONE,data.result.phone)
                        SharedHelper.putString(this@Splash,Constants.USER_CITY,data.result.city)
                        SharedHelper.putString(this@Splash,Constants.USER_COUNTRY,data.result.country)
                        SharedHelper.putString(this@Splash,Constants.USER_AVATAR,data.result.profilePic)
                        SharedHelper.putString(this@Splash,Constants.USERNAME,data.result.userName)
                        SharedHelper.putString(this@Splash,Constants.USER_ADDRESS,data.result.address)
                        SharedHelper.putString(this@Splash,Constants.USER_ACCOUNT_STATUS,data.result.accountStatus)
                        launchActivity(HomeActivity::class.java)
                        finish()
                    }else{
                        Utilities.CheckSessionValid(data.message,this@Splash,this@Splash)
                    }
                }else {
                    Utilities.shortToast("Something went wrong!",this@Splash)
                }
            }
            override fun onFailure(call: Call<GetProfileModel>, t: Throwable) {
                Utilities.shortToast("Something went wrong!",this@Splash)
            }
        })
    }


    fun GoInsideApp(){
        Handler().postDelayed({

            if(SharedHelper.getString(this@Splash,Constants.USER_ACCOUNT_STATUS).equals(Constants.STATUS_PASSWORD_NOT_RESET)){
                Utilities.PleaseResetPassword(this@Splash,true,this@Splash)
            }else{
                if(SharedHelper.getBoolean(this,Constants.IS_LOGGEDIN)){
                    GetProfile()
                }else{
                    launchActivity(LoginActivity::class.java)
                    finish()
                }
            }
        }, 3000);

    }



    private fun showPermissionDetailDialog(listPermissionsNeeded:ArrayList<String>) {
        val dialog = Dialog(this)
        dialog .requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog .setCancelable(false)
        dialog .setContentView(R.layout.dialog_permission_details)
        val yesBtn = dialog .findViewById(R.id.continuePermission) as TextView
        val noBtn = dialog .findViewById(R.id.notNow) as TextView

        yesBtn.setOnClickListener {
            dialog .dismiss()
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toTypedArray(), REQUEST_ID_MULTIPLE_PERMISSIONS)

        }
        noBtn.setOnClickListener {
            dialog .dismiss()
            GoInsideApp()
        }
        dialog .show()
    }


}
