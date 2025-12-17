package com.riege.rmc.terminal.impl.swing.model;

import javax.swing.text.StyledDocument;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Data model for the terminal.
 * Stores document, candidates, and other state.
 */
public class TerminalDataModel {

    private final StyledDocument document;
    private final List<String> candidates = new ArrayList<>();

    public TerminalDataModel(StyledDocument document) {
        this.document = document;
    }

    public StyledDocument getDocument() {
        return document;
    }

    public List<String> getCandidates() {
        return Collections.unmodifiableList(candidates);
    }

    public void addCandidate(String candidate) {
        candidates.add(candidate);
    }

    public void clearCandidates() {
        candidates.clear();
    }
}
