package com.app.yourhabbitbuddy.data;

import androidx.lifecycle.LiveData;
import androidx.room.*;
import java.util.List;

@Dao
public interface HabitDao {
    // ============ HABITS ============
    @Query("SELECT * FROM habits WHERE userId = :userId ORDER BY createdAt DESC")
    LiveData<List<Habit>> getAllHabits(long userId);

    @Query("SELECT * FROM habits WHERE userId = :userId ORDER BY createdAt DESC")
    List<Habit> getAllHabitsSync(long userId);

    @Insert
    long insertHabit(Habit habit);

    @Delete
    void deleteHabit(Habit habit);

    // ============ ENTRIES ============
    @Insert
    void insertEntry(HabitEntry entry);

    @Update
    void updateEntry(HabitEntry entry);

    @Query("SELECT * FROM habit_entries WHERE habitId = :habitId AND date = :date")
    HabitEntry getEntryByDate(long habitId, long date);

    @Query("SELECT COUNT(*) FROM habit_entries WHERE userId = :userId AND completed = 1 AND date >= :startDate AND date < :endDate")
    int getTodayCompletedCount(long userId, long startDate, long endDate);
    @Query("SELECT COUNT(*) FROM habit_entries WHERE habitId = :habitId AND completed = 1")
    LiveData<Integer> getCompletedCount(long habitId);

    @Query("SELECT COUNT(*) FROM habit_entries WHERE habitId = :habitId AND completed = 1 AND date >= :startDate AND date < :endDate")
    int getCompletedCountBetween(long habitId, long startDate, long endDate);

    @Query("SELECT date FROM habit_entries WHERE habitId = :habitId AND completed = 1 ORDER BY date DESC")
    List<Long> getCompletedDates(long habitId);

    // ============ СТАТИСТИКА ПО КОРИСТУВАЧУ ============
    @Query("SELECT COUNT(DISTINCT date) FROM habit_entries WHERE userId = :userId AND completed = 1")
    int getTotalCompletedDays(long userId);

    @Query("SELECT date FROM habit_entries WHERE userId = :userId AND completed = 1 ORDER BY date DESC")
    List<Long> getAllCompletedDatesByUser(long userId);

}