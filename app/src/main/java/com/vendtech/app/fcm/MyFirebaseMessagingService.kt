package com.vendtech.app.fcm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import com.vendtech.app.R
import com.vendtech.app.helper.SharedHelper
import com.vendtech.app.models.profile.GetWalletModel
import com.vendtech.app.network.Uten
import com.vendtech.app.ui.activity.home.HomeActivity
import com.vendtech.app.ui.activity.transaction.DepositTransactionDetails
import com.vendtech.app.ui.activity.transaction.RechargeTransactionDetails
import com.vendtech.app.utils.Constants
import com.vendtech.app.utils.CustomDialog
import com.vendtech.app.utils.MessageEvent
import com.vendtech.app.utils.Utilities
import org.greenrobot.eventbus.EventBus
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val TAG = "MyFirebaseToken"
    private lateinit var notificationManager: NotificationManager
    private val ADMIN_CHANNEL_ID = "com.vendtech.app"

    override fun onNewToken(token: String?) {
        super.onNewToken(token)
        Log.i(TAG, token!!)
        SharedHelper.putString(this,Constants.DEVICE_TOKEN,token.toString())
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage?) {
        super.onMessageReceived(remoteMessage)
        remoteMessage?.let { message ->


            /* Type:1 (Meter recharges)  Type:2 (Deposits)*/

            var g:Gson
            g= Gson()
            Log.v(TAG,g.toJson(remoteMessage.data))

            if(!remoteMessage.data.isEmpty()){

                notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                var intent:Intent

                var code = SharedHelper.getString(this, Constants.CURRENCY_CODE);
                if(message.data.containsKey("type")){

                    if(message.data.get("type").equals("1")){
                        notifyActivity(message.data.get("id").toString())
                         intent = Intent(this, RechargeTransactionDetails::class.java)
                         intent.putExtra("rechargeId",message.data.get("id"))
                         EventBus.getDefault().post( MessageEvent("update_balance"));

                    }else if(message.data.get("type").equals("2")){
                        notifyActivity(message.data.get("id").toString())
                        intent = Intent(this, DepositTransactionDetails::class.java)
                        intent.putExtra("depositId",message.data.get("id"))
                        Log.v("DEPOSITID","FCM DepositId: "+message.data.get("id"))
                        EventBus.getDefault().post( MessageEvent("update_balance"));


                    }
                    else{
                         intent = Intent(this, HomeActivity::class.java)
                        EventBus.getDefault().post( MessageEvent("update_balance"));
                    }
                }else {
                    intent = Intent(this, HomeActivity::class.java)
                    EventBus.getDefault().post( MessageEvent("update_balance"));
                }


                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                val pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                        PendingIntent.FLAG_ONE_SHOT)
                //Setting up Notification channels for android O and above
                if (Build.VERSION.SDK_INT >=Build.VERSION_CODES.O) {
                    setupNotificationChannels()
                }
                val notificationId = Random().nextInt(9999 - 1000) + 1000;

                val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                val notificationBuilder = NotificationCompat.Builder(this, ADMIN_CHANNEL_ID)
                        .setSmallIcon(R.drawable.fcmiconblack)  //a resource for your custom small icon
                        .setContentTitle(message.data.get("title")) //the "title" value you sent in your notification
                        .setContentText(message.data.get("body")) //ditto
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setPriority(Notification.PRIORITY_HIGH)
                        .setStyle(NotificationCompat.BigTextStyle().bigText(message.data.get("body")))
                        .setAutoCancel(true)  //dismisses the notification on click
                        .setContentIntent(pendingIntent)
                        .setSound(defaultSoundUri)

                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.notify(notificationId, notificationBuilder.build())
            }
        }
    }

    private fun notifyActivity(value: String) {
        val intent = Intent("ACTION_UPDATE_VALUE")
        intent.putExtra("VALUE_KEY", value)
        sendBroadcast(intent)
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun setupNotificationChannels() {
        val adminChannelName = getString(R.string.channel_name)
        val adminChannelDescription = getString(R.string.channel_description)
        val adminChannel: NotificationChannel
        adminChannel = NotificationChannel(ADMIN_CHANNEL_ID, adminChannelName, NotificationManager.IMPORTANCE_HIGH)
        adminChannel.description = adminChannelDescription
        adminChannel.enableLights(true)
        adminChannel.lightColor = Color.RED
        adminChannel.enableVibration(true)
        notificationManager.createNotificationChannel(adminChannel)
    }
}