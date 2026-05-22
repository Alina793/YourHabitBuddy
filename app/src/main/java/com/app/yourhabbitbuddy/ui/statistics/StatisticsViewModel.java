package com.app.yourhabbitbuddy.ui.statistics;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.app.yourhabbitbuddy.data.Habit;
import com.app.yourhabbitbuddy.data.User;
import com.app.yourhabbitbuddy.repository.HabitRepository;
import com.app.yourhabbitbuddy.repository.UserRepository;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class StatisticsViewModel extends AndroidViewModel {

    private HabitRepository habitRepository;
    private UserRepository userRepository;
    private MutableLiveData<List<Habit>> habits = new MutableLiveData<>();
    private MutableLiveData<WeeklyData> weeklyData = new MutableLiveData<>();
    private MutableLiveData<MonthlyData> monthlyData = new MutableLiveData<>();
    private MutableLiveData<Stats> stats = new MutableLiveData<>();
    private long currentUserId = -1;

    public static class WeeklyData {
        public List<Integer> values = new ArrayList<>();
    }

    public static class MonthlyData {
        public List<Integer> values = new ArrayList<>();
    }

    public static class Stats {
        public int progress = 0;
        public int streak = 0;
        public int total = 0;
        public int bestDay = 0;
    }

    public StatisticsViewModel(Application app) {
        super(app);
        userRepository = new UserRepository(app);

        userRepository.getCurrentUser(new UserRepository.GetUserCallback() {
            @Override
            public void onResult(User user) {
                if (user != null) {
                    currentUserId = user.getId();
                    habitRepository = new HabitRepository(app, currentUserId);
                    loadHabits();
                }
            }
        });
    }

    public LiveData<List<Habit>> getHabits() {
        return habits;
    }

    public LiveData<WeeklyData> getWeeklyData() {
        return weeklyData;
    }

    public LiveData<MonthlyData> getMonthlyData() {
        return monthlyData;
    }

    public LiveData<Stats> getStats() {
        return stats;
    }

    private void loadHabits() {
        if (habitRepository == null) return;

        new Thread(() -> {
            List<Habit> habitList = habitRepository.getHabitsSync();
            habits.postValue(habitList);
        }).start();
    }

    public void loadStats(long habitId) {
        if (habitRepository == null) return;

        new Thread(() -> {
            // Отримуємо всі дати виконання
            List<Long> completedDates = habitRepository.getCompletedDates(habitId);

            // Загальна статистика
            Stats newStats = new Stats();
            newStats.total = completedDates.size();
            newStats.progress = Math.min((newStats.total * 100 / 30), 100);
            newStats.streak = calculateStreak(completedDates);
            newStats.bestDay = calculateBestDay(habitId);
            stats.postValue(newStats);

            // Тижневі дані
            loadWeeklyData(habitId);

            // Місячні дані
            loadMonthlyData(habitId);
        }).start();
    }

    private void loadWeeklyData(long habitId) {
        WeeklyData data = new WeeklyData();
        data.values = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();

        for (int i = 6; i >= 0; i--) {
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.add(Calendar.DAY_OF_YEAR, -i);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            long startOfDay = calendar.getTimeInMillis();

            calendar.add(Calendar.DAY_OF_YEAR, 1);
            long startOfNextDay = calendar.getTimeInMillis();

            int completed = habitRepository.getCompletedCountBetween(habitId, startOfDay, startOfNextDay);
            data.values.add(completed > 0 ? 100 : 0);
        }
        weeklyData.postValue(data);
    }

    private void loadMonthlyData(long habitId) {
        MonthlyData data = new MonthlyData();
        data.values = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();

        for (int i = 29; i >= 0; i--) {
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.add(Calendar.DAY_OF_YEAR, -i);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            long startOfDay = calendar.getTimeInMillis();

            calendar.add(Calendar.DAY_OF_YEAR, 1);
            long startOfNextDay = calendar.getTimeInMillis();

            int completed = habitRepository.getCompletedCountBetween(habitId, startOfDay, startOfNextDay);
            data.values.add(completed > 0 ? 100 : 0);
        }
        monthlyData.postValue(data);
    }

    private int calculateStreak(List<Long> dates) {
        if (dates.isEmpty()) return 0;

        List<Long> sorted = new ArrayList<>(dates);
        sorted.sort((a, b) -> Long.compare(b, a));

        long today = getTodayStart();
        int streak = 0;
        long expected = today;

        for (long date : sorted) {
            long normalized = getDayStart(date);
            if (normalized == expected) {
                streak++;
                expected -= 24 * 60 * 60 * 1000;
            } else if (normalized < expected) {
                break;
            }
        }
        return streak;
    }

    private int calculateBestDay(long habitId) {
        int max = 0;
        Calendar calendar = Calendar.getInstance();

        for (int i = 0; i < 30; i++) {
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.add(Calendar.DAY_OF_YEAR, -i);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            long startOfDay = calendar.getTimeInMillis();
            calendar.add(Calendar.DAY_OF_YEAR, 1);
            long startOfNextDay = calendar.getTimeInMillis();

            int completed = habitRepository.getCompletedCountBetween(habitId, startOfDay, startOfNextDay);
            if (completed > max) max = completed;
        }
        return max * 100;
    }

    private long getTodayStart() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    private long getDayStart(long time) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(time);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }
}