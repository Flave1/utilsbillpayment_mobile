package com.vendtech.app.ui.activity.authentication

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.*
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.*
import com.google.gson.Gson

import com.vendtech.app.R
import com.vendtech.app.helper.SharedHelper
import com.vendtech.app.network.Uten
import com.vendtech.app.utils.CustomDialog
import kotlinx.android.synthetic.main.activity_sign_up.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.vendtech.app.models.authentications.*
import com.vendtech.app.ui.activity.termspolicies.TermsPoliciesActivity
import com.vendtech.app.utils.Constants
import com.vendtech.app.utils.Utilities
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_sign_up.emailET
import kotlinx.android.synthetic.main.layout_error.*
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import java.lang.Exception


class SignUpActivity : AppCompatActivity() {

    lateinit var imgBack: ImageView
    lateinit var layoutLogin: LinearLayout
    lateinit var customDialog: CustomDialog
    lateinit var countrySpinner:Spinner
    lateinit var citySpinner:Spinner
    lateinit var userSpinner:Spinner
    lateinit var typeSpinner:Spinner
    lateinit var spinner_code:Spinner
    lateinit var CITY_ID:String
    lateinit var COUNTRY_ID:String
    lateinit var USER_TYPE_ID:String
    lateinit var AppUserType:String
    var USER_NAME = ""
    var Agency = ""
    lateinit var mainLayout: NestedScrollView
    lateinit var errorLayout:LinearLayout
    lateinit var backPress:TextView
    lateinit var tv_fname_txt:TextView
    lateinit var tv_l_name_txt:TextView

    lateinit var checkBoxTC:CheckBox
    lateinit var termConditionTV:TextView
    lateinit var companyNameRL:RelativeLayout

    var PASSWORD=""

    var TAG="SignUpActivity"

        override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        imgBack = findViewById(R.id.imgBack)
        layoutLogin = findViewById(R.id.layoutLogin)
            countrySpinner = findViewById(R.id.countrySpinner)
            citySpinner = findViewById(R.id.citySpinner)
            userSpinner = findViewById(R.id.userSpinner)
            spinner_code = findViewById(R.id.spinner_code)
            typeSpinner = findViewById(R.id.typeSpinner)
            mainLayout = findViewById(R.id.mainlayout)
            errorLayout = findViewById(R.id.error_layout)
            backPress = findViewById(R.id.backPress)
            checkBoxTC=findViewById(R.id.agreeTermsCB)
            termConditionTV=findViewById(R.id.termsConditionTV)
            companyNameRL=findViewById(R.id.companyNameRL)
            tv_fname_txt=findViewById(R.id.tv_fname_txt)
            tv_l_name_txt=findViewById(R.id.tv_l_name_txt)
            customDialog= CustomDialog(this)

            USER_TYPE_ID=""
            COUNTRY_ID=""
            CITY_ID=""
            Agency=""


           // USER_NAME=intent.getStringExtra("username")
            //PASSWORD=intent.getStringExtra("password")

