package ru.tn.shinglass.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import ru.tn.shinglass.R

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings_container, SettingsFragment())
            .commit()

    }
}