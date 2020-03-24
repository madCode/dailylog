package com.example.dailylog

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity


class DailyLogHomeScreenActivity : AppCompatActivity()  {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.daily_log_home_screen)

        val catButton = findViewById<Button>(R.id.categories)
        catButton.setOnClickListener{
            val intent = Intent(this, CategorySettingsActivity::class.java)
            startActivity(intent)
        }

        val shortButton = findViewById<Button>(R.id.shortcuts)
        shortButton.setOnClickListener{
            val intent = Intent(this, KeyboardShortcutSettingsActivity::class.java)
            startActivity(intent)
        }

        val logButton = findViewById<Button>(R.id.addToLog)
        logButton.setOnClickListener{
            val intent = Intent(this, AddToLogActivity::class.java)
            startActivity(intent)
        }
    }
}