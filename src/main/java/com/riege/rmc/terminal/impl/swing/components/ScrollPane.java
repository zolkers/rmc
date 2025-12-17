package com.riege.rmc.terminal.impl.swing.components;

import com.riege.rmc.terminal.impl.swing.theme.TerminalTheme;

import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;

/**
 * Modern-styled scroll pane with thin, sleek scrollbars.
 */
public class ScrollPane extends JScrollPane {

    public ScrollPane(Component view) {
        super(view);
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder());
        getViewport().setOpaque(false);

        // Style vertical scrollbar
        JScrollBar vertical = getVerticalScrollBar();
        vertical.setUI(new ModernScrollBarUI());
        vertical.setOpaque(false);
        vertical.setPreferredSize(new Dimension(10, 0));

        // Style horizontal scrollbar
        JScrollBar horizontal = getHorizontalScrollBar();
        horizontal.setUI(new ModernScrollBarUI());
        horizontal.setOpaque(false);
        horizontal.setPreferredSize(new Dimension(0, 10));
    }

    private static class ModernScrollBarUI extends BasicScrollBarUI {

        private static final int THUMB_SIZE = 8;

        @Override
        protected void configureScrollBarColors() {
            thumbColor = TerminalTheme.BACKGROUND_HOVER;
            thumbDarkShadowColor = TerminalTheme.BACKGROUND_HOVER;
            thumbHighlightColor = TerminalTheme.BACKGROUND_HOVER;
            thumbLightShadowColor = TerminalTheme.BACKGROUND_HOVER;
            trackColor = TerminalTheme.BACKGROUND_PRIMARY;
            trackHighlightColor = TerminalTheme.BACKGROUND_PRIMARY;
        }

        @Override
        protected JButton createDecreaseButton(int orientation) {
            return createZeroButton();
        }

        @Override
        protected JButton createIncreaseButton(int orientation) {
            return createZeroButton();
        }

        private JButton createZeroButton() {
            JButton button = new JButton();
            button.setPreferredSize(new Dimension(0, 0));
            button.setMinimumSize(new Dimension(0, 0));
            button.setMaximumSize(new Dimension(0, 0));
            return button;
        }

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
            if (thumbBounds.isEmpty() || !scrollbar.isEnabled()) {
                return;
            }

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(thumbColor);

            if (scrollbar.getOrientation() == JScrollBar.VERTICAL) {
                int x = thumbBounds.x + (thumbBounds.width - THUMB_SIZE) / 2;
                g2.fillRoundRect(x, thumbBounds.y, THUMB_SIZE, thumbBounds.height, 4, 4);
            } else {
                int y = thumbBounds.y + (thumbBounds.height - THUMB_SIZE) / 2;
                g2.fillRoundRect(thumbBounds.x, y, thumbBounds.width, THUMB_SIZE, 4, 4);
            }

            g2.dispose();
        }

        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
            // Transparent track
        }
    }
}
