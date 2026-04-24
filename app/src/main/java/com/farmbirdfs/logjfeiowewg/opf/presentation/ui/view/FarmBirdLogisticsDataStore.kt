package com.farmbirdfs.logjfeiowewg.opf.presentation.ui.view

import android.annotation.SuppressLint
import android.widget.FrameLayout
import androidx.lifecycle.ViewModel

class FarmBirdLogisticsDataStore : ViewModel(){
    val farmBirdLogisticsViList = mutableListOf<FarmBirdLogisticsVi>()
    var farmBirdLogisticsIsFirstCreate = true
    @SuppressLint("StaticFieldLeak")
    lateinit var farmBirdLogisticsContainerView: FrameLayout
    @SuppressLint("StaticFieldLeak")
    lateinit var farmBirdLogisticsView: FarmBirdLogisticsVi

}