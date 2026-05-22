package com.app.yourhabbitbuddy.notifications;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.app.yourhabbitbuddy.notifications.NotificationHelper;
import com.app.yourhabbitbuddy.notifications.ReminderScheduler;

public class ReminderWorker extends Worker {

    public ReminderWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d("ReminderWorker", "✅ WORKER STARTED at: " + System.currentTimeMillis());

        try {
            // Показуємо сповіщення
            NotificationHelper notificationHelper = new NotificationHelper(getApplicationContext());
            notificationHelper.showDailyReminder();

            // Плануємо наступне сповіщення на завтра
            SharedPreferences prefs = getApplicationContext().getSharedPreferences("app_settings", Context.MODE_PRIVATE);
            String reminderTime = prefs.getString("reminder_time", "09:00");
            String[] timeParts = reminderTime.split(":");
            int hour = Integer.parseInt(timeParts[0]);
            int minute = Integer.parseInt(timeParts[1]);

            // Перевіряємо чи сповіщення все ще увімкнені
            boolean notificationsEnabled = prefs.getBoolean("notifications_enabled", true);

            if (notificationsEnabled) {
                ReminderScheduler.rescheduleForNextDay(getApplicationContext(), hour, minute);
                Log.d("ReminderWorker", "✅ Наступне сповіщення заплановано");
            }

            return Result.success();
        } catch (Exception e) {
            Log.e("ReminderWorker", "❌ Error: " + e.getMessage());
            return Result.failure();
        }
    }
}