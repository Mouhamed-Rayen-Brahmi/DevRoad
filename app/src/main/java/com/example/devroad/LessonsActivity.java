package com.example.devroad;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.devroad.Adapters.LessonAdapter;
import com.example.devroad.Models.Lesson;
import com.example.devroad.Supabase.SupabaseClient;
import com.example.devroad.services.SoundManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LessonsActivity extends AppCompatActivity {
    
    private TextView courseTitleText;
    private RecyclerView lessonsRecyclerView;
    private View progressBar;
    
    private String courseId;
    private String courseTitle;
    private SessionManager sessionManager;
    private SupabaseClient supabaseClient;
    private LessonAdapter lessonAdapter;
    private SoundManager soundManager;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lessons);
        
        courseId = getIntent().getStringExtra("course_id");
        courseTitle = getIntent().getStringExtra("course_title");
        
        sessionManager = new SessionManager(this);
        supabaseClient = SupabaseClient.getInstance();
        soundManager = SoundManager.getInstance(this);
        
        initViews();
        loadLessons();
    }
    
    private void initViews() {
        courseTitleText = findViewById(R.id.course_title);
        lessonsRecyclerView = findViewById(R.id.lessons_recycler);
        progressBar = findViewById(R.id.progress_bar);
        
        courseTitleText.setText(courseTitle);
        
        findViewById(R.id.back_button).setOnClickListener(v -> finish());
        
        lessonsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        lessonAdapter = new LessonAdapter(new ArrayList<>(), this::onLessonClick);
        lessonsRecyclerView.setAdapter(lessonAdapter);
    }
    
    private void loadLessons() {
        progressBar.setVisibility(View.VISIBLE);
        
        supabaseClient.getDataApi()
                .getLessonsByCourse("eq." + courseId, "*", "order_index.asc")
                .enqueue(new Callback<List<Lesson>>() {
                    @Override
                    public void onResponse(Call<List<Lesson>> call, Response<List<Lesson>> response) {
                        progressBar.setVisibility(View.GONE);
                        
                        if (response.isSuccessful() && response.body() != null) {
                            lessonAdapter.updateLessons(response.body());
                        } else {
                            Toast.makeText(LessonsActivity.this, 
                                    "Failed to load lessons", 
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                    
                    @Override
                    public void onFailure(Call<List<Lesson>> call, Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(LessonsActivity.this, 
                                "Error: " + t.getMessage(), 
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
    
    private void onLessonClick(Lesson lesson) {
        if (lesson.isPremium() && sessionManager.getScore() < lesson.getRequiredScore()) {
            Toast.makeText(this, 
                    "You need " + lesson.getRequiredScore() + " points to unlock this lesson!", 
                    Toast.LENGTH_LONG).show();
            return;
        }
        
        Intent intent = new Intent(this, FlashcardActivity.class);
        intent.putExtra("lesson_id", lesson.getId());
        intent.putExtra("lesson_title", lesson.getTitle());
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
