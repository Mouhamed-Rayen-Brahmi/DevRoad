package com.example.devroad.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.devroad.Models.Lesson;
import com.example.devroad.R;

import java.util.List;

public class LessonAdapter extends RecyclerView.Adapter<LessonAdapter.LessonViewHolder> {

    private List<Lesson> lessons;
    private OnLessonClickListener listener;

    public interface OnLessonClickListener {
        void onLessonClick(Lesson lesson);
    }

    public LessonAdapter(List<Lesson> lessons, OnLessonClickListener listener) {
        this.lessons = lessons;
        this.listener = listener;
    }

    public void updateLessons(List<Lesson> newLessons) {
        this.lessons = newLessons;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public LessonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_lesson, parent, false);
        return new LessonViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LessonViewHolder holder, int position) {
        Lesson lesson = lessons.get(position);
        holder.bind(lesson, position + 1, listener);
    }

    @Override
    public int getItemCount() {
        return lessons.size();
    }

    static class LessonViewHolder extends RecyclerView.ViewHolder {
        private TextView numberText;
        private TextView titleText;
        private TextView premiumBadge;
        private CardView cardView;

        public LessonViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (CardView) itemView;
            numberText = itemView.findViewById(R.id.lesson_number);
            titleText = itemView.findViewById(R.id.lesson_title);
            premiumBadge = itemView.findViewById(R.id.premium_badge);
        }

        public void bind(Lesson lesson, int position, OnLessonClickListener listener) {
            numberText.setText(String.valueOf(position));
            titleText.setText(lesson.getTitle());
            
            if (lesson.isPremium()) {
                premiumBadge.setVisibility(View.VISIBLE);
                premiumBadge.setText("🔒 " + lesson.getRequiredScore() + " pts");
            } else {
                premiumBadge.setVisibility(View.GONE);
            }
            
            itemView.setOnClickListener(v -> listener.onLessonClick(lesson));
        }
    }
}
