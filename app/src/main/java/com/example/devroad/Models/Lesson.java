package com.example.devroad.Models;

import com.google.gson.annotations.SerializedName;

public class Lesson {
    @SerializedName("id")
    private String id;

    @SerializedName("cours_id")
    private String coursId;

    @SerializedName("title")
    private String title;

    @SerializedName("order_index")
    private int orderIndex;

    @SerializedName("is_premium")
    private boolean isPremium;

    @SerializedName("required_score")
    private int requiredScore;

    public Lesson() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCoursId() {
        return coursId;
    }

    public void setCoursId(String coursId) {
        this.coursId = coursId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(int orderIndex) {
        this.orderIndex = orderIndex;
    }

    public boolean isPremium() {
        return isPremium;
    }

    public void setPremium(boolean premium) {
        isPremium = premium;
    }

    public int getRequiredScore() {
        return requiredScore;
    }

    public void setRequiredScore(int requiredScore) {
        this.requiredScore = requiredScore;
    }
}
