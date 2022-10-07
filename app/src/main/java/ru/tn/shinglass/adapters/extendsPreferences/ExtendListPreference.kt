package ru.tn.shinglass.adapters.extendsPreferences

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.app.AlertDialog
import androidx.preference.ListPreference
import ru.tn.shinglass.R
import ru.tn.shinglass.adapters.DynamicListAdapter
import ru.tn.shinglass.models.Warehouse

class ExtendListPreference<T>(context: Context, attrs: AttributeSet? = null) :
    ListPreference(context, attrs) {

    //private var selectedData: T? = null

    private lateinit var adapter: DynamicListAdapter<T>
    private var dataList = arrayListOf<T>()
    private var dlgTitle: String = ""

    override fun onClick() {
        val clickedDialogEntryIndex = findIndexOfValue(value)

        val builder = AlertDialog.Builder(context)
        //adapter = DynamicListAdapter(context, R.layout.dynamic_prefs_layout)
        updateDataList()
        builder.setTitle(dlgTitle)
        builder.setSingleChoiceItems(adapter, clickedDialogEntryIndex) { alertDialog, which ->
            val selectedData = dataList[which]

            if (selectedData is Warehouse) {
                summary = selectedData.title
                value = selectedData.guid
            }
            alertDialog.cancel()
        }

        builder.setNegativeButton(R.string.cancel_text) { alertDialog, _ ->
            alertDialog.cancel()
        }

        builder.setCancelable(true)
        val dialog = builder.create()
        dialog.show()

    }
//    var myClickListener: DialogInterface.OnClickListener =
//        DialogInterface.OnClickListener { dialog, which ->
//            selectedData = dataList[which]
//
//            dialog.cancel()
//        }

//    fun <T>setSelectedData(selectedData: T) {
//
//    }

//    fun getDataList(): ArrayList<T> {
//        val warehousesList = arrayListOf(
//            Warehouses(1, "Склад 1", "000-000-000", 0),
//            Warehouses(2, "Склад 2", "000-000-000", 0),
//            Warehouses(3, "Склад 3", "000-000-000", 0),
//        )
//        return warehousesList
//    }

    private fun updateDataList() {
        if (adapter.count == 0 && dataList.isNotEmpty()) adapter.addAll(dataList)
    }

    fun setAdapter(customAdapter: DynamicListAdapter<T>) {
        adapter = customAdapter
    }

    fun setDataListArray(dataList: ArrayList<T>) {
        this.dataList = dataList
        updateDataList()
    }

    fun getDataListArray() = this.dataList

    fun setDialogTitle(dlgTitle: String = "") {
        this.dlgTitle = dlgTitle
    }

}