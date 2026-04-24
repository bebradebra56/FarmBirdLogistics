package com.farmbirdfs.logjfeiowewg.opf

import android.app.Activity
import android.graphics.Rect
import android.view.View
import android.widget.FrameLayout
import com.farmbirdfs.logjfeiowewg.opf.presentation.app.FarmBirdLogisticsApplication

class FarmBirdLogisticsGlobalLayoutUtil {

    private var farmBirdLogisticsMChildOfContent: View? = null
    private var farmBirdLogisticsUsableHeightPrevious = 0

    fun farmBirdLogisticsAssistActivity(activity: Activity) {
        val content = activity.findViewById<FrameLayout>(android.R.id.content)
        farmBirdLogisticsMChildOfContent = content.getChildAt(0)

        farmBirdLogisticsMChildOfContent?.viewTreeObserver?.addOnGlobalLayoutListener {
            possiblyResizeChildOfContent(activity)
        }
    }

    private fun possiblyResizeChildOfContent(activity: Activity) {
        val farmBirdLogisticsUsableHeightNow = farmBirdLogisticsComputeUsableHeight()
        if (farmBirdLogisticsUsableHeightNow == farmBirdLogisticsUsableHeightPrevious) return

        val fullHeight = farmBirdLogisticsMChildOfContent?.rootView?.height ?: 0
        val keyboardDelta = fullHeight - farmBirdLogisticsUsableHeightNow
        val isKeyboardLikelyVisible = keyboardDelta > (fullHeight / 4)

        if (isKeyboardLikelyVisible || !isKeyboardLikelyVisible) {
            activity.window.setSoftInputMode(FarmBirdLogisticsApplication.farmBirdLogisticsInputMode)
        }
        farmBirdLogisticsUsableHeightPrevious = farmBirdLogisticsUsableHeightNow
    }

    private fun farmBirdLogisticsComputeUsableHeight(): Int {
        val r = Rect()
        farmBirdLogisticsMChildOfContent?.getWindowVisibleDisplayFrame(r)
        return r.bottom - r.top  // Visible height без status bar
    }
}