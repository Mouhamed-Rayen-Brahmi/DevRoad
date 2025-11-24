package com.example.devroad;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.devroad.Supabase.SupabaseClient;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private TextView formTitle;
    private EditText emailInput;
    private EditText passwordInput;
    private EditText confirmPasswordInput;
    private EditText usernameInput;
    private Button authButton;
    private TextView toggleLink;
    private View usernameLayout;
    private View confirmPasswordLayout;

    private boolean isLoginMode = true;
    private SessionManager sessionManager;
    private SupabaseClient supabaseClient;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize SessionManager and SupabaseClient
        sessionManager = new SessionManager(this);
        supabaseClient = SupabaseClient.getInstance();

        // Check if user is already logged in
        if (sessionManager.isLoggedIn()) {
            navigateToMain();
            return;
        }

        // Animate logo with bounce
        View logoCard = findViewById(R.id.logo_card);
        logoCard.setScaleX(0f);
        logoCard.setScaleY(0f);
        logoCard.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(600)
                .setInterpolator(new OvershootInterpolator())
                .start();

        // Animate login card slide up
        View loginCard = findViewById(R.id.login_card);
        loginCard.setTranslationY(300f);
        loginCard.setAlpha(0f);
        loginCard.animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(800)
                .setStartDelay(300)
                .setInterpolator(new DecelerateInterpolator())
                .start();

        // Initialize views
        formTitle = findViewById(R.id.form_title);
        emailInput = findViewById(R.id.email_input);
        passwordInput = findViewById(R.id.password_input);
        confirmPasswordInput = findViewById(R.id.confirm_password_input);
        usernameInput = findViewById(R.id.username_input);
        authButton = findViewById(R.id.auth_button);
        toggleLink = findViewById(R.id.toggle_link);
        usernameLayout = findViewById(R.id.username_layout);
        confirmPasswordLayout = findViewById(R.id.confirm_password_layout);

        // Set click listeners
        toggleLink.setOnClickListener(v -> toggleMode());
        authButton.setOnClickListener(v -> handleAuth());
    }

    private void toggleMode() {
        isLoginMode = !isLoginMode;

        if (isLoginMode) {
            formTitle.setText("Welcome Back");
            authButton.setText("Login");
            toggleLink.setText("Don't have an account? Register");
            
            // Animate out
            confirmPasswordLayout.animate()
                    .alpha(0f)
                    .translationY(-20f)
                    .setDuration(200)
                    .withEndAction(() -> confirmPasswordLayout.setVisibility(View.GONE))
                    .start();
            
            usernameLayout.animate()
                    .alpha(0f)
                    .translationY(-20f)
                    .setDuration(200)
                    .withEndAction(() -> usernameLayout.setVisibility(View.GONE))
                    .start();
        } else {
            formTitle.setText("Create Account");
            authButton.setText("Register");
            toggleLink.setText("Already have an account? Login");
            
            // Animate in
            confirmPasswordLayout.setVisibility(View.VISIBLE);
            confirmPasswordLayout.setAlpha(0f);
            confirmPasswordLayout.setTranslationY(-20f);
            confirmPasswordLayout.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(300)
                    .start();
            
            usernameLayout.setVisibility(View.VISIBLE);
            usernameLayout.setAlpha(0f);
            usernameLayout.setTranslationY(-20f);
            usernameLayout.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(300)
                    .start();
        }

        emailInput.setText("");
        passwordInput.setText("");
        confirmPasswordInput.setText("");
        usernameInput.setText("");
    }

    private void handleAuth() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (email.isEmpty()) {
            emailInput.setError("Email is required");
            emailInput.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError("Invalid email format");
            emailInput.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            passwordInput.setError("Password is required");
            passwordInput.requestFocus();
            return;
        }

        if (password.length() < 6) {
            passwordInput.setError("Password must be at least 6 characters");
            passwordInput.requestFocus();
            return;
        }

        if (isLoginMode) {
            performLogin(email, password);
        } else {
            String confirmPassword = confirmPasswordInput.getText().toString().trim();
            String username = usernameInput.getText().toString().trim();

            if (username.isEmpty()) {
                usernameInput.setError("Username is required");
                usernameInput.requestFocus();
                return;
            }

            if (confirmPassword.isEmpty()) {
                confirmPasswordInput.setError("Please confirm your password");
                confirmPasswordInput.requestFocus();
                return;
            }

            if (!password.equals(confirmPassword)) {
                confirmPasswordInput.setError("Passwords do not match");
                confirmPasswordInput.requestFocus();
                return;
            }

            performRegistration(email, password, username);
        }
    }

    private void performLogin(String email, String password) {
        authButton.setEnabled(false);
        authButton.setText("Loading...");

        SupabaseClient.SignInRequest request = new SupabaseClient.SignInRequest(email, password);

        supabaseClient.getAuthApi().signIn(request).enqueue(new Callback<SupabaseClient.AuthResponse>() {
            @Override
            public void onResponse(Call<SupabaseClient.AuthResponse> call, Response<SupabaseClient.AuthResponse> response) {
                authButton.setEnabled(true);
                authButton.setText("Login");

                if (response.isSuccessful() && response.body() != null) {
                    SupabaseClient.AuthResponse authResponse = response.body();

                    if (authResponse.error != null) {
                        Toast.makeText(LoginActivity.this,
                                "Login failed: " + authResponse.errorDescription,
                                Toast.LENGTH_LONG).show();
                        return;
                    }

                    String username = "";
                    if (authResponse.user.userMetadata != null &&
                            authResponse.user.userMetadata.containsKey("username")) {
                        username = (String) authResponse.user.userMetadata.get("username");
                    }

                    sessionManager.saveSession(
                            authResponse.accessToken,
                            authResponse.refreshToken,
                            authResponse.user.id,
                            authResponse.user.email,
                            username
                    );

                    supabaseClient.setAccessToken(authResponse.accessToken);

                    Toast.makeText(LoginActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
                    navigateToMain();
                } else {
                    Toast.makeText(LoginActivity.this,
                            "Login failed. Please check your credentials.",
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<SupabaseClient.AuthResponse> call, Throwable t) {
                authButton.setEnabled(true);
                authButton.setText("Login");
                Toast.makeText(LoginActivity.this,
                        "Login failed: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void performRegistration(String email, String password, String username) {
        authButton.setEnabled(false);
        authButton.setText("Loading...");

        Map<String, String> userData = new HashMap<>();
        userData.put("username", username);
        userData.put("full_name", username);

        SupabaseClient.SignUpRequest request = new SupabaseClient.SignUpRequest(email, password, userData);

        supabaseClient.getAuthApi().signUp(request).enqueue(new Callback<SupabaseClient.AuthResponse>() {
            @Override
            public void onResponse(Call<SupabaseClient.AuthResponse> call, Response<SupabaseClient.AuthResponse> response) {
                authButton.setEnabled(true);
                authButton.setText("Register");

                if (response.isSuccessful() && response.body() != null) {
                    SupabaseClient.AuthResponse authResponse = response.body();

                    if (authResponse.error != null) {
                        Toast.makeText(LoginActivity.this,
                                "Registration failed: " + authResponse.errorDescription,
                                Toast.LENGTH_LONG).show();
                        return;
                    }

                    Toast.makeText(LoginActivity.this,
                            "Registration successful! You can now login.",
                            Toast.LENGTH_LONG).show();

                    toggleMode();
                } else {
                    Toast.makeText(LoginActivity.this,
                            "Registration failed. Please try again.",
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<SupabaseClient.AuthResponse> call, Throwable t) {
                authButton.setEnabled(true);
                authButton.setText("Register");
                Toast.makeText(LoginActivity.this,
                        "Registration failed: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void navigateToMain() {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        finish();
    }
}
