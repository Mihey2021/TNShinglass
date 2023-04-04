package ru.tn.shinglass.adapters.extendsComponents

import android.content.Context
import android.content.SharedPreferences
import android.util.AttributeSet
import androidx.appcompat.app.AlertDialog
import androidx.preference.ListPreference
import androidx.preference.Preference
import ru.tn.shinglass.R
import ru.tn.shinglass.adapters.DynamicListAdapter
import ru.tn.shinglass.models.Cell
import ru.tn.shinglass.models.Division
import ru.tn.shinglass.models.Warehouse

class ExtendListPreference<T>(context: Context, attrs: AttributeSet? = null) :
    ListPreference(context, attrs) {

    //private var selectedData: T? = null

    private lateinit var adapter: DynamicListAdapter<T>
    private var dataList = arrayListOf<T>()
    private var dlgTitle: String = ""

    override fun onClick() = showDialog()

    fun showDialog() {
        val clickedDialogEntryIndex = findIndexOfValue(value)

        val builder = AlertDialog.Builder(context)
        //adapter = DynamicListAdapter(context, R.layout.dynamic_prefs_layout)
        updateDataList()
        builder.setTitle(dlgTitle)
        builder.setSingleChoiceItems(adapter, clickedDialogEntryIndex) { alertDialog, which ->
            val selectedData = dataList[which]

            if (selectedData is Warehouse) {
                summary = selectedData.warehouseTitle
                value = selectedData.warehouseGuid
            }
            if (selectedData is Division) {
                summary = selectedData.divisionTitle
                value = selectedData.divisionGuid
            }
            if (selectedData is Cell) {
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

        if (adapter.count > 0) dialog.show()
    }

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

    fun clearAdapterData() {
        adapter.clear()
    }

    fun getDataListArray() = this.dataList

    fun setDialogTitle(dlgTitle: String = "") {
        this.dlgTitle = dlgTitle
    }
}