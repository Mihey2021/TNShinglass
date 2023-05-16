package ru.tn.shinglass.dto.models

import android.widget.CheckBox
import com.google.android.material.textfield.TextInputLayout
import ru.tn.shinglass.R

enum class HeaderFields(val viewId: Int) {
    WAREHOUSE(R.id.warehouseTextInputLayout),
    WAREHOUSE_RECEIVER(R.id.warehouseReceiverTextInputLayout),
    PHYSICAL_PERSON(R.id.physicalPersonTextInputLayout),
    EMPLOYEE(R.id.employeeTextInputLayout),
    DIVISION(R.id.divisionTextInputLayout),
    COUNTERPARTY(R.id.counterpartyTextInputLayout),
    INCOMING_DATE(R.id.incomingDateTextInputLayout),
    INCOMING_NUMBER(R.id.incomingNumberTextInputLayout),
}

enum class DetailScanFields(val viewId: Int, val fieldType: String) {
    CELL(R.id.cellTextInputLayout, "TextInputLayout"),
    CELL_RECEIVER(R.id.cellReceiverTextInputLayout, "TextInputLayout"),
    ITEM(R.id.itemTextInputLayout, "TextInputLayout"),
    COUNT(R.id.countTextInputLayout, "TextInputLayout"),
    QUALITY(R.id.qualityTextInputLayout, "TextInputLayout"),
    WORKWEAR_ORDINARY(R.id.workwearOrdinaryCheckBox, "CheckBox"),
    WORKWEAR_DISPOSABLE(R.id.workwearDisposableCheckBox, "CheckBox"),
}