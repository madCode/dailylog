package com.example.dailylog

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.dailylog.ui.main.LogView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.container, LogView.newInstance())
                    .commitNow()
        }
    }
}