package com.vendtech.app.ui.activity.transaction

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.StrictMode
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.downloader.Error
import com.downloader.OnDownloadListener
import com.downloader.PRDownloader
import com.google.gson.Gson
import com.vendtech.app.R
import com.vendtech.app.helper.SharedHelper
import com.vendtech.app.models.transaction.DepositTransactionDetails
import com.vendtech.app.models.transaction.DepositTransactionInvoiceModel
import com.vendtech.app.network.Uten
import com.vendtech.app.utils.Constants
import com.vendtech.app.utils.CustomDialog
import com.vendtech.app.utils.Utilities
import kotlinx.android.synthetic.main.activity_transaction_details.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.lang.Exception
import java.text.NumberFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class DepositTransactionDetails : Activity(){

    var transIDS=""
    var amountTrans=""
    var dateTransaction=""
    var timeTransaction=""
    var statusTransaction=""
    var vendorName=""
    var cheque_slip_no=""
    var commentTrans=""
    var payMode=""
    lateinit var downloadInvoicePDF: LinearLayout
    var TAG="DepositTransactionDetails"
    var depositId=0

    //Download Details
    var downloadID = 0
    internal var INVOICE_URL = ""
    private var type=""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction_details)
        var builder =  StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        downloadInvoicePDF=findViewById(R.id.downloadInvoice)


        depositId=intent.getIntExtra("depositId",0)
        type=intent.getStringExtra("type")!!

        if (type.equals("notification")){
            transctinDetailReprint.visibility=View.GONE
        }
        Log.v("DEPOSITID","Activity DepositId: "+depositId)


        GetDepositDetail()

    }


    fun SetData(){
            vendorIdLL.visibility = View.GONE
            vendorNameLL.visibility = View.GONE
            chequeslipLL.visibility = View.VISIBLE
            commentBoxLL.visibility = View.GONE
            paymodeLL.visibility = View.VISIBLE
            meternoLL.visibility= View.GONE

            Glide.with(this).load(R.drawable.wallet_icon).into(rechargeLogoIV)
            rechargeTypeTV.text = "Wallet Recharge"
            transID.text = transIDS
            amntTrans.text = "SSL : " + amountTrans; //${NumberFormat.getNumberInstance(Locale.US).format(amountTrans.toDouble().toInt())}"
            dateTrans.text = dateTransaction
            statusTrans.text = statusTransaction
            paymodeTrans.text = payMode
            vendornameTrans.text = vendorName
            checknoTrans.text = cheque_slip_no
            commentTV.text = commentTrans
            timeTrans.text=timeTransaction

            if(statusTransaction.equals("Pending")){
                statusTrans.setTextColor(ContextCompat.getColor(this, R.color.colorred))
            }else if (statusTransaction.equals("Rejected")){
                statusTrans.setTextColor(ContextCompat.getColor(this, R.color.colorred))
            }else if (statusTransaction.equals("Approved")){
                statusTrans.setTextColor(ContextCompat.getColor(this, R.color.colorgreen))
            }else {
                statusTrans.setTextColor(ContextCompat.getColor(this, R.color.colororange))
            }

        imgBack.setOnClickListener(View.OnClickListener {
            finish()
            overridePendingTransition(R.anim.activity_back_in, R.anim.activity_back_out)
        })


        downloadInvoicePDF.setOnClickListener(View.OnClickListener {
            if(checkAndRequestPermissions()){
                GetDepositDetailsPDF()
            }
        })

    }


    fun GetDepositDetailsPDF(){

        var customDialog:CustomDialog
        customDialog=CustomDialog(this)
        customDialog.show()

        val call: Call<DepositTransactionInvoiceModel> = Uten.FetchServerData().get_depositdetail_pdf(SharedHelper.getString(this,Constants.TOKEN),depositId.toString())
        call.enqueue(object : Callback<DepositTransactionInvoiceModel> {
            override fun onResponse(call: Call<DepositTransactionInvoiceModel>, response: Response<DepositTransactionInvoiceModel>) {

                if(customDialog.isShowing){
                    customDialog.dismiss()
                }
                var data=response.body()
                if(data!=null){
                    if(data.status.equals("true")){
                        if(!TextUtils.isEmpty(data.result.path)){
                            if(data.result.path.contains(".pdf")){
                                INVOICE_URL=data.result.path
                                PerformDownload()
                            }else {
                                Utilities.shortToast("Error while downloading the file",this@DepositTransactionDetails)
                            }
                        }else{
                            Utilities.shortToast("File not found",this@DepositTransactionDetails)
                        }
                    }else{
                        Utilities.CheckSessionValid(data.message,this@DepositTransactionDetails,this@DepositTransactionDetails)
                    }
                }
            }

            override fun onFailure(call: Call<DepositTransactionInvoiceModel>, t: Throwable) {
                val  gs = Gson()
                gs.toJson(t.localizedMessage)
                if(customDialog.isShowing){
                    customDialog.dismiss()
                }
            }

        })
    }



    fun PerformDownload(){

        var customDialog: CustomDialog
        customDialog= CustomDialog(this)
        customDialog.show()

        var filedirectorys = File(Environment.getExternalStorageDirectory(),"/VendTech/Invoice");
        if (!filedirectorys.exists()) {
            filedirectorys.mkdirs()
        }

        var filename = "VTD"+System.currentTimeMillis().toString()+".pdf"

        downloadID = PRDownloader.download(INVOICE_URL,filedirectorys.path, filename)
                .build()
                .setOnStartOrResumeListener {
                }
                .setOnPauseListener {
                }
                .setOnCancelListener {
                }
                .setOnProgressListener { progress ->
                }
                .start(object : OnDownloadListener {
                    override fun onDownloadComplete() {
                        customDialog.dismiss()
                        Utilities.shortToast("Download complete",this@DepositTransactionDetails)
                        var openFile=  File(Environment.getExternalStorageDirectory(),"/VendTech/Invoice/"+filename);
                        OpenPdfFile(openFile)
                    }

                    override fun onError(error: Error) {
                        customDialog.dismiss()
                        var g: Gson
                        g= Gson()
                        Log.v("DownloadError",g.toJson(error))
                        Utilities.shortToast("Downloading failed",this@DepositTransactionDetails)
                    }
                });

    }




    fun OpenPdfFile(file:File){

        var target =  Intent(Intent.ACTION_VIEW);
        target.setDataAndType(Uri.fromFile(file),"application/pdf");
        target.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

        var intent = Intent.createChooser(target, "Open File");
        try {
            startActivity(intent);
        } catch ( e : ActivityNotFoundException) {
            // Instruct the user to install a PDF reader here, or something
            Utilities.shortToast("Unable to found any PDF reader application. Please install any PDF reader",this)
        }

    }

    private fun checkAndRequestPermissions(): Boolean {
        val writepermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)


        val listPermissionsNeeded = ArrayList<String>()

        if (writepermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toTypedArray(), REQUEST_ID_MULTIPLE_PERMISSIONS)
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_ID_MULTIPLE_PERMISSIONS -> {

                val perms = HashMap<String, Int>()
                // Initialize the map with both permissions
                perms[Manifest.permission.WRITE_EXTERNAL_STORAGE] = PackageManager.PERMISSION_GRANTED
                // Fill with actual results from user
                if (grantResults.size > 0) {
                    for (i in permissions.indices)
                        perms[permissions[i]] = grantResults[i]
                    // Check for both permissions
                    if (perms[Manifest.permission.WRITE_EXTERNAL_STORAGE] == PackageManager.PERMISSION_GRANTED

                    ) {
                        // process the normal flow
                        GetDepositDetailsPDF()
                        //else any one or both the permissions are not granted
                    } else {
                        Log.d(TAG, "Some permissions are not granted ask again ")
                        //permission is denied (this is the first time, when "never ask again" is not checked) so ask again explaining the usage of permission
                        //                        // shouldShowRequestPermissionRationale will return true
                        //show the dialog or snackbar saying its necessary and try again otherwise proceed with setup.
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        ) {
                            showDialogOK("Service Permissions are required for this app",
                                    DialogInterface.OnClickListener { dialog, which ->
                                        when (which) {
                                            DialogInterface.BUTTON_POSITIVE -> checkAndRequestPermissions()
                                            DialogInterface.BUTTON_NEGATIVE ->
                                                dialog.dismiss()
                                            // proceed with logic by disabling the related features or quit the app.
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



    fun GetDepositDetail(){


        var customDialog:CustomDialog
        customDialog=CustomDialog(this)
        customDialog.show()

        val call: Call<DepositTransactionDetails> = Uten.FetchServerData().get_depositdetail(SharedHelper.getString(this, Constants.TOKEN),depositId.toString())
        call.enqueue(object : Callback<DepositTransactionDetails> {
            override fun onResponse(call: Call<DepositTransactionDetails>, response: Response<DepositTransactionDetails>) {

                if(customDialog.isShowing){
                    customDialog.dismiss()
                }
                val  g = Gson()
                g.toJson(response.body())

                var data=response.body()
                if(data!=null){


                    if(data.status.equals("true")){

                        if(data.result.transactionId==null || data.result.transactionId.equals("")){

                            transIDS="N/A"
                        }else{
                            transIDS=data.result.transactionId
                        }

                        amountTrans=data.result.amount
                        //dateTransaction=Utilities.changeDateFormat(this@DepositTransactionDetails,data.result.createdAt)
                        dateTransaction=data.result.createdAt;
                        timeTransaction=Utilities.changeTimeFormat(this@DepositTransactionDetails,data.result.createdAt)
                        try {
                            statusTransaction = data.result.status
                        }catch (exceptin:Exception){

                        }
                        vendorName=data.result.vendorName
                        cheque_slip_no=data.result.chkNoOrSlipId
                        commentTrans=data.result.comments?:""
                        payMode=data.result.type


                        SetData()

                    }else{
                        Utilities.CheckSessionValid(data.message,this@DepositTransactionDetails,this@DepositTransactionDetails)

                    }
                }

            }

            override fun onFailure(call: Call<DepositTransactionDetails>, t: Throwable) {
                val  gs = Gson()
                gs.toJson(t.localizedMessage)
                if(customDialog.isShowing){
                    customDialog.dismiss()
                }
            }

        })

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


}