package com.vendtech.app.ui.activity.profile

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.app.AlertDialog
import android.content.ContentUris
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.gson.Gson

import com.vendtech.app.R
import com.vendtech.app.helper.SharedHelper
import com.vendtech.app.models.authentications.*
import com.vendtech.app.models.profile.GetProfileModel
import com.vendtech.app.models.profile.ResultProfile
import com.vendtech.app.models.profile.UpdateProfileModel
import com.vendtech.app.network.Uten
import com.vendtech.app.ui.activity.authentication.LoginActivity
import com.vendtech.app.ui.activity.home.HomeActivity
import com.vendtech.app.utils.Constants
import com.vendtech.app.utils.CustomDialog
import com.vendtech.app.utils.PathUtil
import com.vendtech.app.utils.Utilities
import kotlinx.android.synthetic.main.activity_edit_profile.*
import kotlinx.android.synthetic.main.layout_error.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class EditProfileActivity : Activity(), View.OnClickListener {


    lateinit var back: ImageView
    lateinit var updateProfileTV: TextView
    lateinit var deleteProfileTV: TextView
    lateinit var pickImageRL: RelativeLayout
    var TAG = "EditProfileActivty"
    lateinit var customDialog: CustomDialog

    var CITY_ID = ""
    var COUNTRY_ID = ""
    var filePathImage = ""


    private val FINAL_TAKE_PHOTO = 1
    private val FINAL_CHOOSE_PHOTO = 2
    private var imageUri: Uri? = null
    val IMAGE_DIRECTORY = "/VendTech/Profile"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)
        customDialog = CustomDialog(this)
        customDialog.show()
        initViews()
        GetProfile()

    }


    fun initViews() {

        back = findViewById<View>(R.id.imgBack) as ImageView
        updateProfileTV = findViewById<View>(R.id.updateProfileTV) as TextView
        deleteProfileTV = findViewById<View>(R.id.deleteProfileTV) as TextView
        pickImageRL = findViewById<View>(R.id.pickImageRL) as RelativeLayout
        back.setOnClickListener(this)
        pickImageRL.setOnClickListener(this)
        updateProfileTV.setOnClickListener(this)
        deleteProfileTV.setOnClickListener(this)


        backPress.setOnClickListener(View.OnClickListener {
            finish()
            overridePendingTransition(R.anim.activity_back_in, R.anim.activity_back_out)
        })

        retry.setOnClickListener(View.OnClickListener {

            error_layout.visibility = View.GONE
            mainlayout.visibility = View.VISIBLE

            if (TextUtils.isEmpty(COUNTRY_ID)) {
                getCountries()
            } else {
                getCities(COUNTRY_ID)
            }
        })

        selectCountry.setOnClickListener(View.OnClickListener {
            countrySpinner.performClick()
        })


        selectCity.setOnClickListener(View.OnClickListener {
            citySpinner.performClick()
        })


    }

    override fun onClick(v: View) {

        when (v.id) {

            R.id.imgBack -> {
                finish()
                overridePendingTransition(R.anim.activity_back_in, R.anim.activity_back_out)
            }

            R.id.updateProfileTV -> {
                if (TextUtils.isEmpty(fnameET.text.toString().trim())) {
                    Utilities.shortToast("Enter first name.", this)
                } else if (TextUtils.isEmpty(lnameET.text.toString().trim())) {
                    Utilities.shortToast("Enter last name", this)
                } else if (TextUtils.isEmpty(phoneET.text.toString().trim())) {
                    Utilities.shortToast("Enter phone number.", this)
                } else if (phoneET.text.toString().trim().length != 8) {
                    Utilities.shortToast("Enter a valid phone number.", this)
                } else if (TextUtils.isEmpty(addressET.text.toString().trim())) {
                    Utilities.shortToast("Enter address.", this)
                } else if (addressET.text.toString().trim().length < 7) {
                    Utilities.shortToast(resources.getString(R.string.address_length), this)
                } else {
                    if (Uten.isInternetAvailable(this)) {
                        UpdateProfile()
                    } else {
                        Utilities.shortToast("No internet connection. Please check your network connectivity", this)
                    }
                }
            }

            R.id.deleteProfileTV -> {
                ShowAlertForDelete(emailET.text.toString())
            }

            R.id.pickImageRL -> {
                if (checkAndRequestPermissions()) {
                    SelectImageUploadOption()
                }
            }
        }
    }

    fun ShowAlertForDelete(emailET: String) {

        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.app_name)
        builder.setMessage("We're sorry to see you go! If you wish to delete your account information from VendTechSL, please click on confirm")
        builder.setIcon(R.drawable.appicon)
        builder.setPositiveButton("Confirm") { dialogInterface, which ->


            var customDialog: CustomDialog
            customDialog = CustomDialog(this)
            customDialog.show()


//            val emailVal = RequestBody.create(MediaType.parse("text/plain"), emailET.text.toString().trim())

            val call: Call<DeleteProfileModel> = Uten.FetchServerData().delete_user(emailET)
            call.enqueue(object : Callback<DeleteProfileModel> {

                override fun onResponse(call: Call<DeleteProfileModel>, response: Response<DeleteProfileModel>) {
                    customDialog.dismiss()
                    var data = response.body()

                    if (data != null) {
                        Utilities.shortToast(data.message, this@EditProfileActivity)
                        if (data.status.equals("true")) {


                            SharedHelper.removeUserData(this@EditProfileActivity)
                            SharedHelper.putBoolean(this@EditProfileActivity, Constants.IS_LOGGEDIN, false)
                            val i = Intent(this@EditProfileActivity, LoginActivity::class.java)
                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            startActivity(i)
                            var vv=SharedHelper.getString(this@EditProfileActivity, Constants.POS_NUMBER)
                            finish()


                        } else {

                            Utilities.CheckSessionValid(data.message, this@EditProfileActivity, this@EditProfileActivity)

                        }
                    }
                }

                override fun onFailure(call: Call<DeleteProfileModel>, t: Throwable) {
                    customDialog.dismiss()
                    Utilities.shortToast("Something went wrong!", this@EditProfileActivity)
                }
            })





        }
        builder.setNegativeButton("Cancel") { dialogInterface, which ->
        }
        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()

    }


    fun UpdateProfile() {

        var customDialog: CustomDialog
        customDialog = CustomDialog(this)
        customDialog.show()

        val fname = RequestBody.create(MediaType.parse("text/plain"), fnameET.text.toString().trim())
        val lname = RequestBody.create(MediaType.parse("text/plain"), lnameET.text.toString().trim())
        val phone = RequestBody.create(MediaType.parse("text/plain"), phoneET.text.toString().trim())
        val city = RequestBody.create(MediaType.parse("text/plain"), CITY_ID)
        val country = RequestBody.create(MediaType.parse("text/plain"), COUNTRY_ID)
        val address = RequestBody.create(MediaType.parse("text/plain"), addressET.text.toString().trim())
        //val passCode = RequestBody.create(MediaType.parse("text/plain"), SharedHelper.getString(this, Constants.PASS_CODE_VALUE))

        val call: Call<UpdateProfileModel> = Uten.FetchServerData().update_profile(SharedHelper.getString(this, Constants.TOKEN), fname, lname, phone, city, country, address, getImageAsPart(filePathImage))
        call.enqueue(object : Callback<UpdateProfileModel> {

            override fun onResponse(call: Call<UpdateProfileModel>, response: Response<UpdateProfileModel>) {
                customDialog.dismiss()
                var data = response.body()

                if (data != null) {
                    Utilities.shortToast(data.message, this@EditProfileActivity)
                    if (data.status.equals("true")) {

                        SharedHelper.putBoolean(this@EditProfileActivity, Constants.IS_LOGGEDIN, true)
                        SharedHelper.putString(this@EditProfileActivity, Constants.USER_FNAME, data.result.user.name)
                        SharedHelper.putString(this@EditProfileActivity, Constants.USER_LNAME, data.result.user.surName)
                        SharedHelper.putString(this@EditProfileActivity, Constants.USER_ADDRESS, data.result.user.address)
                        SharedHelper.putString(this@EditProfileActivity, Constants.USER_PHONE, data.result.user.phone)
                        SharedHelper.putString(this@EditProfileActivity, Constants.USER_CITY, data.result.user.city)
                        SharedHelper.putString(this@EditProfileActivity, Constants.USER_COUNTRY, data.result.user.country)
                        SharedHelper.putString(this@EditProfileActivity, Constants.USER_AVATAR, data.result.user.profilePic)

                        GotoHome()
                    } else {

                        Utilities.CheckSessionValid(data.message, this@EditProfileActivity, this@EditProfileActivity)

                    }
                }
            }

            override fun onFailure(call: Call<UpdateProfileModel>, t: Throwable) {
                customDialog.dismiss()
                Utilities.shortToast("Something went wrong!", this@EditProfileActivity)
            }
        })
    }


    fun SetDataOnFields(profile: ResultProfile) {

        fnameET.setText(SharedHelper.getString(this, Constants.USER_FNAME))
        lnameET.setText(SharedHelper.getString(this, Constants.USER_LNAME))
        usernameET.setText(SharedHelper.getString(this, Constants.USERNAME))
        emailET.setText(SharedHelper.getString(this, Constants.USER_EMAIL))
        phoneET.setText(SharedHelper.getString(this, Constants.USER_PHONE))
        addressET.setText(SharedHelper.getString(this, Constants.USER_ADDRESS))
        Glide.with(this).load(SharedHelper.getString(this, Constants.USER_AVATAR)).asBitmap().error(R.drawable.dummyuser).placeholder(R.drawable.dummyuser).into(userPicCIV)

    }


    private fun checkAndRequestPermissions(): Boolean {
        val camerapermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        val writepermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        val permissionRecordAudio = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
        val listPermissionsNeeded = ArrayList<String>()

        if (camerapermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CAMERA)
        }
        if (writepermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if (permissionRecordAudio != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toTypedArray(), REQUEST_ID_MULTIPLE_PERMISSIONS)
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
                perms[Manifest.permission.CAMERA] = PackageManager.PERMISSION_GRANTED
                perms[Manifest.permission.WRITE_EXTERNAL_STORAGE] = PackageManager.PERMISSION_GRANTED
                perms[Manifest.permission.READ_EXTERNAL_STORAGE] = PackageManager.PERMISSION_GRANTED
                // Fill with actual results from user
                if (grantResults.size > 0) {
                    for (i in permissions.indices)
                        perms[permissions[i]] = grantResults[i]
                    // Check for both permissions
                    if (perms[Manifest.permission.CAMERA] == PackageManager.PERMISSION_GRANTED
                            && perms[Manifest.permission.WRITE_EXTERNAL_STORAGE] == PackageManager.PERMISSION_GRANTED
                            && perms[Manifest.permission.READ_EXTERNAL_STORAGE] == PackageManager.PERMISSION_GRANTED
                    ) {
                        Log.d(TAG, "sms & location services permission granted")
                        // process the normal flow
                        //  GoInsideApp()
                        SelectImageUploadOption()
                        //else any one or both the permissions are not granted
                    } else {
                        Log.d(TAG, "Some permissions are not granted ask again ")
                        //permission is denied (this is the first time, when "never ask again" is not checked) so ask again explaining the usage of permission
                        //                        // shouldShowRequestPermissionRationale will return true
                        //show the dialog or snackbar saying its necessary and try again otherwise proceed with setup.
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)
                                || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)
                                || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)) {
                            showDialogOK("Service Permissions are required for this app",
                                    DialogInterface.OnClickListener { dialog, which ->
                                        when (which) {
                                            DialogInterface.BUTTON_POSITIVE -> checkAndRequestPermissions()
                                            DialogInterface.BUTTON_NEGATIVE ->
                                                dialog.dismiss()
                                            // proceed with logic by disabling the related features or quit the app.
                                            //finish()
                                            //  GoInsideApp()
                                            //  PickImage()
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


    fun GotoHome() {

        val intent = Intent(this@EditProfileActivity, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
        overridePendingTransition(R.anim.activity_back_in, R.anim.activity_back_out)


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            FINAL_TAKE_PHOTO ->
                if (resultCode == Activity.RESULT_OK) {
                    val thumbnail = data!!.extras!!.get("data") as Bitmap
                    saveImage(thumbnail)
                    // filePathImage=PathUtil.getPath(this,imageUri)
                }
            FINAL_CHOOSE_PHOTO ->
                if (resultCode == Activity.RESULT_OK) {
                    if (Build.VERSION.SDK_INT >= 19) {
//                        4.4以上
                        handleImageOnKitkat(data)
                    } else {
//                        4.4以下
                        handleImageBeforeKitkat(data)
                    }
                }
        }

    }


    @TargetApi(19)
    private fun handleImageOnKitkat(data: Intent?) {
        var imagePath: String? = null
        val uri = data!!.data
        if (DocumentsContract.isDocumentUri(this, uri)) {
//            document类型的Uri，用document id处理
            val docId = DocumentsContract.getDocumentId(uri)
            if ("com.android.providers.media.documents" == uri?.authority) {
                val id = docId.split(":")[1]
                val selsetion = MediaStore.Images.Media._ID + "=" + id
                imagePath = imagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selsetion)
            } else if ("com.android.providers.downloads.documents" == uri?.authority) {
                val contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), java.lang.Long.valueOf(docId))
                imagePath = imagePath(contentUri, null)
            }
        } else if ("content".equals(uri?.scheme, ignoreCase = true)) {
//            content类型Uri 普通方式处理
            imagePath = imagePath(uri, null)
        } else if ("file".equals(uri?.scheme, ignoreCase = true)) {
            imagePath = uri?.path
        }
        displayImage(imagePath)
    }

    //    No 4.4 devices, skip
    private fun handleImageBeforeKitkat(data: Intent?) {
        var imagePath: String? = null;
        val uri = data!!.data;

        imagePath = PathUtil.getPath(this, uri);
        displayImage(imagePath);
    }

    private fun imagePath(uri: Uri?, selection: String?): String {
        var path: String? = null
//        通过Uri和selection获取路径
        val cursor = contentResolver.query(uri!!, null, selection, null, null)
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA))
            }
            cursor.close()
        }
        return path!!
    }

    private fun displayImage(imagePath: String?) {
        if (imagePath != null) {


            filePathImage = imagePath
            val bitmap = BitmapFactory.decodeFile(imagePath)
            //picture?.setImageBitmap(bitmap)
            Glide.with(this).load(filePathImage).asBitmap().error(R.drawable.dummyuser).placeholder(R.drawable.dummyuser).into(userPicCIV)
        } else {
            Toast.makeText(this, "Failed to get image", Toast.LENGTH_SHORT).show()
        }
    }

    fun getCountries() {


        val call: Call<GetCountriesModel> = Uten.FetchServerData().get_countries()

        call.enqueue(object : Callback<GetCountriesModel> {
            override fun onResponse(call: Call<GetCountriesModel>, response: Response<GetCountriesModel>) {

                var dataCountry = response.body()

                if (dataCountry != null) {

                    if (dataCountry.status.equals("true")) {
                        setCountries(dataCountry.result)
                    } else {
                        Utilities.CheckSessionValid(dataCountry.message, this@EditProfileActivity, this@EditProfileActivity)
                    }
                }
            }

            override fun onFailure(call: Call<GetCountriesModel>, t: Throwable) {
                val gs = Gson()
                gs.toJson(t.localizedMessage)
                if (customDialog.isShowing) {

                    customDialog.dismiss()
                }

                error_layout.visibility = View.VISIBLE
                mainlayout.visibility = View.GONE

            }

        })
    }


    fun getCities(countryId: String) {

        val call: Call<GetCitiesModel> = Uten.FetchServerData().get_cities(countryId)

        call.enqueue(object : Callback<GetCitiesModel> {
            override fun onResponse(call: Call<GetCitiesModel>, response: Response<GetCitiesModel>) {

                customDialog.dismiss()

                var data = response.body()

                if (data != null) {

                    if (data.status.equals("true")) {
                        setCities(data.result)
                    } else {
                        Utilities.CheckSessionValid(data.message, this@EditProfileActivity, this@EditProfileActivity)
                    }
                }
            }

            override fun onFailure(call: Call<GetCitiesModel>, t: Throwable) {

                customDialog.dismiss()

                error_layout.visibility = View.VISIBLE
                mainlayout.visibility = View.GONE

            }
        })
    }

    fun setCountries(data: List<ResultCountries>) {

        val list: MutableList<String> = ArrayList()
        var indexItem = 0

        for (i in 0..data.size - 1) {
            list.add(data.get(i).name)
        }

        if (list.size > 0) {

            for (i in 0..list.size - 1) {

                if (list.get(i).equals(SharedHelper.getString(this, Constants.USER_COUNTRY))) {
                    indexItem = i
                }
            }
        }

        val countryAdapter = ArrayAdapter<CharSequence>(this, R.layout.spinner_text_second, list as List<CharSequence>)
        countryAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_second)
        countrySpinner.setAdapter(countryAdapter)
        countrySpinner.setSelection(indexItem)

        countrySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                try {
                    getCities(data[position].countryId)
                    COUNTRY_ID = data[position].countryId
                } catch (e: Exception) {
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
    }


    fun setCities(data: List<ResultCities>) {

        val list: MutableList<String> = ArrayList()
        var indexItem = 0

        for (i in 0..data.size - 1) {
            list.add(data.get(i).name)
        }

        if (list.size > 0) {

            for (i in 0..list.size - 1) {

                if (list.get(i).equals(SharedHelper.getString(this, Constants.USER_CITY))) {
                    indexItem = i
                }
            }
        }

        val cityAdapter = ArrayAdapter<CharSequence>(this, R.layout.spinner_text_second, list as List<CharSequence>)
        cityAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_second)
        citySpinner.setAdapter(cityAdapter)
        citySpinner.setSelection(indexItem)


        citySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // Toast.makeText(this@SignUpActivity, "City ID: " + data[position].cityId, Toast.LENGTH_SHORT).show()
                try {
                    CITY_ID = data[position].cityId
                } catch (e: Exception) {
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
    }


    private fun getImageAsPart(real_image: String): MultipartBody.Part? {
        val MEDIA_TYPE_PNG = MediaType.parse("image/jpeg")
        if (TextUtils.isEmpty(real_image)) {
            return null
        }
        val file = File(real_image)
        Log.v("image_file", file.path)
        return MultipartBody.Part.createFormData("Image", file.name, RequestBody.create(MEDIA_TYPE_PNG, file))
    }


    fun SelectImageUploadOption() {
        // setup the alert builder
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Choose an option")
        // add a list
        val animals = arrayOf("Camera", "Gallery")
        builder.setItems(animals) { dialog, which ->
            when (which) {
                0 -> {
                    CaptureImage()
                }
                1 -> {
                    OpenGallery()
                }
            }
        }
        // create and show the alert dialog
        val dialog = builder.create()
        dialog.show()
    }


    fun CaptureImage() {

        val intent = Intent("android.media.action.IMAGE_CAPTURE")
        startActivityForResult(intent, FINAL_TAKE_PHOTO)
    }


    fun OpenGallery() {

        val intent = Intent("android.intent.action.GET_CONTENT")
        intent.type = "image/*"
        startActivityForResult(intent, FINAL_CHOOSE_PHOTO)
    }


    fun saveImage(myBitmap: Bitmap): String {
        val bytes = ByteArrayOutputStream()
        myBitmap.compress(Bitmap.CompressFormat.JPEG, 90, bytes)
        val wallpaperDirectory = File(
                (Environment.getExternalStorageDirectory()).toString() + IMAGE_DIRECTORY)
        // have the object build the directory structure, if needed.
        Log.d("fee", wallpaperDirectory.toString())
        if (!wallpaperDirectory.exists()) {
            wallpaperDirectory.mkdirs()
        }
        try {
            Log.d("heel", wallpaperDirectory.toString())
            val f = File(wallpaperDirectory, ((Calendar.getInstance()
                    .getTimeInMillis()).toString() + ".jpg"))
            f.createNewFile()
            val fo = FileOutputStream(f)
            fo.write(bytes.toByteArray())
            MediaScannerConnection.scanFile(this,
                    arrayOf(f.getPath()),
                    arrayOf("image/jpeg"), null)
            fo.close()
            Log.d("TAG", "File Saved::--->" + f.getAbsolutePath())
            filePathImage = f.absolutePath
            Glide.with(this).load(filePathImage).asBitmap().error(R.drawable.dummyuser).placeholder(R.drawable.dummyuser).into(userPicCIV)

            return f.getAbsolutePath()
        } catch (e1: IOException) {
            e1.printStackTrace()
        }

        return ""
    }


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
                    //Utilities.shortToast(data.message,this@EditProfileActivity)
                    if (data.status.equals("true")) {

                        try {
                            SharedHelper.putBoolean(this@EditProfileActivity, Constants.IS_LOGGEDIN, true)
                            SharedHelper.putString(this@EditProfileActivity, Constants.USER_FNAME, data.result.name)
                            SharedHelper.putString(this@EditProfileActivity, Constants.USER_LNAME, data.result.surName)
                            SharedHelper.putString(this@EditProfileActivity, Constants.USER_ID, data.result.userId)
                            SharedHelper.putString(this@EditProfileActivity, Constants.USER_EMAIL, data.result.email)
                            SharedHelper.putString(this@EditProfileActivity, Constants.USER_PHONE, data.result.phone)
                            SharedHelper.putString(this@EditProfileActivity, Constants.USER_CITY, data.result.city)
                            SharedHelper.putString(this@EditProfileActivity, Constants.USER_COUNTRY, data.result.country)
                            SharedHelper.putString(this@EditProfileActivity, Constants.USER_AVATAR, data.result.profilePic)
                            SharedHelper.putString(this@EditProfileActivity, Constants.USERNAME, data.result.userName)
                            SharedHelper.putString(this@EditProfileActivity, Constants.USER_ADDRESS, data.result.address)

                            SetDataOnFields(data.result)
                            getCountries()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    } else {
                        Utilities.CheckSessionValid(data.message, this@EditProfileActivity, this@EditProfileActivity)
                    }
                } else {
                    Utilities.shortToast("Something went wrong!", this@EditProfileActivity)
                }
            }

            override fun onFailure(call: Call<GetProfileModel>, t: Throwable) {
                customDialog.dismiss()
                Utilities.shortToast("Something went wrong!", this@EditProfileActivity)
            }
        })
    }

}
