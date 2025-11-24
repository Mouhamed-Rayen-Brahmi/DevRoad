package com.example.devroad.Models;

import com.google.gson.annotations.SerializedName;

public class Flashcard {
    @SerializedName("id")
    private String id;

    @SerializedName("lesson_id")
    private String lessonId;

    @SerializedName("front_content")
    private String frontContent;

    @SerializedName("back_content")
    private String backContent;

    @SerializedName("order_index")
    private int orderIndex;

    public Flashcard() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLessonId() {
        return lessonId;
    }

    public void setLessonId(String lessonId) {
        this.lessonId = lessonId;
    }

    public String getFrontContent() {
        return frontContent;
    }

    public void setFrontContent(String frontContent) {
        this.frontContent = frontContent;
    }

    public String getBackContent() {
        return backContent;
    }

    public void setBackContent(String backContent) {
        this.backContent = backContent;
    }

    public int getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(int orderIndex) {
        this.orderIndex = orderIndex;
    }
}
