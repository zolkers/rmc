package com.riege.rmc.terminal.impl.swing.controller;

import com.riege.rmc.terminal.Terminal;
import com.riege.rmc.terminal.impl.swing.model.TerminalDataModel;
import com.riege.rmc.terminal.impl.swing.ui.ModernTerminalView;

import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.ArrayList;

/**
 * Controller for the modern terminal.
 * Handles user input, command history, and tab completion.
 */
public class ModernTerminalController {

    private final TerminalDataModel model;
    private final ModernTerminalView view;
    private final Terminal.NativeCallback inputCallback;
    private final Terminal.NativeCallback tabCallback;

    private int candidateIndex = 0;
    private final List<String> commandHistory = new ArrayList<>();
    private int historyIndex = -1;

    public ModernTerminalController(
        TerminalDataModel model,
        ModernTerminalView view,
        Terminal.NativeCallback inputCallback,
        Terminal.NativeCallback tabCallback
    ) {
        this.model = model;
        this.view = view;
        this.inputCallback = inputCallback;
        this.tabCallback = tabCallback;

        initializeController();
    }

    private void initializeController() {
        System.out.println("[DEBUG] Initializing controller listeners...");

        // Handle Enter key - submit command
        view.getInputField().addActionListener(e -> {
            System.out.println("[DEBUG] Enter pressed!");
            handleCommandSubmit();
        });

        // Handle special keys - Tab, Up, Down
        view.getInputField().addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                System.out.println("[DEBUG] Key pressed: " + e.getKeyCode());
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_TAB:
                        e.consume();
                        handleTabCompletion();
                        break;

                    case KeyEvent.VK_UP:
                        e.consume();
                        handleHistoryUp();
                        break;

                    case KeyEvent.VK_DOWN:
                        e.consume();
                        handleHistoryDown();
                        break;

                    case KeyEvent.VK_C:
                        if (e.isControlDown()) {
                            handleInterrupt();
                        }
                        break;

                    case KeyEvent.VK_L:
                        if (e.isControlDown()) {
                            e.consume();
                            handleClearScreen();
                        }
                        break;
                }
            }
        });
    }

    private void handleCommandSubmit() {
        String input = view.getInputField().getText().trim();

        if (input.isEmpty()) {
            return;
        }

        // Add to history
        commandHistory.add(input);
        historyIndex = -1;

        // Clear input field
        view.getInputField().setText("");

        // Clear tab completion candidates
        model.clearCandidates();
        candidateIndex = 0;

        // Update status
        view.setStatus("Executing: " + input);

        // Invoke callback
        if (inputCallback != null) {
            try {
                inputCallback.invoke(input);
                view.setStatus("Ready");
            } catch (Exception e) {
                view.setStatus("Error: " + e.getMessage());
            }
        }
    }

    private void handleTabCompletion() {
        String currentInput = view.getInputField().getText();

        // If no candidates yet, request them
        if (model.getCandidates().isEmpty()) {
            if (tabCallback != null) {
                tabCallback.invoke(currentInput);
            }
        }

        // Cycle through candidates
        List<String> candidates = model.getCandidates();
        if (!candidates.isEmpty()) {
            view.getInputField().setText(candidates.get(candidateIndex));
            candidateIndex = (candidateIndex + 1) % candidates.size();
            view.setStatus("Tab completion: " + (candidateIndex) + "/" + candidates.size());
        }
    }

    private void handleHistoryUp() {
        if (commandHistory.isEmpty()) return;
        if (historyIndex == -1) historyIndex = commandHistory.size();
        if (historyIndex > 0) {
            historyIndex--;
            view.getInputField().setText(commandHistory.get(historyIndex));
        }
        model.clearCandidates();
        candidateIndex = 0;
    }

    private void handleHistoryDown() {
        if (commandHistory.isEmpty() || historyIndex == -1) return;
        historyIndex++;
        if (historyIndex < commandHistory.size()) {
            view.getInputField().setText(commandHistory.get(historyIndex));
        } else {
            view.getInputField().setText("");
            historyIndex = -1;
        }
        model.clearCandidates();
        candidateIndex = 0;
    }

    private void handleInterrupt() {
        view.getInputField().setText("");
        model.clearCandidates();
        candidateIndex = 0;
        view.setStatus("Interrupted");
    }

    private void handleClearScreen() {
        try {
            view.getTextPane().getDocument().remove(
                0,
                view.getTextPane().getDocument().getLength()
            );
            view.setStatus("Screen cleared");
        } catch (Exception e) {
            view.setStatus("Failed to clear screen");
        }
    }
}
