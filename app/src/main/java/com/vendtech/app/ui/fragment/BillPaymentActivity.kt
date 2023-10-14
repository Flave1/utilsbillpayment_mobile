package com.vendtech.app.ui.fragment

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.Window
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.robinhood.ticker.TickerUtils
import com.robinhood.ticker.TickerView
import com.vendtech.app.R
import com.vendtech.app.adapter.number.NumberListAutoCompleteAdapter
import com.vendtech.app.adapter.number.NumberListDialogAdapter
import com.vendtech.app.adapter.profile.UserServicesAdapter
import com.vendtech.app.helper.SharedHelper
import com.vendtech.app.models.meter.GetMetersModel
import com.vendtech.app.models.meter.MeterListResults
import com.vendtech.app.models.meter.PosResultModel
import com.vendtech.app.models.airtime.AirtimeRechargeModel
import com.vendtech.app.models.meter.RechargeMeterModel
import com.vendtech.app.models.profile.GetWalletModel
import com.vendtech.app.models.profile.UserAssignedServicesModel
import com.vendtech.app.models.profile.UserServicesResult
import com.vendtech.app.network.Uten
import com.vendtech.app.ui.Print.PrintScreenActivity
import com.vendtech.app.ui.alerts.AirtimePurchaseSuccess
import com.vendtech.app.utils.*
import kotlinx.android.synthetic.main.airtime_layout_confirm_pay.*
import kotlinx.android.synthetic.main.fragment_bill_payment.*
import kotlinx.android.synthetic.main.layout_confirm_pay.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*
import kotlin.collections.ArrayList

class BillPaymentActivity : Activity(), View.OnClickListener, NumberListDialogAdapter.ItemClickListener, UserServicesAdapter.ItemClickListener {

    private  var data:MeterListResults?=null;


    lateinit var contactVendtechTV: TextView
    lateinit var errorDashTV: TextView
    lateinit var errorAccountService: LinearLayout

    //BALANCE DETAIL LAYOUT
    lateinit var totalBalanceTV: TextView
    lateinit var phoneET: TextView
    var totalAvlblBalance = 0.0
    lateinit var tickerViewBalance: TickerView

    //SERVICE MENU LAYOUT

    lateinit var serviceLayout: RelativeLayout
    lateinit var servicesRecyclerview: RecyclerView
    lateinit var servicesAdapter: UserServicesAdapter
    //confirmpay
    lateinit var confirmPayCancel:TextView
    lateinit var confirmPayPayBtn:TextView

    lateinit var confirmAirtimePayCancel:TextView
    lateinit var confirmAirtimePayBtn:TextView

    //ANIMATION
    lateinit var slide_in: Animation
    lateinit var slide_out: Animation

    //SERVICE PAY LAYOUT
    lateinit var payBillTV: TextView
    lateinit var payForAirtimeTV: TextView
    lateinit var moneyET: EditText
    lateinit var airTimeAmtET: EditText
    lateinit var fabBack: FloatingActionButton
    lateinit var atFabBack: FloatingActionButton
    lateinit var confirmFabBack: FloatingActionButton
    lateinit var payLayout: RelativeLayout
    lateinit var airtimeLayout: RelativeLayout
    lateinit var autoCompleteTV: AutoCompleteTextView
    internal var meterListModels: MutableList<MeterListResults> = ArrayList()
    lateinit var selectedMeterID: String
    lateinit var showListmeterIV: ImageView
    lateinit var meterDialogAdapter: NumberListDialogAdapter
    lateinit var dialogMain: Dialog
    var IsSelectFromMeterList = false
    var PlatformId = "";
    lateinit var cbSaveMeter: CheckBox
    lateinit var posSpinner: Spinner

    //ANIMATION
    lateinit var slide_up: Animation
    lateinit var slide_down: Animation
    var posId =""
    var balance = ""

