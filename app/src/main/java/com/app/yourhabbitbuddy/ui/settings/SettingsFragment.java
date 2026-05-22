package com.app.yourhabbitbuddy.ui.settings;

import static android.content.Context.MODE_PRIVATE;

import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;
import com.app.yourhabbitbuddy.R;
import com.app.yourhabbitbuddy.notifications.ReminderScheduler;
import java.util.Locale;

public class SettingsFragment extends Fragment {

    private static final int NOTIFICATION_PERMISSION_REQUEST = 1002;

    private SharedPreferences sharedPreferences;
    private Switch switchDarkTheme;
    private Switch switchNotifications;
    private TextView tvReminderTime;
    private TextView tvLanguage;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sharedPreferences = requireActivity().getSharedPreferences("app_settings", MODE_PRIVATE);

        switchDarkTheme = view.findViewById(R.id.switch_dark_theme);
        switchNotifications = view.findViewById(R.id.switch_notifications);
        tvReminderTime = view.findViewById(R.id.tv_reminder_time);
        tvLanguage = view.findViewById(R.id.tv_language_value);

        // Завантажуємо налаштування
        updateLanguageDisplay();
        loadNotificationSettings();
        loadThemeSettings();

        // Обробники
        View layoutLanguage = view.findViewById(R.id.layout_language);
        if (layoutLanguage != null) {
            layoutLanguage.setOnClickListener(v -> showLanguageDialog());
        }

        View layoutReminderTime = view.findViewById(R.id.layout_reminder_time);
        if (layoutReminderTime != null) {
            layoutReminderTime.setOnClickListener(v -> showTimePickerDialog());
        }

        if (switchDarkTheme != null) {
            switchDarkTheme.setOnCheckedChangeListener((buttonView, isChecked) -> {
                sharedPreferences.edit().putBoolean("dark_mode", isChecked).apply();
                applyTheme(isChecked);
            });
        }

        if (switchNotifications != null) {
            switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
                sharedPreferences.edit().putBoolean("notifications_enabled", isChecked).apply();
                if (isChecked) {
                    checkNotificationPermission();
                } else {
                    ReminderScheduler.cancelReminder(requireContext());
                    if (getView() != null) {
                        Snackbar.make(getView(), getString(R.string.notifications_disabled), Snackbar.LENGTH_SHORT).show();
                    }
                }
            });
        }

        TextView tvVersion = view.findViewById(R.id.tv_version);
        TextView tvAbout = view.findViewById(R.id.tv_about);

        if (tvVersion != null) {
            tvVersion.setText(getString(R.string.version) + " 1.0.0");
        }
        if (tvAbout != null) {
            tvAbout.setText(getString(R.string.about_text) + "\n\n" + getString(R.string.made_with));
        }
    }

    private void loadNotificationSettings() {
        boolean notificationsEnabled = sharedPreferences.getBoolean("notifications_enabled", true);
        if (switchNotifications != null) {
            switchNotifications.setChecked(notificationsEnabled);
        }

        String reminderTime = sharedPreferences.getString("reminder_time", "09:00");
        if (tvReminderTime != null) {
            tvReminderTime.setText(reminderTime);
        }
    }

    private void loadThemeSettings() {
        boolean isDarkMode = sharedPreferences.getBoolean("dark_mode", false);
        if (switchDarkTheme != null) {
            switchDarkTheme.setChecked(isDarkMode);
        }
    }

    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(),
                    android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(new String[]{
                        android.Manifest.permission.POST_NOTIFICATIONS
                }, NOTIFICATION_PERMISSION_REQUEST);
            } else {
                enableNotifications();
            }
        } else {
            enableNotifications();
        }
    }

    private void enableNotifications() {
        String reminderTime = sharedPreferences.getString("reminder_time", "09:00");
        String[] timeParts = reminderTime.split(":");
        int hour = Integer.parseInt(timeParts[0]);
        int minute = Integer.parseInt(timeParts[1]);

        ReminderScheduler.scheduleReminder(requireContext(), hour, minute);
        if (getView() != null) {
            Snackbar.make(getView(), getString(R.string.notifications_enabled) + " " + reminderTime, Snackbar.LENGTH_SHORT).show();
        }
    }

    private void showTimePickerDialog() {
        String currentTime = sharedPreferences.getString("reminder_time", "09:00");
        String[] timeParts = currentTime.split(":");
        int currentHour = Integer.parseInt(timeParts[0]);
        int currentMinute = Integer.parseInt(timeParts[1]);

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                requireContext(),
                (view, hourOfDay, minute) -> {
                    String time = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                    if (tvReminderTime != null) {
                        tvReminderTime.setText(time);
                    }
                    sharedPreferences.edit().putString("reminder_time", time).apply();

                    if (sharedPreferences.getBoolean("notifications_enabled", true)) {
                        ReminderScheduler.scheduleReminder(requireContext(), hourOfDay, minute);
                        if (getView() != null) {
                            Snackbar.make(getView(), getString(R.string.reminder_time_set) + ": " + time, Snackbar.LENGTH_SHORT).show();
                        }
                    }
                },
                currentHour, currentMinute, true
        );
        timePickerDialog.show();
    }

    private void applyTheme(boolean isDarkMode) {
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            if (getView() != null) {
                Snackbar.make(getView(), getString(R.string.dark_theme_enabled), Snackbar.LENGTH_SHORT).show();
            }
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            if (getView() != null) {
                Snackbar.make(getView(), getString(R.string.light_theme_enabled), Snackbar.LENGTH_SHORT).show();
            }
        }
        requireActivity().recreate();
    }

    private void updateLanguageDisplay() {
        String currentLanguage = sharedPreferences.getString("language", "uk");
        if (tvLanguage != null) {
            tvLanguage.setText(currentLanguage.equals("uk") ? getString(R.string.ukrainian) : getString(R.string.english));
        }
    }

    private void showLanguageDialog() {
        String[] languages = {getString(R.string.ukrainian), getString(R.string.english)};
        String[] languageCodes = {"uk", "en"};

        new androidx.appcompat.app.AlertDialog.Builder(getContext())
                .setTitle(getString(R.string.choose_language))
                .setItems(languages, (dialog, which) -> {
                    String selectedLanguage = languageCodes[which];
                    sharedPreferences.edit().putString("language", selectedLanguage).apply();
                    setLocale(selectedLanguage);
                    requireActivity().recreate();
                })
                .show();
    }

    private void setLocale(String languageCode) {
        java.util.Locale locale = new java.util.Locale(languageCode);
        java.util.Locale.setDefault(locale);
        android.content.res.Configuration config = new android.content.res.Configuration();
        config.setLocale(locale);
        requireActivity().getResources().updateConfiguration(config,
                requireActivity().getResources().getDisplayMetrics());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableNotifications();
            } else {
                if (switchNotifications != null) {
                    switchNotifications.setChecked(false);
                }
                sharedPreferences.edit().putBoolean("notifications_enabled", false).apply();
                Toast.makeText(getContext(), getString(R.string.permission_required), Toast.LENGTH_LONG).show();
            }
        }
    }
}