            listListners()
            getUserTypes()
            getCountries();
            setCountryCode()
    }

    private fun listListners() {

        imgBack.setOnClickListener { v -> onBackPressed() }
        layoutLogin.setOnClickListener { v -> GotoLogin() }
        signUpTV.setOnClickListener(View.OnClickListener {
            if (TextUtils.isEmpty(Agency)){
                Utilities.shortToast("Select Agency",this)
            }
            else if(TextUtils.isEmpty(USER_TYPE_ID)){
                Utilities.shortToast("Select user type",this)
            }else if(USER_TYPE_ID.contentEquals("20") && TextUtils.isEmpty(companynameET.text.toString().trim())){
                Utilities.shortToast("Enter company name",this)
            }else if(USER_TYPE_ID.contentEquals("20") && companynameET.text.toString().trim().length<3){
                Utilities.shortToast("Company name should be atleast 3 charachters",this)
            }
             else if(TextUtils.isEmpty(fnameET.text.toString().trim())){
                Utilities.shortToast("Enter first name",this)
            }else if(fnameET.text.toString().trim().length<3) {
                Utilities.shortToast(resources.getString(R.string.first_name_length),this)
             }else if(TextUtils.isEmpty(lastnameTV.text.toString().trim())) {
                Utilities.shortToast("Enter last name",this)
            }else if(lastnameTV.text.toString().trim().length<3) {
                Utilities.shortToast(resources.getString(R.string.last_name_length),this)
            }
             /*  else if(TextUtils.isEmpty(usernameET.text.toString().trim())) {
                Utilities.shortToast("Enter Username",this)
              }else if(usernameET.text.toString().trim().length<3) {
                Utilities.shortToast(resources.getString(R.string.user_name_length),this)
             }*/
             else if(TextUtils.isEmpty(emailET.text.toString().trim())) {
                Utilities.shortToast("Enter email address",this)
            }else if(!emailET.text.matches(Patterns.EMAIL_ADDRESS.toRegex())){
                Utilities.shortToast("Enter a valid email address",this);
            } else if(TextUtils.isEmpty(phoneET.text.toString().trim())) {
                Utilities.shortToast("Enter phone number",this)
             } else if(phoneET.text.toString().trim().length!=8) {
                Utilities.shortToast("Enter a valid phone number",this)
            } else if(TextUtils.isEmpty(addressET.text.toString().trim())) {
                Utilities.shortToast("Enter address",this)
            } else if(TextUtils.isEmpty(COUNTRY_ID)) {
                Utilities.shortToast("Select country",this)

            } else if(TextUtils.isEmpty(CITY_ID)) {
                Utilities.shortToast("Select city",this)
            } else if(addressET.text.toString().trim().length<7) {
                Utilities.shortToast(resources.getString(R.string.address_length),this)
            }/*else if(TextUtils.isEmpty(passwordET.text.toString().trim())) {
                Utilities.shortToast("Enter password",this)
            } else if(passwordET.text.toString().trim().length<6) {
                Utilities.shortToast(resources.getString(R.string.pass_length),this)
            } else if(!checkBoxTC.isChecked) {
                Utilities.shortToast("Please accept Terms & Conditions and Privacy Policy",this)
            }*/else {
                if(Uten.isInternetAvailable(this)){
                  //  USER_NAME=usernameET.text.toString().trim()
                    DoSignUp()
                }else{
                    Utilities.shortToast("No internet connection. Please check your network connectivity.",this)
                  }
               }
        })

        selectCityTV.setOnClickListener(View.OnClickListener {
            Utilities.shortToast("Select country first",this)
        })

        backPress.setOnClickListener(View.OnClickListener {
            GotoLogin()
        })

        selectUser.setOnClickListener(View.OnClickListener {
            userSpinner.performClick()
        })

        select_type.setOnClickListener {
                typeSpinner.performClick();
        }

        selectCity.setOnClickListener(View.OnClickListener {
            if(selectCityTV.visibility==View.VISIBLE){
                Utilities.shortToast("Select country first",this)
            }else{
                citySpinner.performClick()
            }
        })

        selectCountry.setOnClickListener(View.OnClickListener {
            countrySpinner.performClick()
        })

        retry.setOnClickListener(View.OnClickListener {

            ShowMainLayout()

            if(TextUtils.isEmpty(USER_TYPE_ID)){
                getUserTypes()
            }else if(TextUtils.isEmpty(COUNTRY_ID)){
                getCountries()
            }else{
                getCities(COUNTRY_ID)
            }
        })

        termConditionTV.makeLinks(
                Pair("Terms & Conditions and Privacy Policy", View.OnClickListener {
                    Handler().postDelayed({
                        val i = Intent(this@SignUpActivity, TermsPoliciesActivity::class.java)
                        i.putExtra("title", "OUR POLICIES")
                        i.putExtra("type","1")
                        startActivity(i)
                        overridePendingTransition(R.anim.slide_in, R.anim.slide_out)
                    }, 200)


                })
             /*   Pair("Privacy Policy", View.OnClickListener {
                    Handler().postDelayed({
                        val i = Intent(this@SignUpActivity, TermsPoliciesActivity::class.java)
                        i.putExtra("title", "PRIVACY POLICY")
                        i.putExtra("type","2")
                        startActivity(i)
                        overridePendingTransition(R.anim.slide_in, R.anim.slide_out)
                    }, 200)

                })*/
        )

    }


    fun ShowMainLayout(){

        errorLayout.visibility=View.GONE
        mainLayout.visibility=View.VISIBLE
    }


    fun DoSignUp(){


        var companyName=""

        if(USER_TYPE_ID.contentEquals("20")){
            companyName=companynameET.text.toString().trim()
        }else{
            companyName=""
        }

        //USER_TYPE_ID="AppUser"
        USER_TYPE_ID="Vendor"
          var  DeviceType="ABC"
          var  AppType=Constants.DEVICE_TYPE
        customDialog.show()


       /* val call: Call<SignUpResponse> = Uten.FetchServerData().sign_up(Agency,emailET.text.toString().trim(),PASSWORD,
                fnameET.text.toString().trim(),
                lastnameTV.text.toString().trim(),
                USER_NAME,
                USER_TYPE_ID,
                companyName,
                COUNTRY_ID,
                CITY_ID,
                addressET.text.toString().trim(),
                phoneET.text.toString().trim(),
                referralcodeET.text.toString().trim(),
                DeviceType,AppType,SharedHelper.getString(this,Constants.TOKEN),
                AppUserType)*/


        val call: Call<SignUpResponse> = Uten.FetchServerData().sign_up(Agency,emailET.text.toString().trim(),
                fnameET.text.toString().trim(),
                lastnameTV.text.toString().trim(),
                USER_TYPE_ID,
                companyName,
                COUNTRY_ID,
                CITY_ID,
                addressET.text.toString().trim(),
                phoneET.text.toString().trim(),
                referralcodeET.text.toString().trim(),
                DeviceType,AppType,SharedHelper.getString(this,Constants.TOKEN),
                AppUserType)

        call.enqueue(object : Callback<SignUpResponse> {
            override fun onResponse(call: Call<SignUpResponse>, response: Response<SignUpResponse>) {

                if(customDialog.isShowing){
                    customDialog.dismiss()
                }

                var data=response.body()

                if(data!=null){
                    if(data.status.equals("true")){
                       // GotoLogin()
                        SharedHelper.putString(this@SignUpActivity,Constants.USER_ID,data.result.userId)
                        SharedHelper.putString(this@SignUpActivity,Constants.USER_ACCOUNT_STATUS,data.result.accountStatus)
                        //GotoVerifyOTP()
                        finish();
                        Utilities.shortToast(resources.getString(R.string.thank_msg),this@SignUpActivity)
                       // Utilities.CheckSessionValid(data.message,this@SignUpActivity,this@SignUpActivity)
                    }else{
                        Utilities.CheckSessionValid(data.message,this@SignUpActivity,this@SignUpActivity)
                    }
                }
            }

            override fun onFailure(call: Call<SignUpResponse>, t: Throwable) {
                  if(customDialog.isShowing){
                    customDialog.dismiss()
                }
            }

        })

    }

    fun GotoLogin(){

        val intent = Intent(this@SignUpActivity, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
        overridePendingTransition(R.anim.activity_back_in, R.anim.activity_back_out)

    }

    fun GotoVerifyOTP(){

        val intent = Intent(this@SignUpActivity, VerificationCodeActivity::class.java)
        startActivity(intent)
        finish()
        overridePendingTransition(R.anim.slide_in, R.anim.slide_out)

    }

    fun getCountries(){

        val call: Call<GetCountriesModel> = Uten.FetchServerData().get_countries()

        call.enqueue(object : Callback<GetCountriesModel> {
            override fun onResponse(call: Call<GetCountriesModel>, response: Response<GetCountriesModel>) {

                var dataCountry=response.body()

                if(dataCountry!=null){

                    if(dataCountry.status.equals("true")){
                        setCountries(dataCountry.result)

                    }else {
                        Utilities.CheckSessionValid(dataCountry.message,this@SignUpActivity,this@SignUpActivity)
                    }
                }
                if(customDialog.isShowing){
                    customDialog.dismiss()
                }
            }

            override fun onFailure(call: Call<GetCountriesModel>, t: Throwable) {
                val  gs = Gson()
                gs.toJson(t.localizedMessage)
                if(customDialog.isShowing){

                    customDialog.dismiss()
                }

                errorLayout.visibility =View.VISIBLE
                mainLayout.visibility = View.GONE

            }

        })
    }

    fun getUserTypes(){

        customDialog.setCancelable(false)
        customDialog.show()


        val call: Call<GetUserTypesModel> = Uten.FetchServerData().get_usertypes()

        call.enqueue(object : Callback<GetUserTypesModel> {
            override fun onResponse(call: Call<GetUserTypesModel>, response: Response<GetUserTypesModel>) {

                val  g = Gson()
                g.toJson(response.body())

                var data = response.body()


                if(data!=null){

                    if(data.status.equals("true")){

                        setUsers(data.result);
                        setType(data.result);
                        getCountries()

                    }else{
                        customDialog.dismiss()
                        Utilities.CheckSessionValid(data.message,this@SignUpActivity,this@SignUpActivity)
                    }
                }
            }

            override fun onFailure(call: Call<GetUserTypesModel>, t: Throwable) {

                customDialog.dismiss()

                errorLayout.visibility =View.VISIBLE
                mainLayout.visibility = View.GONE

            }
        })
    }

    fun getCities(countryId:String){

        val call: Call<GetCitiesModel> = Uten.FetchServerData().get_cities(countryId)

        call.enqueue(object : Callback<GetCitiesModel> {
            override fun onResponse(call: Call<GetCitiesModel>, response: Response<GetCitiesModel>) {

                customDialog.dismiss()

                var data = response.body()

                if(data!=null){

                    if(data.status.equals("true")){
                        setCities(data.result)

                    }else{
                        Utilities.CheckSessionValid(data.message,this@SignUpActivity,this@SignUpActivity)

                    }
                }
            }

            override fun onFailure(call: Call<GetCitiesModel>, t: Throwable) {

                customDialog.dismiss()

                errorLayout.visibility =View.VISIBLE
                mainLayout.visibility = View.GONE

            }
        })
    }

    fun setCountries(data: List<ResultCountries>){

        val list: MutableList<String> = ArrayList()
        val typeLIst: MutableList<String> = ArrayList();


        for (i in 0..data.size-1){
            list.add(data.get(i).name)
        }

        list.add("Select Country")
        var listTrunclate = list.size-1


        val countryAdapter = object :ArrayAdapter<CharSequence>(this,R.layout.spinner_text, list as List<CharSequence>){
            override fun getCount(): Int {
                return (listTrunclate) // Truncate the list
            }
        }
        countryAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown)
        countrySpinner.setAdapter(countryAdapter)
        countrySpinner.setSelection(listTrunclate)


        countrySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                //Toast.makeText(this@SignUpActivity, "Country ID: " + data[position].countryId, Toast.LENGTH_SHORT).show()
                CITY_ID=""

                try {
                    getCities(data[position].countryId)
                    COUNTRY_ID=data[position].countryId
                    customDialog.show()
                    selectCityTV.visibility = View.GONE
                    citySpinner.visibility=View.VISIBLE

                }catch (e:Exception){
                    customDialog.dismiss()
                    selectCityTV.visibility = View.VISIBLE
                    citySpinner.visibility=View.GONE

                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Code to perform some action when nothing is selected
            }
        }
    }
    fun setCities(data: List<ResultCities>){
        val list: MutableList<String> = ArrayList()
        for (i in 0..data.size-1){
            list.add(data.get(i).name)
        }
        list.add("Select City")
        var listTrunclate = list.size-1
        val cityAdapter =object :ArrayAdapter<CharSequence>(this,R.layout.spinner_text, list as List<CharSequence>){
            override fun getCount(): Int {
                return (listTrunclate) // Truncate the list
            }
        }
        cityAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown)
        citySpinner.setAdapter(cityAdapter)
        citySpinner.setSelection(listTrunclate)

        citySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
               // Toast.makeText(this@SignUpActivity, "City ID: " + data[position].cityId, Toast.LENGTH_SHORT).show()
                try {
                    CITY_ID=data[position].cityId
                }catch (e:Exception){
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Code to perform some action when nothing is selected
            }
        }
    }
    fun setUsers(data: Result){

        val list: MutableList<String> = ArrayList()

        for (i in 0..data.result2.size-1){
            list.add(data.result2.get(i).text);
        }
        list.add("Select User Type")

        var listTrunclate = list.size-1

        val usersAdapter = object :ArrayAdapter<String>(this,R.layout.spinner_text, list as List<String>){
            override fun getCount(): Int {
                return (listTrunclate) // Truncate the list
            }
        }
        usersAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown)
        userSpinner.setAdapter(usersAdapter)
        userSpinner.setSelection(listTrunclate)


        userSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // Toast.makeText(this@SignUpActivity, "City ID: " + data[position].cityId, Toast.LENGTH_SHORT).show()
                try {
                    USER_TYPE_ID=data.result2[position].value
                    AppUserType=data.result2[position].value
                    //Show company name field if User type is company
                    if(data.result2[position].value.contentEquals("20")){
                        companyNameRL.visibility = View.VISIBLE;
                        tv_fname_txt.setText(resources.getString(R.string.rep_f_name))
                        tv_l_name_txt.setText(resources.getString(R.string.rep_l_name))

                    }else {
                        companyNameRL.visibility = View.GONE
                        companynameET.setText("");
                        tv_fname_txt.setText(resources.getString(R.string.f_name))
                        tv_l_name_txt.setText(resources.getString(R.string.l_name))

                    }
                }catch (e:Exception){}
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }
    fun setType(data: Result){
        val list: MutableList<String> = ArrayList()

        for (items in data.result1) {
            Log.d("ListResult","--"+items.text);
            list.add(items.text)
        }

      /*  val adapter = ArrayAdapter(this, R.layout.spinner_text, list)
        typeSpinner.adapter = adapter;*/

        val usersAdapter = object :ArrayAdapter<String>(this,R.layout.spinner_text, list as List<String>){
            override fun getCount(): Int {
                return (list.size) // Truncate the list
            }
        }
        usersAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown)
        typeSpinner.setAdapter(usersAdapter);

        typeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                //Toast.makeText(this@SignUpActivity, "City ID: " + data.result1[position].text, Toast.LENGTH_SHORT).show()
                try {
                   Agency=data.result1[position].value;
                }catch (e:Exception){
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
       // userSpinner.setSelection(listTrunclate)
    }

    fun setCountryCode(){

        val list: MutableList<String> = ArrayList()

        list.add("+232")



        /*  val adapter = ArrayAdapter(this, R.layout.spinner_text, list)
          typeSpinner.adapter = adapter;*/

        val usersAdapter = object :ArrayAdapter<String>(this,R.layout.spinner_text, list as List<String>){
            override fun getCount(): Int {
                return (list.size) // Truncate the list
            }
        }
        usersAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown)
        spinner_code.setAdapter(usersAdapter);

        spinner_code.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                //Toast.makeText(this@SignUpActivity, "City ID: " + list[position], Toast.LENGTH_SHORT).show()

            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }


    }


    fun TextView.makeLinks(vararg links: Pair<String, View.OnClickListener>) {
        val spannableString = SpannableString(this.text)
        for (link in links) {
            val clickableSpan = object : ClickableSpan() {
                override fun onClick(view: View) {
                    Selection.setSelection((view as TextView).text as Spannable, 0)
                    view.invalidate()
                    link.second.onClick(view)
                }
            }
            val startIndexOfLink = this.text.toString().indexOf(link.first)
            spannableString.setSpan(clickableSpan, startIndexOfLink, startIndexOfLink + link.first.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        this.movementMethod = LinkMovementMethod.getInstance() // without LinkMovementMethod, link can not click
        this.setText(spannableString, TextView.BufferType.SPANNABLE)
    }



}
