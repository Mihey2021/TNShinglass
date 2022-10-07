package ru.tn.shinglass.activity.utilites

import android.os.Bundle
import ru.tn.shinglass.dto.models.User1C
import ru.tn.shinglass.models.Option
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

object OptionObj : ReadWriteProperty<Bundle, Option?> {
    override fun getValue(thisRef: Bundle, property: KProperty<*>): Option? =
        thisRef.getSerializable(property.name) as? Option


    override fun setValue(thisRef: Bundle, property: KProperty<*>, value: Option?) {
        thisRef.putSerializable(property.name, value)
    }
}

object UserData : ReadWriteProperty<Bundle, User1C?> {
    override fun getValue(thisRef: Bundle, property: KProperty<*>): User1C? =
        thisRef.getSerializable(property.name) as? User1C


    override fun setValue(thisRef: Bundle, property: KProperty<*>, value: User1C?) {
        thisRef.putSerializable(property.name, value)
    }
}