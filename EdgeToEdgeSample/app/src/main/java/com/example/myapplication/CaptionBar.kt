package com.example.myapplication

import android.os.Build
import android.view.WindowInsetsController.APPEARANCE_TRANSPARENT_CAPTION_BAR_BACKGROUND
import androidx.activity.ComponentActivity

fun ComponentActivity.setAppearanceTransparentCaptionBar(isTransparent: Boolean) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
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
}
