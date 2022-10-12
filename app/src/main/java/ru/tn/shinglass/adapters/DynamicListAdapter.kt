package ru.tn.shinglass.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import ru.tn.shinglass.databinding.DynamicPrefsLayoutBinding
import ru.tn.shinglass.models.PhisicalPerson
import ru.tn.shinglass.models.Warehouse

class DynamicListAdapter<T> : ArrayAdapter<T> {
    constructor(context: Context, layout: Int) : super(context, layout)
    constructor(context: Context, layout: Int, listData: List<T>) : super(context, layout, listData)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val binding = DynamicPrefsLayoutBinding.inflate(
            LayoutInflater.from(parent?.context ?: context),
            parent,
            false
        )

        val item = getItem(position)

        with(binding) {
            itemListTextView.setTextColor(Color.BLACK)

            if (item is Warehouse) {
                itemListTextView.text = item.title
            }
            if (item is PhisicalPerson) {
                itemListTextView.text = item.fio
            } else {
                itemListTextView.text = ""
            }
        }

        return binding.root
    }



}