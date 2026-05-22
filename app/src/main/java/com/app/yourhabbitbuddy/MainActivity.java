package com.app.yourhabbitbuddy;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.app.yourhabbitbuddy.data.Habit;
import com.app.yourhabbitbuddy.data.User;
import com.app.yourhabbitbuddy.repository.HabitRepository;
import com.app.yourhabbitbuddy.repository.UserRepository;
import com.app.yourhabbitbuddy.ui.maps.SportsMapActivity;
import com.google.android.material.navigation.NavigationView;
import com.app.yourhabbitbuddy.R;

import java.util.List;
import java.util.Locale;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private NavController navController;
    private AppBarConfiguration appBarConfiguration;
    private SharedPreferences sharedPreferences;
    private HabitRepository habitRepository;
    private UserRepository userRepository;
    private NavigationView navigationView;
    private long currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        loadLanguage();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = getSharedPreferences("app_settings", MODE_PRIVATE);
        userRepository = new UserRepository(this);

        userRepository.getCurrentUser(new UserRepository.GetUserCallback() {
            @Override
            public void onResult(User user) {
                if (user != null) {
                    currentUserId = user.getId();
                    habitRepository = new HabitRepository(MainActivity.this, currentUserId);
                    runOnUiThread(() -> {
                        updateHeaderWithUserData(user);
                        updateHeaderStats();
                    });
                }
            }
        });

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setupNavigation();
        setupLogout();
    }

    private void loadLanguage() {
        SharedPreferences prefs = getSharedPreferences("app_settings", MODE_PRIVATE);
        String languageCode = prefs.getString("language", "uk");
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
    }

    private void setupNavigation() {
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        navController = Navigation.findNavController(this, R.id.nav_host_fragment);

        appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_habits, R.id.nav_settings)
                .setOpenableLayout(drawerLayout)
                .build();

        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }

    private void updateHeaderWithUserData(User user) {
        if (user == null || navigationView == null) return;

        View headerView = navigationView.getHeaderView(0);
        if (headerView == null) return;

        TextView tvName = headerView.findViewById(R.id.header_name);
        TextView tvEmail = headerView.findViewById(R.id.header_email);

        if (tvName != null) tvName.setText(user.getName());
        if (tvEmail != null) tvEmail.setText(user.getEmail());
    }

    private void setupLogout() {
        if (navigationView == null) return;

        View headerView = navigationView.getHeaderView(0);
        if (headerView == null) return;

        Button btnLogout = headerView.findViewById(R.id.btn_logout);
        if (btnLogout == null) return;

        btnLogout.setOnClickListener(v -> {
            new Thread(() -> {
                userRepository.logout();
                runOnUiThread(() -> {
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                });
            }).start();
        });
    }

    private void updateHeaderStats() {
        if (habitRepository == null) return;

        new Thread(() -> {
            List<Habit> habits = habitRepository.getHabitsSync();

            int totalHabits = habits != null ? habits.size() : 0;
            int totalDays = 0;
            int bestStreak = 0;

            if (habits != null) {
                for (Habit habit : habits) {
                    long days = (System.currentTimeMillis() - habit.getCreatedAt()) / (24 * 60 * 60 * 1000);
                    totalDays += days;
                    if (days > bestStreak) {
                        bestStreak = (int) days;
                    }
                }
            }

            final int finalTotalDays = totalDays;
            final int finalBestStreak = bestStreak;
            final int finalTotalHabits = totalHabits;

            runOnUiThread(() -> {
                if (navigationView == null) return;

                View headerView = navigationView.getHeaderView(0);
                if (headerView == null) return;

                TextView habitsCount = headerView.findViewById(R.id.header_habits_count);
                TextView streakCount = headerView.findViewById(R.id.header_streak_count);
                TextView daysCount = headerView.findViewById(R.id.header_days_count);

                if (habitsCount != null) habitsCount.setText(String.valueOf(finalTotalHabits));
                if (streakCount != null) streakCount.setText(String.valueOf(finalBestStreak));
                if (daysCount != null) daysCount.setText(String.valueOf(finalTotalDays));
            });
        }).start();
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, appBarConfiguration) || super.onSupportNavigateUp();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_habits) {
            navController.navigate(R.id.nav_habits);
        } else if (id == R.id.nav_statistics) {
            navController.navigate(R.id.nav_statistics);
        } else if (id == R.id.nav_settings) {
            navController.navigate(R.id.nav_settings);
        } else if (id == R.id.nav_share) {
            shareApp();
        } else if (id == R.id.nav_achievements) {
            showAchievements();
        } else if (id == R.id.nav_challenge) {
            showDailyChallenge();
        } else if (id == R.id.nav_fact) {
            showRandomHabitFact();
        } else if (id == R.id.nav_calendar) {
            showSimpleStats();
        } else if (id == R.id.nav_sports_map) {
            startActivity(new Intent(this, SportsMapActivity.class));
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void shareApp() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_thanks) + "\n\n" + getString(R.string.app_name));
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share)));
    }

    private void showRandomHabitFact() {
        String[] facts = {
                getString(R.string.habit_fact_1),
                getString(R.string.habit_fact_2),
                getString(R.string.habit_fact_3),
                getString(R.string.habit_fact_4),
                getString(R.string.habit_fact_5),
                getString(R.string.habit_fact_6),
                getString(R.string.habit_fact_7),
                getString(R.string.habit_fact_8),
                getString(R.string.habit_fact_9),
                getString(R.string.habit_fact_10)
        };
        Random random = new Random();
        String fact = facts[random.nextInt(facts.length)];

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(getString(R.string.habit_fact_title))
                .setMessage(fact)
                .setPositiveButton(getString(R.string.habit_fact_ok), null)
                .show();
    }

    private void showDailyChallenge() {
        String[][] challenges = {
                {"Сходи 5000 кроків", "Walk 5000 steps"},
                {"Випий 2 літри води", "Drink 2 liters of water"},
                {"Прочитай 20 хвилин", "Read for 20 minutes"},
                {"Прибери на столі", "Clean your desk"},
                {"Подзвони близьким", "Call loved ones"},
                {"Зроби 10 присідань", "Do 10 squats"},
                {"Помедитуй 5 хвилин", "Meditate for 5 minutes"},
                {"Вимкни телефон на 1 годину", "Turn off phone for 1 hour"},
                {"Напиши план на завтра", "Write tomorrow's plan"},
                {"З'їж щось корисне", "Eat something healthy"}
        };

        String lang = Locale.getDefault().getLanguage();
        Random random = new Random();
        int index = random.nextInt(challenges.length);
        String challenge = lang.equals("uk") ? challenges[index][0] : challenges[index][1];

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(getString(R.string.daily_challenge))
                .setMessage(challenge + "\n\n✨ " + getString(R.string.complete_challenge))
                .setPositiveButton(getString(R.string.will_do), null)
                .setNegativeButton(getString(R.string.maybe_later), null)
                .show();
    }

    private void showSimpleStats() {
        if (habitRepository == null) return;

        new Thread(() -> {
            List<Habit> habits = habitRepository.getHabitsSync();

            runOnUiThread(() -> {
                if (habits == null || habits.isEmpty()) {
                    new androidx.appcompat.app.AlertDialog.Builder(MainActivity.this)
                            .setTitle(getString(R.string.calendar_title))
                            .setMessage(getString(R.string.no_habits_data))
                            .setPositiveButton("OK", null)
                            .show();
                    return;
                }

                int activeHabits = habits.size();
                int totalDays = 0;
                int bestStreak = 0;
                String bestHabit = "";

                StringBuilder message = new StringBuilder();
                message.append("🎯 ").append(getString(R.string.active_habits)).append(": ").append(activeHabits).append("\n\n");
                message.append("📋 ").append(getString(R.string.habits_list)).append("\n\n");

                for (Habit habit : habits) {
                    long days = (System.currentTimeMillis() - habit.getCreatedAt()) / (24 * 60 * 60 * 1000);
                    totalDays += days;
                    String emoji = habit.getType().equals("good") ? "" : "";
                    message.append("   ").append(emoji).append(" ").append(habit.getName())
                            .append(" — ").append(days).append(" ").append(getString(R.string.days_abbr)).append("\n");
                    if (days > bestStreak) {
                        bestStreak = (int) days;
                        bestHabit = habit.getName();
                    }
                }

                message.append("\n📈 ").append(getString(R.string.total_stats)).append("\n\n");
                message.append("     ").append(getString(R.string.total_days_count)).append(": ").append(totalDays).append("\n");
                message.append("     ").append(getString(R.string.longest_habit)).append(": ").append(bestHabit).append("\n");
                message.append("     ").append(getString(R.string.record_days)).append(": ").append(bestStreak).append(" ").append(getString(R.string.days_full));

                new androidx.appcompat.app.AlertDialog.Builder(MainActivity.this)
                        .setTitle(getString(R.string.calendar_title))
                        .setMessage(message.toString())
                        .setPositiveButton(getString(R.string.close), null)
                        .setNegativeButton(getString(R.string.share_report), (d, w) -> shareReport(message.toString()))
                        .show();
            });
        }).start();
    }

    private void showAchievements() {
        if (habitRepository == null) return;

        new Thread(() -> {
            List<Habit> habits = habitRepository.getHabitsSync();

            runOnUiThread(() -> {
                if (habits == null || habits.isEmpty()) {
                    new androidx.appcompat.app.AlertDialog.Builder(this)
                            .setTitle(getString(R.string.achievements))
                            .setMessage(getString(R.string.no_habits_data))
                            .setPositiveButton("OK", null)
                            .show();
                    return;
                }

                int totalDays = 0;
                for (Habit habit : habits) {
                    long days = (System.currentTimeMillis() - habit.getCreatedAt()) / (24 * 60 * 60 * 1000);
                    totalDays += days;
                }

                String level;
                String emoji;

                if (totalDays >= 100) {
                    level = getString(R.string.level_legend);
                    emoji = "👑";
                } else if (totalDays >= 50) {
                    level = getString(R.string.level_diamond);
                    emoji = "💎";
                } else if (totalDays >= 21) {
                    level = getString(R.string.level_gold);
                    emoji = "🥇";
                } else if (totalDays >= 7) {
                    level = getString(R.string.level_silver);
                    emoji = "🥈";
                } else if (totalDays >= 3) {
                    level = getString(R.string.level_bronze);
                    emoji = "🥉";
                } else {
                    level = getString(R.string.beginner);
                    emoji = "🌱";
                }

                new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle(getString(R.string.achievements))
                        .setMessage(emoji + " " + level + "\n\n📊 " + getString(R.string.total_tracking_days) + ": " + totalDays)
                        .setPositiveButton(getString(R.string.awesome), null)
                        .show();
            });
        }).start();
    }

    private void shareReport(String report) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, report);
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_report)));
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}