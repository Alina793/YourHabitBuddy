package com.app.yourhabbitbuddy.repository;

import android.content.Context;
import androidx.lifecycle.LiveData;
import com.app.yourhabbitbuddy.data.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HabitRepository {
    private HabitDao habitDao;
    private ExecutorService executor;
    private long currentUserId;

    public HabitRepository(Context context, long userId) {
        habitDao = HabitDatabase.getInstance(context).habitDao();
        executor = Executors.newSingleThreadExecutor();
        this.currentUserId = userId;
    }

    // ============ HABITS ============
    public LiveData<List<Habit>> getAllHabits() {
        return habitDao.getAllHabits(currentUserId);
    }

    public List<Habit> getHabitsSync() {
        return habitDao.getAllHabitsSync(currentUserId);
    }

    public void insertHabit(Habit habit) {
        habit.setUserId(currentUserId);
        executor.execute(() -> habitDao.insertHabit(habit));
    }

    public void deleteHabit(Habit habit) {
        executor.execute(() -> habitDao.deleteHabit(habit));
    }

    // ============ ENTRIES ============
    public void toggleHabit(long habitId, long date, boolean completed) {
        executor.execute(() -> {
            HabitEntry existing = habitDao.getEntryByDate(habitId, date);
            if (existing != null) {
                existing.setCompleted(completed);
                habitDao.updateEntry(existing);
            } else {
                HabitEntry entry = new HabitEntry();
                entry.setHabitId(habitId);
                entry.setUserId(currentUserId);
                entry.setDate(date);
                entry.setCompleted(completed);
                habitDao.insertEntry(entry);
            }
        });
    }

    public int getTodayCompletedCount(long startDate, long endDate) {
        return habitDao.getTodayCompletedCount(currentUserId, startDate, endDate);
    }

    public LiveData<Integer> getCompletedCount(long habitId) {
        return habitDao.getCompletedCount(habitId);
    }

    public int getCompletedCountBetween(long habitId, long start, long end) {
        return habitDao.getCompletedCountBetween(habitId, start, end);
    }

    public List<Long> getCompletedDates(long habitId) {
        return habitDao.getCompletedDates(habitId);
    }

    // ============ СТАТИСТИКА ПО КОРИСТУВАЧУ ============
    public int getTotalCompletedDays() {
        return habitDao.getTotalCompletedDays(currentUserId);
    }

    public List<Long> getAllCompletedDatesByUser() {
        return habitDao.getAllCompletedDatesByUser(currentUserId);
    }
}