import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class BrickGamePanel extends JPanel implements ActionListener, KeyListener {
    private final Main mainApp;
    private float hueShift = 0f;
    private Timer timer;
    private boolean paused = false;
    private PauseMenu pauseMenu;
    private int paddleX = 200;     // start position
    private int paddleY = 500;     // fixed vertical position
    private final int paddleWidth = 100;
    private final int paddleHeight = 15;
    private int paddleSpeed = 15;  // speed of movement
    
    public BrickGamePanel(Main mainApp) {
        this.mainApp = mainApp;
        setPreferredSize(new Dimension(500, 600));
        setBackground(new Color(20, 24, 58));
        setLayout(null); // absolute positioning for overlay

        timer = new Timer(50, this);
        timer.start();

        addKeyListener(this);
        setFocusable(true);
        requestFocusInWindow();

    addMouseMotionListener(new MouseMotionAdapter() {
    @Override
    public void mouseMoved(MouseEvent e) {
        if (paused) return; // <-- prevents movement when paused

        paddleX = e.getX() - (paddleWidth / 2);

        // clamp
        if (paddleX < 0) paddleX = 0;
        if (paddleX + paddleWidth > getWidth()) {
            paddleX = getWidth() - paddleWidth;
        }

        repaint();
    }
});
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        //=== BRICKS ===
        int rows = 6, columns = 8;
        int brickWidth = 50, brickHeight = 20, gap = 6;
        int totalBricksWidth = columns * (brickWidth + gap) - gap;
        int startX = (getWidth() - totalBricksWidth) / 2, startY = 60;

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {
                float hue = (hueShift + (row * 0.1f) + (col * 0.02f)) % 1f;
                g2.setColor(Color.getHSBColor(hue, 0.7f, 1.0f));
                int x = startX + col * (brickWidth + gap);
                int y = startY + row * (brickHeight + gap);
                g2.fillRect(x, y, brickWidth, brickHeight);
            }
        }

        //=== PLATFORM ===
        g2.setColor(Color.LIGHT_GRAY);
        g2.fillRoundRect(paddleX, paddleY, paddleWidth, paddleHeight, 10, 10);

        //=== BALL ===
        g2.setColor(new Color(255, 240, 200));
        int ballSize = 12;
        int ballX = getWidth() / 2 - ballSize / 2;
        int ballY = paddleY - 25;
        g2.fillOval(ballX, ballY, ballSize, ballSize);

        //=== DIM BACKGROUND WHEN PAUSED ===
        if (paused) {
            g2.setColor(new Color(0, 0, 0, 150));
            g2.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        hueShift += 0.01f;
        if (hueShift > 1f) hueShift = 0f;
        
        if (paddleX < 0) paddleX = 0;
        if (paddleX + paddleWidth > getWidth()) 
            paddleX = getWidth() - paddleWidth;

        repaint();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            togglePause();
        }
     
    }
    

    private void togglePause() {
        paused = !paused;
        if (paused) {
            timer.stop();
            showPauseMenu();
        } else {
            hidePauseMenu();
            timer.start();
        }
        repaint();
    }

    private void showPauseMenu() {
        if (pauseMenu == null) {
            pauseMenu = new PauseMenu(this, mainApp);
            int w = 300, h = 150;
            pauseMenu.setBounds((getWidth() - w) / 2, (getHeight() - h) / 2, w, h);
            add(pauseMenu, Integer.valueOf(1)); // ensure top layer
            pauseMenu.setVisible(true);
            repaint();
        }
    }

    private void hidePauseMenu() {
        if (pauseMenu != null) {
            remove(pauseMenu);
            pauseMenu = null;
            repaint();
        }
    }

    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}
}
