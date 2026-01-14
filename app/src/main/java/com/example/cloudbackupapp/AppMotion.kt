package com.example.cloudbackupapp

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith

/**
 * Central place to tweak ALL motion in the app.
 *
 * Change these values in Android Studio and every screen transition / press animation will update.
 */
object AppMotion {

    /** Screen-to-screen transition settings. */
    data class ScreenTransition(
        val durationMs: Int = 260,
        val fadeDurationMs: Int = 180,
        val slidePx: Int = 60,
        val easing: Easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)
    )

    /** Button/tile press animation settings (scale down while pressed). */
    data class Press(
        val pressedScale: Float = 0.96f,
        val durationMs: Int = 90,
        val easing: Easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)
    )

    // Edit these defaults to change animation app-wide.
    var screen = ScreenTransition()
    var press = Press()

    fun screenContentTransform(isPop: Boolean): ContentTransform {
        val cfg = screen
        val enterSlide = slideInHorizontally(
            animationSpec = tween(cfg.durationMs, easing = cfg.easing)
        ) { fullWidth ->
            if (isPop) -cfg.slidePx else cfg.slidePx
        }
        val exitSlide = slideOutHorizontally(
            animationSpec = tween(cfg.durationMs, easing = cfg.easing)
        ) { fullWidth ->
            if (isPop) cfg.slidePx else -cfg.slidePx
        }

        val enterFade = fadeIn(animationSpec = tween(cfg.fadeDurationMs, easing = cfg.easing))
        val exitFade = fadeOut(animationSpec = tween(cfg.fadeDurationMs, easing = cfg.easing))

        return (enterSlide + enterFade).togetherWith(exitSlide + exitFade)
    }
}
