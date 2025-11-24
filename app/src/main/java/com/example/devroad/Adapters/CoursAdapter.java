package com.example.devroad.Adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.devroad.Models.Cours;
import com.example.devroad.R;

import java.util.List;

public class CoursAdapter extends RecyclerView.Adapter<CoursAdapter.CoursViewHolder> {

    private List<Cours> courses;
    private OnCourseClickListener listener;

    public interface OnCourseClickListener {
        void onCourseClick(Cours cours);
    }

    public CoursAdapter(List<Cours> courses, OnCourseClickListener listener) {
        this.courses = courses;
        this.listener = listener;
    }

    public void updateCourses(List<Cours> newCourses) {
        this.courses = newCourses;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CoursViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_course, parent, false);
        return new CoursViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CoursViewHolder holder, int position) {
        Cours cours = courses.get(position);
        holder.bind(cours, listener);
        
        // Animate item
        holder.itemView.setAnimation(AnimationUtils.loadAnimation(
                holder.itemView.getContext(), R.anim.item_animation_slide_from_right));
    }

    @Override
    public int getItemCount() {
        return courses.size();
    }

    static class CoursViewHolder extends RecyclerView.ViewHolder {
        private CardView cardView;
        private TextView iconText;
        private TextView titleText;
        private TextView descriptionText;
        private TextView premiumBadge;

        public CoursViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (CardView) itemView;
            iconText = itemView.findViewById(R.id.course_icon);
            titleText = itemView.findViewById(R.id.course_title);
            descriptionText = itemView.findViewById(R.id.course_description);
            premiumBadge = itemView.findViewById(R.id.premium_badge);
        }

        public void bind(Cours cours, OnCourseClickListener listener) {
            iconText.setText(cours.getIcon());
            titleText.setText(cours.getTitle());
            descriptionText.setText(cours.getDescription());
            
            if (cours.isPremium()) {
                premiumBadge.setVisibility(View.VISIBLE);
                premiumBadge.setText("🔒 " + cours.getRequiredScore() + " pts");
            } else {
                premiumBadge.setVisibility(View.GONE);
            }
            
            // Set color if provided
            if (cours.getColor() != null && !cours.getColor().isEmpty()) {
                try {
                    cardView.setCardBackgroundColor(Color.parseColor(cours.getColor()));
                } catch (Exception e) {
                    // Use default
                }
            }
            
            itemView.setOnClickListener(v -> listener.onCourseClick(cours));
        }
    }
}
