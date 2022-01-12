package com.pujit.keepthenotes

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat
import com.pujit.keepthenotes.ui.EntryActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        statusBarColor(R.color.color_main_252525)
        startActivity(Intent(this,EntryActivity::class.java))
        finish()
    }

    @SuppressLint("NewApi")
    fun statusBarColor(id: Int) {
        window.decorView.systemUiVisibility = 0
        window.statusBarColor = ContextCompat.getColor(this, id)
    }
}