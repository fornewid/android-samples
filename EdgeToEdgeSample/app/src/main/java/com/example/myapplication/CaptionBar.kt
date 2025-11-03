package com.example.myapplication

import android.content.res.Configuration
import android.graphics.Rect
import android.os.Build
import android.view.WindowInsetsController.APPEARANCE_LIGHT_CAPTION_BARS
import android.view.WindowInsetsController.APPEARANCE_TRANSPARENT_CAPTION_BAR_BACKGROUND
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import androidx.core.view.WindowInsetsCompat

fun ComponentActivity.enableTransparentCaptionBar() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
        val inFreeformWindowingMode = true // There's no public API.
        if (inFreeformWindowingMode) {
            setAppearanceTransparentCaptionBar(isTransparent = false)
        } else {
            setAppearanceTransparentCaptionBar(isTransparent = true)
            setAppearanceLightCaptionBar(isLight = isSystemInDarkTheme().not())
        }
    }
}

private fun ComponentActivity.isSystemInDarkTheme(): Boolean {
    val uiMode = resources.configuration.uiMode
    return (uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
}

@RequiresApi(35)
private fun ComponentActivity.setAppearanceTransparentCaptionBar(isTransparent: Boolean) {
    if (isTransparent) {
        window.insetsController?.setSystemBarsAppearance(
            APPEARANCE_TRANSPARENT_CAPTION_BAR_BACKGROUND,
            APPEARANCE_TRANSPARENT_CAPTION_BAR_BACKGROUND,
        )
    } else {
        window.insetsController?.setSystemBarsAppearance(
            0,
            APPEARANCE_TRANSPARENT_CAPTION_BAR_BACKGROUND,
        )
    }
}

@RequiresApi(35)
private fun ComponentActivity.setAppearanceLightCaptionBar(isLight: Boolean) {
    if (isLight) {
        window.insetsController?.setSystemBarsAppearance(
            APPEARANCE_LIGHT_CAPTION_BARS,
            APPEARANCE_LIGHT_CAPTION_BARS,
        )
    } else {
        window.insetsController?.setSystemBarsAppearance(
            0,
            APPEARANCE_LIGHT_CAPTION_BARS,
        )
    }
}

val WindowInsetsCompat.captionBar: CaptionBarCompat?
    get() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            val typeMask = WindowInsetsCompat.Type.captionBar()
            val isCaptionBarVisible = isVisible(typeMask)
            if (isCaptionBarVisible) {
                return CaptionBarCompat.wrap(
                    boundingRects = toWindowInsets()?.getBoundingRects(typeMask),
                )
            }
        }
        return null
    }

@Suppress("unused")
class CaptionBarCompat private constructor(
    private val boundingRects: List<Rect>,
) {
    val safeInsetTop: Int = boundingRects.maxOfOrNull { it.height() } ?: 0
    val safeInsetBottom: Int = 0
    val safeInsetLeft: Int = boundingRects.firstOrNull()?.width() ?: 0
    val safeInsetRight: Int = boundingRects.lastOrNull()?.width() ?: 0

    override fun toString(): String {
        return "CaptionBarCompat{$boundingRects}"
    }

    companion object {
        fun wrap(boundingRects: List<Rect>?): CaptionBarCompat? {
            return if (boundingRects != null) {
                CaptionBarCompat(boundingRects = boundingRects)
            } else {
                null
            }
        }
    }
}
