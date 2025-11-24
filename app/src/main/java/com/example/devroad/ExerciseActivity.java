package com.example.devroad;

import android.content.ClipData;
import android.graphics.Color;
import android.os.Bundle;
import android.view.DragEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.devroad.Models.Exercise;
import com.example.devroad.Supabase.SupabaseClient;
import com.example.devroad.services.SoundManager;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ExerciseActivity extends AppCompatActivity {
    
    private TextView lessonTitleText;
    private TextView progressText;
    private TextView questionText;
    private LinearLayout exerciseContainer;
    private Button submitButton;
    private View progressBar;
    
    private String lessonId;
    private String lessonTitle;
    private List<Exercise> exercises = new ArrayList<>();
    private int currentIndex = 0;
    private int totalScore = 0;
    
    private SessionManager sessionManager;
    private SoundManager soundManager;
    private Gson gson = new Gson();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise);
        
        lessonId = getIntent().getStringExtra("lesson_id");
        lessonTitle = getIntent().getStringExtra("lesson_title");
        
        sessionManager = new SessionManager(this);
        soundManager = SoundManager.getInstance(this);
        
        initViews();
        loadExercises();
    }
    
    private void initViews() {
        lessonTitleText = findViewById(R.id.lesson_title);
        progressText = findViewById(R.id.progress_text);
        questionText = findViewById(R.id.question_text);
        exerciseContainer = findViewById(R.id.exercise_container);
        submitButton = findViewById(R.id.submit_button);
        progressBar = findViewById(R.id.progress_bar);
        
        lessonTitleText.setText(lessonTitle);
        
        findViewById(R.id.back_button).setOnClickListener(v -> finish());
        submitButton.setOnClickListener(v -> checkAnswer());
    }
    
    private void loadExercises() {
        progressBar.setVisibility(View.VISIBLE);
        
        SupabaseClient.getInstance().getDataApi()
                .getExercisesByLesson("eq." + lessonId, "*", "order_index.asc")
                .enqueue(new Callback<List<Exercise>>() {
                    @Override
                    public void onResponse(Call<List<Exercise>> call, Response<List<Exercise>> response) {
                        progressBar.setVisibility(View.GONE);
                        
                        if (response.isSuccessful() && response.body() != null) {
                            exercises = response.body();
                            if (!exercises.isEmpty()) {
                                displayExercise(0);
                            } else {
                                Toast.makeText(ExerciseActivity.this, 
                                        "No exercises available", 
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                    
                    @Override
                    public void onFailure(Call<List<Exercise>> call, Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(ExerciseActivity.this, 
                                "Error: " + t.getMessage(), 
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
    
    private void displayExercise(int index) {
        if (exercises.isEmpty() || index >= exercises.size()) {
            // Finished all exercises
            showResults();
            return;
        }
        
        currentIndex = index;
        Exercise exercise = exercises.get(index);
        
        questionText.setText(exercise.getQuestion());
        progressText.setText((index + 1) + " / " + exercises.size());
        
        exerciseContainer.removeAllViews();
        
        switch (exercise.getType()) {
            case "drag_drop":
                setupDragDropExercise(exercise);
                break;
            case "multiple_choice":
                setupMultipleChoiceExercise(exercise);
                break;
            case "fill_blanks":
                setupFillBlanksExercise(exercise);
                break;
            case "arrange_code":
                setupArrangeCodeExercise(exercise);
                break;
        }
    }
    
    private void setupDragDropExercise(Exercise exercise) {
        // Parse data: {"items": ["item1", "item2"], "targets": ["target1", "target2"]}
        DragDropData data = gson.fromJson(exercise.getData(), DragDropData.class);
        
        LinearLayout itemsLayout = new LinearLayout(this);
        itemsLayout.setOrientation(LinearLayout.VERTICAL);
        itemsLayout.setPadding(16, 16, 16, 16);
        
        // Targets
        for (String target : data.targets) {
            CardView targetCard = createDragTarget(target);
            itemsLayout.addView(targetCard);
        }
        
        // Add spacer
        View spacer = new View(this);
        spacer.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 40));
        itemsLayout.addView(spacer);
        
        // Items to drag
        List<String> shuffledItems = new ArrayList<>(data.items);
        Collections.shuffle(shuffledItems);
        
        LinearLayout draggableLayout = new LinearLayout(this);
        draggableLayout.setOrientation(LinearLayout.HORIZONTAL);
        draggableLayout.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 
                ViewGroup.LayoutParams.WRAP_CONTENT));
        
        for (String item : shuffledItems) {
            CardView itemCard = createDraggableItem(item);
            draggableLayout.addView(itemCard);
        }
        
        itemsLayout.addView(draggableLayout);
        exerciseContainer.addView(itemsLayout);
    }
    
    private CardView createDraggableItem(String text) {
        CardView card = new CardView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        params.setMargins(8, 8, 8, 8);
        card.setLayoutParams(params);
        card.setCardElevation(4f);
        card.setRadius(12f);
        card.setTag(text);
        
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setPadding(16, 16, 16, 16);
        textView.setTextSize(14);
        textView.setTextColor(Color.parseColor("#212121"));
        
        card.addView(textView);
        
        card.setOnLongClickListener(v -> {
            ClipData data = ClipData.newPlainText("", "");
            View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(v);
            v.startDragAndDrop(data, shadowBuilder, v, 0);
            return true;
        });
        
        return card;
    }
    
    private CardView createDragTarget(String text) {
        CardView card = new CardView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(8, 8, 8, 8);
        card.setLayoutParams(params);
        card.setCardElevation(4f);
        card.setRadius(12f);
        card.setCardBackgroundColor(Color.parseColor("#F5F5F5"));
        card.setTag("target_" + text);
        
        TextView textView = new TextView(this);
        textView.setText(text + " ⬇");
        textView.setPadding(16, 32, 16, 32);
        textView.setTextSize(16);
        
        card.addView(textView);
        
        card.setOnDragListener((v, event) -> {
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_ENTERED:
                    card.setCardBackgroundColor(Color.parseColor("#E3F2FD"));
                    break;
                case DragEvent.ACTION_DRAG_EXITED:
                    card.setCardBackgroundColor(Color.parseColor("#F5F5F5"));
                    break;
                case DragEvent.ACTION_DROP:
                    View draggedView = (View) event.getLocalState();
                    v.setTag("dropped_" + draggedView.getTag());
                    ((TextView) ((CardView) v).getChildAt(0))
                            .setText(text + " ✓ " + draggedView.getTag());
                    draggedView.setVisibility(View.GONE);
                    break;
            }
            return true;
        });
        
        return card;
    }
    
    private void setupMultipleChoiceExercise(Exercise exercise) {
        // Parse data: {"options": ["option1", "option2", "option3", "option4"]}
        MultipleChoiceData data = gson.fromJson(exercise.getData(), MultipleChoiceData.class);
        
        LinearLayout optionsLayout = new LinearLayout(this);
        optionsLayout.setOrientation(LinearLayout.VERTICAL);
        optionsLayout.setPadding(16, 16, 16, 16);
        
        for (int i = 0; i < data.options.size(); i++) {
            String option = data.options.get(i);
            CardView optionCard = createChoiceOption(option, i);
            optionsLayout.addView(optionCard);
        }
        
        exerciseContainer.addView(optionsLayout);
    }
    
    private CardView createChoiceOption(String text, int index) {
        CardView card = new CardView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 12, 0, 12);
        card.setLayoutParams(params);
        card.setCardElevation(3f);
        card.setRadius(16f);
        card.setTag("unselected");
        card.setClickable(true);
        card.setFocusable(true);
        
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setPadding(20, 20, 20, 20);
        layout.setGravity(android.view.Gravity.CENTER_VERTICAL);
        
        TextView numberText = new TextView(this);
        numberText.setText(String.valueOf((char)('A' + index)));
        numberText.setTextSize(18);
        numberText.setTextColor(Color.WHITE);
        numberText.setTypeface(null, android.graphics.Typeface.BOLD);
        numberText.setPadding(16, 8, 16, 8);
        numberText.setBackground(getDrawable(R.drawable.circle_number));
        
        TextView optionText = new TextView(this);
        optionText.setText(text);
        optionText.setTextSize(16);
        optionText.setPadding(16, 0, 0, 0);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        optionText.setLayoutParams(textParams);
        
        layout.addView(numberText);
        layout.addView(optionText);
        card.addView(layout);
        
        card.setOnClickListener(v -> {
            // Deselect all
            for (int i = 0; i < ((ViewGroup) card.getParent()).getChildCount(); i++) {
                View child = ((ViewGroup) card.getParent()).getChildAt(i);
                child.setTag("unselected");
                ((CardView) child).setCardBackgroundColor(Color.WHITE);
            }
            // Select this one
            v.setTag("selected_" + text);
            card.setCardBackgroundColor(Color.parseColor("#E8F5E9"));
        });
        
        return card;
    }
    
    private void setupFillBlanksExercise(Exercise exercise) {
        // Parse data: {"blanks": [{"text": "The ___ is", "options": ["sun", "moon"]}]}
        FillBlanksData data = gson.fromJson(exercise.getData(), FillBlanksData.class);
        
        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setPadding(16, 16, 16, 16);
        
        for (int i = 0; i < data.blanks.size(); i++) {
            BlankItem blank = data.blanks.get(i);
            
            TextView textView = new TextView(this);
            textView.setText(blank.text);
            textView.setTextSize(16);
            textView.setPadding(0, 16, 0, 8);
            mainLayout.addView(textView);
            
            LinearLayout optionsLayout = new LinearLayout(this);
            optionsLayout.setOrientation(LinearLayout.HORIZONTAL);
            
            for (String option : blank.options) {
                Button optionBtn = createBlankOption(option, i);
                optionsLayout.addView(optionBtn);
            }
            
            mainLayout.addView(optionsLayout);
        }
        
        exerciseContainer.addView(mainLayout);
    }
    
    private Button createBlankOption(String text, int blankIndex) {
        Button button = new Button(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        params.setMargins(4, 4, 4, 4);
        button.setLayoutParams(params);
        button.setText(text);
        button.setTag("blank_" + blankIndex + "_unselected");
        button.setBackground(getDrawable(R.drawable.button_outline));
        
        button.setOnClickListener(v -> {
            // Deselect siblings
            ViewGroup parent = (ViewGroup) v.getParent();
            for (int i = 0; i < parent.getChildCount(); i++) {
                View child = parent.getChildAt(i);
                child.setTag("blank_" + blankIndex + "_unselected");
                child.setBackground(getDrawable(R.drawable.button_outline));
            }
            // Select this one
            v.setTag("blank_" + blankIndex + "_selected_" + text);
            v.setBackground(getDrawable(R.drawable.button_success));
        });
        
        return button;
    }
    
    private void setupArrangeCodeExercise(Exercise exercise) {
        // Parse data: {"lines": ["line1", "line2", "line3"]}
        ArrangeCodeData data = gson.fromJson(exercise.getData(), ArrangeCodeData.class);
        
        List<String> shuffledLines = new ArrayList<>(data.lines);
        Collections.shuffle(shuffledLines);
        
        LinearLayout codeLayout = new LinearLayout(this);
        codeLayout.setOrientation(LinearLayout.VERTICAL);
        codeLayout.setPadding(16, 16, 16, 16);
        codeLayout.setTag("code_container");
        
        for (String line : shuffledLines) {
            CardView lineCard = createCodeLine(line);
            codeLayout.addView(lineCard);
        }
        
        exerciseContainer.addView(codeLayout);
    }
    
    private CardView createCodeLine(String text) {
        CardView card = new CardView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 8, 0, 8);
        card.setLayoutParams(params);
        card.setCardElevation(2f);
        card.setRadius(8f);
        card.setTag(text);
        
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setTextSize(14);
        textView.setTypeface(android.graphics.Typeface.MONOSPACE);
        textView.setPadding(16, 16, 16, 16);
        textView.setBackgroundColor(Color.parseColor("#F5F5F5"));
        
        card.addView(textView);
        
        card.setOnLongClickListener(v -> {
            ClipData data = ClipData.newPlainText("", "");
            View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(v);
            v.startDragAndDrop(data, shadowBuilder, v, 0);
            return true;
        });
        
        card.setOnDragListener((v, event) -> {
            switch (event.getAction()) {
                case DragEvent.ACTION_DROP:
                    View draggedView = (View) event.getLocalState();
                    ViewGroup parent = (ViewGroup) v.getParent();
                    
                    int draggedIndex = parent.indexOfChild(draggedView);
                    int targetIndex = parent.indexOfChild(v);
                    
                    parent.removeView(draggedView);
                    parent.addView(draggedView, targetIndex);
                    break;
            }
            return true;
        });
        
        return card;
    }
    
    private void checkAnswer() {
        Exercise exercise = exercises.get(currentIndex);
        boolean isCorrect = false;
        
        switch (exercise.getType()) {
            case "drag_drop":
                isCorrect = checkDragDropAnswer(exercise);
                break;
            case "multiple_choice":
                isCorrect = checkMultipleChoiceAnswer(exercise);
                break;
            case "fill_blanks":
                isCorrect = checkFillBlanksAnswer(exercise);
                break;
            case "arrange_code":
                isCorrect = checkArrangeCodeAnswer(exercise);
                break;
        }
        
        if (isCorrect) {
            // Play correct answer sound with cool effect
            soundManager.playCorrectSound();
            
            totalScore += exercise.getPoints();
            
            // Animate submit button with success
            submitButton.setBackgroundColor(Color.parseColor("#4CAF50"));
            submitButton.animate()
                    .scaleX(1.1f)
                    .scaleY(1.1f)
                    .setDuration(200)
                    .withEndAction(() -> submitButton.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(200)
                            .start())
                    .start();
            
            Toast.makeText(this, "🎉 Correct! +" + exercise.getPoints() + " points", 
                    Toast.LENGTH_SHORT).show();
        } else {
            // Play wrong answer sound
            soundManager.playWrongSound();
            
            // Shake animation for wrong answer
            submitButton.setBackgroundColor(Color.parseColor("#F44336"));
            submitButton.animate()
                    .translationX(-10f)
                    .setDuration(50)
                    .withEndAction(() -> submitButton.animate()
                            .translationX(10f)
                            .setDuration(50)
                            .withEndAction(() -> submitButton.animate()
                                    .translationX(-10f)
                                    .setDuration(50)
                                    .withEndAction(() -> submitButton.animate()
                                            .translationX(0f)
                                            .setDuration(50)
                                            .start())
                                    .start())
                            .start())
                    .start();
            
            Toast.makeText(this, "❌ Incorrect. Try again!", 
                    Toast.LENGTH_SHORT).show();
        }
        
        // Move to next exercise after a delay
        submitButton.postDelayed(() -> {
            // Reset button color
            submitButton.setBackgroundColor(Color.parseColor("#6200EA"));
            displayExercise(currentIndex + 1);
        }, 1500);
    }
    
    private boolean checkDragDropAnswer(Exercise exercise) {
        // Simple check: verify all targets have been filled
        for (int i = 0; i < exerciseContainer.getChildCount(); i++) {
            View child = exerciseContainer.getChildAt(i);
            if (child instanceof LinearLayout) {
                LinearLayout layout = (LinearLayout) child;
                for (int j = 0; j < layout.getChildCount(); j++) {
                    View item = layout.getChildAt(j);
                    if (item.getTag() != null && item.getTag().toString().startsWith("target_")) {
                        return false; // Not all filled
                    }
                }
            }
        }
        return true;
    }
    
    private boolean checkMultipleChoiceAnswer(Exercise exercise) {
        LinearLayout container = (LinearLayout) exerciseContainer.getChildAt(0);
        for (int i = 0; i < container.getChildCount(); i++) {
            View child = container.getChildAt(i);
            if (child.getTag() != null && child.getTag().toString().startsWith("selected_")) {
                String selected = child.getTag().toString().replace("selected_", "");
                return selected.equals(exercise.getAnswer());
            }
        }
        return false;
    }
    
    private boolean checkFillBlanksAnswer(Exercise exercise) {
        // Check if answer matches selected options
        return exercise.getAnswer().equals("correct"); // Simplified
    }
    
    private boolean checkArrangeCodeAnswer(Exercise exercise) {
        ArrangeCodeData data = gson.fromJson(exercise.getData(), ArrangeCodeData.class);
        LinearLayout codeContainer = (LinearLayout) exerciseContainer.getChildAt(0);
        
        for (int i = 0; i < codeContainer.getChildCount(); i++) {
            View child = codeContainer.getChildAt(i);
            if (!child.getTag().toString().equals(data.lines.get(i))) {
                return false;
            }
        }
        return true;
    }
    
    private void showResults() {
        // Update user score in database and session
        sessionManager.updateScore(sessionManager.getScore() + totalScore);
        
        Toast.makeText(this, 
                "Exercise completed! Total score: " + totalScore + " points", 
                Toast.LENGTH_LONG).show();
        
        finish();
    }
    
    // Data classes for JSON parsing
    static class DragDropData {
        List<String> items;
        List<String> targets;
    }
    
    static class MultipleChoiceData {
        List<String> options;
    }
    
    static class FillBlanksData {
        List<BlankItem> blanks;
    }
    
    static class BlankItem {
        String text;
        List<String> options;
    }
    
    static class ArrangeCodeData {
        List<String> lines;
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
