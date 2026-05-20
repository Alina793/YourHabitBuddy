//package com.app.yourhabbitbuddy.ui.statistics;
//
//import android.app.Application;
//import androidx.lifecycle.AndroidViewModel;
//import androidx.lifecycle.LiveData;
//import androidx.lifecycle.MutableLiveData;
//
//import com.app.yourhabbitbuddy.data.Habit;
//import com.app.yourhabbitbuddy.repository.HabitRepository;
//
//import java.util.ArrayList;
//import java.util.Calendar;
//import java.util.List;
//
//public class StatisticsViewModel extends AndroidViewModel {
//
//    private HabitRepository repository;
//    private MutableLiveData<List<Habit>> habits = new MutableLiveData<>();
//    private MutableLiveData<WeeklyData> weeklyData = new MutableLiveData<>();
//    private MutableLiveData<Stats> stats = new MutableLiveData<>();
//
//    public static class WeeklyData {
//        public List<Integer> values = new ArrayList<>();
//    }
//
//    public static class Stats {
//        public int progress = 0;
//        public int streak = 0;
//        public int total = 0;
//    }
//
//    public StatisticsViewModel(Application application) {
//        super(application);
//        repository = new HabitRepository(application);
//        loadHabits();
//    }
//
//    public LiveData<List<Habit>> getHabits() {
//        return habits;
//    }
//
//    public LiveData<WeeklyData> getWeeklyData() {
//        return weeklyData;
//    }
//
//    public LiveData<Stats> getStats() {
//        return stats;
//    }
//
//    private void loadHabits() {
//        new Thread(() -> {
//            List<Habit> habitList = repository.getAllHabits().getValue();
//            if (habitList == null) {
//                habitList = new ArrayList<>();
//            }
//            habits.postValue(habitList);
//        }).start();
//    }
//
//    public void loadStats(long habitId) {
//        new Thread(() -> {
//            Stats newStats = new Stats();
//
//            List<Long> completedDates = repository.getCompletedDates(habitId);
//            if (completedDates == null) {
//                completedDates = new ArrayList<>();
//            }
//
//            newStats.total = completedDates.size();
//            newStats.progress = Math.min((newStats.total * 100 / 30), 100);
//            newStats.streak = calculateCurrentStreak(completedDates);
//
//            stats.postValue(newStats);
//            loadWeeklyData(habitId);
//        }).start();
//    }
//
//    private void loadWeeklyData(long habitId) {
//        new Thread(() -> {
//            WeeklyData data = new WeeklyData();
//            data.values = new ArrayList<>();
//            Calendar calendar = Calendar.getInstance();
//
//            for (int i = 6; i >= 0; i--) {
//                calendar.setTimeInMillis(System.currentTimeMillis());
//                calendar.add(Calendar.DAY_OF_YEAR, -i);
//                calendar.set(Calendar.HOUR_OF_DAY, 0);
//                calendar.set(Calendar.MINUTE, 0);
//                calendar.set(Calendar.SECOND, 0);
//                calendar.set(Calendar.MILLISECOND, 0);
//                long startOfDay = calendar.getTimeInMillis();
//
//                calendar.add(Calendar.DAY_OF_YEAR, 1);
//                long startOfNextDay = calendar.getTimeInMillis();
//
//                int completed = repository.getCompletedCountBetween(habitId, startOfDay, startOfNextDay);
//                data.values.add(completed > 0 ? 100 : 0);
//            }
//
//            weeklyData.postValue(data);
//        }).start();
//    }
//
//    private int calculateCurrentStreak(List<Long> completedDates) {
//        if (completedDates == null || completedDates.isEmpty()) {
//            return 0;
//        }
//
//        List<Long> sorted = new ArrayList<>(completedDates);
//        sorted.sort((a, b) -> Long.compare(b, a));
//
//        long today = getTodayStart();
//        int streak = 0;
//        long expectedDate = today;
//
//        for (long date : sorted) {
//            long normalizedDate = getDayStart(date);
//            if (normalizedDate == expectedDate) {
//                streak++;
//                expectedDate -= 24 * 60 * 60 * 1000;
//            } else if (normalizedDate < expectedDate) {
//                break;
//            }
//        }
//
//        return streak;
//    }
//
//    private long getTodayStart() {
//        Calendar calendar = Calendar.getInstance();
//        calendar.set(Calendar.HOUR_OF_DAY, 0);
//        calendar.set(Calendar.MINUTE, 0);
//        calendar.set(Calendar.SECOND, 0);
//        calendar.set(Calendar.MILLISECOND, 0);
//        return calendar.getTimeInMillis();
//    }
//
//    private long getDayStart(long timestamp) {
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTimeInMillis(timestamp);
//        calendar.set(Calendar.HOUR_OF_DAY, 0);
//        calendar.set(Calendar.MINUTE, 0);
//        calendar.set(Calendar.SECOND, 0);
//        calendar.set(Calendar.MILLISECOND, 0);
//        return calendar.getTimeInMillis();
//    }
//}