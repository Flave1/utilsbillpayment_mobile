package com.vendtech.app.base

import android.content.Intent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

/**
 */

open class BaseActivity : AppCompatActivity() {


    fun launchActivity(calledActivity: Class<*>) {
        val myIntent = Intent(this, calledActivity)
        this.startActivity(myIntent)
    }
    internal fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
