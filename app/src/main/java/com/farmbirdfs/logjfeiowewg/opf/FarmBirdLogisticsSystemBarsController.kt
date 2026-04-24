package com.farmbirdfs.logjfeiowewg.opf

import android.app.Activity
import android.content.res.Configuration
import android.os.Build
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.core.view.WindowCompat

class FarmBirdLogisticsSystemBarsController(private val activity: Activity) {

    private val farmBirdLogisticsWindow = activity.window
    private val farmBirdLogisticsDecorView by lazy(LazyThreadSafetyMode.NONE) {
        farmBirdLogisticsWindow.decorView
    }

    fun farmBirdLogisticsSetupSystemBars() {
        val orientation = activity.resources.configuration.orientation
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                farmBirdLogisticsSetupForApi30Plus(orientation == Configuration.ORIENTATION_LANDSCAPE)
            }
            else -> {
                farmBirdLogisticsSetupForApi24To29(orientation == Configuration.ORIENTATION_LANDSCAPE)
            }
        }
    }

    private fun farmBirdLogisticsSetupForApi30Plus(isLandscape: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowCompat.setDecorFitsSystemWindows(farmBirdLogisticsWindow, false)

            val insetsController = farmBirdLogisticsWindow.insetsController ?: return

            insetsController.systemBarsBehavior =
                WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

            if (isLandscape) {
                insetsController.hide(
                    WindowInsets.Type.navigationBars() or WindowInsets.Type.statusBars()
                )
            } else {
                insetsController.hide(WindowInsets.Type.navigationBars())
                insetsController.show(WindowInsets.Type.statusBars())
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun farmBirdLogisticsSetupForApi24To29(isLandscape: Boolean) {
        val baseFlags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY

        val resolvedFlags = if (isLandscape) {
            baseFlags or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_FULLSCREEN
        } else {
            farmBirdLogisticsWindow.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            baseFlags
        }

        farmBirdLogisticsDecorView.systemUiVisibility = resolvedFlags

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            farmBirdLogisticsWindow.navigationBarColor = android.graphics.Color.TRANSPARENT
            farmBirdLogisticsDecorView.systemUiVisibility = resolvedFlags and
                    View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR.inv()
        }
    }


}

fun Activity.farmBirdLogisticsSetupSystemBars() {
    FarmBirdLogisticsSystemBarsController(this).farmBirdLogisticsSetupSystemBars()
}
