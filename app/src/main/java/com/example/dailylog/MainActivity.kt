package com.example.dailylog

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.dailylog.repository.Repository
import com.example.dailylog.ui.permissions.PermissionChecker
import com.example.dailylog.ui.log.LogView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        val repository = Repository(applicationContext, PermissionChecker(this))
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.container, LogView.newInstance(repository))
                    .commitNow()
        }
    }
}