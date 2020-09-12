package com.example.dailylog

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.dailylog.filemanager.FileFormatSettingsActivity
import com.example.dailylog.log.AddToLogActivity
import com.example.dailylog.shortcuts.ShortcutSettingsActivity


class DailyLogHomeScreenActivity : AppCompatActivity()  {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.daily_log_home_screen)

        val shortButton = findViewById<Button>(R.id.shortcuts)
        shortButton.setOnClickListener{
            val intent = Intent(this, ShortcutSettingsActivity::class.java)
            startActivity(intent)
        }

        val logButton = findViewById<Button>(R.id.addToLog)
        logButton.setOnClickListener{
            val intent = Intent(this, AddToLogActivity::class.java)
            startActivity(intent)
        }

        val fileButton = findViewById<Button>(R.id.fileSettingsButton)
        fileButton.setOnClickListener{
            val intent = Intent(this, FileFormatSettingsActivity::class.java)
            startActivity(intent)
        }
    }
}