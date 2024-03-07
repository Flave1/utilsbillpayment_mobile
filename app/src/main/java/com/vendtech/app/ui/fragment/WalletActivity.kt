package com.vendtech.app.ui.fragment

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.Window
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.robinhood.ticker.TickerUtils
import com.robinhood.ticker.TickerView
import com.vendtech.app.R
import com.vendtech.app.adapter.transactions.DepositTransactionAdapter
import com.vendtech.app.adapter.transactions.RechargeTransactionAdapter
import com.vendtech.app.adapter.wallet.AccountAdapter
import com.vendtech.app.helper.SharedHelper
import com.vendtech.app.models.meter.BankResponseModel
import com.vendtech.app.models.meter.PosResultModel
import com.vendtech.app.models.profile.GetWalletModel
import com.vendtech.app.models.transaction.*
import com.vendtech.app.network.Uten
import com.vendtech.app.ui.activity.home.HomeActivity
import com.vendtech.app.utils.Constants
import com.vendtech.app.utils.CustomDialog
import com.vendtech.app.utils.Utilities
import kotlinx.android.synthetic.main.fragment_wallet.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class WalletActivity : Activity(), View.OnClickListener, DatePickerDialog.OnDateSetListener {


    lateinit var addBalanceTV: TextView
    lateinit var tranhistoryTV: TextView
    internal var isFirstLaunch = true


    //ADD BALANCE LAYOUT
    lateinit var addBalanceLayout: ScrollView


    //TRANSACTION HISTORY LAYOUT
    lateinit var transactionHistoryLayout: LinearLayout
    lateinit var fragment_frame: FrameLayout
    lateinit var depositText: TextView
    lateinit var linedeposit: View
    lateinit var depositTRL: RelativeLayout
    lateinit var rechargeText: TextView
    lateinit var linerecharge: View
    lateinit var rechargeTRL: RelativeLayout
    lateinit var et_date: TextView


    //ANIMATION
    lateinit var slide_in: Animation
    lateinit var slide_out: Animation


    //ADD DEPOSIT LAYOUT
    lateinit var spAccounts: Spinner
    lateinit var typeSpinner: Spinner
    lateinit var bankNameSpinner: Spinner
    lateinit var spPosId: Spinner
    lateinit var selectPaytype: RelativeLayout
    var transactionMode = 1

    lateinit var sendNowTV: TextView

    // lateinit var vendornameET: EditText
    lateinit var chxslipET: EditText
    lateinit var depositamountET: EditText
    lateinit var commentET: EditText
    lateinit var plusPercentET: TextView
    lateinit var banknameTV: TextView
    lateinit var accnameTV: TextView
    lateinit var accnumberTV: TextView
    lateinit var accbbanTV: TextView

    // lateinit var commissionLL: LinearLayout
    // lateinit var tvCommisionPercentage: TextView
    lateinit var tvPosNumber: TextView
    lateinit var tvWalletBalance: TextView
    lateinit var totalBalanceTV: TextView
    lateinit var tickerViewBalance: TickerView
    lateinit var commissionPercent: TextView
    lateinit var chequeLayout: LinearLayout
    lateinit var tv_value_date: TextView



    //  lateinit var bankName: EditText
    lateinit var chequeName: EditText

    //RECHARGE TRANSACTION AND DEPOSIT TRANSACTION LAYOUTS
    var bankName = ""
    var typeName = ""
    lateinit var recyclerviewRecharge: RecyclerView
    lateinit var nodataRecharge: TextView
    lateinit var recyclerviewDeposit: RecyclerView
    lateinit var nodataDeposit: TextView
    internal var rechargeListModel: MutableList<RechargeTransactionNewListModel.Result> = java.util.ArrayList()
    internal var depositListModel: MutableList<DepositTransactionNewListModel.Result> = java.util.ArrayList()
    lateinit var rechargetransAdapter: RechargeTransactionAdapter
    lateinit var deposittransAdapter: DepositTransactionAdapter
    var pageRecharge = 1
    var pageDeposit = 1
    var totalItemsNo = 15
    var percentage = 0.0
    var posId = 0
    var bankAccountId = "0"

    //ANIMATION
    lateinit var slide_up: Animation
    lateinit var slide_down: Animation


    //Pagination recharges
    var loadings_r = true
    var pastVisiblesItems_r = 0
    var visibleItemCount_r = 0
    var totalItemCount_r = 0


    //Pagination deposit
    var loadings_d = true
    var pastVisiblesItems_d = 0
    var visibleItemCount_d = 0
    var totalItemCount_d = 0
    var posList = ArrayList<PosResultModel.Result>()
    var depositType = ""

//    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
//        val view = inflater.inflate(R.layout.fragment_wallet, container, false)
//
//        findviews(view)
//        SetDepositLayout()
//        GetBankDetails()
//        GetPosIdList()
//        GetBankNames()
//        //  GetBankDetails()
//
//        return view
//    }

     override fun onCreate(savedInstanceState: Bundle?) {
         super.onCreate(savedInstanceState)
            setContentView(R.layout.fragment_wallet)
            findviews()
            SetDepositLayout()
            GetBankDetails()
            GetPosIdList()
            GetBankNames()
            GetPaymentTypes()

        //  GetBankDetails()
         val imgBack = findViewById<ImageView>(R.id.imgBack)
         imgBack.setOnClickListener{
             onBackPressed()
         }

    }

    private fun GetPaymentTypes() {
        var customDialog: CustomDialog
        customDialog = CustomDialog(this)
        customDialog.show()
        val call = Uten.FetchServerData().getPaymentTypes(SharedHelper.getString(this, Constants.TOKEN))
        call.enqueue(object : Callback<PaymentTypeModel> {
            override fun onFailure(call: Call<PaymentTypeModel>, t: Throwable) {
                val gs = Gson()
                gs.toJson(t.localizedMessage)
                if (customDialog.isShowing) {
                    customDialog.dismiss()
                }
            }

            override fun onResponse(call: Call<PaymentTypeModel>, response: Response<PaymentTypeModel>) {
                if (customDialog.isShowing) {
                    customDialog.dismiss()
                }
                var data = response.body()
                if (data != null) {
                    if (data.status.equals("true")) {
                        if (data.result.size > 0) {
                            setPaymentTypeSpinner(data.result)
                        }

                    }
                }

            }

            private fun setPaymentTypeSpinner(result: List<PaymentTypeModel.Result>) {
                val listItems = ArrayList<String>()
                result.forEach {
                    listItems.add(it.text)
                }
                val adapter = ArrayAdapter<String>(this@WalletActivity, R.layout.spinner_text_second, listItems)
                adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown)
                typeSpinner.adapter = adapter
                typeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

                    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                        transactionMode = result.get(p2).value.toInt()
                        if (transactionMode == 1) {
                            transactionMode = 1
                            chequeLayout.visibility = View.GONE
                            val sdf = SimpleDateFormat("dd/MM/yyyy")
                            val currentDateandTime: String = sdf.format(Date())
                            et_date.setText(currentDateandTime);

                            ll_date.visibility=View.INVISIBLE
                            tv_value_date.visibility=View.GONE

                        } else if (transactionMode==2) {
                            transactionMode = 2
                            chequeLayout.visibility = View.VISIBLE;
                            ll_date.visibility=View.VISIBLE
                            tv_value_date.visibility=View.VISIBLE
                        }else if (transactionMode==3){
                            transactionMode=3;
                            chequeLayout.visibility = View.GONE
                            val sdf = SimpleDateFormat("dd/MM/yyyy")
                            val currentDateandTime: String = sdf.format(Date())
                            et_date.setText(currentDateandTime);
                            ll_date.visibility=View.INVISIBLE;
                            tv_value_date.visibility=View.GONE;
                        }else if (transactionMode==4){
                            transactionMode=4;
                            chequeLayout.visibility = View.GONE
                            val sdf = SimpleDateFormat("dd/MM/yyyy")
                            val currentDateandTime: String = sdf.format(Date())
                            et_date.setText(currentDateandTime);
                            ll_date.visibility=View.INVISIBLE;
                            tv_value_date.visibility=View.GONE;
                        }
                    }



                    override fun onNothingSelected(parent: AdapterView<*>?) {
                    }
                }

            }
        })
    }

    private fun GetBankNames() {
        var customDialog: CustomDialog
        customDialog = CustomDialog(this)
        customDialog.show()
        val call = Uten.FetchServerData().getBankNames(SharedHelper.getString(this, Constants.TOKEN))
        call.enqueue(object : Callback<BankNameModel> {
            override fun onFailure(call: Call<BankNameModel>, t: Throwable) {
                val gs = Gson()
                gs.toJson(t.localizedMessage)
                if (customDialog.isShowing) {
                    customDialog.dismiss()
                }
            }

            override fun onResponse(call: Call<BankNameModel>, response: Response<BankNameModel>) {
                if (customDialog.isShowing) {
                    customDialog.dismiss()
                }
                var data = response.body()
                if (data != null) {
                    if (data.status.equals("true")) {
                        if (data.result.size > 0) {
                            setBankSpinner(data.result)
                        }

                    }
                }

            }

            private fun setBankSpinner(result: List<BankNameModel.Result>) {
                val bank = ArrayList<String>()
                result.forEach {
                    bank.add(it.text)
                }
                val adapter = ArrayAdapter<String>(this@WalletActivity, R.layout.spinner_text_second, bank)
                adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown)
                bankNameSpinner.adapter = adapter
                bankNameSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onNothingSelected(p0: AdapterView<*>?) {

                    }

                    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                        bankName = result.get(p2).value
                    }

                }
            }
        })
    }



    fun findviews() {

        spAccounts = findViewById(R.id.spAccounts) as Spinner
        spPosId = findViewById(R.id.spPosId) as Spinner
        bankNameSpinner = findViewById(R.id.bankNameSpinner) as Spinner
        // addBalanceTV = view.findViewById<View>(R.id.addBalanceTV) as TextView
//        tranhistoryTV = view.findViewById<View>(R.id.tranhistoryTV) as TextView
        chequeLayout = findViewById(R.id.chequeLayout)
        chequeName = findViewById(R.id.chequeNameET)
        // bankName=view.findViewById(R.id.bankNameET)
        slide_down = AnimationUtils.loadAnimation(this, R.anim.slide_down)
        slide_up = AnimationUtils.loadAnimation(this, R.anim.slide_up)
        totalBalanceTV = findViewById<View>(R.id.totalBalanceTV) as TextView
        tvWalletBalance = findViewById(R.id.totalBalanceTV);
        tickerViewBalance = findViewById<View>(R.id.tickerView) as TickerView

        val font = Typeface.createFromAsset(assets, "fonts/roboto_bold.ttf")
        tickerViewBalance.typeface = font
        tickerViewBalance.setCharacterLists(TickerUtils.provideNumberList());
        commissionPercent = findViewById(R.id.commissionPercentTV)
        tv_value_date = findViewById(R.id.tv_value_date)

        //ADD BALANCE LAYOUT
        addBalanceLayout = findViewById<View>(R.id.layoutAddBalance) as ScrollView

        //TRANSACTION HISTORY LAYOUT
        transactionHistoryLayout = findViewById<View>(R.id.layoutTransactionHistory) as LinearLayout
        depositText = findViewById<View>(R.id.depositText) as TextView
        rechargeText = findViewById<View>(R.id.rechargeText) as TextView
        linedeposit = findViewById<View>(R.id.linedeposit) as View
        linerecharge = findViewById<View>(R.id.linerecharge) as View
        depositTRL = findViewById<View>(R.id.deposItTRL) as RelativeLayout
        rechargeTRL = findViewById<View>(R.id.rechargeTRL) as RelativeLayout
        et_date = findViewById<View>(R.id.et_date) as TextView

        rechargeTRL.setOnClickListener(this)
        depositTRL.setOnClickListener(this)
        et_date.setOnClickListener(this)

        //RECHARGE TRANSACTION AND DEPOSIT TRANSACTION LAYOUT
        recyclerviewDeposit = findViewById(R.id.recyclerviewDepositss)
        recyclerviewRecharge = findViewById(R.id.recyclerviewRechargess)
        nodataDeposit = findViewById(R.id.nodataDeposit)
        nodataRecharge = findViewById(R.id.nodataRecharge)

        slide_in = AnimationUtils.loadAnimation(this, R.anim.slide_in)
        slide_out = AnimationUtils.loadAnimation(this, R.anim.activity_back_out)

        //Deposit layout
        typeSpinner = findViewById<View>(R.id.typeSpinner) as Spinner
        selectPaytype = findViewById<View>(R.id.selectPaytype) as RelativeLayout
//        tvPosNumber = view.findViewById(R.id.tvPosNumber) as TextView
        sendNowTV = findViewById<View>(R.id.sendnowTV) as TextView
        // vendornameET = view.findViewById<View>(R.id.vendornameET) as EditText
        chxslipET = findViewById<View>(R.id.chxslipET) as EditText
        depositamountET = findViewById<View>(R.id.depositamountET) as EditText
        plusPercentET = findViewById<View>(R.id.plusPercentET) as TextView
        commentET = findViewById<View>(R.id.commentET) as EditText
        //  commissionLL = view.findViewById(R.id.commissionLL) as LinearLayout
        //tvCommisionPercentage = view.findViewById(R.id.tvCommisionPercentage) as TextView
        /*  lateinit var banknameTV:TextView
    lateinit var accnameTV:TextView
    lateinit var accnumberTV:TextView
    lateinit var accbbanTV:TextView


*/
        //  banknameTV = view.findViewById<View>(R.id.banknameTV) as TextView
        //  accnameTV = view.findViewById<View>(R.id.accnameTV) as TextView
        //   accnumberTV = view.findViewById<View>(R.id.accnumberTV) as TextView
        //   accbbanTV = view.findViewById<View>(R.id.accbbanTV) as TextView

        //addBalanceTV.setOnClickListener(this)
        //tranhistoryTV.setOnClickListener(this)
        selectPaytype.setOnClickListener(this)



        val sdf = SimpleDateFormat("dd/MM/yyyy")
        val currentDateandTime: String = sdf.format(Date())
        et_date.setText(currentDateandTime)


        slide_in = AnimationUtils.loadAnimation(this, R.anim.slide_in)
        slide_out = AnimationUtils.loadAnimation(this, R.anim.activity_back_out)





    }


    fun SetDepositLayout() {

        //SetSpinnerData()
        SetDepositTextChangeListener()
        sendNowTV.setOnClickListener(View.OnClickListener {
            /*  if (TextUtils.isEmpty(vendornameET.text.toString().trim())) {
                  Utilities.shortToast("Enter vendor name", requireActivity())
              } else */
            if (transactionMode == 2) {
                /* if(TextUtils.isEmpty(bankName.text.toString())){
                     Utilities.shortToast("Enter your bank name", requireActivity())
                 }
                 else*/ if (TextUtils.isEmpty(chequeName.text.toString())) {
                         Utilities.shortToast("Enter the name on Cheque", this@WalletActivity)

                }

            }


            if (TextUtils.isEmpty(chxslipET.text.toString().trim())) {
               // Utilities.shortToast(""+transactionMode,requireActivity())
                if (transactionMode == 1) {
                    Utilities.shortToast("Enter slip id", this@WalletActivity)
                } else if (transactionMode==3){
                    Utilities.shortToast("Enter slip id", this@WalletActivity)
                }else {
                    Utilities.shortToast("Enter cheque id", this@WalletActivity)
                }
            } else if (TextUtils.isEmpty(depositamountET.text.toString().trim())) {
                Utilities.shortToast("Enter deposit amount", this@WalletActivity)
            }
            /*else if (TextUtils.isEmpty(commentET.text.toString().trim())) {
                Utilities.shortToast("Please type some comment", requireActivity())
            }*/ else {
                showConfirmationDialog(depositamountET.text.toString())
//                DoDeposit(0);
               // Utilities.shortToast(""+et_date.text.toString(), requireActivity())
            }
        })

    }
    fun showConfirmationDialog(amount: String) {
        val builder = AlertDialog.Builder(this)
        var code = SharedHelper.getString(this, Constants.CURRENCY_CODE);
        builder.setTitle("Deposit Confirmation Alert")
            .setMessage("DO YOU WANT TO ADD ${code} ${amount} TO YOUR WALLET?")
            .setPositiveButton("Continue") { dialog, which ->
                DoDeposit(0)
            }
            .setNegativeButton("Cancel") { dialog, which ->
                // User clicked cancel
                // Add your code to handle the cancel action here
            }
            .show()
    }

    private fun SetDepositTextChangeListener() {
        depositamountET.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                depositamountET.removeTextChangedListener(this);

                try {
                    var originalString = s.toString();
                    var plusPercent = s.toString()
                    var longval: Long;
                    if (originalString.contains(",")) {
                        originalString = originalString.replace(",", "");
                    }
                    longval = originalString.toLong()
                    var formatter = NumberFormat.getNumberInstance(Locale.US);
                    // formatter.applyPattern("#,###,###,###");
                    var formattedString = formatter.format(longval);
                    depositamountET.setText(formattedString);
                    depositamountET.setSelection(depositamountET.text.length);
                    if (s.toString().length > 0) {
                        if (plusPercentET.text.toString().contains(",")) {
                            plusPercent = plusPercent.replace(",", "")
                        }
                        plusPercentET.setText(getPercentage(plusPercent?.toLong()!!))
                    } else plusPercentET.setText("")

                } catch (nfe: NumberFormatException) {
                    nfe.printStackTrace();
                    plusPercentET.setText("")

                }
                depositamountET.addTextChangedListener(this);
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
/*
if(p0?.length!!>0){
    NumberFormat.getNumberInstance(Locale.US).format(p0?.toString().toInt())
    plusPercentET.setText(getPercentage(p0?.toString()?.toInt()))
}
                else plusPercentET.setText("")
            }
*/
            }

        })
    }

    private fun getPercentage(p0: Long): String {
        val percent = (p0 * percentage) / 100

        Log.e("percent", percent.toString())
        val number = p0 + percent.toLong()

        return NumberFormat.getNumberInstance(Locale.US).format(number)
    }

    fun DoDeposit(continueDepoit: Int) {
        var customDialog: CustomDialog
        customDialog = CustomDialog(this@WalletActivity)
        customDialog.show()
        var bankname: String? = null
        var nameOnCheque: String? = null
        if (transactionMode == 1) {
            depositType = transactionMode.toString() // "Cash"
            bankname = null
            nameOnCheque = null
            val sdf = SimpleDateFormat("dd/MM/yyyy")
            val currentDateandTime: String = sdf.format(Date())
            et_date.setText(currentDateandTime)
        } else if (transactionMode==2){
            depositType =  transactionMode.toString()//"Cheque"
            nameOnCheque = chequeName.text.toString()
        }else if (transactionMode==4){
            depositType = transactionMode.toString()// "Transfer"
            nameOnCheque = chequeName.text.toString()
            val sdf = SimpleDateFormat("dd/MM/yyyy")
            val currentDateandTime: String = sdf.format(Date())
            et_date.setText(currentDateandTime)
        }else{
            depositType = transactionMode.toString()// "Purchase Order"
            bankname = null
            nameOnCheque = null
            val sdf = SimpleDateFormat("dd/MM/yyyy")
            val currentDateandTime: String = sdf.format(Date())
            et_date.setText(currentDateandTime)
        }
        var depositAmount = ""
        var plusPercentAmount = ""
        if (depositamountET.text.toString().contains(",")) {
            depositAmount = depositamountET.text.toString().replace(",", "");
        } else {
            depositAmount = depositamountET.text.toString()
        }
        if (plusPercentET.text.toString().contains(",")) {
            plusPercentAmount = plusPercentET.text.toString().replace(",", "");
        } else {
            plusPercentAmount = plusPercentET.text.toString().trim()
        }
//        Toast.makeText(activity, ""+et_date.text.toString(), Toast.LENGTH_SHORT).show();
      val call: Call<DepositRequestModel> = Uten.FetchServerData().deposit_request(SharedHelper.getString(this@WalletActivity,
            Constants.TOKEN),
              posId,
              bankAccountId,
              depositType,
              chxslipET.text.toString().trim(),
              bankname,
              nameOnCheque,
              depositAmount,
              plusPercentAmount,
              et_date.text.toString(),
              continueDepoit)
        call.enqueue(object : Callback<DepositRequestModel> {
            override fun onResponse(call: Call<DepositRequestModel>, response: Response<DepositRequestModel>) {

                if (customDialog.isShowing) {
                    customDialog.dismiss()
                }
                var data = response.body()
                if (data != null) {
                    if (data.status.equals("true")) {
                        if(data.message != Constants.DEPOSIT_SAVED){
                            showdialog(data.message)
                        }else{
                            Utilities.longToast("FUNDS ADDED TO WALLET\n" +
                                    "APPROVAL ALERT WILL BE SENT SHORTLY", this@WalletActivity)
                            ResetLayoutAddDeposit()

                            Handler().postDelayed({
                                val i = Intent(this@WalletActivity, HomeActivity::class.java)
                                startActivity(i)
                                overridePendingTransition(R.anim.slide_in, R.anim.slide_out)
                            }, 400)
                        }

                    } else {
                        Utilities.CheckSessionValid(data.message, this@WalletActivity, this@WalletActivity)

                    }
                }
            }

            override fun onFailure(call: Call<DepositRequestModel>, t: Throwable) {
                val gs = Gson()
                gs.toJson(t.localizedMessage)
                if (customDialog.isShowing) {
                    customDialog.dismiss()
                }
            }

        })
    }

    fun bindAccountSp(list: List<BankResponseModel.Result>) {
        val accountAdapter = AccountAdapter(this, list as MutableList<BankResponseModel.Result>)
        spAccounts.adapter = accountAdapter
        spAccounts.setOnItemSelectedListener(object : OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>, view: View,
                                        position: Int, id: Long) {
                val item = adapterView.getItemAtPosition(position) as BankDetailResult
                if (item != null) {
                    bindAccountDetails(item);
                }
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {

            }
        })
    }

    fun bindAccountDetails(bankDetails: BankDetailResult?) {
        banknameTV.text = bankDetails?.bankName
        accnameTV.text = bankDetails?.accountName
        accnumberTV.text = bankDetails?.accountNumber
        accbbanTV.text = bankDetails?.bban
    }

    fun ResetLayoutAddDeposit() {

        vendornameET.setText("")
        chxslipET.setText("")
        depositamountET.setText("")
        //  bankName.setText("")
        chequeName.setText("")
        // commentET.setText("")
       // SetSpinnerData()

    }


    fun SetSpinnerData() {

        val list: MutableList<String> = ArrayList()
        list.add("Cash")
        list.add("Cheque")
        list.add("Purchase Order")
        list.add("Transfer")

        val adapter = ArrayAdapter<String>(this@WalletActivity, R.layout.spinner_text_second, list)
        adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown)
        typeSpinner.setAdapter(adapter)

        typeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                //Toast.makeText(activity, "Country ID: " +position , Toast.LENGTH_SHORT).show()
                if (position == 0) {
                    transactionMode = 1
                    chequeLayout.visibility = View.GONE
                    val sdf = SimpleDateFormat("dd/MM/yyyy")
                    val currentDateandTime: String = sdf.format(Date())
                    et_date.setText(currentDateandTime);

                    ll_date.visibility=View.INVISIBLE
                    tv_value_date.visibility=View.GONE

                } else if (position==1) {
                    transactionMode = 2
                    chequeLayout.visibility = View.VISIBLE;
                    ll_date.visibility=View.VISIBLE
                    tv_value_date.visibility=View.VISIBLE
                }else if (position==2){
                    transactionMode=3;
                    chequeLayout.visibility = View.GONE
                    val sdf = SimpleDateFormat("dd/MM/yyyy")
                    val currentDateandTime: String = sdf.format(Date())
                    et_date.setText(currentDateandTime);
                    ll_date.visibility=View.INVISIBLE;
                    tv_value_date.visibility=View.GONE;
                }else if (position==3){
                    transactionMode=4;
                    chequeLayout.visibility = View.GONE
                    val sdf = SimpleDateFormat("dd/MM/yyyy")
                    val currentDateandTime: String = sdf.format(Date())
                    et_date.setText(currentDateandTime);
                    ll_date.visibility=View.INVISIBLE;
                    tv_value_date.visibility=View.GONE;
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

    }

    fun SelectAddBalance() {

        addBalanceTV.setTextColor(resources.getColor(R.color.colorblack))
        addBalanceTV.background = resources.getDrawable(R.drawable.yellow_chooser_left)
        tranhistoryTV.setTextColor(resources.getColor(R.color.colorlightgrey))
        tranhistoryTV.background = resources.getDrawable(R.drawable.grey_chooser_right)


        if (transactionHistoryLayout.visibility == View.VISIBLE) {
            transactionHistoryLayout.startAnimation(slide_out)
        }
        transactionHistoryLayout.visibility = View.GONE


        if (addBalanceLayout.visibility == View.GONE) {
            addBalanceLayout.startAnimation(slide_in)
        }
            addBalanceLayout.visibility = View.VISIBLE


        //   addBalanceLayout.setVisibility(View.VISIBLE);
        //  transactionHistoryLayout.setVisibility(View.GONE);


    }

    fun SelectTransactionHistory() {
        recyclerviewRecharge.visibility = View.GONE
        recyclerviewDeposit.visibility = View.GONE
        nodataDeposit.visibility = View.GONE
        nodataRecharge.visibility = View.GONE

        addBalanceTV.setTextColor(ContextCompat.getColor(this, R.color.colorlightgrey))
        addBalanceTV.background = ContextCompat.getDrawable(this, R.drawable.grey_chooser_left)
        tranhistoryTV.setTextColor(ContextCompat.getColor(this, R.color.colorblack))
        tranhistoryTV.background = ContextCompat.getDrawable(this, R.drawable.yellow_chooser_right)

        if (addBalanceLayout.visibility == View.VISIBLE) {
            addBalanceLayout.startAnimation(slide_out)
        }
        addBalanceLayout.visibility = View.GONE



        if (transactionHistoryLayout.visibility == View.GONE) {
            transactionHistoryLayout.startAnimation(slide_in)
        }
        transactionHistoryLayout.visibility = View.VISIBLE


        Handler().postDelayed({
            SelectRechargeTrans()
        }, 500)


        // addBalanceLayout.setVisibility(View.GONE);
        // transactionHistoryLayout.setVisibility(View.VISIBLE);

    }

    fun GetPosIdList() {
        val call = Uten.FetchServerData().getPosList(SharedHelper.getString(this, Constants.TOKEN))
        call.enqueue(object : Callback<PosResultModel> {
            override fun onFailure(call: Call<PosResultModel>, t: Throwable) {
                val gs = Gson()
                gs.toJson(t.localizedMessage)
                Utilities.shortToast("Something went wrong", this@WalletActivity)
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
        posList.addAll(result)
        val list = ArrayList<String>()
        result.forEach {
            list.add(it.serialNumber)
        }
        val adapter = ArrayAdapter<String>(this, R.layout.item_pos, list)
        adapter.setDropDownViewResource(R.layout.sppiner_layout_item)
        spPosId.adapter = adapter
        spPosId.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
//                walletBalance.setText(posList.get(p2).balance)
                tvWalletBalance.setText(posList.get(p2).balance)
                posId = posList.get(p2).posId
                percentage = posList.get(p2).percentage
                commissionPercent.setText("PLUS ${posList.get(p2).percentage}%")

                //Toast.makeText(activity, ""+percentage, Toast.LENGTH_SHORT).show()
                if (percentage.toString().equals("0.0")){
                    commissionPercent.visibility=View.INVISIBLE;
                   // plusPercentET.visibility=View.INVISIBLE;
                }else{
                    commissionPercent.visibility=View.VISIBLE;
                    //plusPercentET.visibility=View.VISIBLE;
                }
            }

        }
    }

    override fun onClick(v: View) {


        when (v.id) {

            R.id.addBalanceTV ->
                SelectAddBalance()

            R.id.tranhistoryTV ->
                SelectTransactionHistory()

            R.id.selectPaytype ->
                typeSpinner.performClick()

            R.id.rechargeTRL ->
                SelectRechargeTrans()

            R.id.et_date->{

                if (transactionMode==2) {
                    val now = Calendar.getInstance()
                    var datePickerDialog = DatePickerDialog(this, this, now.get(Calendar.YEAR),
                            now.get(Calendar.MONTH), // Initial month selection
                            now.get(Calendar.DAY_OF_MONTH));
                    datePickerDialog.show();
                }
            }
            R.id.deposItTRL ->
                SelectDepositTrans()
        }
    }

    companion object {


        fun newInstance(): WalletActivity {
            return WalletActivity()
        }
    }


    fun SelectRechargeTrans() {

        pageDeposit = 1
        linerecharge.setBackgroundColor(ContextCompat.getColor(this, R.color.colorYellow))
        rechargeText.setTextColor(ContextCompat.getColor(this, R.color.colorYellow))
        linedeposit.setBackgroundColor(ContextCompat.getColor(this, R.color.colorWhite))
        depositText.setTextColor(ContextCompat.getColor(this, R.color.colorWhite))

        LoadRechargeTransactionFragment()


    }


    fun SelectDepositTrans() {

        pageRecharge = 1
        linerecharge.setBackgroundColor(ContextCompat.getColor(this, R.color.colorWhite))
        rechargeText.setTextColor(ContextCompat.getColor(this, R.color.colorWhite))
        linedeposit.setBackgroundColor(ContextCompat.getColor(this, R.color.colorYellow))
        depositText.setTextColor(ContextCompat.getColor(this, R.color.colorYellow))


        LoadDepositTransactionFragment()
    }


    fun LoadRechargeTransactionFragment() {

        var customDialog: CustomDialog
        customDialog = CustomDialog(this)
        customDialog.show()

        val call: Call<RechargeTransactionNewListModel> = Uten.FetchServerData().get_meter_recharges(SharedHelper.getString(this, Constants.TOKEN), pageRecharge.toString(), totalItemsNo.toString())
        call.enqueue(object : Callback<RechargeTransactionNewListModel> {
            override fun onResponse(call: Call<RechargeTransactionNewListModel>, response: Response<RechargeTransactionNewListModel>) {


                if (customDialog.isShowing) {
                    customDialog.dismiss()
                }
                var data = response.body()
                if (data != null) {
                    if (data.status.equals("true")) {

                        if (data.result.size > 0) {

                            if (data.result.size < totalItemsNo) {
                                loadings_r = false
                            } else {
                                loadings_r = true
                            }

                            if (pageRecharge == 1) {
                                rechargeListModel.clear()
                                rechargeListModel.addAll(data.result)
                            } else {
                                rechargeListModel.addAll(data.result)
                            }

                            if (pageRecharge == 1) {
                                ShowRechargeTransactionFlow()
                            } else {
                                rechargetransAdapter.notifyDataSetChanged()
                            }

                        } else {
                            if (rechargeListModel.size < 1) {
                                nodataDeposit.visibility = View.GONE
                                nodataRecharge.visibility = View.VISIBLE
                                recyclerviewDeposit.visibility = View.GONE
                                recyclerviewRecharge.visibility = View.GONE

                            }
                        }
                    } else {
                        Utilities.CheckSessionValid(data.message, this@WalletActivity, this@WalletActivity)
                    }
                }

            }

            override fun onFailure(call: Call<RechargeTransactionNewListModel>, t: Throwable) {
                val gs = Gson()
                gs.toJson(t.localizedMessage)
                if (customDialog.isShowing) {
                    customDialog.dismiss()
                }
                Utilities.shortToast("Something went wrong.", this@WalletActivity)
            }

        })
    }


    fun LoadDepositTransactionFragment() {

        var customDialog: CustomDialog
        customDialog = CustomDialog(this@WalletActivity)
        customDialog.show()

        val call: Call<DepositTransactionNewListModel> = Uten.FetchServerData().get_deposits(SharedHelper.getString(this@WalletActivity, Constants.TOKEN), pageDeposit.toString(), totalItemsNo.toString())
        call.enqueue(object : Callback<DepositTransactionNewListModel> {
            override fun onResponse(call: Call<DepositTransactionNewListModel>, response: Response<DepositTransactionNewListModel>) {

                if (customDialog.isShowing) {
                    customDialog.dismiss()
                }
                val g = Gson()
                g.toJson(response.body())

                var data = response.body()
                if (data != null) {
                    if (data.status.equals("true")) {


                        if (data.result.size > 0) {

                            if (data.result.size < totalItemsNo) {
                                loadings_d = false
                            } else {
                                loadings_d = true
                            }

                            if (pageDeposit == 1) {
                                depositListModel.clear()
                                depositListModel.addAll(data.result)
                            } else {
                                depositListModel.addAll(data.result)
                            }
                            if (pageDeposit == 1) {
                                ShowDepositTransactionFlow()
                            } else {
                                deposittransAdapter.notifyDataSetChanged()
                            }

                        } else {

                            if (depositListModel.size < 1) {

                                nodataDeposit.visibility = View.VISIBLE
                                nodataRecharge.visibility = View.GONE
                                recyclerviewDeposit.visibility = View.GONE
                                recyclerviewRecharge.visibility = View.GONE

                            }
                        }
                    } else {
                        Utilities.CheckSessionValid(data.message, this@WalletActivity, this@WalletActivity)

                    }
                }
            }

            override fun onFailure(call: Call<DepositTransactionNewListModel>, t: Throwable) {
                val gs = Gson()
                gs.toJson(t.localizedMessage)
                if (customDialog.isShowing) {
                    customDialog.dismiss()
                }
                Utilities.shortToast("Something went wrong.", this@WalletActivity)
            }

        })
    }


    fun ShowRechargeTransactionFlow() {


        val mLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        rechargetransAdapter = RechargeTransactionAdapter(rechargeListModel, this, this)
        recyclerviewRecharge.adapter = rechargetransAdapter
        recyclerviewRecharge.layoutManager = mLayoutManager
        recyclerviewRecharge.setHasFixedSize(true)
        rechargetransAdapter.notifyDataSetChanged()

        nodataRecharge.visibility = View.GONE
        nodataDeposit.visibility = View.GONE
        recyclerviewDeposit.visibility = View.GONE
        recyclerviewRecharge.visibility = View.GONE


        if (recyclerviewRecharge.visibility == View.GONE) {
            recyclerviewRecharge.startAnimation(slide_up)
        }
        recyclerviewRecharge.visibility = View.VISIBLE



        recyclerviewRecharge.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0)
                //check for scroll down
                {
                    visibleItemCount_r = mLayoutManager.childCount
                    totalItemCount_r = mLayoutManager.itemCount
                    pastVisiblesItems_r = mLayoutManager.findFirstVisibleItemPosition()

                    if (loadings_r) {
                        if (visibleItemCount_r + pastVisiblesItems_r >= totalItemCount_r) {
                            loadings_r = false
                            pageRecharge++
                            Log.v("WalletFragment", "-------------------------------------------Last Item Wow !--------------------")
                            //Do pagination.. i.e. fetch new data
                            LoadRechargeTransactionFragment()

                        }
                    }
                }
            }
        })


    }


    fun GetBankDetails() {
        var customDialog: CustomDialog
        customDialog = CustomDialog(this)
        customDialog.show()
        val call = Uten.FetchServerData().getBankDetail(SharedHelper.getString(this, Constants.TOKEN))
        call.enqueue(object : Callback<BankResponseModel> {
            override fun onFailure(call: Call<BankResponseModel>, t: Throwable) {
                val gs = Gson()
                gs.toJson(t.localizedMessage)
                if (customDialog.isShowing) {
                    customDialog.dismiss()
                }
            }

            override fun onResponse(call: Call<BankResponseModel>, response: Response<BankResponseModel>) {
                if (customDialog.isShowing) {
                    customDialog.dismiss()
                }
                var data = response.body()
                if (data != null) {
                    if (data.status.equals("true")) {
                        if (data.result.size > 0) {
                            setAccountSpinner(data.result)
                        }

                    }
                }

            }
        })
//        val call: Call<BankDetailsModel> = Uten.FetchServerData().bank_details(SharedHelper.getString(requireActivity(), Constants.TOKEN))
//        call.enqueue(object : Callback<BankDetailsModel> {
//            override fun onResponse(call: Call<BankDetailsModel>, response: Response<BankDetailsModel>) {
//
//                if (customDialog.isShowing) {
//                    customDialog.dismiss()
//                }
//                var data = response.body()
//                if (data != null) {
//                    //  Utilities.shortToast(data.message,requireActivity())
//                    if (data.status.equals("true")) {
//
//                        /* lateinit var banknameTV:TextView
//    lateinit var accnameTV:TextView
//    lateinit var accnumberTV:TextView
//    lateinit var accbbanTV:TextView
//*/
////                        if (data.result.size > 0) {
////                            banknameTV.text = data.result.get(0).bankName
////                            accnameTV.text = data.result.get(0).accountName
////                            accnumberTV.text = data.result.get(0).accountNumber
////                            accbbanTV.text = data.result.get(0).bban
////                        }
//
//                        bindAccountSp(data.result)
//
//
//                    } else {
//                        Utilities.CheckSessionValid(data.message, requireContext(), requireActivity())
//                    }
//                }
//            }
//
//            override fun onFailure(call: Call<BankDetailsModel>, t: Throwable) {
//                val gs = Gson()
//                gs.toJson(t.localizedMessage)
//                if (customDialog.isShowing) {
//                    customDialog.dismiss()
//                }
//            }
//
//        })
    }

    private fun setAccountSpinner(result: List<BankResponseModel.Result>) {
        val accountList = ArrayList<String>()
        result.forEach {
            accountList.add(it.text)
        }
        val adapter = ArrayAdapter<String>(this, R.layout.spinner_text_second, accountList)
        adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown)
        spAccounts.adapter = adapter
        spAccounts.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                bankAccountId = result.get(p2).value
            }

        }

    }


    fun ShowDepositTransactionFlow() {

        val mLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)


        deposittransAdapter = DepositTransactionAdapter(depositListModel, this, this)
        recyclerviewDeposit.adapter = deposittransAdapter
        recyclerviewDeposit.layoutManager = mLayoutManager
        recyclerviewDeposit.setHasFixedSize(true)
        deposittransAdapter.notifyDataSetChanged()

        nodataRecharge.visibility = View.GONE
        nodataDeposit.visibility = View.GONE
        recyclerviewDeposit.visibility = View.GONE
        recyclerviewRecharge.visibility = View.GONE

        if (recyclerviewDeposit.visibility == View.GONE) {
            recyclerviewDeposit.startAnimation(slide_up)
        }
        recyclerviewDeposit.visibility = View.VISIBLE

        recyclerviewDeposit.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0)
                //check for scroll down
                {
                    visibleItemCount_d = mLayoutManager.childCount
                    totalItemCount_d = mLayoutManager.itemCount
                    pastVisiblesItems_d = mLayoutManager.findFirstVisibleItemPosition()

                    if (loadings_d) {
                        if (visibleItemCount_d + pastVisiblesItems_d >= totalItemCount_d) {
                            loadings_d = false
                            pageDeposit++
                            Log.v("WalletFragment", "-------------------------------------------Last Item Wow !--------------------")
                            //Do pagination.. i.e. fetch new data
                            LoadDepositTransactionFragment()

                        }
                    }
                }
            }
        })

    }

    override fun onDateSet(view: DatePicker?, year: Int, monthOfYear: Int, dayOfMonth: Int) {
        val date =  dayOfMonth.toString() + "/" + (monthOfYear + 1).toString() + "/" + year
        et_date.setText(date)
    }

    private fun showdialog(amount: String) {

        var code = SharedHelper.getString(this, Constants.CURRENCY_CODE);
        val adDialog = Dialog(this@WalletActivity, R.style.MyDialogThemeBlack);
        adDialog.window!!.requestFeature(Window.FEATURE_NO_TITLE);
        adDialog.setContentView(R.layout.alertwalletdialog);
        adDialog.setCancelable(false);

        val tvpending = adDialog.findViewById<TextView>(R.id.tvpending);
        val tv_cancel = adDialog.findViewById<TextView>(R.id.tv_cancel);
//        val tv_continue = adDialog.findViewById<TextView>(R.id.tv_continue);

        tvpending.setText(code+ " : "+ amount)

        tv_cancel.setOnClickListener {
            adDialog.dismiss();
        }

//        tv_continue.setOnClickListener {
//            DoDeposit(1);
//            adDialog.cancel()
//            adDialog.dismiss();
//        }
        adDialog.show();

    }

    override fun onResume() {
        if (Uten.isInternetAvailable(this)) {
            GetWalletBalance();
        } else {
            NoInternetDialog("No internet connection. Please check your network connectivity.")
        }
        super.onResume()
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
    fun GetWalletBalance() {

        var customDialog: CustomDialog
        customDialog = CustomDialog(this)
        customDialog.show()

        var code = SharedHelper.getString(this, Constants.CURRENCY_CODE);
        val call: Call<GetWalletModel> = Uten.FetchServerData().get_wallet_balance(SharedHelper.getString(this, Constants.TOKEN))
        call.enqueue(object : Callback<GetWalletModel> {
            override fun onResponse(call: Call<GetWalletModel>, response: Response<GetWalletModel>) {

                if (customDialog.isShowing) {
                    customDialog.dismiss()
                }
                var data = response.body()
                if (data != null) {

                    if (data.status.equals("true")) {

                        totalBalanceTV.setText(code+" : " + data.result.balance)
                        tickerViewBalance.setText(NumberFormat.getNumberInstance(Locale.US).format(data.result.balance.toDouble().toInt()))
                        //tickerViewBalance.setText(Utilities.formatCurrencyValue(data.result.balance))
//                        totalAvlblBalance = data.result.balance.toDouble()

                        tickerViewBalance.setText("${code} : " + data.result.balance.toDouble() + "0")
//                        countInterface?.CountIs(data.result.unReadNotifications)
                    } else {
                        Utilities.CheckSessionValid(data.message, this@WalletActivity, this@WalletActivity)
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

}
