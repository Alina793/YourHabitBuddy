package com.app.yourhabbitbuddy.data;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import static androidx.room.ForeignKey.CASCADE;

@Entity(tableName = "habit_entries",
        foreignKeys = {
                @ForeignKey(entity = Habit.class,
                        parentColumns = "id",
                        childColumns = "habitId",
                        onDelete = CASCADE),
                @ForeignKey(entity = User.class,
                        parentColumns = "id",
                        childColumns = "userId",
                        onDelete = CASCADE)
        },
        indices = {@Index("habitId"), @Index("userId")})
public class HabitEntry {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private long habitId;
    private long userId;
    private long date;
    private boolean completed;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public long getHabitId() { return habitId; }
    public void setHabitId(long habitId) { this.habitId = habitId; }
    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }
    public long getDate() { return date; }
    public void setDate(long date) { this.date = date; }
    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
}