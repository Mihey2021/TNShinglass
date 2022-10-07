package ru.tn.shinglass.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import ru.tn.shinglass.databinding.DynamicPrefsLayoutBinding
import ru.tn.shinglass.models.Warehouse

class DynamicListAdapter<T>(context: Context, layout: Int) : ArrayAdapter<T>(context, layout) {

//    override fun getItemId(position: Int): Long {
//        val item = getItem(position)
//        if (item is Warehouses) {
//            return item.id
//        }
//        return super.getItemId(position)
//    }

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
            } else {
                itemListTextView.text = ""
            }
        }

        return binding.root
    }

}