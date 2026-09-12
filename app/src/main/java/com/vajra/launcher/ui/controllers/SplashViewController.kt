package com.vajra.launcher.ui.controllers

import android.animation.ObjectAnimator
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import com.vajra.launcher.databinding.ScreenSplashBinding

class SplashViewController(
    container: ViewGroup,
    private val onFinished: () -> Unit
) {
    val binding: ScreenSplashBinding = ScreenSplashBinding.inflate(
        LayoutInflater.from(container.context),
        container,
        true
    )

    private val handler = Handler(Looper.getMainLooper())

    fun start() {
        // Animate horizontal progress bar from 0 to 100
        val anim = ObjectAnimator.ofInt(binding.splashProgressBar, "progress", 0, 100)
        anim.duration = 900
        anim.interpolator = DecelerateInterpolator()
        anim.start()

        // Progressive checklist display
        handler.postDelayed({ binding.statusItem1.visibility = View.VISIBLE }, 200)
        handler.postDelayed({ binding.statusItem2.visibility = View.VISIBLE }, 450)
        handler.postDelayed({ binding.statusItem3.visibility = View.VISIBLE }, 700)
        handler.postDelayed({ binding.statusItem4.visibility = View.VISIBLE }, 950)

        handler.postDelayed({
            onFinished()
        }, 1200)
    }
}
