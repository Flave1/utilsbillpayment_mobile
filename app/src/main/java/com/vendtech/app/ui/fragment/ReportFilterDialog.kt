package com.vendtech.app.ui.fragment

import android.app.DatePickerDialog
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.fragment.app.DialogFragment
import com.google.gson.Gson
import com.vendtech.app.R
import com.vendtech.app.helper.SharedHelper
import com.vendtech.app.models.meter.BankResponseModel
import com.vendtech.app.models.meter.PosResultModel
import com.vendtech.app.network.Uten
import com.vendtech.app.utils.Constants
import com.vendtech.app.utils.CustomDialog
import com.vendtech.app.utils.Utilities
import kotlinx.android.synthetic.main.dialog_report_filter.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*
import kotlin.collections.ArrayList


class ReportFilterDialog : DialogFragment(){

    var posList=ArrayList<PosResultModel.Result>()
    var posId=0
    var bankAccountId=""
    var from:String=""
    var to:String=""
    var meterNumber:String=""
    var transId:String=""
    var isRecharge=false
    var depositType=0;
    var product="";
    var refNumber="";
    var formattedDate=""
    lateinit var reportFragment: ReportsActivity
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
setStyle(DialogFragment.STYLE_NO_TITLE,android.R.style.ThemeOverlay_Material)

    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view=LayoutInflater.from(context).inflate(R.layout.dialog_report_filter,container,false)
        return view
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isRecharge=arguments?.getBoolean("isRecharge",false)!!


        val newDate = Calendar.getInstance();
        val sliptedDt = LocalDate.now().toString().split('-');
        val year = sliptedDt[0].toInt();
        val month = sliptedDt[1].toInt();
        val day = sliptedDt[2].toInt();
        newDate[year, month-1] = day;
        val format = SimpleDateFormat("dd-MM-yyyy")

        val dt: String = format.format(newDate.time)

        reportFilterFrom.setText(dt)
        reportFilterTo.setText(dt)



