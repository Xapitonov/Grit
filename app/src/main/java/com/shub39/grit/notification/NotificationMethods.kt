package com.shub39.grit.notification

import android.Manifest
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat.getString
import com.shub39.grit.MainActivity
import com.shub39.grit.R
import com.shub39.grit.database.habit.Habit
import java.time.format.DateTimeFormatter

object NotificationMethods {
    // creates notification channel at app start
    fun createNotificationChannel(context: Context) {
        val name = getString(context, R.string.channel_name)
        val descriptionText = getString(context, R.string.channel_description)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel("1", name, importance).apply {
            description = descriptionText
        }
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.createNotificationChannel(channel)
    }

    // shows notification when habit is added if permission is granted. otherwise requests permission
    fun showAddNotification(context: Context, habit: Habit) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent =
            PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        val builder = NotificationCompat.Builder(context, "1")
            .setSmallIcon(R.drawable.round_checklist_24)
            .setContentTitle(getString(context, R.string.new_habit) + " " + habit.title)
            .setContentText(
                getString(context, R.string.at) + " " + habit.time.format(
                    DateTimeFormatter.ofPattern("hh:mm a")
                )
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    context as Activity,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS), // works fine in API 29 :/
                    1
                )
                return
            }

            notify(habit.id.hashCode(), builder.build())
        }
    }

    // shows habit notification if permission granted
    fun habitNotification(context: Context, habit: Habit) {
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("1", habit.id)
            action = IntentActions.ADD_HABIT_STATUS.action
        }
        val pendingBroadcast = PendingIntent.getBroadcast(
            context,
            habit.id.hashCode(),
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat
            .Builder(context, "1")
            .setSmallIcon(R.drawable.round_checklist_24)
            .setContentTitle(habit.title)
            .setContentText(habit.description)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .addAction(R.drawable.round_check_circle_24, "Mark Done", pendingBroadcast)

        with(NotificationManagerCompat.from(context)) {
            if (
                ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }

            notify(habit.id.hashCode(), builder.build())
        }
    }
}