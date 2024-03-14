package com.vendtech.app.network

import android.content.Context
import android.net.ConnectivityManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class Uten {


    companion object{
        
        //live
//        val BASE_URL="https://vendtechsl.com/api/";
        //development;
         val BASE_URL="http://www.vendtechsl.net/api/";

        fun FetchServerData(): ApiInterface {
            val retrofit = Retrofit.Builder().baseUrl(BASE_URL).client(httpClient).addConverterFactory(GsonConverterFactory.create()).build()
            return retrofit.create(ApiInterface::class.java)
        }
        val httpClient=OkHttpClient.Builder().readTimeout(30,TimeUnit.SECONDS).connectTimeout(20, TimeUnit.SECONDS).writeTimeout(20, TimeUnit.SECONDS).addInterceptor( HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)).build()
        fun isInternetAvailable(context: Context): Boolean {
               val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkInfo = connectivityManager.activeNetworkInfo
            return networkInfo != null && networkInfo.isConnected
        }

    }


}