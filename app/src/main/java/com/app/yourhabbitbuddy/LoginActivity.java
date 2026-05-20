package com.app.yourhabbitbuddy;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.snackbar.Snackbar;
import com.app.yourhabbitbuddy.data.User;
import com.app.yourhabbitbuddy.repository.UserRepository;
import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin, btnRegister;
    private TextView tvForgotPassword;
    private UserRepository userRepository;

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        userRepository = new UserRepository(this);

        initViews();
        setupListeners();

        userRepository.getCurrentUser(new UserRepository.GetUserCallback() {
            @Override
            public void onResult(User user) {
                runOnUiThread(() -> {
                    if (user != null) {
                        goToMainActivity();
                    }
                });
            }
        });
    }

    private void initViews() {
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        btnRegister = findViewById(R.id.btn_register);
        tvForgotPassword = findViewById(R.id.tv_forgot_password);
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(v -> login());
        btnRegister.setOnClickListener(v -> register());
        tvForgotPassword.setOnClickListener(v -> forgotPassword());
    }

    private boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    private boolean isValidPassword(String password) {
        return password != null && password.length() >= 4;
    }

    private void login() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (!isValidEmail(email)) {
            etEmail.setError(getString(R.string.invalid_email));
            etEmail.requestFocus();
            Snackbar.make(findViewById(android.R.id.content), getString(R.string.invalid_email), Snackbar.LENGTH_SHORT).show();
            return;
        }

        if (!isValidPassword(password)) {
            etPassword.setError(getString(R.string.password_min_length));
            etPassword.requestFocus();
            Snackbar.make(findViewById(android.R.id.content), getString(R.string.password_min_length), Snackbar.LENGTH_SHORT).show();
            return;
        }

        btnLogin.setEnabled(false);
        btnLogin.setText(getString(R.string.loading));

        userRepository.login(email, password, new UserRepository.LoginCallback() {
            @Override
            public void onResult(User user) {
                runOnUiThread(() -> {
                    btnLogin.setEnabled(true);
                    btnLogin.setText(getString(R.string.login));

                    if (user != null) {
                        userRepository.setCurrentUser(user);
                        Snackbar.make(findViewById(android.R.id.content),
                                String.format(getString(R.string.login_success), user.getName()),
                                Snackbar.LENGTH_SHORT).show();
                        goToMainActivity();
                    } else {
                        Snackbar.make(findViewById(android.R.id.content),
                                getString(R.string.invalid_credentials),
                                Snackbar.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void register() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (!isValidEmail(email)) {
            etEmail.setError(getString(R.string.invalid_email));
            etEmail.requestFocus();
            return;
        }

        if (!isValidPassword(password)) {
            etPassword.setError(getString(R.string.password_min_length));
            etPassword.requestFocus();
            return;
        }

        btnRegister.setEnabled(false);
        btnRegister.setText(getString(R.string.loading));

        userRepository.isUserExists(email, new UserRepository.ExistsCallback() {
            @Override
            public void onResult(boolean exists) {
                runOnUiThread(() -> {
                    if (exists) {
                        btnRegister.setEnabled(true);
                        btnRegister.setText(getString(R.string.register));
                        etEmail.setError(getString(R.string.user_exists));
                        Snackbar.make(findViewById(android.R.id.content),
                                getString(R.string.user_exists),
                                Snackbar.LENGTH_SHORT).show();
                        return;
                    }

                    User user = new User();
                    user.setEmail(email);
                    user.setPassword(password);
                    user.setName(email.split("@")[0]);
                    user.setAvatar("😎");

                    userRepository.register(user, new UserRepository.RegisterCallback() {
                        @Override
                        public void onResult(boolean success) {
                            runOnUiThread(() -> {
                                btnRegister.setEnabled(true);
                                btnRegister.setText(getString(R.string.register));

                                if (success) {
                                    // Отримуємо користувача з БД, щоб дізнатися ID
                                    userRepository.getUserByEmail(email, new UserRepository.GetUserCallback() {
                                        @Override
                                        public void onResult(User savedUser) {
                                            runOnUiThread(() -> {
                                                if (savedUser != null) {
                                                    userRepository.setCurrentUser(savedUser);
                                                    Snackbar.make(findViewById(android.R.id.content),
                                                            getString(R.string.registration_success),
                                                            Snackbar.LENGTH_SHORT).show();
                                                    goToMainActivity();
                                                } else {
                                                    Snackbar.make(findViewById(android.R.id.content),
                                                            getString(R.string.register_error),
                                                            Snackbar.LENGTH_SHORT).show();
                                                }
                                            });
                                        }
                                    });
                                } else {
                                    Snackbar.make(findViewById(android.R.id.content),
                                            getString(R.string.register_error),
                                            Snackbar.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });
                });
            }
        });
    }

    private void forgotPassword() {
        Snackbar.make(findViewById(android.R.id.content),
                getString(R.string.support_email),
                Snackbar.LENGTH_LONG).show();
    }

    private void goToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}