package com.farmbirdfs.logjfeiowewg.opf.presentation.ui.view

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FarmBirdLogisticsViFun(private val context: Context) {
    fun farmBirdLogisticsSavePhoto() : Uri {
        val fileStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val dir = context.filesDir.absoluteFile.also {
            if (!it.exists()) {
                it.mkdir()
            }
        }
        return FileProvider.getUriForFile(
            context,
            "com.farmbirdfs.logjfeiowewg.fileprovider",
            File(dir, "/$fileStamp.jpg")
        )
    }

}