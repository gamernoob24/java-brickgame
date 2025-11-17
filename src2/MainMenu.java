import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainMenu extends JPanel implements ActionListener {
    private final Main mainApp;
    private final JButton startButton, quitButton;
    private float hueShift = 0f;

    public MainMenu(Main mainApp) {
        this.mainApp = mainApp;
        setBackground(new Color(20, 24, 58));
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.insets = new Insets(20, 0, 20, 0);

        startButton = createButton("START GAME");
        quitButton = createButton("QUIT");

        gbc.gridy = 0;
        add(startButton, gbc);
        gbc.gridy = 1;
        add(quitButton, gbc);

        startButton.addActionListener(this);
        quitButton.addActionListener(this);

        // Animate title
        Timer timer = new Timer(50, e -> {
            hueShift += 0.01f;
            if (hueShift > 1f) hueShift = 0f;
            repaint();
        });
        timer.start();
    }

    private JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Impact", Font.PLAIN, 24)); // Impact, NOT bold for buttons
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(40, 45, 90));
        button.setOpaque(true);
        button.setFocusPainted(false);
        button.setBorder(null); // no outline
        button.setPreferredSize(new Dimension(200, 50));

        // hover effect
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

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        float hue = hueShift % 1f;
        g2.setColor(Color.getHSBColor(hue, 0.8f, 1f));
        g2.setFont(new Font("Impact", Font.BOLD, 32)); // Titles bold
        String title = "JAVA BRICK GAME";
        int width = g2.getFontMetrics().stringWidth(title);
        g2.drawString(title, (getWidth() - width) / 2, 120);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == startButton) {
            mainApp.startGame();
        } else if (e.getSource() == quitButton) {
            System.exit(0);
        }
    }
}
