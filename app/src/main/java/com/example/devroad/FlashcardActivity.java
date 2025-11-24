package com.example.devroad;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.devroad.Models.Flashcard;
import com.example.devroad.Supabase.SupabaseClient;
import com.example.devroad.services.SoundManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FlashcardActivity extends AppCompatActivity {
    
    private TextView lessonTitleText;
    private TextView progressText;
    private CardView flashcardFront;
    private CardView flashcardBack;
    private TextView frontContent;
    private TextView backContent;
    private Button prevButton;
    private Button nextButton;
    private Button toExercisesButton;
    private View progressBar;
    
    private String lessonId;
    private String lessonTitle;
    private List<Flashcard> flashcards = new ArrayList<>();
    private int currentIndex = 0;
    private boolean isFrontShowing = true;
    
    private AnimatorSet setRightOut;
    private AnimatorSet setLeftIn;
    private SoundManager soundManager;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flashcard);
        
        lessonId = getIntent().getStringExtra("lesson_id");
        lessonTitle = getIntent().getStringExtra("lesson_title");
        
        soundManager = SoundManager.getInstance(this);
        
        initViews();
        loadAnimations();
        loadFlashcards();
    }
    
    private void initViews() {
        lessonTitleText = findViewById(R.id.lesson_title);
        progressText = findViewById(R.id.progress_text);
        flashcardFront = findViewById(R.id.flashcard_front);
        flashcardBack = findViewById(R.id.flashcard_back);
        frontContent = findViewById(R.id.front_content);
        backContent = findViewById(R.id.back_content);
        prevButton = findViewById(R.id.prev_button);
        nextButton = findViewById(R.id.next_button);
        toExercisesButton = findViewById(R.id.to_exercises_button);
        progressBar = findViewById(R.id.progress_bar);
        
        lessonTitleText.setText(lessonTitle);
        
        findViewById(R.id.back_button).setOnClickListener(v -> finish());
        
        flashcardFront.setOnClickListener(v -> flipCard());
        flashcardBack.setOnClickListener(v -> flipCard());
        
        prevButton.setOnClickListener(v -> previousCard());
        nextButton.setOnClickListener(v -> nextCard());
        toExercisesButton.setOnClickListener(v -> goToExercises());
    }
    
    private void loadAnimations() {
        setRightOut = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.card_flip_right_out);
        setLeftIn = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.card_flip_left_in);
    }
    
    private void loadFlashcards() {
        progressBar.setVisibility(View.VISIBLE);
        
        SupabaseClient.getInstance().getDataApi()
                .getFlashcardsByLesson("eq." + lessonId, "*", "order_index.asc")
                .enqueue(new Callback<List<Flashcard>>() {
                    @Override
                    public void onResponse(Call<List<Flashcard>> call, Response<List<Flashcard>> response) {
                        progressBar.setVisibility(View.GONE);
                        
                        if (response.isSuccessful() && response.body() != null) {
                            flashcards = response.body();
                            if (!flashcards.isEmpty()) {
                                displayCard(0);
                            } else {
                                Toast.makeText(FlashcardActivity.this, 
                                        "No flashcards available", 
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                    
                    @Override
                    public void onFailure(Call<List<Flashcard>> call, Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(FlashcardActivity.this, 
                                "Error: " + t.getMessage(), 
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
    
    private void displayCard(int index) {
        if (flashcards.isEmpty() || index < 0 || index >= flashcards.size()) {
            return;
        }
        
        currentIndex = index;
        Flashcard card = flashcards.get(index);
        
        frontContent.setText(card.getFrontContent());
        backContent.setText(card.getBackContent());
        
        progressText.setText((index + 1) + " / " + flashcards.size());
        
        prevButton.setEnabled(index > 0);
        nextButton.setEnabled(index < flashcards.size() - 1);
        
        // Show front, hide back
        if (!isFrontShowing) {
            flipCard();
        }
    }
    
    private void flipCard() {
        if (isFrontShowing) {
            setRightOut.setTarget(flashcardFront);
            setLeftIn.setTarget(flashcardBack);
            setRightOut.start();
            setLeftIn.start();
            flashcardFront.setVisibility(View.GONE);
            flashcardBack.setVisibility(View.VISIBLE);
        } else {
            setRightOut.setTarget(flashcardBack);
            setLeftIn.setTarget(flashcardFront);
            setRightOut.start();
            setLeftIn.start();
            flashcardBack.setVisibility(View.GONE);
            flashcardFront.setVisibility(View.VISIBLE);
        }
        isFrontShowing = !isFrontShowing;
    }
    
    private void previousCard() {
        if (currentIndex > 0) {
            displayCard(currentIndex - 1);
        }
    }
    
    private void nextCard() {
        if (currentIndex < flashcards.size() - 1) {
            displayCard(currentIndex + 1);
        }
    }
    
    private void goToExercises() {
        Intent intent = new Intent(this, ExerciseActivity.class);
        intent.putExtra("lesson_id", lessonId);
        intent.putExtra("lesson_title", lessonTitle);
        startActivity(intent);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        soundManager.onResume();
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        soundManager.onPause();
    }
}
