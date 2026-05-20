package com.app.yourhabbitbuddy.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "users")
public class User {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private String email;
    private String password;
    private String name;
    private String avatar;
    private boolean isLoggedIn;
    private long createdAt;

    public User() {
        this.createdAt = System.currentTimeMillis();
        this.isLoggedIn = false;
    }

    // Getters
    public long getId() { return id; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getName() { return name; }
    public String getAvatar() { return avatar; }
    public boolean isLoggedIn() { return isLoggedIn; }
    public long getCreatedAt() { return createdAt; }

    // Setters
    public void setId(long id) { this.id = id; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public void setName(String name) { this.name = name; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public void setLoggedIn(boolean loggedIn) { isLoggedIn = loggedIn; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}