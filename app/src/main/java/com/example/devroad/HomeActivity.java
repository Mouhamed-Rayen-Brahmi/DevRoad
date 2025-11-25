package com.example.devroad;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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
    
    private SessionManager sessionManager;
    private SupabaseClient supabaseClient;
    private CoursAdapter coursAdapter;
    private SoundManager soundManager;
    
    // Menu items
    private MenuItem musicMenuItem;
    private MenuItem soundEffectsMenuItem;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        
        // Set up toolbar
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("DevRoad");
        
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
        
        usernameText.setText("Hello, " + sessionManager.getUsername() + "!");
        
        // Initial score display (will be updated after fetch completes)
        updateScoreDisplay();
        
        // Update score display after a brief delay to allow database fetch to complete
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            updateScoreDisplay();
            android.util.Log.d("HomeActivity", "Score refreshed after delay: " + sessionManager.getScore());
        }, 500);
        
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
    
    /**
     * Inflate the options menu
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        
        // Get menu items for later updates
        musicMenuItem = menu.findItem(R.id.menu_music);
        soundEffectsMenuItem = menu.findItem(R.id.menu_sound_effects);
        
        // Update menu item titles with current states
        updateMenuItemTitles();
        
        return true;
    }
    
    /**
     * Handle menu item clicks
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        
        if (itemId == R.id.menu_music) {
            toggleMusic();
            return true;
        } else if (itemId == R.id.menu_sound_effects) {
            toggleSoundEffects();
            return true;
        } else if (itemId == R.id.menu_logout) {
            performLogout();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
    
    /**
     * Update menu item titles to reflect current state
     */
    private void updateMenuItemTitles() {
        if (musicMenuItem != null) {
            String musicState = soundManager.isMusicEnabled() ? "ON" : "OFF";
            musicMenuItem.setTitle("Background Music: " + musicState);
        }
        
        if (soundEffectsMenuItem != null) {
            String effectsState = soundManager.areSoundEffectsEnabled() ? "ON" : "OFF";
            soundEffectsMenuItem.setTitle("Sound Effects: " + effectsState);
        }
    }
    
    /**
     * Toggle background music on/off
     */
    private void toggleMusic() {
        soundManager.toggleMusic();
        updateMenuItemTitles();
        
        Toast.makeText(this, 
                soundManager.isMusicEnabled() ? "🎵 Background Music: ON" : "🔇 Background Music: OFF", 
                Toast.LENGTH_SHORT).show();
    }
    
    /**
     * Toggle sound effects on/off
     */
    private void toggleSoundEffects() {
        soundManager.toggleSoundEffects();
        updateMenuItemTitles();
        
        // Play a test sound effect when enabling
        if (soundManager.areSoundEffectsEnabled()) {
            soundManager.playCorrectSound();
            Toast.makeText(this, 
                    "🔊 Sound Effects: ON", 
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, 
                    "🔇 Sound Effects: OFF", 
                    Toast.LENGTH_SHORT).show();
        }
    }
    
    private void updateSoundButtons() {
        // This method is no longer needed, but kept for compatibility if referenced elsewhere
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
    
    /**
     * Update the score display text
     */
    private void updateScoreDisplay() {
        int currentScore = sessionManager.getScore();
        scoreText.setText(currentScore + " pts");
    }
    
    /**
     * Refresh score with animation
     */
    private void refreshScore() {
        // Update score from session manager (which was updated by ExerciseActivity)
        updateScoreDisplay();
        
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
        
        // Reset score to 0 before clearing session
        sessionManager.resetScore();
        
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
