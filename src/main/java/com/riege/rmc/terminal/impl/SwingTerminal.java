package com.riege.rmc.terminal.impl;

import com.riege.rmc.terminal.Terminal;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;

public final class SwingTerminal implements Terminal {

    private JFrame frame;
    private JTextPane outputPane;
    private JTextField inputField;
    private NativeCallback inputCallback;

    @Override
    public void start() {
        SwingUtilities.invokeLater(() -> {
            frame = new JFrame("RMC Terminal");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1000, 700);
            frame.setLayout(new BorderLayout());

            outputPane = new JTextPane();
            outputPane.setEditable(false);
            outputPane.setBackground(Color.BLACK);
            outputPane.setForeground(Color.WHITE);
            outputPane.setFont(new Font("Monospaced", Font.PLAIN, 14));
            JScrollPane scrollPane = new JScrollPane(outputPane);
            frame.add(scrollPane, BorderLayout.CENTER);

            inputField = new JTextField();
            inputField.setBackground(Color.DARK_GRAY);
            inputField.setForeground(Color.WHITE);
            inputField.setCaretColor(Color.WHITE);
            inputField.setFont(new Font("Monospaced", Font.PLAIN, 14));
            inputField.addActionListener(e -> {
                String text = inputField.getText().trim();
                if (!text.isEmpty() && inputCallback != null) {
                    inputCallback.invoke(text);
                    inputField.setText("");
                }
            });
            frame.add(inputField, BorderLayout.SOUTH);

            // Show
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            inputField.requestFocus();
        });
    }

    @Override
    public void close() {
        if (frame != null) {
            SwingUtilities.invokeLater(() -> frame.dispose());
        }
    }

    @Override
    public void logInfo(String msg) {
        appendText("[INFO] " + msg + "\n", Color.WHITE);
    }

    @Override
    public void logError(String msg) {
        appendText("[ERROR] " + msg + "\n", Color.RED);
    }

    @Override
    public void logSuccess(String msg) {
        appendText("[SUCCESS] " + msg + "\n", Color.GREEN);
    }

    @Override
    public void logWarning(String msg) {
        appendText("[WARNING] " + msg + "\n", Color.YELLOW);
    }

    @Override
    public void logDebug(String msg) {
        appendText("[DEBUG] " + msg + "\n", Color.CYAN);
    }

    private void appendText(String text, Color color) {
        SwingUtilities.invokeLater(() -> {
            if (outputPane != null) {
                try {
                    StyledDocument doc = outputPane.getStyledDocument();
                    SimpleAttributeSet attrs = new SimpleAttributeSet();
                    StyleConstants.setForeground(attrs, color);
                    doc.insertString(doc.getLength(), text, attrs);
                    outputPane.setCaretPosition(doc.getLength());
                } catch (BadLocationException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void addCandidate(String candidate) {}

    @Override
    public void registerInputCallback(NativeCallback callback) {
        this.inputCallback = callback;
    }

    @Override
    public void registerTabCallback(NativeCallback callback) {}
}
