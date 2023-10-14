package com.vendtech.app.ui.activity.authentication

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.RelativeLayout
import com.vendtech.app.R
import com.vendtech.app.base.BaseActivity

class UpdateAppVersion : BaseActivity() {

    lateinit var layoutUpdateApp: RelativeLayout


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update)
        layoutUpdateApp = findViewById(R.id.layoutUpdateApp)


        layoutUpdateApp.setOnClickListener { v ->
            try {
                var playStoreUri1: Uri = Uri.parse("market://details?id=" + packageName)
                var playStoreIntent1: Intent = Intent(Intent.ACTION_VIEW, playStoreUri1)
                startActivity(playStoreIntent1)
            }catch (exp:Exception){
                var playStoreUri2: Uri = Uri.parse("https://play.google.com/store/apps/details?id=" + packageName)
                var playStoreIntent2: Intent = Intent(Intent.ACTION_VIEW, playStoreUri2)
                startActivity(playStoreIntent2)
            }
        }

    }

}
