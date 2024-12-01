package com.example.memorix;

import java.util.List;

public class QuestionData {
    private String question;
    private List<String> choices;
    private String selectedAnswer;
    private String correctAnswer;

    public QuestionData(String question, List<String> choices, String selectedAnswer, String correctAnswer) {
        this.question = question;
        this.choices = choices;
        this.selectedAnswer = selectedAnswer;
        this.correctAnswer = correctAnswer;
    }

    public String getQuestion() {
        return question;
    }

    public List<String> getChoices() {
        return choices;
    }

    public String getSelectedAnswer() {
        return selectedAnswer;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public void setSelectedAnswer(String selectedAnswer) {
        this.selectedAnswer = selectedAnswer;
    }
}

