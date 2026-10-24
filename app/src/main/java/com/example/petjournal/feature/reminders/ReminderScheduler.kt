package com.example.petjournal.feature.reminders

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.petjournal.MainActivity
import com.example.petjournal.R
import com.example.petjournal.domain.model.CareReminder
import java.util.concurrent.TimeUnit

class ReminderScheduler(context: Context) {
    private val appContext = context.applicationContext

    fun schedule(reminder: CareReminder) {
        if (!reminder.notificationEnabled || reminder.status != "ACTIVE" || !hasNotificationPermission()) {
            cancel(reminder.id)
            return
        }
        val request = OneTimeWorkRequestBuilder<ReminderNotificationWorker>()
            .setInitialDelay((reminder.dueAt - System.currentTimeMillis()).coerceAtLeast(0), TimeUnit.MILLISECONDS)
            .setInputData(
                Data.Builder()
                    .putString(ReminderNotificationWorker.KEY_ID, reminder.id)
                    .putString(ReminderNotificationWorker.KEY_TITLE, reminder.title)
                    .putString(ReminderNotificationWorker.KEY_NOTES, reminder.notes)
                    .build()
            )
            .build()
        WorkManager.getInstance(appContext).enqueueUniqueWork(
            workName(reminder.id),
            androidx.work.ExistingWorkPolicy.REPLACE,
            request
        )
    }

    fun cancel(reminderId: String) {
        WorkManager.getInstance(appContext).cancelUniqueWork(workName(reminderId))
    }

    private fun hasNotificationPermission(): Boolean =
        Build.VERSION.SDK_INT < 33 ||
            ContextCompat.checkSelfPermission(appContext, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED

    private fun workName(id: String) = "petjournal-reminder-$id"
}

class ReminderNotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return Result.success()
        }

        val id = inputData.getString(KEY_ID) ?: return Result.failure()
        val title = inputData.getString(KEY_TITLE) ?: return Result.failure()
        val notes = inputData.getString(KEY_NOTES)
        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "Care reminders", NotificationManager.IMPORTANCE_DEFAULT).apply {
                    description = "On-device reminders for your pet's care"
                }
            )
        }
        val intent = Intent(applicationContext, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(title)
            .setContentText(notes?.takeIf(String::isNotBlank) ?: "Your pet has a care reminder.")
            .setStyle(NotificationCompat.BigTextStyle().bigText(notes?.takeIf(String::isNotBlank) ?: title))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(applicationContext).notify(id.hashCode(), notification)
        return Result.success()
    }

    companion object {
        const val KEY_ID = "reminder_id"
        const val KEY_TITLE = "reminder_title"
        const val KEY_NOTES = "reminder_notes"
        private const val CHANNEL_ID = "pet_care_reminders"
    }
}
