package com.example.devroad.Models;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

public class Exercise {
    @SerializedName("id")
    private String id;

    @SerializedName("lesson_id")
    private String lessonId;

    @SerializedName("type")
    private String type; // drag_drop, multiple_choice, fill_blanks, arrange_code

    @SerializedName("question")
    private String question;

    @SerializedName("data")
    private JsonObject data; // JSON object containing exercise-specific data

    @SerializedName("answer")
    private String answer; // Correct answer

    @SerializedName("points")
    private int points;

    @SerializedName("order_index")
    private int orderIndex;

    public Exercise() {
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public JsonObject getData() {
        return data;
    }

    public void setData(JsonObject data) {
        this.data = data;
    }
    
    public String getDataAsString() {
        return data != null ? data.toString() : "{}";
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public int getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(int orderIndex) {
        this.orderIndex = orderIndex;
    }
}
