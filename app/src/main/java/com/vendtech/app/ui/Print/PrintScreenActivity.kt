package com.vendtech.app.ui.Print


import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.app.ProgressDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.*
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
import com.vendtech.app.models.meter.RechargeMeterModel
import com.vendtech.app.models.transaction.SendTransactionSmsModel
import com.vendtech.app.network.Uten
import com.vendtech.app.utils.Constants
import com.vendtech.app.utils.CustomDialog
import com.vendtech.app.utils.Utilities
import kotlinx.android.synthetic.main.activity_forgot_password.*
import kotlinx.android.synthetic.main.print_screen_layout.*
import kotlinx.android.synthetic.main.print_screen_layout.img_close
import kotlinx.android.synthetic.main.sendemaildialog.*
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

class PrintScreenActivity : AppCompatActivity() {



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

    var rechargeMeterModel: RechargeMeterModel?=null;

    private var rechargeTransactionDetailResult: RechargeMeterModel?=null;

    private var dialog: ProgressDialog? = null;

    private fun noPaperDlg() {
        val dlg = AlertDialog.Builder(this@PrintScreenActivity)
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
                    val alertDialog = AlertDialog.Builder(this@PrintScreenActivity)
                    alertDialog.setTitle(R.string.operation_result)
                    alertDialog.setMessage(getString(R.string.LowBattery))
                    alertDialog.setPositiveButton(getString(R.string.dialog_comfirm), DialogInterface.OnClickListener { dialogInterface, i -> })
                    alertDialog.show()
                }
                NOBLACKBLOCK -> Toast.makeText(this@PrintScreenActivity, R.string.maker_not_find, Toast.LENGTH_SHORT).show()
                PRINTVERSION -> {
                    dialog!!.dismiss()
                    if (msg.obj == "1") {
                        // textPrintVersion.setText(printVersion)
                    } else {
                        Toast.makeText(this@PrintScreenActivity, R.string.operation_fail, Toast.LENGTH_LONG).show()
                    }
                }
                PRINTBARCODE -> barcodePrintThread().start()
                PRINTQRCODE -> qrcodePrintThread().start()
                PRINTPAPERWALK -> paperWalkPrintThread().start()
                PRINTCONTENT -> contentPrintThread().start()
                CANCELPROMPT -> if (progressDialog != null && !this@PrintScreenActivity.isFinishing()) {
                    progressDialog!!.dismiss()
                    progressDialog = null
                }
                OVERHEAT -> {
                    val overHeatDialog = AlertDialog.Builder(this@PrintScreenActivity)
                    overHeatDialog.setTitle(R.string.operation_result)
                    overHeatDialog.setMessage(getString(R.string.overTemp))
                    overHeatDialog.setPositiveButton(getString(R.string.dialog_comfirm), DialogInterface.OnClickListener { dialogInterface, i -> })
                    overHeatDialog.show()
                }
                else -> {
                    Toast.makeText(this@PrintScreenActivity, "Print Error!", Toast.LENGTH_LONG).show();
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

                mUsbThermalPrinter.addString("${"VENDOR:"+"                         " + tv_vendor_name.text.toString()}");
                mUsbThermalPrinter.addString("${"POS ID:"+"                          " + tv_pos_id.text.toString()}\n");
                //mUsbThermalPrinter.setAlgin(UsbThermalPrinter.ALGIN_MIDDLE);
                //mUsbThermalPrinter.setBold(true);
                mUsbThermalPrinter.setLineSpace(2);
                mUsbThermalPrinter.addString("--- ${tv_custInfo.text.toString() + " ---"}\n");
                mUsbThermalPrinter.setBold(false);
                //mUsbThermalPrinter.setAlgin(UsbThermalPrinter.ALGIN_RIGHT);
                mUsbThermalPrinter.setLineSpace(lineDistance);
                mUsbThermalPrinter.addString("${tv_customer_txt.text.toString() + "              " + tv_cus_name.text.toString()}");
                mUsbThermalPrinter.addString("${tv_account_txt.text.toString() + "               " + tv_account.text.toString()}");

    
                // var rightALign= StringAlignUtils(tv_address.text.toString().length, StringAlignUtils.Alignment.RIGHT);
                //var rightALignTerrif= StringAlignUtils(tv_terrif.text.toString().length, StringAlignUtils.Alignment.RIGHT);
                mUsbThermalPrinter.setAlgin(UsbThermalPrinter.ALGIN_LEFT);

                //mUsbThermalPrinter.addString("${"ADDRESS:" + "                       " + tv_address.text.toString()}"+"  ");
                var addressValue= String.format("%1$-10s %2$10s ", "${"ADDRESS:"}", "${tv_address.text.toString()}");

                mUsbThermalPrinter.addString(addressValue);

                mUsbThermalPrinter.setAlgin(UsbThermalPrinter.ALGIN_LEFT);
                mUsbThermalPrinter.setLineSpace(2);
                mUsbThermalPrinter.setBold(true);
                //mUsbThermalPrinter.setBold(true);
                mUsbThermalPrinter.addString("${tv_meter_txt.text.toString() + "                        " + tv_meter_number.text.toString()}");
                mUsbThermalPrinter.setLineSpace(1);
                mUsbThermalPrinter.setBold(true);
                mUsbThermalPrinter.addString("${tv_tarif_txt.text.toString() + "                                       " + tv_terrif.text.toString()}");//
                //mUsbThermalPrinter.addString("${tv_tarif_txt.text.toString() + "   " +rightALignTerrif.format(tv_terrif.text.toString())}");


                //mUsbThermalPrinter.addString("${tv_amt_tend_txt.text.toString()+" le:" + "                      " +""+ tv_amount_tendered.text.toString()}\n");

                mUsbThermalPrinter.setBold(true);

                var amountTendred= String.format("%1$-10s %2$10s ", "${tv_amt_tend_txt.text.toString()+"      le: "}", "${tv_amount_tendered.text.toString()}");//
                mUsbThermalPrinter.addString(amountTendred+"\n");
                mUsbThermalPrinter.setBold(false);


                mUsbThermalPrinter.setAlgin(UsbThermalPrinter.ALGIN_MIDDLE);
                mUsbThermalPrinter.setBold(false);
                // mUsbThermalPrinter.addString("-----------${tv_deduct.text.toString() + "-----------"}");
                mUsbThermalPrinter.addString("-----    ${tv_deduct.text.toString() +"    -----"}\n");
                mUsbThermalPrinter.setBold(false);
                mUsbThermalPrinter.setAlgin(UsbThermalPrinter.ALGIN_LEFT);
                //mUsbThermalPrinter.addString("${"GST:" + "                               " + tv_service_charge.text.toString()}");
                //mUsbThermalPrinter.addString("${"GST: le:" + "                                           " + rechargeMeterModel!!.result.tax}");
                var gstVale=String.format("%1$-10s %2$20s ", "${"GST: le:               "}", "      ${rechargeMeterModel!!.result.tax}")//")
                mUsbThermalPrinter.addString(gstVale);
                // var rightALignServiceCharge= StringAlignUtils(tv_service_charge.text.toString().length, StringAlignUtils.Alignment.RIGHT);
                //mUsbThermalPrinter.addString("${"GST:" + "              " + rightALignServiceCharge.format(tv_service_charge.text.toString())}");

                var formatter: DecimalFormat = NumberFormat.getInstance(Locale.US) as DecimalFormat;
                formatter.applyPattern("#,###,###,###");
                // val  chargesDouble:Double=rechargeMeterModel!!.result.charges.toDouble();
                val  chargesDouble:Double=rechargeMeterModel!!.result.charges.replace(",","").toDouble();

                var formattedServiceCharge = formatter.format(chargesDouble);
                //mUsbThermalPrinter.addString("${tv_service_charge_txt.text.toString() + "                       " + tv_service_charge.text.toString()}");
                var serviceChargeValue=String.format("%1$-10s %2$20s", "${tv_service_charge_txt.text.toString()+"le:"}", "${formattedServiceCharge.toString()}") //}")

                mUsbThermalPrinter.addString(serviceChargeValue);

                //mUsbThermalPrinter.addString("${tv_service_charge_txt.text.toString()+" le:" + "                       " + formattedServiceCharge.toString()}");

                val  debitRecoveryDouble:Double=rechargeMeterModel!!.result.debitRecovery.toDouble();
                var formattedRecovery = formatter.format(debitRecoveryDouble);


                //mUsbThermalPrinter.addString("${tv_debit_recovery_txt.text.toString()+" le:" + "                         " + formattedRecovery.toString()}\n");

                var debitRecoveryValue=String.format("%1$-10s %2$20s ", "${tv_debit_recovery_txt.text.toString()+" le:   "}", "${formattedRecovery.toString()}" )//")

                mUsbThermalPrinter.addString(debitRecoveryValue+"\n");
                mUsbThermalPrinter.setAlgin(UsbThermalPrinter.ALGIN_MIDDLE);
                mUsbThermalPrinter.setBold(false);
                mUsbThermalPrinter.setLineSpace(2);
                mUsbThermalPrinter.addString("-----     ${tv_tottext.text.toString() + "    -----"}\n");
                mUsbThermalPrinter.setBold(false);
                mUsbThermalPrinter.setLineSpace(2);
                mUsbThermalPrinter.setAlgin(UsbThermalPrinter.ALGIN_LEFT);

                var formatterFloat: DecimalFormat = NumberFormat.getInstance(Locale.US) as DecimalFormat;
                formatterFloat.applyPattern("#,###,###,###.##");
                val  unitCostDouble:Double=rechargeMeterModel!!.result.unitCost.replace(",","").toDouble();
                var formattedUnitCost=formatterFloat.format(unitCostDouble);

                //mUsbThermalPrinter.addString("${tv_cost_of_unit_txt.text.toString() + "                       " + tv_cost_of_unit.text.toString()}");

                //mUsbThermalPrinter.addString("${tv_cost_of_unit_txt.text.toString()+" le:" + "                       " + formattedUnitCost.toString()}");
                var costOfUnitValue=String.format("%1$-10s %2$10s ", "${tv_cost_of_unit_txt.text.toString()+" le:       "}", "${formattedUnitCost.toString()}" ) //")
                mUsbThermalPrinter.addString(costOfUnitValue);


                //mUsbThermalPrinter.addString("${tv_unit_txt.text.toString() + "                                             " + tv_unit.text.toString()}\n");
                var unitValue=String.format("%1$-10s %2$20s ", "${tv_unit_txt.text.toString()+" kwH:          "}", "  ${tv_unit.text.toString() }" ) //")

                mUsbThermalPrinter.addString(unitValue+"\n");

                mUsbThermalPrinter.setAlgin(UsbThermalPrinter.ALGIN_MIDDLE);
                mUsbThermalPrinter.setTextSize(20);
                mUsbThermalPrinter.setBold(false);
                mUsbThermalPrinter.setLeftIndent(10);
                mUsbThermalPrinter.setLineSpace(4);
                //mUsbThermalPrinter.setTextSize(40);
                mUsbThermalPrinter.addString("********************");
                mUsbThermalPrinter.setTextSize(40);

                mUsbThermalPrinter.addString("${tv_token.text.toString()}");
                mUsbThermalPrinter.setTextSize(20);

                mUsbThermalPrinter.addString("********************\n");
                mUsbThermalPrinter.setBold(false);
                mUsbThermalPrinter.setAlgin(UsbThermalPrinter.ALGIN_LEFT);
                mUsbThermalPrinter.setTextSize(15);
                mUsbThermalPrinter.setLeftIndent(leftDistance);
                mUsbThermalPrinter.setLineSpace(1);
                mUsbThermalPrinter.addString("${"EDSA Serial #:" + " " + tv_serial.text.toString()+"          "+tv_vtech_txt.text.toString() + "" + tv_transaction_id.text.toString()}");
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
        if (progressDialog != null && !this@PrintScreenActivity.isFinishing()) {
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
        setContentView(R.layout.print_screen_layout)

        if (intent.extras != null) {
            rechargeTransactionDetailResult = intent.getSerializableExtra(Constants.DATA) as RechargeMeterModel?;
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

        tv_sms.setOnClickListener {
            rechargeTransactionDetailResult?.result?.let { it1 -> showdialog(it1.pin1) };
        }
        tv_email.setOnClickListener {
            rechargeTransactionDetailResult?.result?.let { it1 -> showEmaildialog(it1.pin1) };
        }


        imgBack.setOnClickListener {
            var intent = Intent();
            setResult(Activity.RESULT_OK);
            finish();
        }
        img_close.setOnClickListener {
            var intent = Intent();
            setResult(Activity.RESULT_OK);
            finish();
        }
        tv_print.setOnClickListener {

//            userThermalprinter();

            try {

                mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
                if (mBluetoothAdapter == null) {
                    userThermalprinter()
                }
                if (!mBluetoothAdapter.isEnabled) {
                    val enableBluetooth = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
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
        setResult(Activity.RESULT_OK);
        finish();
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
            img_bar_code.setImageBitmap(bitmap);
        }

    }
    private fun setData(rechargeMeterModel: RechargeMeterModel){

        if (rechargeMeterModel.result==null){
            return;
        }

        this.rechargeMeterModel=rechargeMeterModel;

        if(rechargeMeterModel.result.mobileShowSmsButton){
            tv_sms.visibility = View.VISIBLE
        }
        if(rechargeMeterModel.result.mobileShowPrintButton){
            tv_print.visibility = View.VISIBLE
        }
        if(rechargeMeterModel.result.pin1.isNotEmpty()){
            tv_email.visibility = View.GONE
        }
        tv_date.setText(rechargeMeterModel.result.transactionDate);
        tv_vendor_name.setText(rechargeMeterModel.result.vendorId);
        tv_pos_id.setText(rechargeMeterModel.result.pos);
        tv_cus_name.setText(rechargeMeterModel.result.customerName);
        tv_account.setText(rechargeMeterModel.result.accountNo);
        tv_address.setText(rechargeMeterModel.result.address);
        tv_meter_number.setText(rechargeMeterModel.result.deviceNumber);
        //tv_terrif.setText(rechargeMeterModel.result.tarrif)

        //var longval:Long=meterListModels[position].amount.toLong();

        //tv_gst.setText("le:"+rechargeMeterModel.result.tax);


        var formatter: DecimalFormat = NumberFormat.getInstance(Locale.US) as DecimalFormat;
        formatter.applyPattern("#,###,###,###");

        var formatterFloat: DecimalFormat = NumberFormat.getInstance(Locale.US) as DecimalFormat;
        formatterFloat.applyPattern("#,###,###,###.##");

        //val  amoundDouble:Double=rechargeMeterModel.result.amount.toDouble();
        //val chargesTemp=rechargeMeterModel.result.charges.replace(",","");
        val  chargesDouble:Double=rechargeMeterModel.result.charges.replace(",","").toDouble();
        val  debitRecoveryDouble:Double=rechargeMeterModel.result.debitRecovery.replace(",","").toDouble();
        val  tarrifDouble:Double=rechargeMeterModel.result.tarrif.replace(",","").toDouble();
        //val  unitCostDouble:Double=rechargeMeterModel.result.unitCost.toDouble();
        val  unitCostDouble:Double=rechargeMeterModel.result.unitCost.replace(",","").toDouble();
        //val  taxDouble:Double=rechargeMeterModel.result.tax.toDouble();
        // var formattedAmount = formatter.format(amoundDouble);
        var formattedServiceCharge = formatter.format(chargesDouble);
        var formattedRecovery = formatter.format(debitRecoveryDouble);
        var formattedTarrif = formatter.format(tarrifDouble);
        var formattedUnitCost=formatterFloat.format(unitCostDouble);
        // var formattedTax=formatterFloat.format(taxDouble);
        //tv_gst.setText("le:"+formattedTax);
        tv_gst.setText("le:"+rechargeMeterModel.result.tax);
        tv_terrif.setText(formattedTarrif);
        tv_amount_tendered.setText(rechargeMeterModel.result.amount);
        tv_service_charge.setText("le:"+formattedServiceCharge);
        tv_debit_recovery.setText("le:"+formattedRecovery);
        tv_debit_recovery.setText("le:"+formattedRecovery);

        tv_cost_of_unit.setText("le:"+formattedUnitCost)

        tv_unit.setText(rechargeMeterModel.result.unit);
        tv_token.setText(rechargeMeterModel.result.pin1);
        tv_serial.setText(rechargeMeterModel.result.serialNo);
        tv_transaction_id.setText(rechargeMeterModel.result.vtechSerial);

//        tv_bar_code_no.setText(rechargeMeterModel.result.deviceNumber);
//        setBarCode(rechargeMeterModel.result.deviceNumber);

    }

    private fun showdialog(pin: String) {
        val adDialog = Dialog(this@PrintScreenActivity, R.style.MyDialogThemeBlack);
        adDialog.window!!.requestFeature(Window.FEATURE_NO_TITLE);
        adDialog.setContentView(R.layout.alertdialog);
        adDialog.setCancelable(false);

        val tv_send = adDialog.findViewById<TextView>(R.id.tv_send);
        val tv_phone_no = adDialog.findViewById<EditText>(R.id.tv_phone_no);
        val img_close = adDialog.findViewById<AppCompatImageButton>(R.id.img_close);

        img_close.setOnClickListener {
            adDialog.dismiss();
        }


        tv_send.setOnClickListener {
            if (tv_phone_no.text.toString().length == 0 || tv_phone_no.text.toString().trim().matches("".toRegex())) {
                Utilities.shortToast("Phone number required",this@PrintScreenActivity)
            }else if(tv_phone_no.text.toString().length != 8) {
                Utilities.shortToast("Invalid phone number",this@PrintScreenActivity)
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

    private fun showEmaildialog(pin: String) {
        val adDialog = Dialog(this@PrintScreenActivity, R.style.MyDialogThemeBlack);
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
                Utilities.shortToast("Email is required",this@PrintScreenActivity)
            }
            else {
                val email = tv_send_via_email.text.toString()
                SendTransactionViaEmail(rechargeTransactionDetailResult!!.result!!.pin1, tv_email_address.text.toString())
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
                        Utilities.longToast("SMS SENT SUCCESSFULLY",this@PrintScreenActivity)
                    }else{
                        Utilities.shortToast("SMS FAILED TO SEND",this@PrintScreenActivity)
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
                        Utilities.longToast("EMAIL SENT SUCCESSFULLY",this@PrintScreenActivity)
                    }else{
                        Utilities.shortToast("EMAIL FAILED TO SEND",this@PrintScreenActivity)
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
                "${tv_edsa.text.toString()}\n" +
                "--------------------------------------------------------------\n" +
                "${tv_date_txt.text.toString() + " " + tv_date.text.toString()}\n" +
                "${tv_vendor_txt.text.toString() + " " + tv_vendor_name.text.toString()}\n" +
                "${tv_pos_id_txt.text.toString() + " " + tv_pos_id.text.toString()}\n" +
                "------${tv_custInfo.text.toString() + "------"}\n" +
                "${tv_customer_txt.text.toString() + " " + tv_cus_name.text.toString()}\n" +
                "${tv_account_txt.text.toString() + " " + tv_account.text.toString()}\n" +
                "${tv_meter_txt.text.toString() + " " + tv_meter_number.text.toString()}\n" +
                "${tv_amt_tend_txt.text.toString() + "               " + tv_amount_tendered.text.toString()}\n" +
                "------${tv_deduct.text.toString() + "------"}\n" +
                "${tv_service_charge_txt.text.toString() + "            " + tv_service_charge.text.toString()}\n" +
                "${tv_debit_recovery_txt.text.toString() + "          " + tv_debit_recovery.text.toString()}\n" +
                "------${tv_tottext.text.toString() + "------"}\n" +
                "${tv_cost_of_unit_txt.text.toString() + " " + tv_cost_of_unit.text.toString()}\n" +
                "${tv_unit_txt.text.toString() + "                " + tv_unit.text.toString()}\n" +
                "${tv_token.text.toString()}\n" +
                "${tv_vtech_txt.text.toString() + " " + tv_transaction_id.text.toString()}\n" +
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
                progressDialog = ProgressDialog.show(this@PrintScreenActivity, getString(R.string.bl_dy), getString(R.string.printing_wait))
                handler!!.sendMessage(handler!!.obtainMessage(PRINTCONTENT, 1, 0, null))
            } else {
                Toast.makeText(this@PrintScreenActivity, getString(R.string.ptintInit), Toast.LENGTH_LONG).show()
            }
        }

    }

    @Throws(IOException::class)
    fun openBT() {
        try {
            // Standard SerialPortService ID
            val uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")
            mmSocket = mmDevice!!.createRfcommSocketToServiceRecord(uuid)
            mmSocket.connect()
            mmOutputStream = mmSocket.getOutputStream()
            mmInputStream = mmSocket.getInputStream()
            beginListenForData()
            Utilities.longToast("USING BLUETOOTH PRINTER",this@PrintScreenActivity)
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

            printContent = "${ "\n\n\n+++++++++++VENDTECH"}\n".replace('+', ' ') +
                    "${"+++"+tv_edsa.text.toString()}\n".replace('+', ' ') +
                    "------------------------\n" +
                    "${tv_date_txt.text.toString() + " +++" + tv_date.text.toString()}\n".replace('+', ' ') +
                    "${tv_vendor_txt.text.toString() + " +++" + tv_vendor_name.text.toString()}\n".replace('+', ' ') +
                    "${tv_pos_id_txt.text.toString() + " +++" + tv_pos_id.text.toString()}\n".replace('+', ' ') +
                    "------${tv_custInfo.text.toString() + "------"}\n" +
                    "${tv_customer_txt.text.toString() + " +++" + tv_cus_name.text.toString()}\n".replace('+', ' ') +
                    "${tv_account_txt.text.toString() + " +++" + tv_account.text.toString()}\n".replace('+', ' ') +
                    "${tv_meter_txt.text.toString() + " +++++++++" + tv_meter_number.text.toString()}\n".replace('+', ' ') +
                    "${tv_amt_tend_txt.text.toString() + "               " + tv_amount_tendered.text.toString()}\n" +
                    "---------${tv_deduct.text.toString() + "---------"}\n" +
                    "${tv_service_charge_txt.text.toString() + "            " + tv_service_charge.text.toString()}\n" +
                    "${tv_debit_recovery_txt.text.toString() + "          " + tv_debit_recovery.text.toString()}\n" +
                    "---------${tv_tottext.text.toString() + "---------"}\n" +
                    "${tv_cost_of_unit_txt.text.toString() + " " + tv_cost_of_unit.text.toString()}\n" +
                    "${tv_unit_txt.text.toString() + "                " + tv_unit.text.toString()}\n" +
                    "${tv_token.text.toString()}\n" +
                    "${tv_vtech_txt.text.toString() + " " + tv_transaction_id.text.toString()}\n" +
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