package com.app.yourhabbitbuddy.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "habits")
public class Habit {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private String name;
    private String type;
    private long createdAt;
    private long userId;

    public Habit() {
        this.createdAt = System.currentTimeMillis();
    }

    // Getters
    public long getId() { return id; }
    public String getName() { return name; }
    public String getType() { return type; }
    public long getCreatedAt() { return createdAt; }
    public long getUserId() { return userId; }

    // Додаємо getStartDate() для сумісності
    public long getStartDate() { return createdAt; }

    // Setters
    public void setId(long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setType(String type) { this.type = type; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public void setUserId(long userId) { this.userId = userId; }
    public void setStartDate(long startDate) { this.createdAt = startDate; }
}