    //INTERFACE COUNTS
    var countInterface: NotificationCount? = null
    var posList = ArrayList<PosResultModel.Result>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_bill_payment)

        try {
            countInterface = this as? NotificationCount
        } catch (e: ClassCastException) {
            throw ClassCastException(" must implement MyInterface ");
        }
        findviews()

        getData();
        tvPosNumber.setText("POS ID : " + SharedHelper.getString(this, Constants.POS_NUMBER))
    }

    private fun getData(){
        try {
//            val bundle = this.arguments;

             data= intent.extras!!.getSerializable("data") as MeterListResults;

            PlatformId = data!!.platformId.toString();
            var numbertype = data!!.numberType.toString();
            if(numbertype == "0"){
                autoCompleteTV.setText(data!!.number);
                showListmeterIV.setOnClickListener(null)
                ShowPayLayout();
            }else{
                phoneET.setText(data!!.number)
                ShowAirtimePurchaseLayout()
            }
            selectedMeterID=data!!.meterId;


        }catch (exception:Exception){

        }



    }


    fun findviews() {

        slide_down = AnimationUtils.loadAnimation(this, R.anim.slide_down)
        slide_up = AnimationUtils.loadAnimation(this, R.anim.slide_up)
        contactVendtechTV = findViewById<View>(R.id.contactVendtechTV) as TextView
        errorDashTV = findViewById<View>(R.id.errorDashTV) as TextView
        errorAccountService = findViewById<View>(R.id.errorAccountService) as LinearLayout

        totalBalanceTV = findViewById<View>(R.id.totalBalanceTV) as TextView
        tickerViewBalance = findViewById<View>(R.id.tickerView) as TickerView
        servicesRecyclerview = findViewById<View>(R.id.servicesRecyclerview) as RecyclerView
        tickerViewBalance.setCharacterLists(TickerUtils.provideNumberList());
        val font = Typeface.createFromAsset(assets, "fonts/roboto_bold.ttf")
        tickerViewBalance.typeface = font

        payBillTV = findViewById<View>(R.id.paynowTV) as TextView
        phoneET = findViewById<View>(R.id.phoneET) as TextView
        payForAirtimeTV = findViewById<View>(R.id.payForAirtimeTV) as TextView
        cbSaveMeter = findViewById(R.id.cbSaveMeter) as CheckBox
        moneyET = findViewById<View>(R.id.moneypayET) as EditText
        airTimeAmtET = findViewById<View>(R.id.airTimeAmtET) as EditText
        fabBack = findViewById<View>(R.id.fabBack) as FloatingActionButton
        atFabBack = findViewById<View>(R.id.atFabBack) as FloatingActionButton
        confirmFabBack = findViewById<View>(R.id.confirmFabBack) as FloatingActionButton
        serviceLayout = findViewById<View>(R.id.serviceLayout) as RelativeLayout
        payLayout = findViewById<View>(R.id.payLayout) as RelativeLayout
        airtimeLayout = findViewById<View>(R.id.airtimeLayout) as RelativeLayout
        autoCompleteTV = findViewById<View>(R.id.autoCompleteTextView) as AutoCompleteTextView
        showListmeterIV = findViewById<View>(R.id.showListmeterIV) as ImageView
        posSpinner = findViewById<View>(R.id.posIdSpinner) as Spinner
        confirmPayCancel = findViewById<View>(R.id.confirmPayCancel) as TextView
        confirmPayPayBtn = findViewById<View>(R.id.confirmPayPayBtn) as TextView
        confirmAirtimePayCancel = findViewById<View>(R.id.confirmAirtimePayCancel) as TextView
        confirmAirtimePayBtn = findViewById<View>(R.id.confirmAirtimePayBtn) as TextView

        payBillTV.setOnClickListener(this)
        payForAirtimeTV.setOnClickListener(this)
        fabBack.setOnClickListener(this)
        atFabBack.setOnClickListener(this)
        confirmFabBack.setOnClickListener(this)
        confirmPayCancel.setOnClickListener(this)
        confirmPayPayBtn.setOnClickListener(this)
        confirmAirtimePayCancel.setOnClickListener(this)
        confirmAirtimePayBtn.setOnClickListener(this)
        showListmeterIV.setOnClickListener(this)
        val imgBack = findViewById<ImageView>(R.id.imgBack)
        imgBack.setOnClickListener{
            onBackPressed()
        }


        payLayout.visibility = View.GONE
        airtimeLayout.visibility = View.GONE
        slide_in = AnimationUtils.loadAnimation(this, R.anim.slide_in)
        slide_out = AnimationUtils.loadAnimation(this, R.anim.activity_back_out)

        moneyET.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                moneyET.removeTextChangedListener(this)
                try {
                    var originalString = s.toString()

                    var longval: Long
                    if (originalString.contains(",")) {
                        originalString = originalString.replace(",", "")
                    }
                    longval = originalString.toLong()
                    var waletBalCrnt = tickerViewBalance.text.toString()
                    if (waletBalCrnt.contains(","))
                        waletBalCrnt = waletBalCrnt.replace(",", "")
                    if (longval <= waletBalCrnt.toLong()) {
                        var formatter: DecimalFormat = NumberFormat.getInstance(Locale.US) as DecimalFormat
                        formatter.applyPattern("#,###,###,###")
                        var formattedString = formatter.format(longval)
                        moneyET.setText(formattedString);
                        moneyET.setSelection(moneyET.text.length)
                    } else {
                        Toast.makeText(this@BillPaymentActivity, "Amount is greater then Wallet Balance", Toast.LENGTH_SHORT).show()
                        var getEnterValue = moneyET.text.toString()
                        var op: String = getEnterValue.dropLast(1)
                        moneyET.setText("" + op)
                        moneyET.setSelection(moneyET.text.length)
                    }
                } catch (nfe: NumberFormatException) {
                    nfe.printStackTrace();
                }
                moneyET.addTextChangedListener(this)
            }
        })

        airTimeAmtET.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                airTimeAmtET.removeTextChangedListener(this)
                try {
                    var originalString = s.toString()

                    var longval: Long
                    if (originalString.contains(",")) {
                        originalString = originalString.replace(",", "")
                    }
                    longval = originalString.toLong()
                    var waletBalCrnt = tickerViewBalance.text.toString()
                    if (waletBalCrnt.contains(","))
                        waletBalCrnt = waletBalCrnt.replace(",", "")
                    if (longval <= waletBalCrnt.toLong()) {
                        var formatter: DecimalFormat = NumberFormat.getInstance(Locale.US) as DecimalFormat
                        formatter.applyPattern("#,###,###,###")
                        var formattedString = formatter.format(longval)
                        airTimeAmtET.setText(formattedString);
                        airTimeAmtET.setSelection(airTimeAmtET.text.length)
                    } else {
                        Toast.makeText(this@BillPaymentActivity, "Amount is greater then Wallet Balance", Toast.LENGTH_SHORT).show()
                        var getEnterValue = airTimeAmtET.text.toString()
                        var op: String = getEnterValue.dropLast(1)
                        airTimeAmtET.setText("" + op)
                        airTimeAmtET.setSelection(airTimeAmtET.text.length)

                    }
                } catch (nfe: NumberFormatException) {
                    nfe.printStackTrace();
                }
                airTimeAmtET.addTextChangedListener(this)
            }
        })

        contactVendtechTV.setOnClickListener(View.OnClickListener {

            var intent = Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:+232 79 990 990"));
            startActivity(intent);
        })


        //Check whether user account status is Activie or Pending
        if (SharedHelper.getString(this, Constants.USER_ACCOUNT_STATUS).equals(Constants.STATUS_ACTIVE)) {
            GetAssignedService()
        } else {
            ErrorAccountApproval()
        }

    }


    fun GetWalletBalance() {

        var customDialog: CustomDialog
        customDialog = CustomDialog(this)
        customDialog.show()

        val call: Call<GetWalletModel> = Uten.FetchServerData().get_wallet_balance(SharedHelper.getString(this, Constants.TOKEN))
        call.enqueue(object : Callback<GetWalletModel> {
            override fun onResponse(call: Call<GetWalletModel>, response: Response<GetWalletModel>) {

                if (customDialog.isShowing) {
                    customDialog.dismiss()
                }
                var data = response.body()
                if (data != null) {

                    if (data.status.equals("true")) {

                        totalBalanceTV.setText("NLE : " + data.result.balance)
                        tickerViewBalance.setText(NumberFormat.getNumberInstance(Locale.US).format(data.result.balance.toDouble().toInt()))
                        //tickerViewBalance.setText(Utilities.formatCurrencyValue(data.result.balance))
                        totalAvlblBalance = data.result.balance.toDouble()
                        tickerViewBalance.setText("NLE : " + data.result.balance.toDouble() + "0")
                        countInterface?.CountIs(data.result.unReadNotifications)
                    } else {
                        Utilities.CheckSessionValid(data.message, this@BillPaymentActivity, this@BillPaymentActivity)
                    }
                }
            }

            override fun onFailure(call: Call<GetWalletModel>, t: Throwable) {
                val gs = Gson()
                gs.toJson(t.localizedMessage)
                if (customDialog.isShowing) {
                    customDialog.dismiss()
                }
            }

        })
    }

    override fun onClick(v: View) {

        when (v.id) {

            R.id.paynowTV -> {
                var minVend = SharedHelper.getString(this, Constants.MIN_VEND)
                if(minVend === ""){
                    minVend = "0"
                }
                var moneyValue = moneyET.text.toString().trim().replace(",", "")

                if (TextUtils.isEmpty(autoCompleteTV.text.toString().trim())) {
                    Utilities.shortToast("Please select a meter number.", this)
                } else if (autoCompleteTV.text.toString().length < 11) {
                    Utilities.shortToast("Please enter correct meter number.", this)
                }
                /*else if (TextUtils.isEmpty(selectedMeterID)) {
                    Utilities.shortToast("Please select a correct meter number.", requireActivity())
                } */ else if (TextUtils.isEmpty(moneyET.text.toString().trim())) {
                    Utilities.shortToast("Please enter recharge amount.", this)
                } else if (totalAvlblBalance < 1) {
                    Utilities.shortToast("You don't have enough balance to recharge", this)
                } else if (moneyValue.toDouble() > totalAvlblBalance) {
                    Utilities.shortToast("Recharge amount is greater than the available balance.", this)
                }
                else if (moneyValue.toLong()  <  minVend.toInt()) {
                    Utilities.shortToast("PLEASE TENDER SLL: $minVend & ABOVE", this)
                }

                else {
                    //  ShowAlertForRecharge(moneyValue)  //show alertPopUp
                    showPayCoinfrmLayout()
                }
            }

            R.id.payForAirtimeTV -> {
                var moneyValue = airTimeAmtET.text.toString().trim().replace(",", "")

                Log.v("moneyValue", moneyValue)

                if (TextUtils.isEmpty(airTimeAmtET.text.toString().trim())) {
                    Utilities.shortToast("Please enter recharge amount.", this)
                } else if (totalAvlblBalance < 1) {
                    Utilities.shortToast("You don't have enough balance to recharge", this)
                } else if (moneyValue.toDouble() > totalAvlblBalance) {
                    Utilities.shortToast("Recharge amount is greater than the available balance.", this)
                }
//                else if (moneyValue.toLong()  <  minVend.toInt()) {
//                    Utilities.shortToast("PLEASE TENDER SLL: $minVend & ABOVE", this)
//                }
                else if (TextUtils.isEmpty(phoneET.text.toString())) {
                    Utilities.shortToast("PHONE NUMBER IS REQUIRED", this)
                }
                else if (phoneET.text.toString().length != 8) {
                    Utilities.shortToast("PLEASE ENTER A VALID PHONE NUMBER.", this)
                }
                else {
                    showAirtimePayConfirmLayout()
                }
            }


            R.id.fabBack -> {
                HidePayLayout()
            }
            R.id.atFabBack -> {
                HideAirtimeLayout()
            }

            R.id.confirmFabBack -> {
                HideConfirmLayout()
            }
            R.id.confirmPayCancel->{
                HideConfirmLayout()
            }

            R.id.confirmPayPayBtn->{
                HideConfirmLayout();
                if (Uten.isInternetAvailable(this)){
                    val amt = confirmPayAmtValue.text.toString().trim().replace(",", "")

                    try {
                        DoRecharge(amt, selectedMeterID, posId)
                    } catch (e: UninitializedPropertyAccessException) {
                        selectedMeterID = "";
                        DoRecharge(amt, selectedMeterID, posId)
                    } finally {
                        // optional finally block
                    }

                } else {
                    Utilities.shortToast("No internet connection. Please check your network connectivity.", this)
                }
            }

            R.id.confirmAirtimePayCancel->{
                HideAirtimeConfirmLayout()
            }

            R.id.confirmAirtimePayBtn->{
                HideAirtimeConfirmLayout();
                if (Uten.isInternetAvailable(this)){
                    val amt = airtimeConfirmAmount.text.toString().trim().replace(",", "")
                    val phone = airtimeConfirmPhone.text.toString();
                    BuyAirtime(amt, phone)

                } else {
                    Utilities.shortToast("No internet connection. Please check your network connectivity.", this)
                }
            }


            R.id.electricityLL -> {
                GetMeterList()
            }

            R.id.showListmeterIV -> {
                showMeterListDialog(meterListModels)
            }
        }
    }

    private fun showPayCoinfrmLayout() {

        confirmPayConfrimText.setText("CONFIRM YOUR EDSA ELECTRICITY PURCHASE")
        confirmPayPosID.text="POS ID - "+ posSpinner.selectedItem.toString()
        confirmPayMeterValue.text=autoCompleteTV.text.toString()
        confirmPayAmtValue.text=moneyET.text.toString()

        autoCompleteTV.setText("")
        moneyET.setText("")

        if (payLayout.visibility == View.VISIBLE) {
            payLayout.startAnimation(slide_down)
        }
        payLayout.visibility = View.GONE

        if (payConfirm.visibility == View.GONE) {
            payConfirm.startAnimation(slide_up)
        }
        payConfirm.visibility = VISIBLE


    }

    private fun showAirtimePayConfirmLayout() {

        if(PlatformId == "2")
            airtimeConfirmPayConfirmText.setText("CONFIRM YOUR ORANGE AIRTIME PURCHASE")
        else if(PlatformId == "3")
            airtimeConfirmPayConfirmText.setText("CONFIRM YOUR AFRICELL AIRTIME PURCHASE")
        else
            airtimeConfirmPayConfirmText.setText("CONFIRM YOUR QCELL AIRTIME PURCHASE")
        airtimeConfirmPhone.text=phoneET.text.toString()


        airtimeConfirmAmount.text=airTimeAmtET.text.toString()

        phoneET.setText("")
        airTimeAmtET.setText("")

        if (airtimeLayout.visibility == View.VISIBLE) {
            airtimeLayout.startAnimation(slide_down)
        }
        airtimeLayout.visibility = View.GONE

        if (payAirtimeConfirm.visibility == View.GONE) {
            payAirtimeConfirm.startAnimation(slide_up)
        }
        payAirtimeConfirm.visibility = VISIBLE


    }

    companion object {

        fun newInstance(): BillPaymentActivity {
            return BillPaymentActivity()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun clickedServiceId(platformId: String, disabled: Boolean, msg: String?) {

        PlatformId = platformId
        if (platformId.contentEquals("1")) {
            if(disabled){
                val messageDialog = MessageDialog(this)
                if (msg != null) {
                    messageDialog.showDialog(msg)
                }
            }else{
                GetMeterList()
                GetPosIdList()
            }
        }

        if (platformId.contentEquals("2")) {
            if(disabled){
                val messageDialog = MessageDialog(this)
                if (msg != null) {
                    messageDialog.showDialog(msg)
                }
            }else{
                ShowAirtimePurchaseLayout()
            }
        }
        if (platformId.contentEquals("3")) {
            if(disabled){
                val messageDialog = MessageDialog(this)
                if (msg != null) {
                    messageDialog.showDialog(msg)
                }
            }else{
                ShowAirtimePurchaseLayout()
            }
        }
        if (platformId.contentEquals("4")) {
            if(disabled){
                val messageDialog = MessageDialog(this)
                if (msg != null) {
                    messageDialog.showDialog(msg)
                }
            }else{
                ShowAirtimePurchaseLayout()
            }
        }
    }


    override fun onResume() {

        if (Uten.isInternetAvailable(this)) {
             GetWalletBalance();
            if (payLayout.visibility == View.VISIBLE) {
                GetMeterList()
                GetPosIdList()
            }
        } else {
            NoInternetDialog("No internet connection. Please check your network connectivity.")
        }
        super.onResume()
    }


   /* fun ShowAlertForRecharge(moneyvalue: String) {

        val builder = AlertDialog.Builder(context)
        builder.setTitle(R.string.app_name)
        builder.setMessage("Are you sure to recharge this meter?")
        builder.setIcon(R.drawable.appicon)

        builder.setPositiveButton("Recharge") { dialogInterface, which ->

            if (Uten.isInternetAvailable(requireActivity())) {
                DoRecharge(moneyvalue, selectedMeterID, posId)
            } else {
                Utilities.shortToast("No internet connection. Please check your network connectivity.", requireActivity())
            }

        }
        builder.setNegativeButton("Check Again") { dialogInterface, which ->
        }
        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }*/

    private fun ShowPayLayout() {

        if (serviceLayout.visibility == View.VISIBLE) {
            serviceLayout.startAnimation(slide_down)
        }
        serviceLayout.visibility = View.GONE

        if (payLayout.visibility == View.GONE) {
            payLayout.startAnimation(slide_up)
        }
        payLayout.visibility = View.VISIBLE
    }

    private fun ShowAirtimePurchaseLayout() {

        if (serviceLayout.visibility == View.VISIBLE) {
            serviceLayout.startAnimation(slide_down)
        }
        serviceLayout.visibility = View.GONE

        if (airtimeLayout.visibility == View.GONE) {
            airtimeLayout.startAnimation(slide_up)
        }
        airtimeLayout.visibility = View.VISIBLE

    }

    private fun HidePayLayout() {

        if (payLayout.visibility == View.VISIBLE) {
            payLayout.startAnimation(slide_down)
        }
        payLayout.visibility = View.GONE
        autoCompleteTV.setText("")
        moneyET.setText("")

        if (serviceLayout.visibility == View.GONE) {
            serviceLayout.startAnimation(slide_up)
        }
        serviceLayout.visibility = View.VISIBLE

    }


    private fun HideAirtimeLayout() {

        if (airtimeLayout.visibility == View.VISIBLE) {
            airtimeLayout.startAnimation(slide_down)
        }
        airtimeLayout.visibility = View.GONE

        autoCompleteTV.setText("")
        moneyET.setText("")

        if (serviceLayout.visibility == View.GONE) {
            serviceLayout.startAnimation(slide_up)
        }
        serviceLayout.visibility = View.VISIBLE

    }

    fun HideConfirmLayout() {
        if (payConfirm.visibility == View.VISIBLE) {
            payConfirm.startAnimation(slide_down);
        }
        payConfirm.visibility = View.GONE;
        ShowPayLayout();
    }
    fun HideAirtimeConfirmLayout() {
        if (payAirtimeConfirm.visibility == View.VISIBLE) {
            payConfirm.startAnimation(slide_down);
        }
        payAirtimeConfirm.visibility = View.GONE;
        ShowAirtimePurchaseLayout();
    }

    fun GetPosIdList() {
        val call = Uten.FetchServerData().getPosList(SharedHelper.getString(this, Constants.TOKEN))
        call.enqueue(object : Callback<PosResultModel> {
            override fun onFailure(call: Call<PosResultModel>, t: Throwable) {
                val gs = Gson()
                gs.toJson(t.localizedMessage)
                Utilities.shortToast("Something went wrong", this@BillPaymentActivity)
            }

            override fun onResponse(call: Call<PosResultModel>, response: Response<PosResultModel>) {
                if (response != null) {
                    if (response?.body() != null) {
                        if (response?.body()?.status == "true") {
                            if (response.body()?.result?.size!! > 0) {
                                SetOnSpinner(response.body()?.result!!)
                            }
                        }
                    }
                }
            }

        })
    }

    private fun SetOnSpinner(result: List<PosResultModel.Result>) {
        posList.clear();
        posList.addAll(result)
        val list = ArrayList<String>()
        result.forEach {
            list.add(it.serialNumber)
        }

        if (this!=null) {
            val adapter = ArrayAdapter<String>(this, R.layout.item_pos_large, list)
            adapter.setDropDownViewResource(R.layout.sppiner_layout_item)
            posSpinner.adapter = adapter
            posSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(p0: AdapterView<*>?) {

                }

                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    balance = posList.get(p2).balance;
                    posId = posList.get(p2).posId.toString();
                    Log.e("balance+pos", "$balance $posId");
                    tvPosNumber.setText("POS ID : " + posList.get(p2).serialNumber);
                    tickerViewBalance.setText(balance);
                }
            }
        }
    }

    fun GetMeterList() {

        var customDialog: CustomDialog
        customDialog = CustomDialog(this)
        customDialog.show()
        var vv = (SharedHelper.getString(this, Constants.TOKEN))
        val call: Call<GetMetersModel> = Uten.FetchServerData().get_meters(SharedHelper.getString(this, Constants.TOKEN), "1", "50")
        call.enqueue(object : Callback<GetMetersModel> {

            override fun onResponse(call: Call<GetMetersModel>, response: Response<GetMetersModel>) {
                var data = response.body()
                if (data != null) {
                    if (data.status.equals("true")) {
                       /* if (data.result.size > 0) {
                            ShowPayLayout()
                            //   meterListModels.clear()
                            if (meterListModels.size > 0) {
                                meterListModels.clear()
                            }
                            meterListModels.addAll(data.result);
                            //SetAutoCompleteData();
                        } else {
                            Utilities.shortToast("No meter found", requireActivity());
                        }*/

                        ShowPayLayout()
                        //   meterListModels.clear()
                        if (meterListModels.size > 0) {
                            meterListModels.clear()
                        }
                        meterListModels.addAll(data.result);
                    } else {
                        Utilities.CheckSessionValid(data.message, this@BillPaymentActivity, this@BillPaymentActivity)
                    }
                }
                if (customDialog.isShowing) {
                    customDialog.dismiss()
                }
            }

            override fun onFailure(call: Call<GetMetersModel>, t: Throwable) {

                val gs = Gson()
                gs.toJson(t.localizedMessage)
                if (customDialog.isShowing) {
                    customDialog.dismiss()
                }
                Utilities.shortToast("Something went wrong", this@BillPaymentActivity)
            }


        })
    }


    fun DoRecharge(amount: String, meterId: String, posId: String) {

        var customDialog: CustomDialog;
        customDialog = CustomDialog(this);
        customDialog.show();
        var meterNumber: String? = null;
        var meterid: String? = null;
        if (meterId.isEmpty()) {
            meterNumber = confirmPayMeterValue.text.toString().trim()
            meterid = null
        } else {
            meterid = meterId
            meterNumber = null
        }
        //val call: Call<RechargeMeterModel> = Uten.FetchServerData().rechargeMeter(SharedHelper.getString(requireActivity(), Constants.TOKEN), meterid, amount, meterNumber, posId,SharedHelper.getString(activity!!, Constants.PASS_CODE_VALUE))
        val call: Call<RechargeMeterModel> = Uten.FetchServerData().rechargeMeter(SharedHelper.getString(this, Constants.TOKEN),amount,meterId,posId, meterNumber.toString())
        call.enqueue(object : Callback<RechargeMeterModel> {
            override fun onResponse(call: Call<RechargeMeterModel>, response: Response<RechargeMeterModel>) {
                if (customDialog.isShowing) {
                    customDialog.dismiss();
                }

                var data = response.body();
                if (data!=null){
                    if (data.message !=null) {
                        Utilities.shortToast(data.message, this@BillPaymentActivity);
                    }else {
                        if (data.status.equals("true")) {
                            if (data!=null){
                                serviceLayout.visibility= VISIBLE;
                                payLayout.visibility= GONE;
                            }
                            var intent = Intent(this@BillPaymentActivity, PrintScreenActivity::class.java);
                            intent.putExtra("data", response.body());
                            startActivityForResult(intent,Constants.REQUEST_CODE);

                        } else {
                            Utilities.CheckSessionValid(data.message, this@BillPaymentActivity, this@BillPaymentActivity);
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
                Utilities.shortToast("Something went wrong", this@BillPaymentActivity)
            }
        })
    }

    fun BuyAirtime(amount: String, phone: String) {

        var customDialog: CustomDialog;
        customDialog = CustomDialog(this);
        customDialog.show();

        val call: Call<AirtimeRechargeModel> = Uten.FetchServerData().buyAirtime(
            SharedHelper.getString(this, Constants.TOKEN),amount,PlatformId,
            SharedHelper.getString(this, Constants.POS_NUMBER),
            phone, SharedHelper.getString(this, Constants.USER_ID), "SLE")
        call.enqueue(object : Callback<AirtimeRechargeModel> {
            override fun onResponse(call: Call<AirtimeRechargeModel>, response: Response<AirtimeRechargeModel>) {
                if (customDialog.isShowing) {
                    customDialog.dismiss();
                }

                var data = response.body();
                if (data!=null){
                    if (data.message !=null) {
                        Utilities.shortToast(data.message!!, this@BillPaymentActivity);
                    }else {
                        if (data.status.equals("true")) {
                            if (data!=null){
                                airtimeLayout.visibility= GONE;
                            }
                            Utilities.shortToast("Successful Recharged", this@BillPaymentActivity);
                            var intent = Intent(this@BillPaymentActivity, AirtimePurchaseSuccess::class.java);
                            intent.putExtra("data", response.body());
                            startActivityForResult(intent, Constants.REQUEST_CODE);
                        } else {
                            Utilities.CheckSessionValid(data.message!!, this@BillPaymentActivity, this@BillPaymentActivity);
                        }
                    }
                }
            }

            override fun onFailure(call: Call<AirtimeRechargeModel>, t: Throwable) {
                val gs = Gson()
                gs.toJson(t.localizedMessage)
                if (customDialog.isShowing) {
                    customDialog.dismiss()
                }
                Utilities.shortToast("Something went wrong", this@BillPaymentActivity)
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode==Constants.REQUEST_CODE){
            if (resultCode== Activity.RESULT_OK){
                ShowPayLayout();
            }
        }

        //Toast.makeText(activity, "OnactivityResult", Toast.LENGTH_SHORT).show();
    }

    fun SetAutoCompleteData() {

        val adapter = NumberListAutoCompleteAdapter(this, android.R.layout.simple_list_item_1, meterListModels)
        autoCompleteTV.setAdapter(adapter)
        autoCompleteTV.threshold = 1
        autoCompleteTV.setOnItemClickListener() { parent, _, position, id ->
            val selectedPoi = parent.adapter.getItem(position) as MeterListResults?
            // autoCompleteTV.setText(selectedPoi?.number.toString())
            //selectedMeterID = selectedPoi?.meterId.toString();

            //Toast.makeText(activity!!,"--"+selectedMeterID,Toast.LENGTH_LONG).show();

        }
    }


    fun GetAssignedService() {

        var customDialog: CustomDialog
        customDialog = CustomDialog(this)
        customDialog.show()

        val call: Call<UserAssignedServicesModel> = Uten.FetchServerData().user_assigned_services(SharedHelper.getString(this, Constants.TOKEN))
        call.enqueue(object : Callback<UserAssignedServicesModel> {
            override fun onResponse(call: Call<UserAssignedServicesModel>, response: Response<UserAssignedServicesModel>) {

                if (customDialog.isShowing) {
                    customDialog.dismiss()
                }
                var data = response.body()
                if (data != null) {
                    if (data.status.equals("true")) {
                        Log.v("AssignedServices", Gson().toJson(data.result))
                        if (data.result.size > 0) {
                            servicesRecyclerview.visibility = View.VISIBLE
                            errorAccountService.visibility = View.GONE
                            UpdateServiceAdapter(data.result)
                        } else {
                            ErrorServiceAssigned()
                        }
                    }else{
                        Utilities.CheckSessionValid(data.message, this@BillPaymentActivity, this@BillPaymentActivity)
                    }
                }
            }

            override fun onFailure(call: Call<UserAssignedServicesModel>, t: Throwable) {
                val gs = Gson()
                gs.toJson(t.localizedMessage)
                if (customDialog.isShowing) {
                    customDialog.dismiss()
                }
                Utilities.shortToast("Something went wrong", this@BillPaymentActivity)
            }
        })
    }

    fun UpdateServiceAdapter(data: MutableList<UserServicesResult>) {

        servicesAdapter = UserServicesAdapter(this, data, this)
        servicesRecyclerview.adapter = servicesAdapter
        servicesRecyclerview.layoutManager = GridLayoutManager(this, 2)
        servicesRecyclerview.setHasFixedSize(true)
        servicesAdapter.notifyDataSetChanged()


    }

    private fun NoInternetDialog(msg: String) {

        val dialog = AlertDialog.Builder(this)
        dialog.setMessage(msg)
                .setPositiveButton("OK") { paramDialogInterface, paramInt ->
                    //  permissionsclass.requestPermission(type,code);
                    startActivity(Intent(Settings.ACTION_SETTINGS))
                }
        dialog.setCancelable(false)
        dialog.show()
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this);
    }

    // UI updates must run on MainThread
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onEvent(event: MessageEvent) {
        if (event.message.equals("update_balance")) {
            GetWalletBalance()
        }
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this);
    }

    fun ErrorServiceAssigned() {

        servicesRecyclerview.visibility = View.GONE
        errorDashTV.text = "Status: ${resources.getString(R.string.service_not_assigned)}"
        errorAccountService.visibility = View.VISIBLE
    }


    fun ErrorAccountApproval() {

        servicesRecyclerview.visibility = View.GONE
        errorDashTV.text = "Status: ${resources.getString(R.string.account_under_approval)}"
        errorAccountService.visibility = View.VISIBLE
    }

    private fun showMeterListDialog(list: MutableList<MeterListResults>) {

        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_select_meter)
        val cancel = dialog.findViewById(R.id.cancelDialog) as AppCompatTextView
        val recyclerview = dialog.findViewById(R.id.recyclerviewMeter) as RecyclerView
        val mLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        meterDialogAdapter = NumberListDialogAdapter(list, this, this)
        recyclerview.adapter = meterDialogAdapter
        recyclerview.layoutManager = mLayoutManager
        recyclerview.setHasFixedSize(true)
        meterDialogAdapter.notifyDataSetChanged()

        dialogMain = dialog
        cancel.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()

    }


    override fun meterId(id: String, name: String) {


        IsSelectFromMeterList = true
        selectedMeterID = id
        autoCompleteTV.setText(name)
        dialogMain.dismiss()



    }

    interface NotificationCount {

        fun CountIs(count: String)
    }

}
