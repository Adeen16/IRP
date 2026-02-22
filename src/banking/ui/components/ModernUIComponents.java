package banking.ui.components;

import banking.ui.UIStyle;
import javax.swing.*;
import java.awt.*;

public class ModernUIComponents {

    public static class RoundedPanel extends JPanel {
        private int cornerRadius = 15;
        private Color backgroundColor;

        public RoundedPanel(int radius, Color bgColor) {
            this.cornerRadius = radius;
            this.backgroundColor = bgColor;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Dimension arcs = new Dimension(cornerRadius, cornerRadius);
            int width = getWidth();
            int height = getHeight();
            Graphics2D graphics = (Graphics2D) g;
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            //Draws the rounded panel with borders.
            if (backgroundColor != null) {
                graphics.setColor(backgroundColor);
            } else {
                graphics.setColor(getBackground());
            }
            graphics.fillRoundRect(0, 0, width - 1, height - 1, arcs.width, arcs.height);
        }
    }

    public static class ShadowCard extends JPanel {
        private int shadowSize = 5;

        public ShadowCard() {
            setLayout(new BorderLayout());
            setBackground(UIStyle.CARD_COLOR);
            setBorder(BorderFactory.createEmptyBorder(shadowSize, shadowSize, shadowSize, shadowSize));
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(0, 0, 0, 20)); // Subtle shadow
            g2.fillRoundRect(shadowSize, shadowSize, getWidth() - shadowSize * 2, getHeight() - shadowSize * 2, 15, 15);
            g2.setColor(UIStyle.CARD_COLOR);
            g2.fillRoundRect(0, 0, getWidth() - shadowSize * 2, getHeight() - shadowSize * 2, 15, 15);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
