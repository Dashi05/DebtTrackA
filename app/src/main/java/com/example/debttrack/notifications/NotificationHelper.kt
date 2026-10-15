package com.example.debttrack.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.os.bundleOf
import androidx.navigation.NavDeepLinkBuilder
import com.example.debttrack.MainActivity
import com.example.debttrack.R

/**
 * Уведомления о приближающемся сроке возврата долга ("изюминка" проекта, Неделя 13).
 */
object NotificationHelper {

    const val CHANNEL_ID = "due_date_reminders"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channel = NotificationChannel(
            CHANNEL_ID,
            "Напоминания о сроках возврата",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Уведомления о долгах с приближающимся или истёкшим сроком возврата"
        }

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    /**
     * Показывает уведомление о долге. Тап по уведомлению открывает "Детали долга"
     * через Navigation Component deep link (с правильным back stack до Главной).
     */
    fun showDueDateNotification(
        context: Context,
        debtId: Long,
        personName: String,
        contentText: String
    ) {
        val pendingIntent: PendingIntent = NavDeepLinkBuilder(context)
            .setGraph(R.navigation.nav_graph)
            .setDestination(R.id.debtDetailFragment)
            .setArguments(bundleOf("debtId" to debtId))
            .setComponentName(MainActivity::class.java)
            .createPendingIntent()

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_clock)
            .setContentTitle(personName)
            .setContentText(contentText)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ActivityCompat.checkSelfPermission(
                context, android.Manifest.permission.POST_NOTIFICATIONS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }

        NotificationManagerCompat.from(context).notify(debtId.toInt(), notification)
    }
}
