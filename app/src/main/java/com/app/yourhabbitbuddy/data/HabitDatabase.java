package com.app.yourhabbitbuddy.data;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Habit.class, HabitEntry.class, User.class}, version = 2, exportSchema = false)
public abstract class HabitDatabase extends RoomDatabase {
    public abstract HabitDao habitDao();
    public abstract UserDao userDao();

    private static volatile HabitDatabase instance;

    public static HabitDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (HabitDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    HabitDatabase.class,
                                    "habit_database")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return instance;
    }
}