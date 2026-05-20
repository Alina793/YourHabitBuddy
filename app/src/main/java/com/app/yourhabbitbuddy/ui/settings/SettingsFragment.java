package com.app.yourhabbitbuddy.ui.settings;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import com.google.android.material.snackbar.Snackbar;
import com.app.yourhabbitbuddy.R;
import java.util.Locale;

public class SettingsFragment extends Fragment {

    private SharedPreferences sharedPreferences;
    private Switch switchDarkTheme;
    private TextView tvLanguage;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sharedPreferences = requireActivity().getSharedPreferences("app_settings", MODE_PRIVATE);
        switchDarkTheme = view.findViewById(R.id.switch_dark_theme);
        tvLanguage = view.findViewById(R.id.tv_language_value);

        updateLanguageDisplay();

        view.findViewById(R.id.layout_language).setOnClickListener(v -> showLanguageDialog());

        boolean isDarkMode = sharedPreferences.getBoolean("dark_mode", false);
        switchDarkTheme.setChecked(isDarkMode);

        switchDarkTheme.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sharedPreferences.edit().putBoolean("dark_mode", isChecked).apply();

            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                Snackbar.make(view, getString(R.string.dark_theme_enabled), Snackbar.LENGTH_SHORT).show();
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                Snackbar.make(view, getString(R.string.light_theme_enabled), Snackbar.LENGTH_SHORT).show();
            }
            requireActivity().recreate();
        });

        TextView tvVersion = view.findViewById(R.id.tv_version);
        TextView tvAbout = view.findViewById(R.id.tv_about);

        tvVersion.setText(getString(R.string.version) + " 1.0.0");
        tvAbout.setText(getString(R.string.about_text) + "\n\n" + getString(R.string.made_with));
    }

    private void updateLanguageDisplay() {
        String currentLanguage = sharedPreferences.getString("language", "uk");
        tvLanguage.setText(currentLanguage.equals("uk") ? "Українська" : "English");
    }

    private void showLanguageDialog() {
        String[] languages = {"Українська", "English"};
        String[] languageCodes = {"uk", "en"};

        new androidx.appcompat.app.AlertDialog.Builder(getContext())
                .setTitle(getString(R.string.language))
                .setItems(languages, (dialog, which) -> {
                    String selectedLanguage = languageCodes[which];
                    sharedPreferences.edit().putString("language", selectedLanguage).apply();
                    setLocale(selectedLanguage);
                    requireActivity().recreate();
                })
                .show();
    }

    private void setLocale(String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        requireActivity().getResources().updateConfiguration(config,
                requireActivity().getResources().getDisplayMetrics());
    }
}