package com.vendtech.app.ui.alerts


import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.app.ProgressDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.BatteryManager
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.View
import android.view.Window
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.app.ActivityCompat
import com.google.gson.Gson
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.telpo.tps550.api.printer.UsbThermalPrinter
import com.telpo.tps550.api.util.StringUtil
import com.telpo.tps550.api.util.SystemUtil
import com.vendtech.app.R
import com.vendtech.app.helper.SharedHelper
import com.vendtech.app.models.airtime.AirtimeRechargeModel
import com.vendtech.app.models.transaction.SendTransactionSmsModel
import com.vendtech.app.network.Uten
import com.vendtech.app.ui.fragment.BillPaymentActivity
import com.vendtech.app.utils.Constants
import com.vendtech.app.utils.CustomDialog
import com.vendtech.app.utils.Utilities
import kotlinx.android.synthetic.main.activity_forgot_password.*
import kotlinx.android.synthetic.main.airtime_success_layout.*
import kotlinx.android.synthetic.main.airtime_success_layout.imgBack
import kotlinx.android.synthetic.main.airtime_success_layout.img_close2
import kotlinx.android.synthetic.main.airtime_success_layout.tv_account
import kotlinx.android.synthetic.main.airtime_success_layout.tv_custInfo
import kotlinx.android.synthetic.main.airtime_success_layout.tv_date
import kotlinx.android.synthetic.main.airtime_success_layout.tv_date_txt
import kotlinx.android.synthetic.main.airtime_success_layout.tv_phone_no
import kotlinx.android.synthetic.main.airtime_success_layout.tv_pos_id
import kotlinx.android.synthetic.main.airtime_success_layout.tv_pos_id_txt
import kotlinx.android.synthetic.main.airtime_success_layout.tv_print
import kotlinx.android.synthetic.main.airtime_success_layout.tv_sms
import kotlinx.android.synthetic.main.airtime_success_layout.tv_transaction_id
import kotlinx.android.synthetic.main.airtime_success_layout.tv_vendor_txt
import kotlinx.android.synthetic.main.airtime_success_layout.tv_vendtech_name
import kotlinx.android.synthetic.main.airtime_success_layout.tv_web_text
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.Charset
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*

class AirtimePurchaseSuccess : AppCompatActivity() {

    //BLUETOOTH PRINTER
    lateinit var myLabel: TextView
    lateinit var myTextbox: EditText
    lateinit var mBluetoothAdapter: BluetoothAdapter
    lateinit var mmSocket: BluetoothSocket
    lateinit var mmDevice: BluetoothDevice
    lateinit var openButton: TextView
    lateinit var sendButton: TextView
    lateinit var closeButton: TextView
    lateinit var mmOutputStream: OutputStream
    lateinit var mmInputStream: InputStream
    lateinit var workerThread: Thread
    lateinit var chalset: Charset
    lateinit var readBuffer: ByteArray
    var readBufferPosition = 0
    @Volatile
    var stopWorker = false
    //BLUETOOTH PRINTER

    /*
     print
     */

    var printVersion: String? = null
    private val NOPAPER = 3
    private val LOWBATTERY = 4
    private val PRINTVERSION = 5
    private val PRINTBARCODE = 6
    private val PRINTQRCODE = 7
    private val PRINTPAPERWALK = 8
    private val PRINTCONTENT = 9
    private val CANCELPROMPT = 10
    private val PRINTERR = 11
    private val OVERHEAT = 12
    private val MAKER = 13
    private val PRINTPICTURE = 14
    private val NOBLACKBLOCK = 15

    // public var handler:MyHandler? = null

    private val size = 660;
    private val size_width = 600;
    private val size_height = 258;

    private var Result: String? = null
    private var nopaper = false
    private var LowBattery = false

    var barcodeStr: String? = null
    var qrcodeStr: String? = null
    var paperWalk = 10
    var printContent: String? = null
    private val leftDistance = 2
    private val lineDistance = 1
    private val wordFont = 0
    private var printGray = 7
    private var progressDialog: ProgressDialog? = null
    private val MAX_LEFT_DISTANCE = 255
    var mUsbThermalPrinter = UsbThermalPrinter(this)

