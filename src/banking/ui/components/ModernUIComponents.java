package banking.ui.components;

import banking.ui.UIStyle;
import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import java.awt.event.ActionEvent;
import java.awt.event.ContainerAdapter;
import java.awt.event.ContainerEvent;
import java.awt.geom.RoundRectangle2D;
import java.awt.*;

public class ModernUIComponents {

    public static class RoundedPanel extends JPanel {
        private final int cornerRadius;
        private final Color backgroundColor;
        private float hoverProgress;
        private float hoverTarget;
        private Timer hoverTimer;

        public RoundedPanel(int radius, Color bgColor) {
            this.cornerRadius = radius;
            this.backgroundColor = bgColor;
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
            installHoverAnimation();
        }

        @Override
        public void paint(Graphics g) {
            Graphics2D graphics = (Graphics2D) g.create();
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            float scale = 1.0f + (0.02f * hoverProgress);
            double cx = getWidth() / 2.0;
            double cy = getHeight() / 2.0;
            graphics.translate(cx, cy);
            graphics.scale(scale, scale);
            graphics.translate(-cx, -cy);

            super.paint(graphics);
            graphics.dispose();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            int width = getWidth();
            int height = getHeight();
            int glowInset = 14;
            int x = glowInset;
            int y = glowInset;
            int w = Math.max(1, width - (glowInset * 2) - 1);
            int h = Math.max(1, height - (glowInset * 2) - 1);

            Graphics2D graphics = (Graphics2D) g.create();
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Shape cardShape = new RoundRectangle2D.Float(x, y, w, h, cornerRadius, cornerRadius);
            paintGoldGlow(graphics, x, y, w, h);

            Color topColor = backgroundColor != null ? backgroundColor.brighter() : UIStyle.CARD_GRADIENT_TOP;
            Color bottomColor = backgroundColor != null ? backgroundColor.darker() : UIStyle.CARD_GRADIENT_BOTTOM;
            GradientPaint fill = new GradientPaint(0, y, topColor, 0, y + h, bottomColor);
            graphics.setPaint(fill);
            graphics.fill(cardShape);

            graphics.setClip(cardShape);
            GradientPaint innerShadow = new GradientPaint(
                    0, y, new Color(0, 0, 0, 24),
                    0, y + h, UIStyle.CARD_INNER_SHADOW
            );
            graphics.setPaint(innerShadow);
            graphics.fillRect(x, y + (h / 3), w, (h * 2) / 3);
            graphics.setClip(null);

            graphics.setColor(UIStyle.CARD_GOLD_BORDER);
            graphics.draw(cardShape);
            graphics.dispose();
        }

        private void paintGoldGlow(Graphics2D graphics, int x, int y, int w, int h) {
            int layers = 6;
            for (int i = layers; i >= 1; i--) {
                float progressFactor = 1.0f + (0.45f * hoverProgress);
                float alpha = (0.02f + (0.01f * hoverProgress)) * (i / (float) layers);
                float stroke = (6f + (i * 3f)) * progressFactor;
                int inset = Math.round((layers - i) * 1.5f);

                graphics.setColor(withAlpha(UIStyle.GOLD_GLOW_COLOR, alpha));
                graphics.setStroke(new BasicStroke(stroke, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                graphics.drawRoundRect(x + inset, y + inset, w - (inset * 2), h - (inset * 2), cornerRadius, cornerRadius);
            }
        }

        private void installHoverAnimation() {
            MouseInputAdapter hoverListener = new MouseInputAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    animateHover(1.0f);
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    PointerInfo pointerInfo = MouseInfo.getPointerInfo();
                    if (pointerInfo == null) {
                        animateHover(0.0f);
                        return;
                    }

                    Point point = pointerInfo.getLocation();
                    SwingUtilities.convertPointFromScreen(point, RoundedPanel.this);
                    if (!contains(point)) {
                        animateHover(0.0f);
                    }
                }
            };

            registerHoverHandlers(this, hoverListener);
            addContainerListener(new ContainerAdapter() {
                @Override
                public void componentAdded(ContainerEvent e) {
                    registerHoverHandlers(e.getChild(), hoverListener);
                }
            });
        }

        private void registerHoverHandlers(Component component, MouseInputAdapter hoverListener) {
            component.addMouseListener(hoverListener);
            if (component instanceof Container container) {
                for (Component child : container.getComponents()) {
                    registerHoverHandlers(child, hoverListener);
                }
            }
        }

        private void animateHover(float target) {
            hoverTarget = target;
            if (hoverTimer == null) {
                hoverTimer = new Timer(15, this::stepHover);
            }
            if (!hoverTimer.isRunning()) {
                hoverTimer.start();
            }
        }

        private void stepHover(ActionEvent event) {
            float delta = 0.1f;
            if (Math.abs(hoverProgress - hoverTarget) <= delta) {
                hoverProgress = hoverTarget;
                hoverTimer.stop();
            } else if (hoverProgress < hoverTarget) {
                hoverProgress += delta;
            } else {
                hoverProgress -= delta;
            }
            repaint();
        }

        private Color withAlpha(Color color, float alpha) {
            return new Color(color.getRed(), color.getGreen(), color.getBlue(), Math.min(255, Math.max(0, Math.round(alpha * 255f))));
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
            g2.setColor(new Color(0, 0, 0, 70));
            g2.fillRoundRect(shadowSize, shadowSize, getWidth() - shadowSize * 2, getHeight() - shadowSize * 2, 15, 15);
            g2.setColor(UIStyle.CARD_COLOR);
            g2.fillRoundRect(0, 0, getWidth() - shadowSize * 2, getHeight() - shadowSize * 2, 15, 15);
            g2.setColor(UIStyle.BORDER_COLOR);
            g2.drawRoundRect(0, 0, getWidth() - shadowSize * 2, getHeight() - shadowSize * 2, 15, 15);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
