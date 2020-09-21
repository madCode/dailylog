package com.example.dailylog.randomentry

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.dailylog.R
import com.example.dailylog.log.AddToLogActivity
import com.example.dailylog.settings.SettingsActivity


class RandomEntryActivity : AppCompatActivity()  {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.daily_log_home_screen)

        val logButton = findViewById<Button>(R.id.addToLog)
        logButton.setOnClickListener{
            val intent = Intent(this, AddToLogActivity::class.java)
            startActivity(intent)
        }
    }
}