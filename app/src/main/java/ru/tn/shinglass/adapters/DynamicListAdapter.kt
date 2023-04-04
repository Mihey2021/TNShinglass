package ru.tn.shinglass.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import ru.tn.shinglass.databinding.DynamicPrefsLayoutBinding
import ru.tn.shinglass.models.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class DynamicListAdapter<T> : ArrayAdapter<T> {
    constructor(context: Context, layout: Int) : super(context, layout)
    constructor(context: Context, layout: Int, listData: List<T>, filterOff: Boolean = true) : super(
        context,
        layout,
        listData
    ) {
        listData.forEach { tempItems.add(it) }
        //this.layout = layout
        this.filterOff = filterOff
    }

    //private val listData: ArrayList<T> = arrayListOf<T>()
    //private var layout: Int = 0
    private val sdf = SimpleDateFormat("dd.MM.yyyy")
    val tempItems: ArrayList<T> = arrayListOf()
    private val suggestions: ArrayList<T> = arrayListOf()
    private var filterOff: Boolean = true

    //private fun getLayout() = this.layout

    @SuppressLint("SetTextI18n")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val binding = DynamicPrefsLayoutBinding.inflate(
            LayoutInflater.from(parent?.context ?: context),
            parent,
            false
        )

//        val view = convertView ?: LayoutInflater.from(parent?.context ?: context).inflate(
//            getLayout(),
//            //R.layout.counterparty_item_layout,
//            parent,
//            false
//        )
//        val itemListTextView = view.findViewById<TextView>(R.id.itemListTextView)
//        val itemListDescriptionTextView =
//            view.findViewById<TextView>(R.id.itemListDecriptionTextView)
//        val itemListDescription2TextView =
//            view.findViewById<TextView>(R.id.itemListDecriptionTextView)

        val item = getItem(position)

       with(binding) {
           itemListTextView?.setTextColor(Color.BLACK)
           when (item) {
               is Warehouse -> itemListTextView?.text = item.warehouseTitle
               is Division -> itemListTextView?.text = item.divisionTitle
               is PhysicalPerson -> itemListTextView?.text = item.physicalPersonFio
               is Employee -> itemListTextView?.text = item.employeeFio
               is Cell -> itemListTextView?.text = item.title
               is Counterparty -> {
                   itemListTextView?.text = item.title
                   itemListDescriptionTextView.text = "ИНН: ${item.inn}. КПП: ${item.kpp}"
                   //itemListDescriptionTextView?.text = "ИНН: ${item.inn}"
                   //itemListDescription2TextView?.text = "КПП: ${item.kpp}"
               }
               is ExternalDocument -> {
                   itemListTextView?.text = "${item.externalOrderDocumentTitle} ${item.externalOrderNumber}"
                   itemListDescriptionTextView.text = "Дата документа: ${if(item.externalOrderDate == 0L) "<нет>" else sdf.format(item.externalOrderDate)}"
               }
               else -> itemListTextView?.text = ""
           }
       }

        return binding.root
        //return view
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                return if (constraint != null && !filterOff){
                //return if (constraint != null) {
                    suggestions.clear()
                    for (item in tempItems) {
                        if (item is PhysicalPerson) {
                            if (item.physicalPersonFio.lowercase(Locale.ROOT)
                                    .contains(constraint.toString().lowercase(Locale.ROOT))
                            ) {
                                suggestions.add(item)
                            }
                        }
                        if (item is Warehouse) {
                            if (item.warehouseTitle.lowercase(Locale.ROOT)
                                    .contains(constraint.toString().lowercase(Locale.ROOT))
                            ) {
                                suggestions.add(item)
                            }
                        }
                        if (item is Division) {
                            if (item.divisionTitle.lowercase(Locale.ROOT)
                                    .contains(constraint.toString().lowercase(Locale.ROOT))
                            ) {
                                suggestions.add(item)
                            }
                        }
                        if (item is Counterparty) {
//                            if (item.divisionTitle.lowercase(Locale.ROOT)
//                                    .contains(constraint.toString().lowercase(Locale.ROOT))
//                            ) {
                            suggestions.add(item)
//                            }
                        }
                    }
                    val filterResults = FilterResults()
                    filterResults.values = suggestions
                    filterResults.count = suggestions.size
                    filterResults
                } else {
                    FilterResults()
                }
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                val filterList = (results?.values ?: arrayListOf<T>()) as ArrayList<T>
                if (results != null && results.count > 0) {
                    clear()
                    for (item in filterList) {
                        add(item)
                        notifyDataSetChanged()
                    }
                }
            }

            override fun convertResultToString(resultValue: Any?): CharSequence {
                if (resultValue is PhysicalPerson)
                    return resultValue.physicalPersonFio
                if (resultValue is Employee)
                    return resultValue.employeeFio
                if (resultValue is Warehouse)
                    return resultValue.warehouseTitle
                if (resultValue is Division)
                    return resultValue.divisionTitle
                if (resultValue is Cell)
                    return resultValue.title
                return if (resultValue is ExternalDocument)
                    "${resultValue.externalOrderDocumentTitle} ${resultValue.externalOrderNumber} " +
                            if(resultValue.externalOrderDate == 0L) "" else " от" + sdf.format(resultValue.externalOrderDate)
                else (resultValue as Counterparty).title
            }
        }
    }
}