package com.vendtech.app.network

import com.vendtech.app.models.airtime.AirtimeRechargeModel
import com.vendtech.app.models.authentications.*
import com.vendtech.app.models.meter.*
import com.vendtech.app.models.profile.*
import com.vendtech.app.models.referral.ReferralCodeModel
import com.vendtech.app.models.termspolicies.ContactUsModel
import com.vendtech.app.models.termspolicies.TermsPoliciesModel
import com.vendtech.app.models.transaction.*
import com.vendtech.app.ui.activity.home.NavigationListModel
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface ApiInterface {

    @GET("Account/GetCountries")
    fun get_countries(): Call<GetCountriesModel>

    @GET("Account/GetAppUserTypes")
    fun get_usertypes(): Call<GetUserTypesModel>

    @GET("Account/GetCities")
    fun get_cities(@Query("countryId") countryId: String): Call<GetCitiesModel>

    @GET("Account/IsUserNameExists")
    fun check_username(@Query("userName") countryId: String): Call<CheckUsernameModel>

    @FormUrlEncoded
    @POST("Account/SignUp")
    fun sign_up(@Field("Agency") Agency: String,@Field("Email") Email: String, /*@Field("Password") Password: String ,*/@Field("FirstName") FirstName: String, @Field("LastName") LastName: String/*, @Field("UserName") UserName: String*/, @Field("UserType") UserType: String, @Field("CompanyName") CompanyName: String, @Field("Country") Country: String, @Field("City") City: String, @Field("Address") Address: String, @Field("Phone") Phone: String, @Field("ReferralCode") ReferralCode: String, @Field("DeviceType") DeviceType: String, @Field("AppType") AppType: String, @Field("DeviceToken") DeviceToken: String, @Field("AppUserType") AppUserType: String
    ): Call<SignUpResponse>

    @FormUrlEncoded
    @POST("Account/VerifyAccountVerificationCode")
    fun verify_otp(@Field("Code") Code: String, @Field("UserId") UserId: String): Call<VerifyOTPModel>

    @POST("Account/ResendAccountVerificationOtp")
    fun resend_otp(@Query("userId") userId: String): Call<ResendOTPModel>

//    @FormUrlEncoded
//    @POST("Account/SignIn")
//    fun sign_in(@Field("Email") email: String,@Field("Password") password:String,@Field("DeviceToken") device_token:String,@Field("AppType") AppType:String):Call<SignInResponse>

    @FormUrlEncoded
    @POST("Account/SignIn")
    fun sign_in(@Field("passCode") password: String, @Field("AppVersion") AppVersion: String, @Field("DeviceToken") device_token: String, @Field("AppType") AppType: String): Call<SignInResponse>

    @FormUrlEncoded
    @POST("Account/SignInNewpasscode")
    fun sign_in_new_passcode(@Field("passCode") password: String, @Field("DeviceToken") device_token: String, @Field("AppType") AppType: String, @Field("UserId") UserId: String): Call<SignInResponse>


    @FormUrlEncoded
    @POST("Meter/SaveMeter")
    fun add_meter(@Header("token") Token: String, @Field("Name") Name: String, @Field("MeterMake") MeterMake: String, @Field("Address") Address: String, @Field("Number") Number: String, @Field("alias") alias: String, @Field("isVerified") isVerified: Boolean): Call<AddMeterModel>


    @FormUrlEncoded
    @POST("Meter/SaveMeter")
    fun update_meter(@Header("token") Token: String, @Field("Name") Name: String, @Field("MeterMake") MeterMake: String, @Field("Address") Address: String, @Field("Number") Number: String, @Field("meterId") meterId: String, @Field("alias") alias: String, @Field("isVerified") isVerified: Boolean): Call<AddMeterModel>

    @GET("Meter/GetMeters")
    fun get_meters(@Header("token") Token: String, @Query("pageNo") pageNo: String, @Query("pageSize") pageSize: String): Call<GetMetersModel>

    @POST("Meter/DeleteMeter")
    fun delete_meter(@Header("token") Token: String, @Query("meterId") meterId: String): Call<DeleteMeterModel>

    @FormUrlEncoded
    @POST("Number/SaveNumber")
    fun add_number(@Header("token") Token: String, @Field("Name") Name: String, @Field("MeterMake") MeterMake: String, @Field("Number") Number: String, @Field("alias") alias: String, @Field("isVerified") isVerified: Boolean): Call<AddMeterModel>

    @FormUrlEncoded
    @POST("Number/SaveNumber")
    fun update_number(@Header("token") Token: String, @Field("Name") Name: String, @Field("MeterMake") MeterMake: String, @Field("Number") Number: String, @Field("meterId") meterId: String, @Field("alias") alias: String, @Field("isVerified") isVerified: Boolean): Call<AddMeterModel>

    @GET("Number/GetNumbers")
    fun get_numbers(@Header("token") Token: String, @Query("pageNo") pageNo: String, @Query("pageSize") pageSize: String): Call<GetMetersModel>

    @POST("Number/DeletePhone")
    fun delete_number(@Header("token") Token: String, @Query("meterId") meterId: String): Call<DeleteMeterModel>


    @FormUrlEncoded
    @POST("Account/GetPOSUserDetails")
    fun getPosUserDetails(@Field("PosNumber") PosNumber: String): Call<SignInResponse>

    @FormUrlEncoded
    @POST("Account/ForgotPasscode")
    fun forgot_password(@Field("Email") email: String, @Field("Phone") phone: String,@Field("PosNumber") PosNumber: String): Call<ForgotPasswordModel>

    @FormUrlEncoded
    @POST("Account/ForgotPasscode2")
    fun forgot_passcode(@Field("Email") email: String,@Field("PosNumber") PosNumber: String): Call<ForgotPasswordModel>

    @GET("User/GetUserProfile")
    fun get_user_profile(@Header("token") Token: String): Call<GetProfileModel>

    @GET("User/GetUserAssignedPlatforms")
    fun get_user_GetUserAssignedPlatforms(@Header("token") Token: String): Call<GetProfileModel>

    @Multipart
    @POST("User/UpdateUserProfile")
    fun update_profile(@Header("token") Token: String, @Part("Name") Name: RequestBody, @Part("SurName") SurName: RequestBody, @Part("Phone") Phone: RequestBody, @Part("City") City: RequestBody, @Part("Country") Country: RequestBody, @Part("Address") Address: RequestBody, @Part() file: MultipartBody.Part?): Call<UpdateProfileModel>;

    @FormUrlEncoded
    @POST("User/ChangePassword")
    fun change_password(@Header("token") Token: String, @Field("OldPassword") OldPassword: String, @Field("Password") Password: String, @Field("ConfirmPassword") ConfirmPassword: String): Call<ChangePasswordModel>

    @FormUrlEncoded
    @POST("User/VerifyChangePasswordOTP")
    fun change_password_OTP_verification(@Header("token") Token: String, @Field("OldPassword") OldPassword: String, @Field("Password") Password: String, @Field("ConfirmPassword") ConfirmPassword: String, @Field("Otp") OTP: String): Call<ChangePasswordOTPModel>

    @GET("User/GetWalletBalance")
    fun get_wallet_balance(@Header("token") Token: String): Call<GetWalletModel>

    @GET("Account/GetBankAccountsSelectList")
    fun getBankDetail(@Header("token") Token: String): Call<BankResponseModel>

    @GET("Account/GetBankNamesForCheque")
    fun getBankNames(@Header("token") Token: String): Call<BankNameModel>

    @GET("Deposit/GetPaymentTypes")
    fun getPaymentTypes(@Header("token") Token: String): Call<PaymentTypeModel>


    @GET("User/GetUserPosPagingList")
    fun getPosList(@Header("token") Token: String, @Query("pageNo") pageNo: Int, @Query("pageSize") pageSize: Int): Call<PosListModel>

    @FormUrlEncoded
    @POST("Report/GetSalesReport")
    fun getSalesReport(@Header("token") token: String, @Field("PosId") posId: Int, @Field("From") from: String, @Field("To") to: String, @Field("Meter") meter: String, @Field("TransactionId") transactionId: String, @Field("PageNo") pageNo: Int, @Field("RecordsPerPage") recordsPerPage: Int, @Field("Product") product: String): Call<RechargeTransactionNewListModel>

    @FormUrlEncoded
    @POST("Report/GetDepositReport")
    fun getDepositReports(@Header("token") token: String, @Field("PosId") posId: Int, @Field("From") from: String, @Field("To") to: String, @Field("Meter") meter: String, @Field("RefNumber") refNumber: String, @Field("TransactionId") transactionId: String, @Field("Bank") bank: String, @Field("DepositType") depositType: Int, @Field("PageNo") pageNo: Int, @Field("RecordsPerPage") recordsPerPage: Int): Call<DepositTransactionNewListModel>

    @FormUrlEncoded
    @POST("Report/CreateEdsaAsPDF")
    fun fetch_receipt_as_pdf(@Header("token")token: String, @Field( "Target")target: String): Call<FetchTransactionASPDFModel>

    @FormUrlEncoded
    @POST("Report/DeleteFileFromDirectory")
    fun remove_file_from_dir(@Header("token")token: String, @Field("Target")target: String): Call<FetchTransactionASPDFModel>


    @FormUrlEncoded
    @POST("Deposit/SaveDepositRequest")
    fun deposit_request(@Header("token") Token: String,
                        @Field("PosId") posId: Int,
                        @Field("BankAccountId") bankAccountId: String,
                        @Field("DepositType") depositType: String,
                        @Field("ChkOrSlipNo") ChkOrSlipNo: String,
                        @Field("ChkBankName") bankName: String?,
                        @Field("NameOnCheque") nameOnCheque: String?,
                        @Field("Amount") amount: String,
                        @Field("TotalAmountWithPercentage") totalAmountWithPercentage: String,
                        @Field("ValueDate") ValueDate:String,
                        @Field("ContinueDepoit") continueDepoit: Int): Call<DepositRequestModel>
    //fun deposit_request(@Header("token") Token: String, @Field("PosId") posId: Int, @Field("BankAccountId") bankAccountId: String, @Field("DepositType") depositType: String, @Field("ChkOrSlipNo") ChkOrSlipNo: String, @Field("ChkBankName") bankName: String?, @Field("NameOnCheque") nameOnCheque: String?, @Field("Amount") amount: String, @Field("TotalAmountWithPercentage") totalAmountWithPercentage: String): Call<DepositRequestModel>

    /* @FormUrlEncoded
    @POST("Meter/RechargeMeter")
    fun recharge_meter(@Header("token") Token: String, @Field("MeterId") MeterId: String?, @Field("Amount") Amount: String, @Field("MeterNumber") MeterNumber: String?, @Field("POSId") posId: Int,@Field("passCode") passCode: String): Call<RechargeMeterModel>*/

    @GET("User/GetUserPos")
    fun getPosList(@Header("token") Token: String): Call<PosResultModel>

    @GET("Account/GetTermsAndConditions")
    fun get_terms(): Call<TermsPoliciesModel>

    @GET("Account/GetPrivacyPolicy")
    fun get_policies(): Call<TermsPoliciesModel>

    @FormUrlEncoded
    @POST("ContactUs/SaveRequest")
    fun contact_us(@Header("token") Token: String, @Field("Subject") subject: String, @Field("Message") message: String): Call<ContactUsModel>

    @GET("Meter/GetUserMeterRecharges")
    fun get_meter_recharges(@Header("token") Token: String, @Query("pageNo") pageNo: String, @Query("pageSize") pageSize: String): Call<RechargeTransactionNewListModel>

    @GET("Deposit/GetDeposits")
    fun get_deposits(@Header("token") Token: String, @Query("pageNo") pageNo: String, @Query("pageSize") pageSize: String): Call<DepositTransactionNewListModel>

    @GET("Meter/GetMeterRechargePdf")
    fun get_rechargedetail_pdf(@Header("token") Token: String, @Query("rechargeId") rechargeId: String): Call<RechargeTransactionInvoiceModel>

    @GET("Meter/GetRechargeDetail")
    fun get_rechargedetail(@Header("token") Token: String, @Query("rechargeId") rechargeId: String): Call<RechargeTransactionDetails>

    @GET("Deposit/GetDepositPdf")
    fun get_depositdetail_pdf(@Header("token") Token: String, @Query("depositId") rechargeId: String): Call<DepositTransactionInvoiceModel>

    @GET("Deposit/GetDepositDetail")
    fun get_depositdetail(@Header("token") Token: String, @Query("depositId") rechargeId: String): Call<DepositTransactionDetails>

    @GET("Dashboard/GetNavigations")
    fun get_navigation(@Header("token") Token: String, @Query("userId") usrid: String): Call<NavigationListModel>

    @POST("User/GenerateReferralCode")
    fun generate_referral_code(@Header("token") Token: String): Call<ReferralCodeModel>

    @GET("User/GetUserAssignedPlatforms")
    fun user_assigned_services(@Header("token") Token: String): Call<UserAssignedServicesModel>

    @GET("Account/GetBankAccounts")
    fun bank_details(@Header("token") Token: String): Call<BankDetailsModel>

    //@GET("User/GetNotifications")
    @GET("User/GetUserNotificationsApi")
    fun get_notifications(@Header("token") Token: String, @Query("pageNo") pageNo: String, @Query("pageSize") pageSize: String): Call<NotificationListModel>

    @FormUrlEncoded
    @POST("Meter/RechargeMeterReceipt")
    fun rechargeMeter(@Header("token")token: String, @Field("Amount")Amount:String, @Field("MeterId")MeterId:String, @Field("POSId")POSId:String, @Field("MeterNumber")MeterNumber:String): Call<RechargeMeterModel>

    @FormUrlEncoded
    @POST("Meter/TransactionDetail")
    fun getTransactionPintDetails(@Header("token")token: String, @Field("Token")Token:String): Call<RechargeMeterModel>

    @FormUrlEncoded
    @POST("Meter/SendSmsOnRecharge")
    fun send_sms_on_recharge(@Header("token")token: String, @Field( "TransactionId")TransactionId: String, @Field("PhoneNo")PhoneNo:String): Call<SendTransactionSmsModel>

    @FormUrlEncoded
    @POST("Meter/SendViaEmail")
    fun send_receipt_email(@Header("token")token: String, @Field( "TransactionId")TransactionId: String, @Field("Email")Email:String): Call<SendTransactionSmsModel>

    @FormUrlEncoded
    @POST("Airtime/RechargePhone")
    fun buyAirtime(@Header("token")token: String, @Field("Amount")Amount:String, @Field("PlatformId")PlatformId:String, @Field("PosId")PosId:String, @Field("Phone")Phone:String, @Field("UserId")UserId:String, @Field("Currency")Currency:String): Call<AirtimeRechargeModel>

    @FormUrlEncoded
    @POST("Airtime/TransactionDetail")
    fun getAirtimeTransactionPintDetails(@Header("token")token: String, @Field("Token")Token:String): Call<RechargeMeterModel>
}