    var handler:MyHandler?=null;

    var rechargePhoneModel: AirtimeRechargeModel?=null;

    private var rechargeTransactionDetailResult: AirtimeRechargeModel?=null;

    private var dialog: ProgressDialog? = null;

    private fun noPaperDlg() {
        val dlg = AlertDialog.Builder(this@AirtimePurchaseSuccess)
        dlg.setTitle(getString(R.string.noPaper))
        dlg.setMessage(getString(R.string.noPaperNotice))
        dlg.setCancelable(false)
        dlg.setPositiveButton(R.string.sure) { dialogInterface, i -> }
        dlg.show()
    }

    inner class MyHandler : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                NOPAPER -> noPaperDlg()
                LOWBATTERY -> {
                    val alertDialog = AlertDialog.Builder(this@AirtimePurchaseSuccess)
                    alertDialog.setTitle(R.string.operation_result)
                    alertDialog.setMessage(getString(R.string.LowBattery))
                    alertDialog.setPositiveButton(getString(R.string.dialog_comfirm), DialogInterface.OnClickListener { dialogInterface, i -> })
                    alertDialog.show()
                }
                NOBLACKBLOCK -> Toast.makeText(this@AirtimePurchaseSuccess, R.string.maker_not_find, Toast.LENGTH_SHORT).show()
                PRINTVERSION -> {
                    dialog!!.dismiss()
                    if (msg.obj == "1") {
                        // textPrintVersion.setText(printVersion)
                    } else {
                        Toast.makeText(this@AirtimePurchaseSuccess, R.string.operation_fail, Toast.LENGTH_LONG).show()
                    }
                }
                PRINTBARCODE -> barcodePrintThread().start()
                PRINTQRCODE -> qrcodePrintThread().start()
                PRINTPAPERWALK -> paperWalkPrintThread().start()
                PRINTCONTENT -> contentPrintThread().start()
                CANCELPROMPT -> if (progressDialog != null && !this@AirtimePurchaseSuccess.isFinishing()) {
                    progressDialog!!.dismiss()
                    progressDialog = null
                }
                OVERHEAT -> {
                    val overHeatDialog = AlertDialog.Builder(this@AirtimePurchaseSuccess)
                    overHeatDialog.setTitle(R.string.operation_result)
                    overHeatDialog.setMessage(getString(R.string.overTemp))
                    overHeatDialog.setPositiveButton(getString(R.string.dialog_comfirm), DialogInterface.OnClickListener { dialogInterface, i -> })
                    overHeatDialog.show()
                }
                else -> {
                    Toast.makeText(this@AirtimePurchaseSuccess, "Print Error!", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private inner class contentPrintThread : Thread() {
        override fun run() {
            super.run()
            try {
                mUsbThermalPrinter.reset();

                /*mUsbThermalPrinter.setAlgin(UsbThermalPrinter.ALGIN_LEFT)
                mUsbThermalPrinter.setLeftIndent(leftDistance)
                mUsbThermalPrinter.setLineSpace(lineDistance)
                if (wordFont == 4) {
                    mUsbThermalPrinter.setTextSize(40)
                } else if (wordFont == 3) {
                    mUsbThermalPrinter.setTextSize(30)
                } else if (wordFont == 2) {
                    mUsbThermalPrinter.setTextSize(20)
                } else if (wordFont == 1) {
                    mUsbThermalPrinter.setTextSize(10)
                }
                //mUsbThermalPrinter.setHighlight(true);
                mUsbThermalPrinter.setGray(printGray)
                mUsbThermalPrinter.addString(printContent)
                mUsbThermalPrinter.printString()
                mUsbThermalPrinter.walkPaper(50)*/


               // mUsbThermalPrinter.setFontSize(4);

                mUsbThermalPrinter.setBold(true);
                mUsbThermalPrinter.setAlgin(UsbThermalPrinter.ALGIN_MIDDLE);
                mUsbThermalPrinter.setLeftIndent(leftDistance);
                mUsbThermalPrinter.setLineSpace(lineDistance);
                mUsbThermalPrinter.setTextSize(40);

                //mUsbThermalPrinter.setFontSize(2);
                //mUsbThermalPrinter.enlargeFontSize(2, 2);

                mUsbThermalPrinter.setGray(printGray);
                mUsbThermalPrinter.addString("VENDTECH");
                mUsbThermalPrinter.setTextSize(25);
                mUsbThermalPrinter.setLineSpace(2);
                mUsbThermalPrinter.addString("EDSA Electricity Purchase");
                mUsbThermalPrinter.addString("--------------------");
                mUsbThermalPrinter.setTextSize(20);
                mUsbThermalPrinter.setLineSpace(lineDistance);
                mUsbThermalPrinter.setAlgin(UsbThermalPrinter.ALGIN_LEFT);
                mUsbThermalPrinter.setBold(false);

                mUsbThermalPrinter.addString("${"DATE:" + "                   " + tv_date.text.toString()}");
                mUsbThermalPrinter.addString("${"POS ID:"+"                          " + tv_pos_id.text.toString()}\n");
                //mUsbThermalPrinter.setAlgin(UsbThermalPrinter.ALGIN_MIDDLE);
                //mUsbThermalPrinter.setBold(true);
                mUsbThermalPrinter.setLineSpace(2);
                mUsbThermalPrinter.addString("--- ${tv_custInfo.text.toString() + " ---"}\n");
                mUsbThermalPrinter.setBold(false);
                //mUsbThermalPrinter.setAlgin(UsbThermalPrinter.ALGIN_RIGHT);
                mUsbThermalPrinter.setLineSpace(lineDistance);
                mUsbThermalPrinter.addString("${tv_vendor_txt.text.toString() + "              " + tv_vendor_txt.text.toString()}");

                mUsbThermalPrinter.setAlgin(UsbThermalPrinter.ALGIN_LEFT);

                mUsbThermalPrinter.setAlgin(UsbThermalPrinter.ALGIN_LEFT);
                mUsbThermalPrinter.setLineSpace(2);
                mUsbThermalPrinter.setBold(true);
                //mUsbThermalPrinter.setBold(true);
                mUsbThermalPrinter.setLineSpace(1);
                mUsbThermalPrinter.setBold(true);
                mUsbThermalPrinter.setBold(true);
                mUsbThermalPrinter.setBold(false);


                mUsbThermalPrinter.setAlgin(UsbThermalPrinter.ALGIN_MIDDLE);
                mUsbThermalPrinter.setBold(false);

                mUsbThermalPrinter.setBold(false);
                mUsbThermalPrinter.setAlgin(UsbThermalPrinter.ALGIN_LEFT);

//                var gstVale=String.format("%1$-10s %2$20s ", "${"GST: le:               "}", "      ${rechargePhoneModel!!.result.tax}")//")
//                mUsbThermalPrinter.addString(gstVale);

                var formatter: DecimalFormat = NumberFormat.getInstance(Locale.US) as DecimalFormat;
                formatter.applyPattern("#,###,###,###");

//                val  chargesDouble:Double=rechargePhoneModel!!.result.charges.replace(",","").toDouble();

//                var formattedServiceCharge = formatter.format(chargesDouble);


//                val  debitRecoveryDouble:Double=rechargePhoneModel!!.result.debitRecovery.toDouble();

                mUsbThermalPrinter.setAlgin(UsbThermalPrinter.ALGIN_MIDDLE);
                mUsbThermalPrinter.setBold(false);
                mUsbThermalPrinter.setLineSpace(2);
                mUsbThermalPrinter.setBold(false);
                mUsbThermalPrinter.setLineSpace(2);
                mUsbThermalPrinter.setAlgin(UsbThermalPrinter.ALGIN_LEFT);

                var formatterFloat: DecimalFormat = NumberFormat.getInstance(Locale.US) as DecimalFormat;
                formatterFloat.applyPattern("#,###,###,###.##");
//                val  unitCostDouble:Double=rechargePhoneModel!!.result.unitCost.replace(",","").toDouble();
//                var formattedUnitCost=formatterFloat.format(unitCostDouble);




                mUsbThermalPrinter.setAlgin(UsbThermalPrinter.ALGIN_MIDDLE);
                mUsbThermalPrinter.setTextSize(20);
                mUsbThermalPrinter.setBold(false);
                mUsbThermalPrinter.setLeftIndent(10);
                mUsbThermalPrinter.setLineSpace(4);
                //mUsbThermalPrinter.setTextSize(40);
                mUsbThermalPrinter.addString("********************");
                mUsbThermalPrinter.setTextSize(40);

                mUsbThermalPrinter.setTextSize(20);

                mUsbThermalPrinter.addString("********************\n");
                mUsbThermalPrinter.setBold(false);
                mUsbThermalPrinter.setAlgin(UsbThermalPrinter.ALGIN_LEFT);
                mUsbThermalPrinter.setTextSize(15);
                mUsbThermalPrinter.setLeftIndent(leftDistance);
                mUsbThermalPrinter.setLineSpace(1);
                // mUsbThermalPrinter.addString("${tv_vtech_txt.text.toString() + "  " + tv_transaction_id.text.toString()}");
                mUsbThermalPrinter.addString("${tv_web_text.text.toString()+"              "+tv_phone_no.text.toString()}\n\n");
                // mUsbThermalPrinter.addString("${tv_phone_no.text.toString()}");

                // mUsbThermalPrinter.addSringOneLine(mUsbThermalPrinter.addSringOneLine())

//                mUsbThermalPrinter.setAlgin(UsbThermalPrinter.ALGIN_MIDDLE);
//                val bitmap = CreateCode(tv_bar_code_no.text.toString(), BarcodeFormat.CODE_39, 300, 30)
//                if (bitmap != null) {
//                    mUsbThermalPrinter.printLogo(bitmap,true);
//                }
                mUsbThermalPrinter.setTextSize(20);
//                mUsbThermalPrinter.addString(tv_bar_code_no.text.toString());
                mUsbThermalPrinter.printString();
               mUsbThermalPrinter.walkPaper(30);

            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                Result = e.toString()
                if (Result == "com.telpo.tps550.api.printer.NoPaperException") {
                    nopaper = true
                } else if (Result == "com.telpo.tps550.api.printer.OverHeatException") {
                    handler!!.sendMessage(handler!!.obtainMessage(OVERHEAT, 1, 0, null))
                } else {
                    handler!!.sendMessage(handler!!.obtainMessage(PRINTERR, 1, 0, null))
                }
            } finally {
                handler!!.sendMessage(handler!!.obtainMessage(CANCELPROMPT, 1, 0, null));
                if (nopaper) {
                    handler!!.sendMessage(handler!!.obtainMessage(NOPAPER, 1, 0, null))
                    nopaper = false
                    return
                }
            }
        }
    }

    override fun onDestroy() {
        if (progressDialog != null && !this@AirtimePurchaseSuccess.isFinishing()) {
            progressDialog!!.dismiss()
            progressDialog = null
        }
        unregisterReceiver(printReceive)
        try {
            mUsbThermalPrinter.stop();
        }catch (exception:java.lang.Exception){

        }
        super.onDestroy()
    }

    private inner class paperWalkPrintThread : Thread() {
        override fun run() {
            super.run()
            try {
                mUsbThermalPrinter.reset()
                mUsbThermalPrinter.walkPaper(paperWalk)
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                Result = e.toString()
                if (Result == "com.telpo.tps550.api.printer.NoPaperException") {
                    nopaper = true
                } else if (Result == "com.telpo.tps550.api.printer.OverHeatException") {
                    handler!!.sendMessage(handler!!.obtainMessage(OVERHEAT, 1, 0, null))
                } else {
                    handler!!.sendMessage(handler!!.obtainMessage(PRINTERR, 1, 0, null))
                }
            } finally {
                handler!!.sendMessage(handler!!.obtainMessage(CANCELPROMPT, 1, 0, null))
                if (nopaper) {
                    handler!!.sendMessage(handler!!.obtainMessage(NOPAPER, 1, 0, null))
                    nopaper = false
                    return
                }
            }
        }
    }

    private inner class qrcodePrintThread : Thread() {
        override fun run() {
            super.run()
            try {
                mUsbThermalPrinter.reset()
                mUsbThermalPrinter.setGray(printGray)
                val bitmap: Bitmap? = CreateCode(qrcodeStr, BarcodeFormat.QR_CODE, 256, 256)
                if (bitmap != null) {
                    mUsbThermalPrinter.printLogo(bitmap, true)
                }
                mUsbThermalPrinter.addString(qrcodeStr)
                mUsbThermalPrinter.printString()
                mUsbThermalPrinter.walkPaper(20)
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                Result = e.toString()
                if (Result == "com.telpo.tps550.api.printer.NoPaperException") {
                    nopaper = true
                } else if (Result == "com.telpo.tps550.api.printer.OverHeatException") {
                    handler!!.sendMessage(handler!!.obtainMessage(OVERHEAT, 1, 0, null))
                } else {
                    handler!!.sendMessage(handler!!.obtainMessage(PRINTERR, 1, 0, null))
                }
            } finally {
                handler!!.sendMessage(handler!!.obtainMessage(CANCELPROMPT, 1, 0, null))
                if (nopaper) {
                    handler!!.sendMessage(handler!!.obtainMessage(NOPAPER, 1, 0, null))
                    nopaper = false
                    return
                }
            }
        }
    }

    @Throws(WriterException::class)
    fun CreateCode(str: String?, type: BarcodeFormat?, bmpWidth: Int, bmpHeight: Int): Bitmap? {
        val mHashtable = Hashtable<EncodeHintType, String?>()
        mHashtable[EncodeHintType.CHARACTER_SET] = "UTF-8"
        // 生成二维矩阵,编码时要指定大小,不要生成了图片以后再进行缩放,以防模糊导致识别失败
        val matrix = MultiFormatWriter().encode(str, type, bmpWidth, bmpHeight, mHashtable)
        val width = matrix.width
        val height = matrix.height
        // 二维矩阵转为一维像素数组（一直横着排）
        val pixels = IntArray(width * height)
        for (y in 0 until height) {
            for (x in 0 until width) {
                if (matrix[x, y]) {
                    pixels[y * width + x] = -0x1000000
                } else {
                    pixels[y * width + x] = -0x1
                }
            }
        }
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        // 通过像素数组生成bitmap,具体参考api
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        return bitmap
    }

    private  inner class barcodePrintThread : Thread() {
        override fun run() {
            super.run()
            try {
                mUsbThermalPrinter.reset()
                mUsbThermalPrinter.setGray(printGray)
                val bitmap: Bitmap? = CreateCode(barcodeStr, BarcodeFormat.CODE_128, 320, 176)
                if (bitmap != null) {
                    mUsbThermalPrinter.printLogo(bitmap, true)
                }
                mUsbThermalPrinter.addString(barcodeStr)
                mUsbThermalPrinter.printString()
                mUsbThermalPrinter.walkPaper(20)
            } catch (e: Exception) {
                e.printStackTrace()
                Result = e.toString()
                if (Result == "com.telpo.tps550.api.printer.NoPaperException") {
                    nopaper = true
                } else if (Result == "com.telpo.tps550.api.printer.OverHeatException") {
                    handler!!.sendMessage(handler!!.obtainMessage(OVERHEAT, 1, 0, null))
                } else {
                    handler!!.sendMessage(handler!!.obtainMessage(PRINTERR, 1, 0, null))
                }
            } finally {
                handler!!.sendMessage(handler!!.obtainMessage(CANCELPROMPT, 1, 0, null))
                if (nopaper) {
                    handler!!.sendMessage(handler!!.obtainMessage(NOPAPER, 1, 0, null))
                    nopaper = false
                    return
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.airtime_success_layout)

        if (intent.extras != null) {
            rechargeTransactionDetailResult = intent.getSerializableExtra(Constants.DATA) as AirtimeRechargeModel?;
        }

        handler = MyHandler();

        setData(rechargeTransactionDetailResult!!);

        val pIntentFilter = IntentFilter();
        pIntentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        pIntentFilter.addAction("android.intent.action.BATTERY_CAPACITY_EVENT");
        registerReceiver(printReceive, pIntentFilter);

        if (SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS900.ordinal) {
            // editTextPrintGray.setText("5")
            //printGray=2
            printGray=7
        }

//        tv_sms.setOnClickListener {
//            rechargeTransactionDetailResult?.result?.let { it1 -> showdialog(it1.pin1) };
//        }


        imgBack.setOnClickListener {
            var intent = Intent();
            setResult(RESULT_OK);
            finish();
            handleOnCloseBtn(rechargeTransactionDetailResult!!)
        }
        img_close2.setOnClickListener {
            var intent = Intent();
            setResult(RESULT_OK);
            finish();
            handleOnCloseBtn(rechargeTransactionDetailResult!!)
        }
        tv_print.setOnClickListener {
            try {
                mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
                if (mBluetoothAdapter == null) {
                    userThermalprinter()
                }
                if (!mBluetoothAdapter.isEnabled) {
                    val enableBluetooth = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return@setOnClickListener
                    }
                    startActivityForResult(enableBluetooth, 0)
                }
                val pairedDevices = mBluetoothAdapter.getBondedDevices()
                if (pairedDevices.size > 0) {
                    for (device in pairedDevices) {

                        if (device.name == "PT-220") {
                            mmDevice = device
                            openBT()
                            break
                        }
                    }
                }else{
                    userThermalprinter()
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }

        }
    }



    private val printReceive: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (action == Intent.ACTION_BATTERY_CHANGED) {
                val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_NOT_CHARGING)
                val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)
                val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 0)
                //TPS390 can not print,while in low battery,whether is charging or not charging
                LowBattery = if (SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS390.ordinal) {
                    if (level * 5 <= scale) {
                        true
                    } else {
                        false
                    }
                } else {
                    if (status != BatteryManager.BATTERY_STATUS_CHARGING) {
                        if (level * 5 <= scale) {
                            true
                        } else {
                            false
                        }
                    } else {
                        false
                    }
                }
            } else if (action == "android.intent.action.BATTERY_CAPACITY_EVENT") {
                val status = intent.getIntExtra("action", 0)
                val level = intent.getIntExtra("level", 0)
                LowBattery = if (status == 0) {
                    if (level < 1) {
                        true
                    } else {
                        false
                    }
                } else {
                    false
                }
            }
        }
    }

    override fun onBackPressed() {
        //super.onBackPressed()
        var intent=Intent();
        setResult(RESULT_OK);
        finish();
        handleOnCloseBtn(rechargeTransactionDetailResult!!)
    }
    @Throws(WriterException::class)

    fun CreateImage(message: String?): Bitmap? {
        var bitMatrix: BitMatrix? = null
        bitMatrix =MultiFormatWriter().encode(message, BarcodeFormat.CODE_39, size_width, size_height);

        val width = bitMatrix.width
        val height = bitMatrix.height
        val pixels = IntArray(width * height)
        for (i in 0 until height) {
            for (j in 0 until width) {
                if (bitMatrix[j, i]) {
                    pixels[i * width + j] = -0x1000000
                } else {
                    pixels[i * width + j] = -0x1
                }
            }
        }
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        return bitmap
    }
    private fun setBarCode(barCodeNo:String){

        var bitmap: Bitmap? = null
        try {
            bitmap = CreateImage(barCodeNo)
            //myBitmap = bitmap
        } catch (we: WriterException) {
            we.printStackTrace()
        }

        if (bitmap != null) {
//            img_bar_code.setImageBitmap(bitmap);
        }

    }

    private fun handleOnCloseBtn(rechargePhoneModel: AirtimeRechargeModel){
        if(rechargePhoneModel.result.isNewRecharge){
            Handler().postDelayed({
                val i = Intent(this@AirtimePurchaseSuccess, BillPaymentActivity::class.java)
                startActivity(i)
                overridePendingTransition(R.anim.slide_in, R.anim.slide_out)
            }, 400)
        }
    }
    private fun setData(rechargePhoneModel: AirtimeRechargeModel){

        if (rechargePhoneModel.result==null){
            return;
        }

        this.rechargePhoneModel=rechargePhoneModel;
        if(rechargePhoneModel.result.mobileShowSmsButton){
            tv_sms.visibility = View.VISIBLE
        }

        if(rechargePhoneModel.result.mobileShowPrintButton){
            tv_print.visibility = View.VISIBLE
        }

        tv_date.setText(rechargePhoneModel.result.transactionDate);
        tv_pos_id.setText(rechargePhoneModel.result.pos);
//        tv_vendor_txt.setText(rechargePhoneModel.result.vendorId);
        tv_platformName.setText(" "+rechargePhoneModel.result.receiptTitle);
        tv_account.setText(rechargePhoneModel.result.phone)

        var formatter: DecimalFormat = NumberFormat.getInstance(Locale.US) as DecimalFormat;
        formatter.applyPattern("#,###,###,###");

        var formatterFloat: DecimalFormat = NumberFormat.getInstance(Locale.US) as DecimalFormat;
        formatterFloat.applyPattern("#,###,###,###.##");
        tv_transaction_id.setText(rechargePhoneModel.result.vtechSerial);

    }

    private fun showdialog(pin: String) {
        val adDialog = Dialog(this@AirtimePurchaseSuccess, R.style.MyDialogThemeBlack);
        adDialog.window!!.requestFeature(Window.FEATURE_NO_TITLE);
        adDialog.setContentView(R.layout.alertdialog);
        adDialog.setCancelable(false);

        val tv_send = adDialog.findViewById<TextView>(R.id.tv_send);
        val tv_phone_no = adDialog.findViewById<EditText>(R.id.tv_phone_no);
        val img_close2 = adDialog.findViewById<AppCompatImageButton>(R.id.img_close2);

        img_close2.setOnClickListener {
            adDialog.dismiss();
        }

//        tv_done.setOnClickListener {
//            //   adDialog.cancel()
//            if (MyApplication.isConnectingToInternet(THIS)) {
//                // var str = edittext.text.toString()
//                if (edittext.text.toString().length == 0 || edittext.text.toString().trim().matches("".toRegex())) {
//                    MyApplication.popErrorMsg("", resources.getString(R.string.plz_enter_eamil), THIS)
//                } else {
//                    adDialog.dismiss();
//                    ForgotPassword(edittext.getText().toString());
//                }
//
//            } else
//                noNetConnection()
//        }

        tv_send.setOnClickListener {
            if (tv_phone_no.text.toString().length == 0 || tv_phone_no.text.toString().trim().matches("".toRegex())) {
                Utilities.shortToast("Phone number required",this@AirtimePurchaseSuccess)
            }else if(tv_phone_no.text.toString().length != 8) {
                Utilities.shortToast("Invalid phone number",this@AirtimePurchaseSuccess)
            }
            else {
                val name = tv_phone_no.text.toString()
                SendTransactionViaSMS(pin, name)
                adDialog.cancel()
                adDialog.dismiss();
            }
        }
        adDialog.show();
    }

    fun  SendTransactionViaSMS(transactionId: String, phoneNo:String){

        var customDialog: CustomDialog
        customDialog= CustomDialog(this)
        customDialog.show()
        val call: Call<SendTransactionSmsModel> = Uten.FetchServerData().send_sms_on_recharge(SharedHelper.getString(this,Constants.TOKEN),transactionId, phoneNo)
        call.enqueue(object : Callback<SendTransactionSmsModel> {
            override fun onResponse(call: Call<SendTransactionSmsModel>, response: Response<SendTransactionSmsModel>)  {

                if(customDialog.isShowing){
                    customDialog.dismiss()
                }
                var data=response.body()
                if(data!=null){
                    if(data.status.equals("true")){
                        Utilities.longToast("SMS SENT SUCCESSFULLY",this@AirtimePurchaseSuccess)
                    }else{
                        Utilities.shortToast("SMS FAILED TO SEND",this@AirtimePurchaseSuccess)
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

    private fun userThermalprinter() {

        printContent = "${tv_vendtech_name.text.toString()}\n" +
                "--------------------------------------------------------------\n" +
                "${tv_date_txt.text.toString() + " " + tv_date.text.toString()}\n" +
//                "${tv_vendor_txt.text.toString() + " " + tv_vendor_name.text.toString()}\n" +
                "${tv_pos_id_txt.text.toString() + " " + tv_pos_id.text.toString()}\n" +
                "------${tv_custInfo.text.toString() + "------"}\n" +
                "${tv_vendor_txt.text.toString() + " " + tv_vendor_txt.text.toString()}\n" +
                "${tv_web_text.text.toString()}\n" +
                "${tv_phone_no.text.toString()}\n";


        if (printContent == null || printContent!!.length == 0) {
            Utilities.longToast(getString(R.string.empty), this)
//            return@setOnClickListener
        }

        if (LowBattery == true) {
            handler!!.sendMessage(handler!!.obtainMessage(LOWBATTERY, 1, 0, null))
        } else {
            if (!nopaper) {
                progressDialog = ProgressDialog.show(this@AirtimePurchaseSuccess, getString(R.string.bl_dy), getString(R.string.printing_wait))
                handler!!.sendMessage(handler!!.obtainMessage(PRINTCONTENT, 1, 0, null))
            } else {
                Toast.makeText(this@AirtimePurchaseSuccess, getString(R.string.ptintInit), Toast.LENGTH_LONG).show()
            }
        }

    }

    @Throws(IOException::class)
    fun openBT() {
        try {
            // Standard SerialPortService ID
            val uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            mmSocket = mmDevice!!.createRfcommSocketToServiceRecord(uuid)
            mmSocket.connect()
            mmOutputStream = mmSocket.getOutputStream()
            mmInputStream = mmSocket.getInputStream()
            beginListenForData()
            Utilities.longToast("USING BLUETOOTH PRINTER",this@AirtimePurchaseSuccess)
        } catch (e: java.lang.Exception) {
            closeBT()
            e.printStackTrace()
        }
    }

    fun beginListenForData() {
        try {
            val handler = Handler()

            // this is the ASCII code for a newline character
            val delimiter: Byte = 15
            stopWorker = false
            readBufferPosition = 0
            readBuffer = ByteArray(2024)
            workerThread = Thread {
                while (!Thread.currentThread().isInterrupted && !stopWorker) {
                    try {
                        val bytesAvailable = mmInputStream!!.available()
                        if (bytesAvailable > 0) {
                            val packetBytes = ByteArray(bytesAvailable)
                            mmInputStream!!.read(packetBytes)
                            for (i in 0 until bytesAvailable) {
                                val b = packetBytes[i]
                                if (b == delimiter) {
                                    val encodedBytes = ByteArray(readBufferPosition)
                                    System.arraycopy(
                                        readBuffer, 0,
                                        encodedBytes, 0,
                                        encodedBytes.size
                                    )

                                    // specify US-ASCII encoding

                                    chalset.encode("US-ASCII")
                                    val data = String(encodedBytes, chalset)
                                    readBufferPosition = 0

                                    // tell the user data were sent to bluetooth printer device
                                    handler.post(Runnable { myLabel!!.text = data })
                                } else {
                                    readBuffer!![readBufferPosition++] = b
                                }
                            }
                        }
                    } catch (ex: IOException) {
                        stopWorker = true
                    }
                }
            }
            workerThread!!.start()

            sendData()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    // this will send text data to be printed by the bluetooth printer
    @Throws(IOException::class)
    fun sendData() {
        try {

            printContent =
                    "${ "\n\n\n+++++++++++VENDTECH"}\n".replace('+', ' ') +
                    "------------------------\n" +
                    "${tv_date_txt.text.toString() + " +++" + tv_date.text.toString()}\n".replace('+', ' ') +
                    "${tv_pos_id_txt.text.toString() + " +++" + tv_pos_id.text.toString()}\n".replace('+', ' ') +
                    "------${tv_custInfo.text.toString() + "------"}\n" +
                    "${tv_vendor_txt.text.toString() + " +++" + tv_vendor_txt.text.toString()}\n".replace('+', ' ') +
                    "${tv_web_text.text.toString()}\n" +
                    "${ "\n"+ tv_phone_no.text.toString()}\n\n"
                    "${ "\n +++++++++++++++ " }\n".replace('+', ' ');

            mmOutputStream!!.write(printContent!!.toByteArray())
            printContent = "";
            closeBT()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            closeBT()
        }
    }

    // close the connection to bluetooth printer.
    @Throws(IOException::class)
    fun closeBT() {
        try {
            stopWorker = true
            mmOutputStream!!.close()
            mmInputStream!!.close()
            mmSocket!!.close()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }
}