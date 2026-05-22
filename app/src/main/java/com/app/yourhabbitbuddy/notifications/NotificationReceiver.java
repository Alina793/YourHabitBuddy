package com.app.yourhabbitbuddy.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("NotificationReceiver", "🔔 Сповіщення спрацювало!");

        // Показуємо сповіщення
        NotificationHelper helper = new NotificationHelper(context);
        helper.showDailyReminder();

        // Отримуємо збережений час
        SharedPreferences prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE);
        String reminderTime = prefs.getString("reminder_time", "09:00");
        Log.d("ReminderScheduler", "Збережений час: " + reminderTime);

        String[] timeParts = reminderTime.split(":");
        int hour = Integer.parseInt(timeParts[0]);
        int minute = Integer.parseInt(timeParts[1]);

        // Плануємо на ЗАВТРА в той самий час
        ReminderScheduler.scheduleReminder(context, hour, minute);
    }
}