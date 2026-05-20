package com.app.yourhabbitbuddy.repository;

import android.content.Context;
import com.app.yourhabbitbuddy.data.HabitDatabase;
import com.app.yourhabbitbuddy.data.User;
import com.app.yourhabbitbuddy.data.UserDao;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UserRepository {
    private UserDao userDao;
    private ExecutorService executor;

    public UserRepository(Context context) {
        userDao = HabitDatabase.getInstance(context).userDao();
        executor = Executors.newSingleThreadExecutor();
    }

    // Реєстрація
    public void register(User user, RegisterCallback callback) {
        executor.execute(() -> {
            long id = userDao.insertUser(user);
            user.setId(id);
            if (callback != null) {
                callback.onResult(id > 0);
            }
        });
    }

    // Логін
    public void login(String email, String password, LoginCallback callback) {
        executor.execute(() -> {
            User user = userDao.login(email, password);
            if (callback != null) {
                callback.onResult(user);
            }
        });
    }

    // Отримати поточного користувача
    public void getCurrentUser(GetUserCallback callback) {
        executor.execute(() -> {
            User user = userDao.getCurrentUser();
            if (callback != null) {
                callback.onResult(user);
            }
        });
    }

    // Встановити поточного користувача
    public void setCurrentUser(User user) {
        executor.execute(() -> {
            userDao.logoutAll();
            if (user != null) {
                user.setLoggedIn(true);
                userDao.updateUser(user);
            }
        });
    }

    // Вийти
    public void logout() {
        executor.execute(() -> userDao.logoutAll());
    }

    // Перевірка чи існує користувач
    public void isUserExists(String email, ExistsCallback callback) {
        executor.execute(() -> {
            boolean exists = userDao.getUserByEmail(email) != null;
            if (callback != null) {
                callback.onResult(exists);
            }
        });
    }

    // Отримати користувача за email (АСИНХРОННО)
    public void getUserByEmail(String email, GetUserCallback callback) {
        executor.execute(() -> {
            User user = userDao.getUserByEmail(email);
            if (callback != null) {
                callback.onResult(user);
            }
        });
    }

    // Callback інтерфейси
    public interface RegisterCallback {
        void onResult(boolean success);
    }

    public interface LoginCallback {
        void onResult(User user);
    }

    public interface GetUserCallback {
        void onResult(User user);
    }

    public interface ExistsCallback {
        void onResult(boolean exists);
    }
}