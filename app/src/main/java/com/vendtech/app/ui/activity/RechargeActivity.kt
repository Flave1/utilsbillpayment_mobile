package com.vendtech.app.ui.activity

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.vendtech.app.R
import com.vendtech.app.helper.SharedHelper
import com.vendtech.app.models.meter.MeterListResults
import com.vendtech.app.models.meter.PosResultModel
import com.vendtech.app.models.meter.RechargeMeterModel
import com.vendtech.app.models.profile.GetWalletModel
import com.vendtech.app.network.Uten
import com.vendtech.app.utils.Constants
import com.vendtech.app.utils.CustomDialog
import com.vendtech.app.utils.Utilities
import kotlinx.android.synthetic.main.activity_recharge.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*

class RechargeActivity: AppCompatActivity() {

    private lateinit var meterListResults: MeterListResults;
    private lateinit var tv_pos_id:TextView;
    private lateinit var tv_meter_number:TextView;
    var totalAvlblBalance = 0.0
    // private lateinit var tv_amount:TextView;
    var posList = ArrayList<PosResultModel.Result>()
    lateinit var posSpinner: Spinner
    var posId = 0;

    var meter_id=""
    private fun getData(){
        if(intent!!.extras!=null){
            meterListResults= intent.extras?.getSerializable("Data") as MeterListResults;
            tv_meter_number.setText(meterListResults.number);
            meter_id=meterListResults.meterId;
            //tv_meter_number.setText(meterListResults.number)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recharge)


        //  tv_pos_id=findViewById(R.id.tv_pos_id);
        tv_meter_number=findViewById(R.id.tv_meter_number);
        posSpinner = findViewById<View>(R.id.posIdSpinner) as Spinner
        //  tv_amount=findViewById(R.id.tv_amount);

        et_money.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                et_money.removeTextChangedListener(this)
                try {
                    var originalString = s.toString();

                    var longval: Long
                    if (originalString.contains(",")) {
                        originalString = originalString.replace(",", "");
                    }
                    longval = originalString.toLong()
                    if (longval <= totalAvlblBalance) {
                        var formatter: DecimalFormat = NumberFormat.getInstance(Locale.US) as DecimalFormat;
                        formatter.applyPattern("#,###,###,###");
                        var formattedString = formatter.format(longval);
                        et_money.setText(formattedString);
                        et_money.setSelection(et_money.text.length);
                    } else {
                        Toast.makeText(applicationContext, "Amount is greater then Wallet Balance", Toast.LENGTH_SHORT).show();
                    }
                } catch (nfe: NumberFormatException) {
                    nfe.printStackTrace();
                }
                et_money.addTextChangedListener(this);
            }

        });
        getData();
        GetWalletBalance();
        GetPosIdList();

        imgBack.setOnClickListener {
            onBackPressed();
        }

        paynowTV.setOnClickListener {
            var moneyValue = et_money.text.toString().trim().replace(",", "");
            if (TextUtils.isEmpty(tv_meter_number.text.toString().trim())) {
                Utilities.shortToast("Please select a meter number.", applicationContext);
            } else if (TextUtils.isEmpty(et_money.text.toString().trim())) {
                Utilities.shortToast("Please enter recharge amount.", applicationContext);
            } else if (totalAvlblBalance < 1) {
                Utilities.shortToast("You don't have enough balance to recharge", applicationContext);
            } else if (moneyValue.toDouble() > totalAvlblBalance) {
                Utilities.shortToast("Recharge amount is greater than the available balance.", applicationContext);
            } else {
                ShowAlertForRecharge(moneyValue);
            }
        }
    }

    fun DoRecharge(amount: String, meterId: String, posId: Int) {

        var customDialog: CustomDialog
        customDialog = CustomDialog(this)
        customDialog.show()


        //var meterNumber: String? = null
       // var meterid: String? = null
       /* if (meterId.isEmpty()) {
            meterNumber = tv_meter_number.text.toString().trim()
            meterid = null
        } else {
            meterid = meterId
            meterNumber = null
        }*/
        //val call: Call<RechargeMeterModel> = Uten.FetchServerData().recharge_meter(SharedHelper.getString(this, Constants.TOKEN), meterId, amount, "", posId,SharedHelper.getString(this, Constants.PASS_CODE_VALUE))
        val call: Call<RechargeMeterModel> = Uten.FetchServerData().rechargeMeter(SharedHelper.getString(this, Constants.TOKEN),amount,meterId,posId.toString(), "")
        call.enqueue(object : Callback<RechargeMeterModel> {
            override fun onResponse(call: Call<RechargeMeterModel>, response: Response<RechargeMeterModel>) {

                if (customDialog.isShowing) {
                    customDialog.dismiss()
                }

                Log.d("PrintResponce","---"+response.body());
                var data = response.body();
                if (data != null) {


                    if (data.message!=null) {
                        Utilities.shortToast(data.message, applicationContext)
                    }else {
                        if (data.status.equals("true")) {
                            //val temp = autoCompleteTV.text.toString()
                            //autoCompleteTV.setText("")
                            et_money.setText("")
                            //HidePayLayout()
                            // GetWalletBalance()
                            /*if (activity != null && cbSaveMeter.isChecked) {
                            val bundle = Bundle();
                            bundle.putString(Constants.METER_NAME, temp)
                            val intent = Intent(activity, AddMeterActivity::class.java)
                            intent.putExtras(bundle)
                            startActivity(intent)
                        }*/

                        } else {
                            Utilities.CheckSessionValid(data.message, applicationContext, this@RechargeActivity);
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
                Utilities.shortToast("Something went wrong", this@RechargeActivity)
            }
        })
    }


    fun ShowAlertForRecharge(moneyvalue: String) {

        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.app_name)
        builder.setMessage("Are you sure to recharge this meter?")
        builder.setIcon(R.drawable.appicon)

        builder.setPositiveButton("Recharge") { dialogInterface, which ->
            if (Uten.isInternetAvailable(this)) {
                DoRecharge(moneyvalue, meter_id, posId);
            } else {
                Utilities.shortToast("No internet connection. Please check your network connectivity.", this);
            }
        }
        builder.setNegativeButton("Check Again") { dialogInterface, which ->
        }
        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
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

                       // totalBalanceTV.setText("SLL : " + data.result.balance)
                        //tickerViewBalance.setText("SLL : " + data.result.balance)
                        //tickerViewBalance.setText(NumberFormat.getNumberInstance(Locale.US).format(data.result.balance.toDouble().toInt()))
                        //tickerViewBalance.setText(Utilities.formatCurrencyValue(data.result.balance))
                        totalAvlblBalance = data.result.balance.toDouble()
                        //countInterface?.CountIs(data.result.unReadNotifications)

                    } else {
                        Utilities.CheckSessionValid(data.message, this@RechargeActivity, this@RechargeActivity)
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

    private fun SetOnSpinner(result: List<PosResultModel.Result>) {
        posList.addAll(result)
        val list = ArrayList<String>()
        result.forEach {
            list.add(it.serialNumber)
        }

        val adapter = ArrayAdapter<String>(this, R.layout.item_pos, list)
        adapter.setDropDownViewResource(R.layout.sppiner_layout_item)
        posSpinner.adapter = adapter
        posSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {

            }
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                //balance = posList.get(p2).balance;
                posId = posList.get(p2).posId;
                //Log.e("balance+pos", "$balance $posId");
               // tvPosNumber.setText("POS ID : " + posList.get(p2).serialNumber);
                //tickerViewBalance.setText(balance);
            }
        }
    }

    fun GetPosIdList() {
        val call = Uten.FetchServerData().getPosList(SharedHelper.getString(this, Constants.TOKEN))
        call.enqueue(object : Callback<PosResultModel> {
            override fun onFailure(call: Call<PosResultModel>, t: Throwable) {
                val gs = Gson()
                gs.toJson(t.localizedMessage)
                Utilities.shortToast("Something went wrong", applicationContext)
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

}