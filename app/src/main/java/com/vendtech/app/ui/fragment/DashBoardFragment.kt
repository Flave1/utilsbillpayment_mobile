package com.vendtech.app.ui.fragment

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import com.vendtech.app.R
import com.vendtech.app.helper.SharedHelper
import com.vendtech.app.models.profile.GetWalletModel
import com.vendtech.app.network.Uten
import com.vendtech.app.utils.Constants
import com.vendtech.app.utils.CustomDialog
import com.vendtech.app.utils.Utilities
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.util.*

class DashBoardFragment : Fragment() {



    var countInterface: NotificationCount? = null

    interface NotificationCount {
        fun CountIs(count: String)
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)

        try {
            countInterface = activity as? NotificationCount
        } catch (e: ClassCastException) {
            throw ClassCastException(activity.toString()
                    + " must implement MyInterface ");
        }

        return view
    }

    companion object {
        fun newInstance(): DashBoardFragment {
            return DashBoardFragment()
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
      //  tvPosNumber.setText("POS ID : " + SharedHelper.getString(activity!!, Constants.POS_NUMBER))
    }


    override fun onResume() {

        if (Uten.isInternetAvailable(requireActivity())) {
            GetWalletBalance();

        } else {
            NoInternetDialog("No internet connection. Please check your network connectivity.")
        }
        super.onResume()
    }

    private fun NoInternetDialog(msg: String) {

        val dialog = AlertDialog.Builder(requireActivity())
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
        customDialog = CustomDialog(requireActivity())
        customDialog.show()

        val call: Call<GetWalletModel> = Uten.FetchServerData().get_wallet_balance(SharedHelper.getString(requireActivity(), Constants.TOKEN))
        call.enqueue(object : Callback<GetWalletModel> {
            override fun onResponse(call: Call<GetWalletModel>, response: Response<GetWalletModel>) {

                if (customDialog.isShowing) {
                    customDialog.dismiss()
                }
                var data = response.body()
                if (data != null) {

                    if (data.status.equals("true")) {

                           countInterface?.CountIs(data.result.unReadNotifications)


                    } else {
                        Utilities.CheckSessionValid(data.message, requireContext(), requireActivity())
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
