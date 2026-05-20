package com.app.yourhabbitbuddy.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface UserDao {
    @Insert
    long insertUser(User user);

    @Update
    void updateUser(User user);

    @Query("SELECT * FROM users WHERE email = :email AND password = :password")
    User login(String email, String password);

    @Query("SELECT * FROM users WHERE email = :email")
    User getUserByEmail(String email);

    @Query("SELECT * FROM users WHERE isLoggedIn = 1 LIMIT 1")
    User getCurrentUser();

    @Query("UPDATE users SET isLoggedIn = 0")
    void logoutAll();

    @Query("DELETE FROM users")
    void deleteAllUsers();
}