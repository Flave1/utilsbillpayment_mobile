package com.vendtech.app.utils


import android.app.AlertDialog
import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import androidx.annotation.RequiresApi
import com.vendtech.app.R
import kotlinx.android.synthetic.main.message_dialog.view.*
import java.util.*

class MessageDialog(private val context: Context) {
    @RequiresApi(Build.VERSION_CODES.O)
    fun showDialog(message: String) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.message_dialog, null)
        dialogView.dialogMessage.text = decode(message);
        val dialogBuilder = AlertDialog.Builder(context).apply {
            setView(dialogView)
        }

        val alertDialog = dialogBuilder.create()

        dialogView.dialogButtonOK.setOnClickListener {
            alertDialog.dismiss()
        }

        alertDialog.show()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun decode(base64EncodedText: String): String {
        try {
            val decodedBytes = Base64.getDecoder().decode(base64EncodedText)
            val decodedString = String(decodedBytes, Charsets.UTF_8)
            return decodedString;
        } catch (e: IllegalArgumentException) {
            println("Error decoding base64 string: ${e.message}")
            return "Service will be enabled shortly";
        }
    }
}


