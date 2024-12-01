package com.example.memorix;

public class FlashCardData {
    private String term;
    private String definition;

    public FlashCardData(String term, String definition) {
        this.term = term;
        this.definition = definition;
    }

    public String getTerm() {
        return term;
    }

    public String getDefinition() {
        return definition;
    }
}