            if(isRecharge){
                depositLayout.visibility=View.GONE;
                liner_depostText.visibility=View.GONE;
                linerDepostValue.visibility=View.GONE;
                meterLayout.visibility=View.VISIBLE;
                tv_title.setText(resources.getString(R.string.sales_report_filters))
                SetProductSpinnerData()
            }
            else {
                tv_title.setText(resources.getString(R.string.deposite_report_filters))
                depositLayout.visibility=View.VISIBLE;
                meterLayout.visibility=View.GONE;
                ll_vt_transaction_id.visibility=View.GONE;
                ll_vt_prod.visibility=View.GONE;
                GetBankDetails();
                SetSpinnerData();
            }
        getPosIdList()
        setUpClick()
    }
    fun SetSpinnerData() {

        val list: MutableList<String> = ArrayList()
        list.add("Select type")
        list.add("Cash")
        list.add("Cheque")



        val adapter= ArrayAdapter<String>(requireActivity(), R.layout.item_pos,list)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        reportFilterDepositSP.setAdapter(adapter)

        reportFilterDepositSP.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                //Toast.makeText(this@SignUpActivity, "Country ID: " + data[position].countryId, Toast.LENGTH_SHORT).show()
                if (position == 0) {
                    depositType = 0
                } else if (position == 1) {
                    depositType = 1
                } else if (position == 2) {
                    depositType = 2
                }

            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
    }

    fun SetProductSpinnerData() {


        val list1: MutableList<String> = ArrayList()
        list1.add("Select Product")
        list1.add("EDSA")
        list1.add("ORANGE")
        list1.add("AFRICELL")

        val adapter1= ArrayAdapter<String>(requireActivity(), R.layout.item_pos,list1)
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        reportFilterProductSP.setAdapter(adapter1)

        reportFilterDepositSP.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                //Toast.makeText(this@SignUpActivity, "Country ID: " + data[position].countryId, Toast.LENGTH_SHORT).show()
                if (position == 0) {
                    depositType = 0
                } else if (position == 1) {
                    depositType = 1
                } else if (position == 2) {
                    depositType = 2
                }

            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        reportFilterProductSP.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position == 0) {
                    product = ""
                } else if (position == 1) {
                    product = "EDSA"
                } else if (position == 2) {
                    product = "ORANGE"
                } else if (position == 3) {
                    product = "AFRICELL"
                }

            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
    }

    private fun setUpClick() {
        back.setOnClickListener { dismiss() }
        reportFilterFrom.setOnClickListener {
            getFormattedDate(reportFilterFrom)
        }
        reportFilterTo.setOnClickListener {
            getFormattedDate(reportFilterTo)
        }
        reportFilterSearch.setOnClickListener {

           //  Toast.makeText(activity!!,""+posId,Toast.LENGTH_LONG).show();
            if(reportFilterFrom.text.toString().isNotEmpty()){
             //  from
            }
            if(reportFilterTo.text.toString().isNotEmpty()){

            }
            if(reportFilterMeterNumber.text.toString().isNotEmpty()){
                meterNumber=reportFilterMeterNumber.text.toString()
            }
            if(reportFilterTransId.text.toString().isNotEmpty()){
                transId=reportFilterTransId.text.toString()
            }
            if(reportFilterRefNumber.text.toString().isNotEmpty()){
                refNumber=reportFilterRefNumber.text.toString()
            }
            if(isRecharge) {
                reportFragment.pageRecharge=1
                reportFragment.filterRechargeData(posId, from, to, meterNumber, transId, product)//now call salesRepot TAB
            }
            else {
                reportFragment.pageDeposit=1
                reportFragment.filterDepositData(posId, from, to, meterNumber, refNumber, transId, bankAccountId, depositType)
            }
            dismiss()
        }
    }

    private fun getFormattedDate(textView: TextView?) {
        val calendar=Calendar.getInstance()
        val dialog=DatePickerDialog(context!!,object :DatePickerDialog.OnDateSetListener{
            override fun onDateSet(datePicker: DatePicker?, p1: Int, p2: Int, p3: Int) {
                val year: Int = datePicker?.year!!
                val month: Int = datePicker.month
                val day: Int = datePicker.dayOfMonth

                val newDate = Calendar.getInstance()
                newDate[year, month] = day
                if(textView==reportFilterTo){
                    val format = SimpleDateFormat("MM-dd-yyyy")
                    val strDate: String = format.format(newDate.time)
                    to=strDate
                }
                else if(textView==reportFilterFrom){
                    val format = SimpleDateFormat("MM-dd-yyyy")
                    val strDate: String = format.format(newDate.time)
                    from=strDate
                }
                val format = SimpleDateFormat("dd-MM-yyyy")
                val strDate: String = format.format(newDate.time)
                textView?.text = strDate
            }

        },calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH))
        dialog.datePicker.maxDate = System.currentTimeMillis();
        dialog.show()
    }
    fun GetBankDetails() {
        var customDialog: CustomDialog
        customDialog = CustomDialog(requireActivity())
        customDialog.show()
        val call = Uten.FetchServerData().getBankDetail(SharedHelper.getString(requireActivity(), Constants.TOKEN))
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
    }

    private fun setAccountSpinner(result: List<BankResponseModel.Result>) {
        val accountList=ArrayList<String>()
        accountList.add("Select Bank")
        result.forEach {
            accountList.add(it.text)
        }
        val adapter= ArrayAdapter<String>(requireActivity(), R.layout.item_pos,accountList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        reportFilterBankSP.adapter=adapter
        reportFilterBankSP.onItemSelectedListener=object :AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                if(p2>0) {
                    bankAccountId = result.get(p2-1).value
                }
                else bankAccountId=""
            }

        }

    }

    fun getPosIdList() {
        val call= Uten.FetchServerData().getPosList(SharedHelper.getString(requireActivity(), Constants.TOKEN))

        call.enqueue(object : Callback<PosResultModel> {
            override fun onFailure(call: Call<PosResultModel>, t: Throwable) {
                val gs = Gson()
                gs.toJson(t.localizedMessage)
                Utilities.shortToast("Something went wrong", requireActivity())
            }

            override fun onResponse(call: Call<PosResultModel>, response: Response<PosResultModel>) {
                if (response != null) {
                    if (response.body() != null) {
                        if (response.body()?.status == "true") {
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
        val list=ArrayList<String>()
       // list.add("Select Pos Id")
        result.forEach {
            list.add(it.serialNumber)
        }
        val adapter= ArrayAdapter<String>(context!!, R.layout.item_pos_large,list)
        adapter.setDropDownViewResource(R.layout.sppiner_layout_item)
        reportFilterPosSP.adapter=adapter
        reportFilterPosSP.onItemSelectedListener=object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                if(p2>0) {
                   // posId = posList[p2 - 1].posId;
                    posId = posList[p2].posId;


                }
                else posId=0
            }

        }
    }

    fun initFragment(reportsFragment: ReportsActivity) {
        this.reportFragment=reportsFragment
    }

}