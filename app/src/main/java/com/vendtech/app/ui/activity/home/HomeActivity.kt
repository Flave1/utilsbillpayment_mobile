package com.vendtech.app.ui.activity.home

//import com.vendtech.app.BuildConfig
import android.app.Activity
import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import com.google.gson.Gson
import com.vendtech.app.BuildConfig
import com.vendtech.app.R
import com.vendtech.app.helper.SharedHelper
import com.vendtech.app.models.meter.MeterListResults
import com.vendtech.app.models.profile.GetPendingModel
import com.vendtech.app.models.profile.GetWalletModel
import com.vendtech.app.models.referral.ReferralCodeModel
import com.vendtech.app.network.Uten
import com.vendtech.app.ui.activity.authentication.ForgotPasswordActivity
import com.vendtech.app.ui.activity.authentication.LoginActivity
import com.vendtech.app.ui.activity.meter.MeterListActivity
import com.vendtech.app.ui.activity.number.NumberListActivity
import com.vendtech.app.ui.activity.profile.EditProfileActivity
import com.vendtech.app.ui.activity.profile.NotificationsListActivity
import com.vendtech.app.ui.activity.termspolicies.ContactUsActivity
import com.vendtech.app.ui.activity.termspolicies.TermsPoliciesActivity
import com.vendtech.app.ui.fragment.*
import com.vendtech.app.utils.Constants
import com.vendtech.app.utils.CustomDialog
import com.vendtech.app.utils.Utilities
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.nav_header.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class HomeActivity : AppCompatActivity(), View.OnClickListener, DashBoardFragment.NotificationCount {


    lateinit var imgNav: ImageView
    lateinit var logoHeader: ImageView
    private var drawerLayout: DrawerLayout? = null
    lateinit var navigationView: NavigationView
    lateinit var headerTitle: TextView
    internal var addMeter: TextView? = null

    lateinit var totalAvlblBalance: TextView
    lateinit var totalPendingBalance: TextView
    //NAVIGATION MENU AND HEADER

    lateinit var dashboard_add_bill_payment: LinearLayout
    lateinit var dashboardLL: LinearLayout
    lateinit var posLL: LinearLayout
    lateinit var walletLL: LinearLayout
    lateinit var reportLL: LinearLayout
    lateinit var meterLL: LinearLayout
    lateinit var pendingDepLV: LinearLayout
    lateinit var numberLL: LinearLayout
    lateinit var changepasswordLL: LinearLayout
    lateinit var fl_count: FrameLayout
    lateinit var termsLL: LinearLayout
    lateinit var privacyLL: LinearLayout
    lateinit var shareappLL: LinearLayout
    lateinit var contactUsll: LinearLayout
    lateinit var logoutLL: LinearLayout
//    lateinit var editprofileTV: TextView
    lateinit var userpic: CircleImageView
    lateinit var usernameTV: TextView
    lateinit var vendorTV: TextView
    lateinit var posTV: TextView
    lateinit var editUserProfile: ImageView
    lateinit var notificationCountTV: TextView

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            GetWalletBalance()
            Log.v("SOMETHING ","HAPPENED ")
        }
    }
    override fun onDestroy() {
//        registerReceiver(broadcastReceiver, intentFilter)
        super.onDestroy()
        unregisterReceiver(broadcastReceiver)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        imgNav = findViewById(R.id.imgNav)
        drawerLayout = findViewById(R.id.activity_main)
        navigationView = findViewById(R.id.nv)
        val headerView = navigationView.getHeaderView(0)
        findNavView(headerView)
        findViews()
//        GetWalletBalance()
        Log.v("LinkCode", Utilities.GenerateInviteCode())



        //LoadBillPaymentFragment();
         loadDashBoardFragment();

        val intentFilter = IntentFilter("ACTION_UPDATE_VALUE")
        registerReceiver(broadcastReceiver, intentFilter)
    }


    fun findViews() {

        logoHeader = findViewById<View>(R.id.logoheader) as ImageView
        headerTitle = findViewById<View>(R.id.menuTitle) as TextView

    }

    override fun CountIs(count: String) {
        Log.v("CountIS", count)

        if (!TextUtils.isEmpty(count)) {

            var countIs = count.toInt()

            if (countIs == 0) {

                notificationCountTV.visibility = View.GONE
                notificationCountTV.text = countIs.toString()

            } else if (countIs < 100) {
                notificationCountTV.visibility = View.VISIBLE
                notificationCountTV.text = countIs.toString()

            } else {

                notificationCountTV.visibility = View.VISIBLE
                notificationCountTV.text = countIs.toString() + "+"

            }
        }

    }

    fun findNavView(navigationView: View) {


        dashboardLL = navigationView.findViewById<View>(R.id.dashboardLL) as LinearLayout
        dashboard_add_bill_payment = navigationView.findViewById<View>(R.id.dashboard_add_bill_payment) as LinearLayout
        posLL = navigationView.findViewById<View>(R.id.posLL) as LinearLayout
        walletLL = navigationView.findViewById<View>(R.id.walletLL) as LinearLayout
        reportLL = navigationView.findViewById<View>(R.id.reportLL) as LinearLayout
        meterLL = navigationView.findViewById<View>(R.id.meterLL) as LinearLayout
        numberLL = navigationView.findViewById<View>(R.id.numberLL) as LinearLayout
        changepasswordLL = navigationView.findViewById<View>(R.id.changepassLL) as LinearLayout
        fl_count = navigationView.findViewById<View>(R.id.fl_count) as FrameLayout
        termsLL = navigationView.findViewById<View>(R.id.tcLL) as LinearLayout
        privacyLL = navigationView.findViewById<View>(R.id.privacyLL) as LinearLayout
        logoutLL = navigationView.findViewById<View>(R.id.logoutLL) as LinearLayout
        contactUsll = navigationView.findViewById<View>(R.id.contactusLL) as LinearLayout
        shareappLL = navigationView.findViewById<View>(R.id.shareappLL) as LinearLayout
//        editprofileTV = navigationView.findViewById<View>(R.id.editprofileTV) as TextView
        usernameTV = navigationView.findViewById<View>(R.id.usernameTV) as TextView
        vendorTV = navigationView.findViewById<View>(R.id.vendorTV) as TextView
        posTV = navigationView.findViewById<View>(R.id.posTV) as TextView
        userpic = navigationView.findViewById<View>(R.id.userpic) as CircleImageView
        editUserProfile = navigationView.findViewById<View>(R.id.editUserProfile) as ImageView
        notificationCountTV = navigationView.findViewById<View>(R.id.notificationCountTV) as TextView
        totalAvlblBalance = navigationView.findViewById<View>(R.id.totalAvlblBalance) as TextView
        totalPendingBalance = navigationView.findViewById<View>(R.id.totalPendingBalance) as TextView
        pendingDepLV = navigationView.findViewById<View>(R.id.pendingDepLV) as LinearLayout
//        pendingDepLV = findViewById(R.id.pendingDepLV) as LinearLayout
        reportLL.setOnClickListener(this)

        posLL.setOnClickListener(this)
        dashboard_add_bill_payment.setOnClickListener(this)
        dashboardLL.setOnClickListener(this)
        walletLL.setOnClickListener(this)
        meterLL.setOnClickListener(this)
        numberLL.setOnClickListener(this)
        changepasswordLL.setOnClickListener(this)
        termsLL.setOnClickListener(this)
        privacyLL.setOnClickListener(this)
        logoutLL.setOnClickListener(this)
        contactUsll.setOnClickListener(this)
        userpic.setOnClickListener(this)
        editUserProfile.setOnClickListener(this)
//        editprofileTV.setOnClickListener(this)
        shareappLL.setOnClickListener(this)
        fl_count.setOnClickListener(this)

        SetUpProfile()

        SetUpMenuOptions()

        imgNav.setOnClickListener { v ->
            if (drawerLayout!!.isDrawerOpen(Gravity.START))
                drawerLayout!!.closeDrawer(Gravity.START)
            else
                drawerLayout!!.openDrawer(Gravity.START)
        }

    }

    fun SetUpProfile() {
        usernameTV.setText(SharedHelper.getString(this, Constants.USER_FNAME) + " " + SharedHelper.getString(this, Constants.USER_LNAME))
        posTV.setText(SharedHelper.getString(this, Constants.POS_NUMBER))
        vendorTV.setText(SharedHelper.getString(this, Constants.VENDOR))
        Glide.with(this).load(SharedHelper.getString(this, Constants.USER_AVATAR)).asBitmap().error(R.drawable.dummyuser).into(userpic)

    }

    fun SetUpMenuOptions() {
        getNavigationList()


        //Check whether user account status is Active or Pending
        if (SharedHelper.getString(this, Constants.USER_ACCOUNT_STATUS).equals(Constants.STATUS_ACTIVE)) {

            walletLL.alpha = 1.toFloat()
            meterLL.alpha = 1.toFloat()
            numberLL.alpha = 1.toFloat()
            //notificationLL.alpha = 1.toFloat()

            walletLL.isEnabled = true
            meterLL.isEnabled = true
            numberLL.isEnabled = true
            //notificationLL.isEnabled = true

        } else {

            walletLL.alpha = 0.5.toFloat()
            meterLL.alpha = 0.5.toFloat()
            numberLL.alpha = 0.5.toFloat()
            //notificationLL.alpha = 0.5.toFloat()

            walletLL.isEnabled = false
            meterLL.isEnabled = false
            numberLL.isEnabled = false
            //notificationLL.isEnabled = false

        }


    }

    override fun onClick(v: View) {

        when (v.id) {

            R.id.dashboardLL -> {
                if (drawerLayout!!.isDrawerOpen(Gravity.START))
                    drawerLayout!!.closeDrawer(Gravity.START)
                Handler().postDelayed({ loadDashBoardFragment() }, 400)
            }
            R.id.dashboard_add_bill_payment -> {
                if (drawerLayout!!.isDrawerOpen(Gravity.START))
                    drawerLayout!!.closeDrawer(Gravity.START)

//                Handler().postDelayed({ LoadBillPaymentFragment() }, 400)
                Handler().postDelayed({
                    val i = Intent(this@HomeActivity, BillPaymentActivity::class.java)
                    startActivity(i)
                    overridePendingTransition(R.anim.slide_in, R.anim.slide_out)
                }, 400)
            }
            R.id.posLL -> {
                if (drawerLayout!!.isDrawerOpen(Gravity.START))
                    drawerLayout!!.closeDrawer(Gravity.START)

//                Handler().postDelayed({ LoadPosListFragment() }, 400)
                Handler().postDelayed({
                    val i = Intent(this@HomeActivity, PosListActivity::class.java)
                    startActivity(i)
                    overridePendingTransition(R.anim.slide_in, R.anim.slide_out)
                }, 400)
            }
            R.id.walletLL -> {
                if (drawerLayout!!.isDrawerOpen(Gravity.START))
                    drawerLayout!!.closeDrawer(Gravity.START)
//                Handler().postDelayed({ LoadWalletFragment() }, 400)
                Handler().postDelayed({
                    val i = Intent(this@HomeActivity, WalletActivity::class.java)
                    startActivity(i)
                    overridePendingTransition(R.anim.slide_in, R.anim.slide_out)
                }, 400)
            }

            R.id.reportLL -> {
                if (drawerLayout!!.isDrawerOpen(Gravity.START))
                    drawerLayout!!.closeDrawer(Gravity.START)
//                Handler().postDelayed({ LoadReportsFragment() }, 400)
                Handler().postDelayed({
                    val i = Intent(this@HomeActivity, ReportsActivity::class.java)
                    startActivity(i)
                    overridePendingTransition(R.anim.slide_in, R.anim.slide_out)
                }, 400)
            }
            R.id.meterLL -> {
                  if (drawerLayout!!.isDrawerOpen(Gravity.START))
                     drawerLayout!!.closeDrawer(Gravity.START)

                Handler().postDelayed({
                    val i = Intent(this@HomeActivity, MeterListActivity::class.java)
                    startActivity(i)
                    overridePendingTransition(R.anim.slide_in, R.anim.slide_out)
                }, 400)
            }
            R.id.numberLL -> {
                if (drawerLayout!!.isDrawerOpen(Gravity.START))
                    drawerLayout!!.closeDrawer(Gravity.START)

                Handler().postDelayed({
                    val i = Intent(this@HomeActivity, NumberListActivity::class.java)
                    startActivity(i)
                    overridePendingTransition(R.anim.slide_in, R.anim.slide_out)
                }, 400)
            }
            R.id.changepassLL -> {
                /* if (drawerLayout!!.isDrawerOpen(Gravity.START))
                     drawerLayout!!.closeDrawer(Gravity.START)*/

//                Handler().postDelayed({
//                    val i = Intent(this@HomeActivity, ChangePasswordActivity::class.java)
//                    startActivity(i)
//                    overridePendingTransition(R.anim.slide_in, R.anim.slide_out)
//                }, 400)

                SharedHelper.removeUserData(this)
                SharedHelper.putBoolean(this, Constants.IS_LOGGEDIN, false)
                GotoForgotPassword()
                this.finish()
            }
            R.id.tcLL -> {
                /* if (drawerLayout!!.isDrawerOpen(Gravity.START))
                     drawerLayout!!.closeDrawer(Gravity.START)*/

                Handler().postDelayed({
                    val i = Intent(this@HomeActivity, TermsPoliciesActivity::class.java)
                    i.putExtra("title", "OUR POLICIES")
                    i.putExtra("type", "1")
                    startActivity(i)
                    overridePendingTransition(R.anim.slide_in, R.anim.slide_out)
                }, 400)
            }
            R.id.shareappLL -> {

                if (drawerLayout!!.isDrawerOpen(Gravity.START))
                    drawerLayout!!.closeDrawer(Gravity.START)
                Handler().postDelayed({
                    GenerateRefferalCode()
                }, 400)
            }
            R.id.fl_count -> {
                Handler().postDelayed({
                    val i = Intent(this@HomeActivity, NotificationsListActivity::class.java)
                    startActivity(i)
                    overridePendingTransition(R.anim.slide_in, R.anim.slide_out)

                }, 400)
            }
            R.id.contactusLL -> {
                /*  if (drawerLayout!!.isDrawerOpen(Gravity.START))
                      drawerLayout!!.closeDrawer(Gravity.START)
  */
                Handler().postDelayed({
                    val i = Intent(this@HomeActivity, ContactUsActivity::class.java)
                    startActivity(i)
                    overridePendingTransition(R.anim.slide_in, R.anim.slide_out)
                }, 400)
            }
            R.id.logoutLL -> {
                /*  if (drawerLayout!!.isDrawerOpen(Gravity.START))
                      drawerLayout!!.closeDrawer(Gravity.START)*/

                Handler().postDelayed({

                    ShowAlertForLogout()

                }, 400)
            }
            R.id.editUserProfile -> {
                /* if (drawerLayout!!.isDrawerOpen(Gravity.START))
                     drawerLayout!!.closeDrawer(Gravity.START)*/

                Handler().postDelayed({
                    val i = Intent(this@HomeActivity, EditProfileActivity::class.java)
                    startActivity(i)
                    overridePendingTransition(R.anim.slide_in, R.anim.slide_out)
                }, 400)
            }
            R.id.userpic -> {
                /* if (drawerLayout!!.isDrawerOpen(Gravity.START))
                     drawerLayout!!.closeDrawer(Gravity.START)*/

                Handler().postDelayed({
                    val i = Intent(this@HomeActivity, EditProfileActivity::class.java)
                    startActivity(i)
                    overridePendingTransition(R.anim.slide_in, R.anim.slide_out)
                }, 400)
            }
//            R.id.editprofileTV -> {
//                /* if (drawerLayout!!.isDrawerOpen(Gravity.START))
//                     drawerLayout!!.closeDrawer(Gravity.START)
// */
//                Handler().postDelayed({
//                    val i = Intent(this@HomeActivity, EditProfileActivity::class.java)
//                    startActivity(i)
//                    overridePendingTransition(R.anim.slide_in, R.anim.slide_out)
//                }, 400)
//            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode==1201){
            //Toast.makeText(this,"fnksfnksnfksnf",Toast.LENGTH_LONG).show();

            if (resultCode== Activity.RESULT_OK) {
                var data: MeterListResults = data!!.getSerializableExtra("data") as MeterListResults
                Handler().postDelayed({ LoadBillPaymentFragment(data) }, 400)
            }
        }
    }

    fun GenerateRefferalCode() {

        var customDialog: CustomDialog
        customDialog = CustomDialog(this)
        customDialog.show()

        val call: Call<ReferralCodeModel> = Uten.FetchServerData().generate_referral_code(SharedHelper.getString(this, Constants.TOKEN))
        call.enqueue(object : Callback<ReferralCodeModel> {
            override fun onResponse(call: Call<ReferralCodeModel>, response: Response<ReferralCodeModel>) {

                if (customDialog.isShowing) {
                    customDialog.dismiss()
                }

                var data = response.body()
                if (data != null) {
                    if (data.status.equals("true")) {
                        ShareAppLink(data.result.code)
                    } else {
                        Utilities.CheckSessionValid(data.message, this@HomeActivity, this@HomeActivity)
                    }
                }
            }

            override fun onFailure(call: Call<ReferralCodeModel>, t: Throwable) {
                val gs = Gson()
                gs.toJson(t.localizedMessage)
                Utilities.shortToast("Something went wrong", this@HomeActivity)
                if (customDialog.isShowing) {
                    customDialog.dismiss()
                }
            }
        })

    }

    fun ShareAppLink(code: String) {
        try {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "VendTech")
            var shareMessage = "\nLet me recommend you this application. Please install this application and use my referral code \"$code\" on Sign Up \n\n"
            shareMessage = shareMessage + "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID + "\n\n"
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
            startActivity(Intent.createChooser(shareIntent, "Choose one"))
        } catch (e: Exception) {

        }
    }

    fun ShowAlertForLogout() {

        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.app_name)
        builder.setMessage("Please confirm Logout!!!")
        builder.setIcon(R.drawable.appicon)
        builder.setPositiveButton("Confirm") { dialogInterface, which ->

            SharedHelper.removeUserData(this)
            SharedHelper.putBoolean(this, Constants.IS_LOGGEDIN, false)
            val i = Intent(this@HomeActivity, LoginActivity::class.java)
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(i)
            var vv=SharedHelper.getString(this@HomeActivity, Constants.POS_NUMBER)
            finish()
        }
        builder.setNegativeButton("Cancel") { dialogInterface, which ->
        }
        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()

    }

    fun ShowTitle() {
        logoHeader.visibility = View.GONE
        headerTitle.visibility = View.VISIBLE
    }

    fun ShowLogo() {
        logoHeader.visibility = View.VISIBLE
        headerTitle.visibility = View.GONE
    }

    private fun loadDashBoardFragment(){
        ShowLogo()
        val fragment = DashBoardFragment.newInstance();
      //  fragment.setArguments(bundle);

        val ft = supportFragmentManager.beginTransaction()
        ft.replace(R.id.fragment_frame, fragment)
        ft.commit()
    }

    fun LoadBillPaymentFragment(data:MeterListResults?=null) {

//        val bundle = Bundle();
//        if (data!=null){
//            bundle.putSerializable("data",data);
//        }
        ShowLogo()



//        val fragment = BillPaymentFragment.newInstance();
//        fragment.setArguments(bundle);
//
//        val ft = supportFragmentManager.beginTransaction()
//        ft.replace(R.id.fragment_frame, fragment)
//        ft.commit()

        Handler().postDelayed({
            val i = Intent(this@HomeActivity, BillPaymentActivity::class.java)
            i.putExtra("data", data)
            startActivityForResult(i,1201)
            overridePendingTransition(R.anim.slide_in, R.anim.slide_out)
        }, 400)
    }

    fun LoadWalletFragment() {

        ShowTitle()
        headerTitle.text = "MANAGE WALLET"

//        val fragment = WalletFragment.newInstance()
//        val ft = supportFragmentManager.beginTransaction()
//        ft.replace(R.id.fragment_frame, fragment)
//        ft.commit()
    }

    fun LoadReportsFragment() {

        ShowTitle()
        headerTitle.text = "REPORTS"

//        val fragment = ReportsFragment.newInstance()
//        val ft = supportFragmentManager.beginTransaction()
//        ft.replace(R.id.fragment_frame, fragment)
//        ft.commit()
    }

    fun LoadPosListFragment() {
        ShowTitle()
        headerTitle.text = "POS"

//        val fragment = PosListFragment.newInstance()
//        val ft = supportFragmentManager.beginTransaction()
//        ft.replace(R.id.fragment_frame, fragment)
//        ft.commit()
    }

    fun getNavigationList() {

        var customDialog: CustomDialog
        customDialog = CustomDialog(this)
        customDialog.show();
        val call: Call<NavigationListModel> = Uten.FetchServerData().get_navigation(SharedHelper.getString(this, Constants.TOKEN), SharedHelper.getString(this, Constants.USER_ID))
        call.enqueue(object : Callback<NavigationListModel> {
            override fun onResponse(call: Call<NavigationListModel>, response: Response<NavigationListModel>) {

                if (customDialog.isShowing) {
                    customDialog.dismiss()
                }

                var data = response.body()
                if (data != null) {
                    if (data.status.equals("true")) {
                        //DO CODE
                        Log.e("", "");

                        changepassLL.visibility = VISIBLE

                        dashboard_add_bill_payment.visibility = GONE
                        meterLL.visibility = GONE
                        numberLL.visibility = GONE
                        walletLL.visibility = GONE
                        reportLL.visibility = GONE

                        if (data.result.size > 0) {

                            for (j in 0 until data.result.size) {

                                if(Constants.NAV4_Bill_Payment.equals(data.result[j].modules)){
                                    dashboard_add_bill_payment.visibility = VISIBLE
                                }
                                if(data.result.any{it.modules == Constants.NAV6_Saved_Numbers}){
                                    if(Constants.NAV5_Saved_Meters.equals(data.result[j].modules)) {
                                        meterLL.visibility = GONE
                                    }else{
                                        meterLL.visibility = VISIBLE
                                    }
                                }
                                if(data.result.any{it.modules == Constants.NAV6_Saved_Numbers}){
                                    if(Constants.NAV9_PhoneNumbers.equals(data.result[j].modules)) {
                                        numberLL.visibility = GONE
                                    }else{
                                        numberLL.visibility = VISIBLE
                                    }
                                }
                                if(Constants.NAV3_Manage_Wallet.equals(data.result[j].modules)){
                                    walletLL.visibility = VISIBLE
                                }
                                if(Constants.NAV7_MANAGE_REPORTS.equals(data.result[j].modules)){
                                    reportLL.visibility = VISIBLE
                                }

                            }
//                            var listMenu = ArrayList<String>()
//                            listMenu.add(Constants.NAV5_Saved_Meters)
//                            listMenu.add(Constants.NAV6_Saved_Numbers)
//                            listMenu.add(Constants.NAV3_Manage_Wallet)
//                            listMenu.add(Constants.NAV7_MANAGE_REPORTS)
//                            listMenu.add(Constants.NAV8_RESET_PASSCODE)
//                            listMenu.add(Constants.NAV4_Bill_Payment)
//                            for (i in 0 until listMenu.size) {
//                                for (j in 0 until data.result.size) {
//                                    if (listMenu[i].equals(data.result[j].modules, ignoreCase = true)) {
//                                        dashboard_add_bill_payment.visibility = VISIBLE
//                                        meterLL.visibility = VISIBLE
//                                        if (i == 0) {
//                                            meterLL.visibility = VISIBLE
//                                            numberLL.visibility = VISIBLE
//                                        }
//                                        else if (i == 1)
//                                            walletLL.visibility = VISIBLE
//                                        else if (i == 2)
//                                            reportLL.visibility = VISIBLE
//                                        else if (i == 3)
//                                            changepassLL.visibility = VISIBLE
//                                        else if (i == 4)
//                                            dashboard_add_bill_payment.visibility = VISIBLE
//                                        break
//                                    }
//                                }
//                            }
                        }
                    } else {
                        Utilities.CheckSessionValid(data.message, this@HomeActivity, this@HomeActivity)
                    }
                }
            }

            override fun onFailure(call: Call<NavigationListModel>, t: Throwable) {
                val gs = Gson()
                gs.toJson(t.localizedMessage)
                Utilities.shortToast("Something went wrong", this@HomeActivity)
                if (customDialog.isShowing) {
                    customDialog.dismiss()
                }
            }
        })

    }

    fun GotoForgotPassword() {

        val intent = Intent(this@HomeActivity, ForgotPasswordActivity::class.java)
        startActivity(intent)

        overridePendingTransition(R.anim.slide_in, R.anim.slide_out)
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
                        pendingDepLV.visibility = GONE
                        totalAvlblBalance.setText(code+" :  " +data.result.stringBalance)
                        GetPendingBalance()
                    } else {
                        //Utilities.CheckSessionValid(data.message, this@RechargeActivity, this@RechargeActivity)
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

    fun GetPendingBalance() {

        var customDialog: CustomDialog
        customDialog = CustomDialog(this)
        customDialog.show()

        var code = SharedHelper.getString(this, Constants.CURRENCY_CODE);
        val call: Call<GetPendingModel> = Uten.FetchServerData().get_pending_balance(SharedHelper.getString(this, Constants.TOKEN))
        call.enqueue(object : Callback<GetPendingModel> {
            override fun onResponse(call: Call<GetPendingModel>, response: Response<GetPendingModel>) {

                if (customDialog.isShowing) {
                    customDialog.dismiss()
                }
                var data = response.body()
                if (data != null) {
                    if (data.status.equals("true")) {
                        if(data.result.isDepositPending){
                            pendingDepLV.visibility = VISIBLE
                            totalPendingBalance.setText("PENDING APPROVAL:  " +code+" :  " +data.result.pendingDepositBalance)
                        }else{
                            totalPendingBalance.setText(code+" :  " +data.result.pendingDepositBalance)
                            pendingDepLV.visibility = GONE
                        }
                    } else {
                        //Utilities.CheckSessionValid(data.message, this@RechargeActivity, this@RechargeActivity)
                    }
                }
            }

            override fun onFailure(call: Call<GetPendingModel>, t: Throwable) {
                val gs = Gson()
                gs.toJson(t.localizedMessage)
                if (customDialog.isShowing) {
                    customDialog.dismiss()
                }
            }

        })
    }

    override fun onResume() {

        if (Uten.isInternetAvailable(this)) {
            GetWalletBalance()
        } else {
//            Toast(this, "No internet connection. Please check your network connectivity.")
        }
        super.onResume()
    }
}
