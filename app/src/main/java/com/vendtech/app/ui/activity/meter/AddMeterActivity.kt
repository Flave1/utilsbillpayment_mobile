package com.vendtech.app.ui.activity.meter

import android.app.Activity
import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.widget.*
import com.google.gson.Gson

import com.vendtech.app.R
import com.vendtech.app.adapter.meter.MeterMakeAdapter
import com.vendtech.app.helper.SharedHelper
import com.vendtech.app.models.meter.AddMeterModel
import com.vendtech.app.network.Uten
import com.vendtech.app.utils.Constants
import com.vendtech.app.utils.CustomDialog
import com.vendtech.app.utils.Utilities
import kotlinx.android.synthetic.main.activity_add_meter.*
import kotlinx.android.synthetic.main.activity_add_meter.addressET
import kotlinx.android.synthetic.main.activity_sign_up.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class AddMeterActivity : Activity(), View.OnClickListener {

    lateinit var back: ImageView
    lateinit var paynowTV: TextView
    lateinit var customDialog: CustomDialog
    lateinit var metermakeTV: TextView
    lateinit var valueInMeterName: String
    lateinit var spMeterMake: Spinner
    lateinit var meterMakeAdapter: MeterMakeAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_meter)
        valueInMeterName = intent?.extras?.getString(Constants.METER_NAME) ?: ""
        initViews()
        customDialog = CustomDialog(this)

        meternoET.setText(valueInMeterName)
        bindSpMeterMake()
    }

    private fun bindSpMeterMake() {
        var list: MutableList<String> = ArrayList()
        list.add("CONLOG")
        list.add("HOLLEY")
        list.add("SAGENCOM")
        metermakeET.text = list[0]
        meterMakeAdapter = MeterMakeAdapter(this@AddMeterActivity, list)
        spMeterMake.adapter = meterMakeAdapter
        spMeterMake.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>, view: View,
                                        position: Int, id: Long) {
                val item = adapterView.getItemAtPosition(position) as String
                if (item != null) {
                    metermakeET.text = item
                }
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {

            }
        })
    }


    fun initViews() {

        spMeterMake = findViewById(R.id.spMeterMake) as Spinner
        back = findViewById<View>(R.id.imgBack) as ImageView
        paynowTV = findViewById<View>(R.id.paynowTV) as TextView
        metermakeTV = findViewById<View>(R.id.metermakeET) as TextView

        back.setOnClickListener(this)
        paynowTV.setOnClickListener(this)
        metermakeTV.setOnClickListener(this)

        val isVerified = findViewById<CheckBox>(R.id.isVerified)

        isVerified.setOnCheckedChangeListener { _, isChecked ->
            // Handle checkbox state change here
            if (isChecked) {
                // Checkbox is checked
            } else {
                // Checkbox is unchecked
            }
        }


        meternoET.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                if (p0.toString().trim().length > 11) {
                    Utilities.shortToast("Meter number must be of 11 digits", this@AddMeterActivity)
                }

            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {


            }
        })


    }

    override fun onClick(v: View) {

        when (v.id) {

            R.id.imgBack -> {
                finish()
                overridePendingTransition(R.anim.activity_back_in, R.anim.activity_back_out)
            }

            R.id.metermakeET -> {
                DatePick()
            }

            R.id.paynowTV -> {

                if (TextUtils.isEmpty(meternoET.text.toString().trim())) {
                    Utilities.shortToast("Enter meter number", this)
                } else if (meternoET.text.toString().trim().length != 11) {
                    Utilities.shortToast("Meter number must be of 11 digits", this)
                } else if (TextUtils.isEmpty(meternameET.text.toString().trim())) {
                    Utilities.shortToast("Enter name on meter", this)
                } else if (TextUtils.isEmpty(addressET.text.toString().trim())) {
                    Utilities.shortToast("Enter address", this)
                } else if (addressET.text.toString().trim().length < 7) {
                    Utilities.shortToast(resources.getString(R.string.address_length), this)
                } else if (TextUtils.isEmpty(metermakeET.text.toString().trim())) {
                    Utilities.shortToast("Enter meter make", this)
                }  else if (TextUtils.isEmpty(aliasET.text.toString().trim())) {
                    Utilities.shortToast("Enter Alias", this)
                } else {
                    if (Uten.isInternetAvailable(this)) {
                        AddMeter()
                    } else {
                        Utilities.shortToast("No internet connection. Please check your network connectivity.", this)
                    }

                }
            }

        }

    }


    fun DatePick() {

        var cal = Calendar.getInstance()

        val dateSetListener = DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, monthOfYear)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            val myFormat = "dd MMM, yyyy" // mention the format you need
            val sdf = SimpleDateFormat(myFormat, Locale.US)

            metermakeTV.setText(sdf.format(cal.time))

        }
        val dialog = DatePickerDialog(this, dateSetListener, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
        dialog.datePicker.maxDate = cal.getTimeInMillis()
        dialog.show()

    }


    fun AddMeter() {

        customDialog.show()

        val call: Call<AddMeterModel> = Uten.FetchServerData().add_meter(SharedHelper.getString(this, Constants.TOKEN), meternameET.text.toString().trim(), metermakeET.text.toString().trim(), addressET.text.toString().trim(), meternoET.text.toString().trim(), aliasET.text.toString().trim(), isVerified.isChecked)

        call.enqueue(object : Callback<AddMeterModel> {
            override fun onResponse(call: Call<AddMeterModel>, response: Response<AddMeterModel>) {
                if (customDialog.isShowing) {
                    customDialog.dismiss()
                }
                var data = response.body()
                if (data != null) {
                    if (data.status.equals("true")) {
                        onBackPressed()
                        finish()
                    } else {
                        Utilities.CheckSessionValid(data.message, this@AddMeterActivity, this@AddMeterActivity)
                    }
                }
            }

            override fun onFailure(call: Call<AddMeterModel>, t: Throwable) {
                val gs = Gson()
                gs.toJson(t.localizedMessage)
                if (customDialog.isShowing) {
                    customDialog.dismiss()
                }
            }

        })
    }
}
