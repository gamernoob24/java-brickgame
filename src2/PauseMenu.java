import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class PauseMenu extends JPanel {
    private float hueShift = 0f; // for color cycling

    public PauseMenu(BrickGamePanel gamePanel, Main mainApp) {
        setOpaque(true);
        setBackground(new Color(25, 25, 50, 220));
        setLayout(null); // absolute positioning

        // MAIN MENU BUTTON
        JButton returnButton = createStyledButton("MAIN MENU");
        returnButton.setBounds(50, 80, 200, 40);
        returnButton.addActionListener((ActionEvent e) -> mainApp.showMainMenu());
        add(returnButton);

        // Timer for updating hueShift
        Timer timer = new Timer(50, e -> {
            hueShift += 0.01f;
            if (hueShift > 1f) hueShift = 0f;
            repaint();
        });
        timer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // PAUSED TITLE
        float hue = hueShift % 1f;
        g2.setColor(Color.getHSBColor(hue, 0.8f, 1f));
        g2.setFont(new Font("Impact", Font.BOLD, 28));
        String text = "PAUSED";
        FontMetrics fm = g2.getFontMetrics();
        int x = (getWidth() - fm.stringWidth(text)) / 2;
        int y = 50; // vertical position
        g2.drawString(text, x, y);
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Impact", Font.PLAIN, 16));
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(40, 45, 90));
        button.setOpaque(true);
        button.setFocusPainted(false);
        button.setBorder(null);

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(new Color(80, 90, 160));
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(new Color(40, 45, 90));
            }
        });

        return button;
    }
}
