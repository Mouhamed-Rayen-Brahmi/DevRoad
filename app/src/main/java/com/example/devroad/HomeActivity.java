package com.example.devroad;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.devroad.Adapters.CoursAdapter;
import com.example.devroad.Models.Cours;
import com.example.devroad.Supabase.SupabaseClient;
import com.example.devroad.services.SoundManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity {
    
    private TextView usernameText;
    private TextView scoreText;
    private RecyclerView coursesRecyclerView;
    private View progressBar;
    private ImageButton musicToggleButton;
    private ImageButton soundEffectsToggleButton;
    private ImageButton logoutButton;
    
    private SessionManager sessionManager;
    private SupabaseClient supabaseClient;
    private CoursAdapter coursAdapter;
    private SoundManager soundManager;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        
        sessionManager = new SessionManager(this);
        supabaseClient = SupabaseClient.getInstance();
        supabaseClient.setAccessToken(sessionManager.getAccessToken());
        soundManager = SoundManager.getInstance(this);
        
        initViews();
        loadUserData();
        loadCourses();
        
        // Start background music when entering the game
        soundManager.startBackgroundMusic();
    }
    
    private void initViews() {
        usernameText = findViewById(R.id.username_text);
        scoreText = findViewById(R.id.score_text);
        coursesRecyclerView = findViewById(R.id.courses_recycler);
        progressBar = findViewById(R.id.progress_bar);
        musicToggleButton = findViewById(R.id.music_toggle_button);
        soundEffectsToggleButton = findViewById(R.id.sound_effects_toggle_button);
        logoutButton = findViewById(R.id.logout_button);
        
        usernameText.setText("Hello, " + sessionManager.getUsername() + "!");
        scoreText.setText(sessionManager.getScore() + " pts");
        
        // Setup sound control buttons
        updateSoundButtons();
        
        musicToggleButton.setOnClickListener(v -> {
            soundManager.toggleMusic();
            updateSoundButtons();
            
            // Visual feedback
            v.animate()
                    .scaleX(0.8f)
                    .scaleY(0.8f)
                    .setDuration(100)
                    .withEndAction(() -> v.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(100)
                            .start())
                    .start();
            
            Toast.makeText(this, 
                    soundManager.isMusicEnabled() ? "🎵 Music ON" : "🔇 Music OFF", 
                    Toast.LENGTH_SHORT).show();
        });
        
        soundEffectsToggleButton.setOnClickListener(v -> {
            soundManager.toggleSoundEffects();
            updateSoundButtons();
            
            // Test sound effect when enabled
            if (soundManager.areSoundEffectsEnabled()) {
                soundManager.playCorrectSound();
            }
            
            // Visual feedback
            v.animate()
                    .scaleX(0.8f)
                    .scaleY(0.8f)
                    .setDuration(100)
                    .withEndAction(() -> v.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(100)
                            .start())
                    .start();
            
            Toast.makeText(this, 
                    soundManager.areSoundEffectsEnabled() ? "🔊 Effects ON" : "🔇 Effects OFF", 
                    Toast.LENGTH_SHORT).show();
        });
        
        logoutButton.setOnClickListener(v -> {
            // Visual feedback
            v.animate()
                    .scaleX(0.8f)
                    .scaleY(0.8f)
                    .setDuration(100)
                    .withEndAction(() -> {
                        v.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(100)
                                .start();
                        performLogout();
                    })
                    .start();
        });
        
        // Animate header
        View headerCard = findViewById(R.id.header_card);
        headerCard.setAlpha(0f);
        headerCard.setTranslationY(-50f);
        headerCard.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(600)
                .start();
        
        coursesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        coursAdapter = new CoursAdapter(new ArrayList<>(), this::onCourseClick);
        coursesRecyclerView.setAdapter(coursAdapter);
    }
    
    private void updateSoundButtons() {
        // Update music button icon
        if (soundManager.isMusicEnabled()) {
            musicToggleButton.setImageResource(android.R.drawable.ic_lock_silent_mode_off);
            musicToggleButton.setAlpha(1.0f);
        } else {
            musicToggleButton.setImageResource(android.R.drawable.ic_lock_silent_mode);
            musicToggleButton.setAlpha(0.5f);
        }
        
        // Update sound effects button icon
        if (soundManager.areSoundEffectsEnabled()) {
            soundEffectsToggleButton.setImageResource(android.R.drawable.ic_lock_silent_mode_off);
            soundEffectsToggleButton.setAlpha(1.0f);
        } else {
            soundEffectsToggleButton.setImageResource(android.R.drawable.ic_lock_silent_mode);
            soundEffectsToggleButton.setAlpha(0.5f);
        }
    }
    
    private void loadUserData() {
        // Could fetch updated user data from Supabase here
    }
    
    private void loadCourses() {
        progressBar.setVisibility(View.VISIBLE);
        
        supabaseClient.getDataApi().getAllCourses("*", "order_index.asc")
                .enqueue(new Callback<List<Cours>>() {
                    @Override
                    public void onResponse(Call<List<Cours>> call, Response<List<Cours>> response) {
                        progressBar.setVisibility(View.GONE);
                        
                        if (response.isSuccessful() && response.body() != null) {
                            coursAdapter.updateCourses(response.body());
                            
                            // Animate RecyclerView items
                            coursesRecyclerView.scheduleLayoutAnimation();
                        } else {
                            Toast.makeText(HomeActivity.this, 
                                    "Failed to load courses", 
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                    
                    @Override
                    public void onFailure(Call<List<Cours>> call, Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(HomeActivity.this, 
                                "Error: " + t.getMessage(), 
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
    
    private void onCourseClick(Cours cours) {
        // Check if premium and user has enough score
        if (cours.isPremium() && sessionManager.getScore() < cours.getRequiredScore()) {
            Toast.makeText(this, 
                    "You need " + cours.getRequiredScore() + " points to unlock this course!", 
                    Toast.LENGTH_LONG).show();
            return;
        }
        
        Intent intent = new Intent(this, LessonsActivity.class);
        intent.putExtra("course_id", cours.getId());
        intent.putExtra("course_title", cours.getTitle());
        startActivity(intent);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        soundManager.onResume();
        
        // Refresh score when returning to home activity
        refreshScore();
    }
    
    private void refreshScore() {
        // Update score from session manager (which was updated by ExerciseActivity)
        int currentScore = sessionManager.getScore();
        scoreText.setText(currentScore + " pts");
        
        // Add a subtle animation to draw attention to score update
        scoreText.animate()
                .scaleX(1.2f)
                .scaleY(1.2f)
                .setDuration(200)
                .withEndAction(() -> scoreText.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(200)
                        .start())
                .start();
    }
    
    private void performLogout() {
        // Stop background music
        soundManager.stopBackgroundMusic();
        
        // Clear session
        sessionManager.clearSession();
        
        // Navigate back to login
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
        
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        soundManager.onPause();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Don't release SoundManager here since it's a singleton
        // It will be reused across activities
    }
}
