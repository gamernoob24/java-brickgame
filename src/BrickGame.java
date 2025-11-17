import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class BrickGame extends JPanel implements ActionListener {

    private float hueShift = 0f; //For dynamic brick colors
    private Timer timer;

    private int ballX = 244;
    private int ballY = 505;
    private int ballVelX = 2;
    private int ballVelY = -3;
    private final int ballSize = 12;

    public BrickGame() {
        setPreferredSize(new Dimension(500, 600)); //Window size
        setBackground(new Color(20, 24, 58)); //Background color

        //Timer makes the game update every 10ms so things can move or change
        timer = new Timer(10, this);
        timer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        //Smoothen object edges
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        //=== BRICKS ===
        int rows = 6;
        int columns = 8;
        int brickWidth = 50;
        int brickHeight = 20;
        int gap = 6;

        int totalBricksWidth = columns * (brickWidth + gap) - gap;
        int startX = (getWidth() - totalBricksWidth) / 2;
        int startY = 60;

        //For dynamic brick colors
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {

                //Each brick changes color a little differently depending on where it is
                float hue = (hueShift + (row * 0.1f) + (column * 0.02f)) % 1f;
                Color brickColor = Color.getHSBColor(hue, 0.7f, 1.0f);
                g2.setColor(brickColor);

                int brickX = startX + column * (brickWidth + gap);
                int brickY = startY + row * (brickHeight + gap);
                g2.fillRect(brickX, brickY, brickWidth, brickHeight);
            }
        }

        //=== PLATFORM ===
        g2.setColor(new Color(240, 240, 240)); // light gray
        int paddleWidth = 100;
        int paddleHeight = 15;
        int paddleX = (getWidth() - paddleWidth) / 2;
        int paddleY = getHeight() - 70;
        g2.fillRoundRect(paddleX, paddleY, paddleWidth, paddleHeight, 10, 10);

        //=== BALL ===
        g2.setColor(new Color(255, 240, 200)); // cream
        g2.fillOval(ballX, ballY, ballSize, ballSize);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        //Slowly change the color every time the timer updates
        hueShift += 0.01f;
        if (hueShift > 1f) hueShift = 0f;

        // Ball movement
        ballX += ballVelX;
        ballY += ballVelY;

        if (ballX <= 0 || ballX >= getWidth() - ballSize) {
            ballVelX = -ballVelX;
        }
        if (ballY <= 0) {
            ballVelY = -ballVelY;
        }

        repaint(); //refresh the screen so the new colors show
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame window = new JFrame("Brick Game (Color Cycle)");
            window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            window.setResizable(false);
            window.add(new BrickGame());
            window.pack();
            window.setLocationRelativeTo(null);
            window.setVisible(true);
        });
    }
}
