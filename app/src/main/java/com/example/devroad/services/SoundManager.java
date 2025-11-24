package com.example.devroad.services;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.net.Uri;
import android.util.Log;

import java.io.IOException;

/**
 * SoundManager - Manages all game sounds including background music and sound effects
 * Singleton pattern ensures one instance across the app
 */
public class SoundManager {
    private static final String TAG = "SoundManager";
    private static final String PREFS_NAME = "SoundPrefs";
    private static final String KEY_MUSIC_ENABLED = "music_enabled";
    private static final String KEY_SOUND_EFFECTS_ENABLED = "sound_effects_enabled";

    private static SoundManager instance;
    private Context context;
    
    // Background Music
    private MediaPlayer backgroundMusic;
    private boolean isMusicEnabled = true;
    private boolean isMusicPrepared = false;
    
    // Sound Effects
    private SoundPool soundPool;
    private int correctSoundId = -1;
    private int wrongSoundId = -1;
    private boolean areSoundEffectsEnabled = true;
    
    private SharedPreferences prefs;

    private SoundManager(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        
        // Load preferences
        isMusicEnabled = prefs.getBoolean(KEY_MUSIC_ENABLED, true);
        areSoundEffectsEnabled = prefs.getBoolean(KEY_SOUND_EFFECTS_ENABLED, true);
        
        initializeSoundPool();
        initializeBackgroundMusic();
    }

    public static synchronized SoundManager getInstance(Context context) {
        if (instance == null) {
            instance = new SoundManager(context);
        }
        return instance;
    }

    /**
     * Initialize SoundPool for sound effects
     */
    private void initializeSoundPool() {
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(5)
                .setAudioAttributes(audioAttributes)
                .build();

        // Load sound effects from raw folder
        try {
            int correctSoundResId = context.getResources().getIdentifier(
                    "correct_answer", "raw", context.getPackageName());
            int wrongSoundResId = context.getResources().getIdentifier(
                    "wrong_answer", "raw", context.getPackageName());

            if (correctSoundResId != 0) {
                correctSoundId = soundPool.load(context, correctSoundResId, 1);
            }
            if (wrongSoundResId != 0) {
                wrongSoundId = soundPool.load(context, wrongSoundResId, 1);
            }

            Log.d(TAG, "Sound effects loaded successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error loading sound effects", e);
        }
    }

    /**
     * Initialize background music MediaPlayer
     */
    private void initializeBackgroundMusic() {
        try {
            int musicResId = context.getResources().getIdentifier(
                    "background_music", "raw", context.getPackageName());

            if (musicResId != 0) {
                backgroundMusic = MediaPlayer.create(context, musicResId);
                if (backgroundMusic != null) {
                    backgroundMusic.setLooping(true);
                    backgroundMusic.setVolume(0.5f, 0.5f); // 50% volume for ambiance
                    isMusicPrepared = true;
                    Log.d(TAG, "Background music initialized");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error initializing background music", e);
        }
    }

    /**
     * Play correct answer sound with cool effect
     */
    public void playCorrectSound() {
        if (areSoundEffectsEnabled && correctSoundId != -1) {
            soundPool.play(correctSoundId, 1.0f, 1.0f, 1, 0, 1.0f);
            Log.d(TAG, "Playing correct answer sound");
        }
    }

    /**
     * Play wrong answer sound
     */
    public void playWrongSound() {
        if (areSoundEffectsEnabled && wrongSoundId != -1) {
            soundPool.play(wrongSoundId, 1.0f, 1.0f, 1, 0, 1.0f);
            Log.d(TAG, "Playing wrong answer sound");
        }
    }

    /**
     * Start background music
     */
    public void startBackgroundMusic() {
        if (isMusicEnabled && backgroundMusic != null && isMusicPrepared && !backgroundMusic.isPlaying()) {
            try {
                backgroundMusic.start();
                Log.d(TAG, "Background music started");
            } catch (Exception e) {
                Log.e(TAG, "Error starting background music", e);
            }
        }
    }

    /**
     * Pause background music
     */
    public void pauseBackgroundMusic() {
        if (backgroundMusic != null && backgroundMusic.isPlaying()) {
            backgroundMusic.pause();
            Log.d(TAG, "Background music paused");
        }
    }

    /**
     * Stop background music
     */
    public void stopBackgroundMusic() {
        if (backgroundMusic != null && backgroundMusic.isPlaying()) {
            backgroundMusic.stop();
            try {
                backgroundMusic.prepare();
                isMusicPrepared = true;
            } catch (IOException e) {
                Log.e(TAG, "Error preparing music after stop", e);
            }
            Log.d(TAG, "Background music stopped");
        }
    }

    /**
     * Toggle music on/off
     */
    public void toggleMusic() {
        isMusicEnabled = !isMusicEnabled;
        prefs.edit().putBoolean(KEY_MUSIC_ENABLED, isMusicEnabled).apply();
        
        if (isMusicEnabled) {
            startBackgroundMusic();
        } else {
            pauseBackgroundMusic();
        }
        Log.d(TAG, "Music toggled: " + (isMusicEnabled ? "ON" : "OFF"));
    }

    /**
     * Toggle sound effects on/off
     */
    public void toggleSoundEffects() {
        areSoundEffectsEnabled = !areSoundEffectsEnabled;
        prefs.edit().putBoolean(KEY_SOUND_EFFECTS_ENABLED, areSoundEffectsEnabled).apply();
        Log.d(TAG, "Sound effects toggled: " + (areSoundEffectsEnabled ? "ON" : "OFF"));
    }

    /**
     * Set music enabled state
     */
    public void setMusicEnabled(boolean enabled) {
        isMusicEnabled = enabled;
        prefs.edit().putBoolean(KEY_MUSIC_ENABLED, enabled).apply();
        
        if (enabled) {
            startBackgroundMusic();
        } else {
            pauseBackgroundMusic();
        }
    }

    /**
     * Set sound effects enabled state
     */
    public void setSoundEffectsEnabled(boolean enabled) {
        areSoundEffectsEnabled = enabled;
        prefs.edit().putBoolean(KEY_SOUND_EFFECTS_ENABLED, enabled).apply();
    }

    /**
     * Check if music is enabled
     */
    public boolean isMusicEnabled() {
        return isMusicEnabled;
    }

    /**
     * Check if sound effects are enabled
     */
    public boolean areSoundEffectsEnabled() {
        return areSoundEffectsEnabled;
    }

    /**
     * Release all resources
     */
    public void release() {
        if (backgroundMusic != null) {
            if (backgroundMusic.isPlaying()) {
                backgroundMusic.stop();
            }
            backgroundMusic.release();
            backgroundMusic = null;
        }
        
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
        
        Log.d(TAG, "SoundManager resources released");
    }

    /**
     * Resume music if it was playing
     */
    public void onResume() {
        if (isMusicEnabled && backgroundMusic != null && !backgroundMusic.isPlaying()) {
            startBackgroundMusic();
        }
    }

    /**
     * Pause music when app goes to background
     */
    public void onPause() {
        pauseBackgroundMusic();
    }
}
