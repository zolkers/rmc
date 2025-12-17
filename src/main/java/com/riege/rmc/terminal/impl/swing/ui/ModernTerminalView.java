package com.riege.rmc.terminal.impl.swing.ui;

import com.riege.rmc.terminal.impl.swing.components.ModernScrollPane;
import com.riege.rmc.terminal.impl.swing.components.ModernTextField;
import com.riege.rmc.terminal.impl.swing.theme.TerminalTheme;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Modern terminal view with sleek design and smooth animations.
 */
public class ModernTerminalView extends JFrame {

    private final JTextPane textPane;
    private final JTextField inputField;
    private final JLabel statusBar;

    private static final String URL_REGEX = "\\b(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
    private static final Pattern URL_PATTERN = Pattern.compile(URL_REGEX);

    private static final int MAX_LINES = 5000;
    private int currentLineCount = 0;

    public ModernTerminalView(StyledDocument document) {
        super("RMC Terminal - Riege Minecraft Client");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setMinimumSize(new Dimension(800, 600));
        setAutoRequestFocus(true);

        // Main container with BorderLayout
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(TerminalTheme.BACKGROUND_PRIMARY);
        setContentPane(mainPanel);

        // Create header
        JPanel header = createHeader();
        mainPanel.add(header, BorderLayout.NORTH);

        // Create text pane
        textPane = createTextPane(document);
        ModernScrollPane scrollPane = new ModernScrollPane(textPane);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Create input field - SIMPLE VERSION THAT WORKS
        inputField = new JTextField();
        inputField.setBackground(TerminalTheme.BACKGROUND_SECONDARY);
        inputField.setForeground(TerminalTheme.TEXT_PRIMARY);
        inputField.setCaretColor(TerminalTheme.ACCENT_PRIMARY);
        inputField.setFont(TerminalTheme.getMonoFont());
        inputField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, TerminalTheme.BORDER_DEFAULT),
            BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));
        mainPanel.add(inputField, BorderLayout.SOUTH);

        // Create status bar
        statusBar = createStatusBar();
        mainPanel.add(statusBar, BorderLayout.PAGE_END);

        setLocationRelativeTo(null);
    }

    /**
     * Show the window and ensure input field gets focus.
     */
    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) {
            // Force focus to input field when window becomes visible
            SwingUtilities.invokeLater(() -> {
                inputField.requestFocusInWindow();
                // Set as default component to receive focus
                getRootPane().setDefaultButton(null);
            });
        }
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(TerminalTheme.BACKGROUND_SECONDARY);
        header.setBorder(BorderFactory.createEmptyBorder(
            TerminalTheme.PADDING_MEDIUM,
            TerminalTheme.PADDING_MEDIUM,
            TerminalTheme.PADDING_MEDIUM,
            TerminalTheme.PADDING_MEDIUM
        ));

        JLabel title = new JLabel("Terminal");
        title.setFont(TerminalTheme.FONT_TITLE);
        title.setForeground(TerminalTheme.TEXT_PRIMARY);
        header.add(title, BorderLayout.WEST);

        return header;
    }

    private JTextPane createTextPane(StyledDocument document) {
        JTextPane pane = new JTextPane(document);
        pane.setEditable(false);
        pane.setBackground(TerminalTheme.BACKGROUND_PRIMARY);
        pane.setFont(TerminalTheme.getMonoFont());
        pane.setForeground(TerminalTheme.TEXT_PRIMARY);
        pane.setSelectionColor(TerminalTheme.SELECTION_BACKGROUND);
        pane.setSelectedTextColor(TerminalTheme.SELECTION_TEXT);
        pane.setMargin(new Insets(
            TerminalTheme.PADDING_MEDIUM,
            TerminalTheme.PADDING_MEDIUM,
            TerminalTheme.PADDING_MEDIUM,
            TerminalTheme.PADDING_MEDIUM
        ));

        // Click handler for links
        pane.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleLinkClick(e);
            }
        });

        // Hover cursor for links
        pane.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int pos = pane.viewToModel2D(e.getPoint());
                Element element = document.getCharacterElement(pos);
                AttributeSet as = element.getAttributes();
                if (as.getAttribute("link") != null) {
                    pane.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                } else {
                    pane.setCursor(Cursor.getDefaultCursor());
                }
            }
        });

        return pane;
    }

    private JLabel createStatusBar() {
        JLabel status = new JLabel(" Ready");
        status.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        status.setForeground(TerminalTheme.TEXT_SECONDARY);
        status.setBackground(TerminalTheme.BACKGROUND_SECONDARY);
        status.setOpaque(true);
        status.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));
        return status;
    }

    private void handleLinkClick(MouseEvent e) {
        int pos = textPane.viewToModel2D(e.getPoint());
        Element element = ((StyledDocument) textPane.getDocument()).getCharacterElement(pos);
        AttributeSet as = element.getAttributes();
        Object linkAttr = as.getAttribute("link");

        if (linkAttr != null && Desktop.isDesktopSupported() &&
            Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            try {
                Desktop.getDesktop().browse(new URI(linkAttr.toString()));
            } catch (Exception ex) {
                setStatus("Failed to open link: " + ex.getMessage());
            }
        }
    }

    public void appendText(String text, Color color) {
        SwingUtilities.invokeLater(() -> {
            try {
                StyledDocument doc = textPane.getStyledDocument();

                // Trim old lines if needed
                if (currentLineCount >= MAX_LINES) {
                    int firstLineEnd = doc.getText(0, doc.getLength()).indexOf('\n');
                    if (firstLineEnd > 0) {
                        doc.remove(0, firstLineEnd + 1);
                        currentLineCount--;
                    }
                }

                // Process text for links
                Matcher matcher = URL_PATTERN.matcher(text);
                int lastEnd = 0;

                while (matcher.find()) {
                    int start = matcher.start();
                    int end = matcher.end();

                    // Add text before link
                    if (start > lastEnd) {
                        SimpleAttributeSet attrs = new SimpleAttributeSet();
                        StyleConstants.setForeground(attrs, color);
                        doc.insertString(doc.getLength(), text.substring(lastEnd, start), attrs);
                    }

                    // Add link
                    SimpleAttributeSet linkAttrs = new SimpleAttributeSet();
                    StyleConstants.setForeground(linkAttrs, TerminalTheme.LINK_DEFAULT);
                    StyleConstants.setUnderline(linkAttrs, true);
                    linkAttrs.addAttribute("link", text.substring(start, end));
                    doc.insertString(doc.getLength(), text.substring(start, end), linkAttrs);

                    lastEnd = end;
                }

                // Add remaining text
                if (lastEnd < text.length()) {
                    SimpleAttributeSet attrs = new SimpleAttributeSet();
                    StyleConstants.setForeground(attrs, color);
                    doc.insertString(doc.getLength(), text.substring(lastEnd), attrs);
                }

                // Add newline
                doc.insertString(doc.getLength(), "\n", null);
                currentLineCount++;

                // Auto-scroll to bottom
                textPane.setCaretPosition(doc.getLength());

            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        });
    }

    public void setStatus(String status) {
        SwingUtilities.invokeLater(() -> statusBar.setText(" " + status));
    }

    public JTextField getInputField() {
        return inputField;
    }

    public JTextPane getTextPane() {
        return textPane;
    }
}
