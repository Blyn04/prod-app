package com.example.moodchecker.model;

import java.io.Serializable;
import java.util.List;

public class Flashcard implements Serializable {
    private String question;
    private String answer;
    private String answerType;
    private List<String> options;
    private String correctAnswer;
    private boolean isAnswered = false;
    private boolean isCorrect = false;

    public Flashcard(String question, String answer, String answerType, List<String> options) {
        this.question = question;
        this.answer = answer;
        this.answerType = answerType;
        this.options = options;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getAnswerType() {
        return answerType;
    }

    public void setAnswerType(String answerType) {
        this.answerType = answerType;
    }

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }

    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public boolean isAnswered() {
        return isAnswered;
    }

    public void setAnswered(boolean answered) {
        isAnswered = answered;
    }

    public boolean isCorrect() {
        return isCorrect;
    }

    public void setCorrect(boolean correct) {
        isCorrect = correct;
    }
}
