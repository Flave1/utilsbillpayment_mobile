package com.vendtech.app.adapter.wallet

import android.content.ClipData.Item
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.vendtech.app.R
import com.vendtech.app.models.meter.BankResponseModel
import com.vendtech.app.models.transaction.BankDetailResult


class AccountAdapter(context: Context, items: MutableList<BankResponseModel.Result>) : BaseAdapter() {
    private val context //context
            : Context
    private val items //data source of the list adapter
            : MutableList<BankResponseModel.Result>

    override fun getCount(): Int {
        return items.size //returns total of items in the list
    }

    override fun getItem(position: Int): Any {
        return items[position] //returns list item at the specified position
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {

        var viewHolder: ViewHolder?
        var convertView: View? = convertView
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_account, parent, false)
            viewHolder = ViewHolder(convertView!!)
            convertView.tag = viewHolder
        } else {
            viewHolder = convertView.tag as ViewHolder
        }

        viewHolder.bindView(items[position])

        // returns the view for the current row
        return convertView
    }

    private class ViewHolder(view: View) {

        fun bindView(bankDetailResult: BankResponseModel.Result) {
            tvTitle.text = bankDetailResult.text
        }

        var tvTitle: TextView
//        var itemDescription: TextView

        init {
            tvTitle = view.findViewById<View>(R.id.tvTitle) as TextView
//            itemDescription = view.findViewById<View>(R.id.text_view_item_description) as TextView
        }
    }

    //public constructor
    init {
        this.context = context
        this.items = items
    }
}