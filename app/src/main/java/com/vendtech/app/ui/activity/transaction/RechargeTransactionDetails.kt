package com.vendtech.app.ui.activity.transaction

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
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
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.Window
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.downloader.*
import com.google.gson.Gson
import com.vendtech.app.R
import com.vendtech.app.helper.SharedHelper
import com.vendtech.app.models.airtime.AirtimeRechargeModel
import com.vendtech.app.models.meter.RechargeMeterModel
import com.vendtech.app.models.transaction.RechargeTransactionDetailResult
import com.vendtech.app.models.transaction.RechargeTransactionDetails
import com.vendtech.app.models.transaction.RechargeTransactionInvoiceModel
import com.vendtech.app.models.transaction.SendTransactionSmsModel
import com.vendtech.app.network.Uten
import com.vendtech.app.ui.Print.PrintScreenActivity
import com.vendtech.app.ui.alerts.AirtimePurchaseSuccess
import com.vendtech.app.utils.Constants
import com.vendtech.app.utils.CustomDialog
import com.vendtech.app.utils.Utilities
import kotlinx.android.synthetic.main.activity_transaction_details.*
import kotlinx.android.synthetic.main.activity_transaction_details.imgBack
import kotlinx.android.synthetic.main.activity_transaction_details.tv_email
import kotlinx.android.synthetic.main.activity_transaction_details.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.text.NumberFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class RechargeTransactionDetails : Activity(){

    var transIDS="";
    var amountTrans="";
    var dateTransaction="";
    var statusTransaction="";
    var meterNo="";
    lateinit var downloadInvoicePDF:LinearLayout;
    var TAG="RechargeTransactionDetails";
    var rechargeID="";
    var timeTransaction="";
    var meterId="";
    var posIds="";


    //Download DetailsL
    var downloadID = 0
    internal var INVOICE_URL = ""

    private var rechargePin="";
    private var type=""


    private var result:RechargeTransactionDetailResult?=null;
    private var rechargeTransactionDetailResult: AirtimeRechargeModel?=null;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction_details)
        var builder =  StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        downloadInvoicePDF=findViewById(R.id.downloadInvoice)
        rechargeID=intent.getIntExtra("rechargeId",0).toString()
        type=intent.getStringExtra("type")!!

        transctinDetailReprint.visibility=VISIBLE

        if (type.equals("notification")){
            transctinDetailReprint.visibility=View.GONE
        }
        Log.v("DEPOSITID","Activity rechargeId: "+rechargeID)

        GetRechargeDetail()

        imgBack.setOnClickListener(View.OnClickListener {
            finish();
            overridePendingTransition(R.anim.activity_back_in, R.anim.activity_back_out);
        });

        transctinDetailReprint.setOnClickListener{
            //startActivity(Intent(this@RechargeTransactionDetails,PrintScreenActivity::class.java))
           /* var intent=Intent(this,PrintScreenActivity::class.java);
              intent.putExtra(Constants.DATA,result);
              startActivity(intent)
            */

            if(result?.platformId == 2 || result?.platformId == 3 || result?.platformId == 4){
                result?.transactionId?.let { it1 -> getAirtimePrintData(it1) };
            }else{
                result?.rechargePin?.let { it1 -> getPrintData(it1) };
            }

        }

        tv_email.setOnClickListener {
            result?.let { it1 -> showEmaildialog(it1.rechargePin) };
        }

    }


    private fun showEmaildialog(pin: String) {
        val adDialog = Dialog(this@RechargeTransactionDetails, R.style.MyDialogThemeBlack);
        adDialog.window!!.requestFeature(Window.FEATURE_NO_TITLE);
        adDialog.setContentView(R.layout.sendemaildialog);
        adDialog.setCancelable(false);

        val tv_send_via_email = adDialog.findViewById<TextView>(R.id.tv_send_via_email);
        val tv_email_address = adDialog.findViewById<EditText>(R.id.tv_email_address);
        val img_close = adDialog.findViewById<AppCompatImageButton>(R.id.img_close);

        img_close.setOnClickListener {
            adDialog.dismiss();
        }


        tv_send_via_email.setOnClickListener {
            if(tv_email_address.text.isEmpty()) {
                Utilities.shortToast("Email is required",this@RechargeTransactionDetails)
            }
            else {
                val email = tv_send_via_email.text.toString()
                SendTransactionViaEmail(result!!.rechargePin, tv_email_address.text.toString())
                adDialog.cancel()
                adDialog.dismiss();
            }
        }
        adDialog.show();


    }

    fun  SendTransactionViaEmail(transactionId: String, email:String){

        var customDialog: CustomDialog
        customDialog= CustomDialog(this)
        customDialog.show()
        val call: Call<SendTransactionSmsModel> = Uten.FetchServerData().send_receipt_email(SharedHelper.getString(this,Constants.TOKEN),transactionId, email)
        call.enqueue(object : Callback<SendTransactionSmsModel> {
            override fun onResponse(call: Call<SendTransactionSmsModel>, response: Response<SendTransactionSmsModel>)  {

                if(customDialog.isShowing){
                    customDialog.dismiss()
                }
                var data=response.body()
                if(data!=null){
                    if(data.status.equals("true")){
                        Utilities.longToast("EMAIL SENT SUCCESSFULLY",this@RechargeTransactionDetails)
                    }else{
                        Utilities.shortToast("EMAIL FAILED TO SEND",this@RechargeTransactionDetails)
                    }
                }
            }

            override fun onFailure(call: Call<SendTransactionSmsModel>, t: Throwable) {
                val  gs = Gson()
                gs.toJson(t.localizedMessage)
                if(customDialog.isShowing){
                    customDialog.dismiss()
                }
            }
        })
    }


    fun getAirtimePrintData(token: String) {

        var customDialog: CustomDialog;
        customDialog = CustomDialog(this);
        customDialog.show();

        val call: Call<RechargeMeterModel> = Uten.FetchServerData().getAirtimeTransactionPintDetails(SharedHelper.getString(this, Constants.TOKEN),token);
        call.enqueue(object : Callback<RechargeMeterModel> {
            override fun onResponse(call: Call<RechargeMeterModel>, response: Response<RechargeMeterModel>) {
                if (customDialog.isShowing) {
                    customDialog.dismiss();
                }

                var data = response.body();
                if (data!=null){
                    if (data.message !=null) {
                        Utilities.shortToast(data.message,this@RechargeTransactionDetails);
                    }else {
                        if (data.status.equals("true")) {
                            var intent = Intent(this@RechargeTransactionDetails, AirtimePurchaseSuccess::class.java);
                            val resultData = response.body()!!.result;
//                            val receiptStatus = com.vendtech.app.models.airtime.ReceiptStatus(resultData?.receiptStatus?.status, resultData?.receiptStatus?.message)
                            var title = "";
                            if(result?.platformId == 2)
                                title = "ORANGE "
                            else if(result?.platformId == 3)
                                title = "AFRICELL "
                            else
                                title = "QCELL "

                            var result = com.vendtech.app.models.airtime.Result(
                                resultData.receiptNo,
                                resultData.accountNo,
                                resultData.amount,
                                resultData.accountNo,
                                resultData.pos,
                                resultData.serialNo,
                                resultData.transactionDate,
                                resultData.edsaSerial,
                                resultData.vtechSerial,
                                false,
                                false,
                                true,
                                null,
                                title,
                                false
                            );
                            val dto = AirtimeRechargeModel(response.body()!!.status, response.body()!!.message, result, "NLe");

                            intent.putExtra("data", dto);
                            startActivity(intent);
                        } else {
                            Utilities.CheckSessionValid(data.message, this@RechargeTransactionDetails!!, this@RechargeTransactionDetails!!);
                        }
                    }
                }
            }
            override fun onFailure(call: Call<RechargeMeterModel>, t: Throwable) {
                val gs = Gson()
                gs.toJson(t.localizedMessage)
                if (customDialog.isShowing) {
                    customDialog.dismiss()

                }
                Utilities.shortToast("Something went wrong", this@RechargeTransactionDetails)
            }
        })
    }


    fun getPrintData(token: String) {

        var customDialog: CustomDialog;
        customDialog = CustomDialog(this);
        customDialog.show();

        val call: Call<RechargeMeterModel> = Uten.FetchServerData().getTransactionPintDetails(SharedHelper.getString(this, Constants.TOKEN),token);
        call.enqueue(object : Callback<RechargeMeterModel> {
            override fun onResponse(call: Call<RechargeMeterModel>, response: Response<RechargeMeterModel>) {
                if (customDialog.isShowing) {
                    customDialog.dismiss();
                }

                var data = response.body();
                if (data!=null){
                    if (data.message !=null) {
                        Utilities.shortToast(data.message,this@RechargeTransactionDetails);
                    }else {
                        if (data.status.equals("true")) {
                            var intent = Intent(this@RechargeTransactionDetails, PrintScreenActivity::class.java);
                            intent.putExtra("data", response.body());
                            startActivity(intent);
                        } else {
                            Utilities.CheckSessionValid(data.message, this@RechargeTransactionDetails!!, this@RechargeTransactionDetails!!);
                        }
                    }
                }
            }
            override fun onFailure(call: Call<RechargeMeterModel>, t: Throwable) {
                val gs = Gson()
                gs.toJson(t.localizedMessage)
                if (customDialog.isShowing) {
                    customDialog.dismiss()

                }
                Utilities.shortToast("Something went wrong", this@RechargeTransactionDetails)
            }
        })
    }

    fun SetData(result: RechargeTransactionDetailResult) {
        this.result=result;

           // vendorIdLL.visibility = View.GONE
            //vendorNameLL.visibility = View.GONE
            chequeslipLL.visibility = GONE
            commentBoxLL.visibility = GONE
            paymodeLL.visibility = GONE
            meternoLL.visibility = VISIBLE


            Glide.with(this).load(R.drawable.light).into(rechargeLogoIV)

            transID.text = transIDS
            vendorIdTrans.text="${result.posId}"
            vendornameTrans.text=result.vendorName
            amntTrans.text= "SLL: ${NumberFormat.getNumberInstance(Locale.US).format(amountTrans.toDouble().toInt())}"

        tv_token_no.setText(result.rechargePin);

        if(result?.platformId == 2 || result.platformId == 3 || result.platformId == 4) {
            transctinDetailReprint.visibility = VISIBLE
            meterNoLabel.text = "Phone No.:"
            tv_email.visibility = GONE
            tokenLabel.visibility = GONE
            if(result?.platformId == 3){
                rechargeTypeTV.text = "Africell Airtime Purchase"
                Glide.with(this).load(R.drawable.africell).into(rechargeLogoIV)
            }
            else if(result?.platformId == 4){
                rechargeTypeTV.text = "QCELL Airtime Purchase"
                Glide.with(this).load(R.drawable.qcell).into(rechargeLogoIV)
            }
            else{
                rechargeTypeTV.text = "Orange Airtime Purchase"
                Glide.with(this).load(R.drawable.orange).into(rechargeLogoIV)
            }
        }else{
            tokenLabel.visibility = VISIBLE
            meterNoLabel.text = "Meter No.:"
            tv_email.visibility = VISIBLE
            rechargeTypeTV.text = "Electricity Recharge"
            Glide.with(this).load(R.drawable.light).into(rechargeLogoIV)
        }

        rechargePin = if(result.rechargePin == null) {
            result.meterNumber
        } else
            rechargePin.replace("\\s".toRegex(), "");


        //  amntTrans.text = amountTrans
            dateTrans.text = dateTransaction
            statusTrans.text = statusTransaction
            meternoTrans.text = meterNo
            timeTrans.text=timeTransaction

            if(statusTransaction.equals("Pending")){
                statusTrans.setTextColor(ContextCompat.getColor(this,R.color.colorred))
            }else if (statusTransaction.equals("Rejected")){
                statusTrans.setTextColor(ContextCompat.getColor(this,R.color.colorred))
            }else if (statusTransaction.equals("Success")){
                statusTrans.setTextColor(ContextCompat.getColor(this,R.color.colorgreen))
            }else {
                statusTrans.setTextColor(ContextCompat.getColor(this,R.color.colororange))
            }





        downloadInvoicePDF.setOnClickListener(View.OnClickListener {
            if(checkAndRequestPermissions()){
                GetRechargeDetailsPDF()
            }
        })

    }


    fun GetRechargeDetail(){

        var customDialog:CustomDialog;
        customDialog=CustomDialog(this);
        customDialog.show();

        val call: Call<RechargeTransactionDetails> = Uten.FetchServerData().get_rechargedetail(SharedHelper.getString(this, Constants.TOKEN),rechargeID)
        call.enqueue(object : Callback<RechargeTransactionDetails> {
            override fun onResponse(call: Call<RechargeTransactionDetails>, response: Response<RechargeTransactionDetails>) {

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
                       // dateTransaction=Utilities.changeDateFormat(/*this@RechargeTransactionDetails,*/data.result.createdAt)
                       // dateTransaction=data.result.createdAt;
                        dateTransaction=Utilities.changeDateFormatWithAmPm(this@RechargeTransactionDetails,data.result.createdAt)

                        timeTransaction=Utilities.changeTimeFormat(this@RechargeTransactionDetails,data.result.createdAt)
                        statusTransaction=data.result.status
                        meterNo=data.result.meterNumber;
                        if(data.result.meterId != null){
                            meterId=data.result.meterId
                        }
                        posIds=data.result.posId;


                        SetData(data.result);
                    }else{
                        Utilities.CheckSessionValid(data.message,this@RechargeTransactionDetails,this@RechargeTransactionDetails)
                    }
                }
            }

            override fun onFailure(call: Call<RechargeTransactionDetails>, t: Throwable) {
                val  gs = Gson()
                gs.toJson(t.localizedMessage)
                if(customDialog.isShowing){
                    customDialog.dismiss()
                }
            }
        })
    }

    fun GetRechargeDetailsPDF(){

        var customDialog:CustomDialog
        customDialog=CustomDialog(this)
        customDialog.show()

        val call: Call<RechargeTransactionInvoiceModel> = Uten.FetchServerData().get_rechargedetail_pdf(SharedHelper.getString(this,Constants.TOKEN),rechargeID)
        call.enqueue(object : Callback<RechargeTransactionInvoiceModel> {
            override fun onResponse(call: Call<RechargeTransactionInvoiceModel>, response: Response<RechargeTransactionInvoiceModel>) {

                if(customDialog.isShowing){
                    customDialog.dismiss()
                }
                val  g = Gson()
                g.toJson(response.body())

                var data=response.body()
                if(data!=null){


                    if(data.status.equals("true")){

                        if(!TextUtils.isEmpty(data.result.path)){

                            if(data.result.path.contains(".pdf")){

                                INVOICE_URL=data.result.path
                                PerformDownload()
                            }else {
                                Utilities.shortToast("Error while downloading the file",this@RechargeTransactionDetails)
                            }
                        }else{
                            Utilities.shortToast("File not found",this@RechargeTransactionDetails)
                        }
                    }else{
                        Utilities.CheckSessionValid(data.message,this@RechargeTransactionDetails,this@RechargeTransactionDetails)
                    }
                }
            }

            override fun onFailure(call: Call<RechargeTransactionInvoiceModel>, t: Throwable) {
                val  gs = Gson()
                gs.toJson(t.localizedMessage)
                if(customDialog.isShowing){
                    customDialog.dismiss()
                }
            }
        })
    }


    fun PerformDownload(){

        var customDialog:CustomDialog
        customDialog=CustomDialog(this)
        customDialog.show()

        var filedirectorys = File(Environment.getExternalStorageDirectory(),"/VendTech/Invoice");

        if (!filedirectorys.exists()) {
            filedirectorys.mkdirs()
        }

        var filename = "VTR"+System.currentTimeMillis().toString()+".pdf"
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
                        Utilities.shortToast("Download complete",this@RechargeTransactionDetails)
                        var openFile=  File(Environment.getExternalStorageDirectory(),"/VendTech/Invoice/"+filename);
                        OpenPdfFile(openFile)
                    }

                    override fun onError(error: Error) {
                        customDialog.dismiss()
                        var g:Gson
                        g= Gson()
                        Log.v("DownloadError",g.toJson(error))
                        Utilities.shortToast("Downloading failed",this@RechargeTransactionDetails)
                    }
                })
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
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
                        //  GoInsideApp()
                        //PerformDownload()
                        GetRechargeDetailsPDF()
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
}