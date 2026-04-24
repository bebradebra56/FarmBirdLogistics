package com.farmbirdfs.logjfeiowewg

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import com.farmbirdfs.logjfeiowewg.opf.FarmBirdLogisticsGlobalLayoutUtil
import com.farmbirdfs.logjfeiowewg.opf.farmBirdLogisticsSetupSystemBars
import com.farmbirdfs.logjfeiowewg.opf.presentation.app.FarmBirdLogisticsApplication
import com.farmbirdfs.logjfeiowewg.opf.presentation.pushhandler.FarmBirdLogisticsPushHandler
import org.koin.android.ext.android.inject

class FarmBirdLogisticsActivity : AppCompatActivity() {

    private val farmBirdLogisticsPushHandler by inject<FarmBirdLogisticsPushHandler>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContentView(R.layout.activity_farm_bird_logistics)

        val farmBirdLogisticsRootView = findViewById<View>(android.R.id.content)
        FarmBirdLogisticsGlobalLayoutUtil().farmBirdLogisticsAssistActivity(this)
        ViewCompat.setOnApplyWindowInsetsListener(farmBirdLogisticsRootView) { farmBirdLogisticsView, farmBirdLogisticsInsets ->
            val farmBirdLogisticsSystemBars = farmBirdLogisticsInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            val farmBirdLogisticsDisplayCutout = farmBirdLogisticsInsets.getInsets(WindowInsetsCompat.Type.displayCutout())
            val farmBirdLogisticsIme = farmBirdLogisticsInsets.getInsets(WindowInsetsCompat.Type.ime())


            val farmBirdLogisticsTopPadding = maxOf(farmBirdLogisticsSystemBars.top, farmBirdLogisticsDisplayCutout.top)
            val farmBirdLogisticsLeftPadding = maxOf(farmBirdLogisticsSystemBars.left, farmBirdLogisticsDisplayCutout.left)
            val farmBirdLogisticsRightPadding = maxOf(farmBirdLogisticsSystemBars.right, farmBirdLogisticsDisplayCutout.right)
            window.setSoftInputMode(FarmBirdLogisticsApplication.farmBirdLogisticsInputMode)

            if (window.attributes.softInputMode == WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN) {
                Log.d(FarmBirdLogisticsApplication.FARM_BIRD_LOGISTICS_MAIN_TAG, "ADJUST PUN")
                val farmBirdLogisticsBottomInset = maxOf(farmBirdLogisticsSystemBars.bottom, farmBirdLogisticsDisplayCutout.bottom)

                farmBirdLogisticsView.setPadding(farmBirdLogisticsLeftPadding, farmBirdLogisticsTopPadding, farmBirdLogisticsRightPadding, 0)

                farmBirdLogisticsView.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    bottomMargin = farmBirdLogisticsBottomInset
                }
            } else {
                Log.d(FarmBirdLogisticsApplication.FARM_BIRD_LOGISTICS_MAIN_TAG, "ADJUST RESIZE")

                val farmBirdLogisticsBottomInset = maxOf(farmBirdLogisticsSystemBars.bottom, farmBirdLogisticsDisplayCutout.bottom, farmBirdLogisticsIme.bottom)

                farmBirdLogisticsView.setPadding(farmBirdLogisticsLeftPadding, farmBirdLogisticsTopPadding, farmBirdLogisticsRightPadding, 0)

                farmBirdLogisticsView.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    bottomMargin = farmBirdLogisticsBottomInset
                }
            }



            WindowInsetsCompat.CONSUMED
        }
        Log.d(FarmBirdLogisticsApplication.FARM_BIRD_LOGISTICS_MAIN_TAG, "Activity onCreate()")
        farmBirdLogisticsPushHandler.farmBirdLogisticsHandlePush(intent.extras)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            farmBirdLogisticsSetupSystemBars()
        }
    }

    override fun onResume() {
        super.onResume()
        farmBirdLogisticsSetupSystemBars()
    }
}