package com.vendtech.app.adapter.meter

import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.annotation.LayoutRes
import com.vendtech.app.models.meter.MeterListResults


 class MeterListAutoCompleteAdapter(context: Context, @LayoutRes private val layoutResource: Int, private val allPois: List<MeterListResults>):
        ArrayAdapter<MeterListResults>(context, layoutResource, allPois),
        Filterable {

    private var mPois: List<MeterListResults> = allPois

    override fun getCount(): Int {
        return mPois.size
    }

    override fun getItem(p0: Int): MeterListResults? {
        return mPois.get(p0)

    }
    override fun getItemId(p0: Int): Long {
        // Or just return p0
        return  mPois.get(p0).meterId.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: TextView = convertView as TextView? ?: LayoutInflater.from(context).inflate(layoutResource, parent, false) as TextView
        view.text = "${mPois[position].name}"+" - "+"${mPois[position].number}"

        val font= Typeface.createFromAsset(context.assets,"fonts/medium.ttf")
        view.setTypeface(font)
        return view
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun publishResults(charSequence: CharSequence?, filterResults: Filter.FilterResults) {
                mPois = filterResults.values as List<MeterListResults>
                notifyDataSetChanged()
            }

            override fun performFiltering(charSequence: CharSequence?): Filter.FilterResults {
                val queryString = charSequence?.toString()?.toLowerCase()

                val filterResults = Filter.FilterResults()
                filterResults.values = if (queryString==null || queryString.isEmpty())
                    allPois
                else
                    allPois.filter {
                        it.name.toLowerCase().contains(queryString) ||
                                it.name.toLowerCase().contains(queryString) ||
                                it.number.toLowerCase().contains(queryString)
                    }

                return filterResults
            }

        }
    }
}