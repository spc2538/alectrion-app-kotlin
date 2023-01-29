package com.project.alectrion_app_kotlin.firebase
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import com.project.alectrion_app_kotlin.R
import com.project.alectrion_app_kotlin.main.MainActivity
import com.project.alectrion_app_kotlin.utils.StoragePacket
import okhttp3.*
import java.io.IOException
class StandardFirebaseMessagingService : FirebaseMessagingService() {
    private val CHANNEL_ID = "alectrion"
    private val CHANNEL_NAME = "alectrion"
    private val notificationManager by lazy { NotificationManagerCompat.from(this) }
    private val storagePacket = StoragePacket
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                println("Fetching FCM registration token failed" + task.exception)
                return@OnCompleteListener
            }
            println("token::" + task.result)
        })
        FirebaseMessaging.getInstance().subscribeToTopic("alectrion")
        FirebaseMessaging.getInstance().unsubscribeFromTopic("test")
    }
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        println("message received")
        if (!remoteMessage.data.isNotEmpty()) return
        val alectrionService = remoteMessage.data.get("android_channel_id").toString()
        listServices(alectrionService, remoteMessage.data.get("title").toString())
    }
    private fun listServices(alectrionService: String, dataJson: String){
        println("dataJson:: " + dataJson)
        val gson = Gson()
        val jsonData = gson.fromJson(dataJson, NotificationJson::class.java)
        when (alectrionService) {
            "alectrion" -> {
                val intent = Intent(this, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                notificationManager.notify(1, createNotification("Servicio caido: " + jsonData.message, jsonData.status))
            }
            "test" -> {
                println ("FCM:: test " + storagePacket.settingsJson.environment)
                if (storagePacket.settingsJson.environment == "development"){
                    val intent = Intent(this, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    notificationManager.notify(1, createNotification("Servicio caido test: " + jsonData.message, jsonData.status))
                }
            }
        }
    }
    private fun createNotification(titleService: String, contentNotification: String): Notification {
        val contentIntent = Intent(this, MainActivity::class.java)
        val contentPendingIntent = PendingIntent.getActivity(this, 0, contentIntent, 0)
        return NotificationCompat.Builder(this, CHANNEL_NAME)
            .setSmallIcon(R.mipmap.alectrion_notification)
            .setContentTitle(titleService)
            .setContentText(contentNotification)
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)
            .setFullScreenIntent(contentPendingIntent, true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .build()
    }
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH
        ).apply {
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }
        notificationManager.createNotificationChannel(channel)
    }
    override fun onNewToken(token: String) {
        FirebaseMessaging.getInstance().subscribeToTopic("alectrion")
        FirebaseMessaging.getInstance().unsubscribeFromTopic("test")
    }
}