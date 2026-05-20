package com.app.yourhabbitbuddy;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import androidx.appcompat.app.AppCompatDelegate;
import java.util.Locale;

public class YourHabitApplication extends Application {

    private static YourHabitApplication instance;
    private SharedPreferences sharedPreferences;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        sharedPreferences = getSharedPreferences("app_settings", MODE_PRIVATE);

        // Завантажуємо мову
        loadLanguage();

        // Завантажуємо тему
        applyTheme();
    }

    private void loadLanguage() {
        String languageCode = sharedPreferences.getString("language", "uk");
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        Configuration config = new Configuration();
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
    }

    private void applyTheme() {
        boolean isDarkMode = sharedPreferences.getBoolean("dark_mode", false);
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    public static YourHabitApplication getInstance() {
        return instance;
    }

    public SharedPreferences getSharedPreferences() {
        return sharedPreferences;
    }
}