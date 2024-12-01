package com.example.memorix;

public class LibraryItem {
    private String name;
    private String questionnaireId;
    private String title;
    private int itemsCount;

    public LibraryItem(String name, String questionnaireId, String title, int itemsCount) {
        this.name = name;
        this.questionnaireId = questionnaireId;
        this.title = title;
        this.itemsCount = itemsCount;
    }

    public String getName() {
        return name;
    }

    public String getQuestionnaireId() {
        return questionnaireId;
    }

    public String getTitle() {
        return title;
    }

    public int getItemsCount() {
        return itemsCount;
    }
}

