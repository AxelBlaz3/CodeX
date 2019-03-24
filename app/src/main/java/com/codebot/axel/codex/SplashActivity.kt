package com.codebot.axel.codex

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.support.v7.app.AppCompatDelegate
import android.widget.ImageView
import kotlinx.android.synthetic.main.activity_splash.*

class SplashActivity : AppCompatActivity() {

    init {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
    }

    val SPLASH_DURATION = 1000L
    val context = this

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        splash_imageVIew.scaleType = ImageView.ScaleType.FIT_XY

        Handler().postDelayed(object : Runnable {
            override fun run() {
                startActivity(Intent(context, MainActivity::class.java))
                finish()
            }

        }, SPLASH_DURATION)
    }
}
