package com.maple.voidextraction

import android.app.Activity
import android.os.Bundle
import android.view.WindowInsets

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.insetsController?.hide(WindowInsets.Type.systemBars())
        setContentView(GameView(this))
    }
}
