package com.example.voco.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.voco.R
import com.example.voco.service.MyFirebaseMessagingService.Constants.CHANNEL_ID
import com.example.voco.service.MyFirebaseMessagingService.Constants.CHANNEL_NAME
import com.example.voco.ui.SplashActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlin.random.Random


class MyFirebaseMessagingService : FirebaseMessagingService() {
    object Constants {
        const val CHANNEL_ID = "channel_id"
        const val CHANNEL_NAME = "channel_name"
    }
    // 클라우드 서버에 등록되었을 때 호출됨
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // send token to server

    }
    // 클라우드 서버에서 메시지를 전송하면 자동으로 호출됨
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        // notification
        val title = remoteMessage.data["title"]
        val message = remoteMessage.data["content"]

        if (title != null && message != null) {
            sendNotification(title, message)
        }
    }
    private fun sendNotification(title:String, message: String){
        val intent = Intent(applicationContext,SplashActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(applicationContext, 0, intent, FLAG_IMMUTABLE)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationID = Random.nextInt()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }

        val notificationBuilder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_america)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(notificationID,notificationBuilder)
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH).apply {
            description = "Channel Description"
            enableLights(true)
            lightColor = Color.GREEN
        }
        notificationManager.createNotificationChannel(channel)
    }
}