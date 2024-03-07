package com.vendtech.app.adapter.profile

import android.annotation.SuppressLint
import android.content.Context
import android.opengl.Visibility
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.vendtech.app.R
import com.vendtech.app.models.profile.UserServicesResult
import com.vendtech.app.utils.Utilities

class UserServicesAdapter(internal var context: Context,internal var serviceList:MutableList<UserServicesResult>,var itemClickListeners: ItemClickListener): RecyclerView.Adapter<UserServicesAdapter.ViewHolder>(), View.OnClickListener {


    lateinit var itemClickListener: ItemClickListener
    var itemEditables = false



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        @SuppressLint("InflateParams") val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user_services, null)
        this.itemClickListener = itemClickListeners

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {


        var selectedService = serviceList.get(position);
        holder.serviceName.text=serviceList.get(position).title

        if(selectedService.disablePlatform){
            holder.serviceName.visibility = View.GONE;
            holder.disabled.visibility = View.VISIBLE;
        }else{
            holder.serviceName.visibility = View.VISIBLE;
            holder.disabled.visibility = View.GONE;
        }
        if(selectedService.platformId.contentEquals("1")){
            holder.serviceIcon.setImageResource(R.drawable.edsanew)
        }else if(selectedService.platformId.contentEquals("2")){
            holder.serviceIcon.setImageResource(R.drawable.orange)
        }else if(selectedService.platformId.contentEquals("3")){
            holder.serviceIcon.setImageResource(R.drawable.africelnew)
        }else if(selectedService.platformId.contentEquals("4"))
            holder.serviceIcon.setImageResource(R.drawable.qcell)
        else if(selectedService.platformId.contentEquals("7")){
            holder.serviceIcon.setImageResource(R.drawable.dstv)
        }else {
            holder.serviceIcon.setImageResource(R.drawable.noicon)
        }

        holder.itemViews.setOnClickListener(View.OnClickListener {
            var selectedPlatformId = serviceList.get(position).platformId;

            if(selectedPlatformId.contentEquals("1")){
                if(itemClickListener!=null){
                    itemClickListener.clickedServiceId(selectedPlatformId, selectedService.disablePlatform, selectedService?.diabledPlaformMessage)
                }

            }else if(selectedPlatformId.contentEquals("2")){
                if(itemClickListener!=null){
                    itemClickListener.clickedServiceId(selectedPlatformId, selectedService.disablePlatform, selectedService?.diabledPlaformMessage)
                }
             }
            else if(selectedPlatformId.contentEquals("3")){
                if(itemClickListener!=null){
                    itemClickListener.clickedServiceId(selectedPlatformId, selectedService.disablePlatform, selectedService?.diabledPlaformMessage )
                }
            }
            else if(selectedPlatformId.contentEquals("4")){
                if(itemClickListener!=null){
                    itemClickListener.clickedServiceId(selectedPlatformId, selectedService.disablePlatform, selectedService?.diabledPlaformMessage)
                }
            }
            else{
                Utilities.shortToast("This service will be available soon.",context)
            }
        })

    }

    override fun getItemCount(): Int {
        return serviceList.size
    }

    override fun onClick(v: View) {

    }



    interface ItemClickListener {
        fun clickedServiceId(platformId:String, disabled: Boolean, msg: String?)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {


        internal var serviceName: TextView
        internal var serviceIcon: ImageView
        lateinit var itemViews:RelativeLayout
        internal var disabled: TextView

        init {
            serviceName = itemView.findViewById<View>(R.id.serviceNameTV) as TextView
            disabled = itemView.findViewById<View>(R.id.disabledTV) as TextView
            serviceIcon = itemView.findViewById<View>(R.id.serviceIconIV) as ImageView
            itemViews=itemView.findViewById<View>(R.id.itemView)as RelativeLayout
        }



    }

}