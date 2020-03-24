package com.example.dailylog

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout

class AddToLogActivity : AppCompatActivity() {
    private lateinit var presenter: AddToLogPresenter
    private lateinit var view: AddToLogView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_to_log_screen)
        view = AddToLogView(findViewById<ConstraintLayout>(R.id.addToLogView), applicationContext)
        presenter = AddToLogPresenter(applicationContext)
        view.render(presenter)
    }
}