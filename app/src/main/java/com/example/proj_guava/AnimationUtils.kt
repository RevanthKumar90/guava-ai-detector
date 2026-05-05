package com.example.proj_guava

import android.animation.ObjectAnimator
import android.view.View
import android.view.animation.*

object AnimationUtils {

    fun fadeIn(view: View) {
        view.alpha = 0f
        view.visibility = View.VISIBLE
        view.animate().alpha(1f).setDuration(500).start()
    }

    fun slideUp(view: View) {
        view.translationY = 100f
        view.alpha = 0f

        view.animate()
            .translationY(0f)
            .alpha(1f)
            .setDuration(500)
            .setInterpolator(DecelerateInterpolator())
            .start()
    }

    fun buttonClick(view: View) {
        val scaleDown = ScaleAnimation(
            1f, 0.95f,
            1f, 0.95f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        )
        scaleDown.duration = 100

        val scaleUp = ScaleAnimation(
            0.95f, 1f,
            0.95f, 1f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        )
        scaleUp.duration = 100

        view.startAnimation(scaleDown)
        view.postDelayed({ view.startAnimation(scaleUp) }, 100)
    }

    fun animateProgress(bar: android.widget.ProgressBar, value: Int) {
        val anim = ObjectAnimator.ofInt(bar, "progress", 0, value)
        anim.duration = 1000
        anim.start()
    }

    fun pulse(view: View) {
        val pulse = ScaleAnimation(
            1f, 1.05f,
            1f, 1.05f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        )
        pulse.duration = 600
        pulse.repeatCount = Animation.INFINITE
        pulse.repeatMode = Animation.REVERSE

        view.startAnimation(pulse)
    }

    // 🔥 IMPORTANT (your error fix)
    fun cardEntry(view: View) {
        view.translationY = 150f
        view.alpha = 0f

        view.animate()
            .translationY(0f)
            .alpha(1f)
            .setDuration(600)
            .setInterpolator(DecelerateInterpolator())
            .start()
    }
}