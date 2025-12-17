package com.riege.rmc.terminal.impl.swing.components;

import com.riege.rmc.terminal.impl.swing.theme.TerminalTheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Modern text field with command history support.
 */
public class TextField extends JTextField {

    private final List<String> history = new ArrayList<>();
    private int historyIndex = -1;
    private String currentInput = "";

    public TextField() {
        setBackground(TerminalTheme.BACKGROUND_SECONDARY);
        setForeground(TerminalTheme.TEXT_PRIMARY);
        setCaretColor(TerminalTheme.ACCENT_PRIMARY);
        setFont(TerminalTheme.getMonoFont());
        setSelectionColor(TerminalTheme.SELECTION_BACKGROUND);
        setSelectedTextColor(TerminalTheme.SELECTION_TEXT);

        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, TerminalTheme.BORDER_DEFAULT),
            new EmptyBorder(
                TerminalTheme.PADDING_MEDIUM,
                TerminalTheme.PADDING_MEDIUM,
                TerminalTheme.PADDING_MEDIUM,
                TerminalTheme.PADDING_MEDIUM
            )
        ));

        // Focus border
        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(2, 0, 0, 0, TerminalTheme.BORDER_FOCUS),
                    new EmptyBorder(
                        TerminalTheme.PADDING_MEDIUM - 1,
                        TerminalTheme.PADDING_MEDIUM,
                        TerminalTheme.PADDING_MEDIUM,
                        TerminalTheme.PADDING_MEDIUM
                    )
                ));
            }

            @Override
            public void focusLost(FocusEvent e) {
                setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(1, 0, 0, 0, TerminalTheme.BORDER_DEFAULT),
                    new EmptyBorder(
                        TerminalTheme.PADDING_MEDIUM,
                        TerminalTheme.PADDING_MEDIUM,
                        TerminalTheme.PADDING_MEDIUM,
                        TerminalTheme.PADDING_MEDIUM
                    )
                ));
            }
        });
    }

    public void addToHistory(String command) {
        if (command != null && !command.trim().isEmpty()) {
            history.add(command);
            historyIndex = history.size();
        }
    }

    public void navigateHistoryUp() {
        if (history.isEmpty()) {
            return;
        }

        if (historyIndex == history.size()) {
            currentInput = getText();
        }

        if (historyIndex > 0) {
            historyIndex--;
            setText(history.get(historyIndex));
        }
    }

    public void navigateHistoryDown() {
        if (history.isEmpty() || historyIndex >= history.size()) {
            return;
        }

        historyIndex++;

        if (historyIndex < history.size()) {
            setText(history.get(historyIndex));
        } else {
            setText(currentInput);
        }
    }

    public void resetHistory() {
        historyIndex = history.size();
        currentInput = "";
    }
}
