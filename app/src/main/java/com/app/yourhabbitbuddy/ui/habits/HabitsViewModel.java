package com.app.yourhabbitbuddy.ui.habits;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.app.yourhabbitbuddy.data.Habit;
import com.app.yourhabbitbuddy.data.User;
import com.app.yourhabbitbuddy.repository.HabitRepository;
import com.app.yourhabbitbuddy.repository.UserRepository;
import java.util.Calendar;
import java.util.List;

public class HabitsViewModel extends AndroidViewModel {

    private HabitRepository habitRepository;
    private UserRepository userRepository;
    private LiveData<List<Habit>> allHabits;
    private MutableLiveData<Stats> stats = new MutableLiveData<>(new Stats());
    private MutableLiveData<Integer> totalDays = new MutableLiveData<>(0);
    private long currentUserId;

    public static class Stats {
        public int todayCount = 0;
        public int totalCount = 0;
        public int bestStreak = 0;
    }

    public HabitsViewModel(Application app) {
        super(app);
        userRepository = new UserRepository(app);

        // Отримуємо поточного користувача асинхронно
        userRepository.getCurrentUser(new UserRepository.GetUserCallback() {
            @Override
            public void onResult(User user) {
                if (user != null) {
                    currentUserId = user.getId();
                    habitRepository = new HabitRepository(app, currentUserId);
                    allHabits = habitRepository.getAllHabits();
                    loadStats();
                }
            }
        });
    }

    public LiveData<List<Habit>> getAllHabits() {
        return allHabits;
    }

    public LiveData<Stats> getStats() {
        return stats;
    }

    public LiveData<Integer> getTotalDays() {
        return totalDays;
    }

    public void addHabit(String name, String type) {
        if (habitRepository == null) return;

        new Thread(() -> {
            Habit habit = new Habit();
            habit.setName(name);
            habit.setType(type);
            habit.setUserId(currentUserId);
            habitRepository.insertHabit(habit);
            loadStats();
        }).start();
    }

    public void deleteHabit(Habit habit) {
        if (habitRepository == null) return;
        habitRepository.deleteHabit(habit);
        loadStats();
    }

    public void toggleHabit(Habit habit, boolean completed) {
        if (habitRepository == null) return;
        long today = getTodayStart();
        habitRepository.toggleHabit(habit.getId(), today, completed);
        loadStats();
    }

    private void loadStats() {
        if (habitRepository == null) return;

        new Thread(() -> {
            List<Habit> habits = habitRepository.getHabitsSync();
            if (habits == null) {
                stats.postValue(new Stats());
                totalDays.postValue(0);
                return;
            }

            long today = getTodayStart();
            long tomorrow = today + 86400000;
            int todayCount = habitRepository.getTodayCompletedCount(today, tomorrow);

            int totalDaysSum = 0;
            for (Habit h : habits) {
                long days = (System.currentTimeMillis() - h.getCreatedAt()) / (24 * 60 * 60 * 1000);
                totalDaysSum += days;
            }

            Stats s = new Stats();
            s.todayCount = todayCount;
            s.totalCount = habits.size();
            s.bestStreak = calculateBestStreak(habits);

            stats.postValue(s);
            totalDays.postValue(totalDaysSum);
        }).start();
    }

    private int calculateBestStreak(List<Habit> habits) {
        int max = 0;
        for (Habit h : habits) {
            List<Long> dates = habitRepository.getCompletedDates(h.getId());
            int streak = 0;
            long expected = getTodayStart();
            for (long date : dates) {
                if (date >= expected) {
                    streak++;
                    expected -= 86400000;
                } else break;
            }
            if (streak > max) max = streak;
        }
        return max;
    }

    private long getTodayStart() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }
}