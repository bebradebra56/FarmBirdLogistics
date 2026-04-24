package com.farmbirdfs.logjfeiowewg.opf.presentation.notificiation

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.os.bundleOf
import com.farmbirdfs.logjfeiowewg.FarmBirdLogisticsActivity
import com.farmbirdfs.logjfeiowewg.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.farmbirdfs.logjfeiowewg.opf.presentation.app.FarmBirdLogisticsApplication

private const val FARM_BIRD_LOGISTICS_CHANNEL_ID = "farm_bird_logistics_notifications"
private const val FARM_BIRD_LOGISTICS_CHANNEL_NAME = "FarmBirdLogistics Notifications"
private const val FARM_BIRD_LOGISTICS_NOT_TAG = "FarmBirdLogistics"

class FarmBirdLogisticsPushService : FirebaseMessagingService(){
    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        remoteMessage.notification?.let { payload ->
            farmBirdLogisticsShowNotification(
                title = payload.title ?: FARM_BIRD_LOGISTICS_NOT_TAG,
                message = payload.body.orEmpty(),
                data = remoteMessage.data["url"]
            )
        }
        remoteMessage.data.takeIf { it.isNotEmpty() }?.let(::farmBirdLogisticsHandleDataPayload)
    }

    private fun farmBirdLogisticsShowNotification(title: String, message: String, data: String?) {
        val farmBirdLogisticsNotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                FARM_BIRD_LOGISTICS_CHANNEL_ID,
                FARM_BIRD_LOGISTICS_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            )
            farmBirdLogisticsNotificationManager.createNotificationChannel(channel)
        }

        val farmBirdLogisticsIntent = Intent(this, FarmBirdLogisticsActivity::class.java)
            .putExtras(bundleOf("url" to data))
            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val farmBirdLogisticsPendingIntent = PendingIntent.getActivity(
            this,
            0,
            farmBirdLogisticsIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val farmBirdLogisticsNotification = NotificationCompat.Builder(this, FARM_BIRD_LOGISTICS_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.farm_bird_logistics_noti_ic)
            .setAutoCancel(true)
            .setContentIntent(farmBirdLogisticsPendingIntent)
            .build()

        farmBirdLogisticsNotificationManager.notify(System.currentTimeMillis().toInt(), farmBirdLogisticsNotification)
    }

    private fun farmBirdLogisticsHandleDataPayload(data: Map<String, String>) {
        data.entries.forEach { entry ->
            Log.d(FarmBirdLogisticsApplication.FARM_BIRD_LOGISTICS_MAIN_TAG, "Data key=${entry.key} value=${entry.value}")
        }
    }
}