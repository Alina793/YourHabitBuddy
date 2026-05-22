package com.app.yourhabbitbuddy.notifications;

import android.content.Context;
import android.util.Log;

import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class ReminderScheduler {

    private static final String REMINDER_WORK_NAME = "habit_reminder_daily";

    public static void scheduleReminder(Context context, int hour, int minute) {
        // Розраховуємо час наступного спрацювання
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        long currentTime = System.currentTimeMillis();
        long triggerTime = calendar.getTimeInMillis();

        if (triggerTime <= currentTime) {
            triggerTime += TimeUnit.DAYS.toMillis(1);
        }

        long initialDelay = triggerTime - currentTime;

        Log.d("ReminderScheduler", "Перше сповіщення через: " + initialDelay / 1000 + " секунд");

        // Скасовуємо старі завдання
        WorkManager.getInstance(context).cancelAllWork();

        // Використовуємо OneTimeWorkRequest і переплануємо після кожного спрацювання
        scheduleOneTime(context, hour, minute, initialDelay);
    }

    private static void scheduleOneTime(Context context, int hour, int minute, long delay) {
        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(ReminderWorker.class)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .build();

        WorkManager.getInstance(context).enqueue(workRequest);

        Log.d("ReminderScheduler", "Сповіщення заплановано на " + hour + ":" + minute);
    }

    // Цей метод викликається після кожного сповіщення, щоб запланувати наступне
    public static void rescheduleForNextDay(Context context, int hour, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        long nextTime = calendar.getTimeInMillis();
        long currentTime = System.currentTimeMillis();
        long delay = nextTime - currentTime;

        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(ReminderWorker.class)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .build();

        WorkManager.getInstance(context).enqueue(workRequest);

        Log.d("ReminderScheduler", "Наступне сповіщення через: " + delay / 1000 + " секунд");
    }

    public static void cancelReminder(Context context) {
        WorkManager.getInstance(context).cancelAllWork();
        Log.d("ReminderScheduler", "❌ Всі сповіщення скасовано");
    }
}