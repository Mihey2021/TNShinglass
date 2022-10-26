package ru.tn.shinglass.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import ru.tn.shinglass.databinding.DynamicPrefsLayoutBinding
import ru.tn.shinglass.models.Division
import ru.tn.shinglass.models.PhysicalPerson
import ru.tn.shinglass.models.Warehouse
import java.util.*
import kotlin.collections.ArrayList


class DynamicListAdapter<T> : ArrayAdapter<T> {
    constructor(context: Context, layout: Int) : super(context, layout)
    constructor(context: Context, layout: Int, listData: List<T>) : super(context, layout, listData){
        listData.forEach { tempItems.add(it) }
    }

    //private val listData: ArrayList<T> = arrayListOf<T>()
    val tempItems: ArrayList<T> = arrayListOf()
    private val suggestions: ArrayList<T> = arrayListOf()


    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val binding = DynamicPrefsLayoutBinding.inflate(
            LayoutInflater.from(parent?.context ?: context),
            parent,
            false
        )

        val item = getItem(position)

        with(binding) {
            itemListTextView.setTextColor(Color.BLACK)

            when(item) {
                is Warehouse -> itemListTextView.text = item.title
                is Division -> itemListTextView.text = item.title
                is PhysicalPerson -> itemListTextView.text = item.fio
                else -> itemListTextView.text = ""
            }
        }

        return binding.root
    }

    override fun getFilter(): Filter {
        return object: Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                return if (constraint != null) {
                    suggestions.clear()
                    for (item in tempItems) {
                        if (item is PhysicalPerson) {
                            if (item.fio.lowercase(Locale.ROOT)
                                    .contains(constraint.toString().lowercase(Locale.ROOT))
                            ) {
                                suggestions.add(item)
                            }
                        }
                        if (item is Warehouse) {
                            if (item.title.lowercase(Locale.ROOT)
                                    .contains(constraint.toString().lowercase(Locale.ROOT))
                            ) {
                                suggestions.add(item)
                            }
                        }
                        if (item is Division) {
                            if (item.title.lowercase(Locale.ROOT)
                                    .contains(constraint.toString().lowercase(Locale.ROOT))
                            ) {
                                suggestions.add(item)
                            }
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
                if(resultValue is PhysicalPerson)
                    return resultValue.fio
                return if(resultValue is Warehouse)
                    resultValue.title
                else (resultValue as Division).title
            }
        }
    }
}