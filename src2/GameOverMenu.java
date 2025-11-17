import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class GameOverMenu extends JPanel {
    private float hueShift = 0f;
    private int finalScore;

    public GameOverMenu(BrickGamePanel gamePanel, Main mainApp, int finalScore) {
        this.finalScore = finalScore;
        setOpaque(true);
        setBackground(new Color(25, 25, 50, 240));
        setLayout(null);

        // Try again button
        JButton tryAgainButton = createStyledButton("TRY AGAIN");
        tryAgainButton.setBounds(100, 180, 200, 50);
        tryAgainButton.addActionListener((ActionEvent e) -> {
            gamePanel.restartGame();
        });
        add(tryAgainButton);

        // main menu button
        JButton mainMenuButton = createStyledButton("MAIN MENU");
        
        mainMenuButton.setBounds(100, 240, 200, 50);
        mainMenuButton.addActionListener((ActionEvent e) -> {
            mainApp.showMainMenu();
        });
        add(mainMenuButton);

        // Timer for color cycling animation
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

        // Game over title
        float hue = hueShift % 1f;
        g2.setColor(Color.getHSBColor(hue, 0.8f, 1f));
        g2.setFont(new Font("Impact", Font.BOLD, 42));
        String title = "GAME OVER";
        FontMetrics fm = g2.getFontMetrics();
        int x = (getWidth() - fm.stringWidth(title)) / 2;
        g2.drawString(title, x, 70);

        // Final Score
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Impact", Font.PLAIN, 24));
        String scoreText = "Final Score: " + getScore();
        fm = g2.getFontMetrics();
        x = (getWidth() - fm.stringWidth(scoreText)) / 2;
        g2.drawString(scoreText, x, 130);
    }

    private int getScore() {
        // Return the final score passed through the constructor
        return finalScore;
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Impact", Font.PLAIN, 20));
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
