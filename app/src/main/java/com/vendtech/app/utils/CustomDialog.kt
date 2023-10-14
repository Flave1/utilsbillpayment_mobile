package com.vendtech.app.utils

import android.app.Dialog
import android.content.Context
import android.view.Gravity
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout

import com.vendtech.app.R

class CustomDialog(context: Context) : Dialog(context, R.style.TransparentProgressDialog) {

    private val iv: ImageView? = null

    init {
        val wlmp = window!!.attributes
        wlmp.gravity = Gravity.CENTER_HORIZONTAL
        window!!.attributes = wlmp
        setTitle(null)
        setCancelable(false)
        setOnCancelListener(null)
        val layout = LinearLayout(context)
        layout.orientation = LinearLayout.VERTICAL
        setContentView(R.layout.progress_dialog)
    }

    override fun show() {
        super.show()

    }
}