import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class BrickGamePanel extends JPanel implements ActionListener, KeyListener {
    private final Main mainApp;
    private float hueShift = 0f;
    private Timer timer;
    private boolean paused = false;
    private PauseMenu pauseMenu;
    private int score = 0;
    private int paddleX = 200;     // start position
    private int paddleY = 500;     // fixed vertical position
    private final int paddleWidth = 100;
    private final int paddleHeight = 15;
    private int paddleSpeed = 15;  // speed of movement

    private int ballX = 244;
    private int ballY = 475;
    private int ballVelX = 2;
    private int ballVelY = -3;
    private final int ballSize = 24;

    private final int rows = 6, columns = 8;
    private boolean[][] bricks;

    private int frameCount = 0;
    private long lastFpsUpdateTime = 0;
    private int fps = 0;

    public BrickGamePanel(Main mainApp) {
        this.mainApp = mainApp;
        setPreferredSize(new Dimension(500, 600));
        setBackground(new Color(20, 24, 58));
        setLayout(null); // absolute positioning for overlay

        bricks = new boolean[rows][columns];
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {
                bricks[row][col] = true;
            }
        }

        timer = new Timer(16, this); // ~60 FPS
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
        int brickWidth = 50, brickHeight = 20, gap = 6;
        int totalBricksWidth = columns * (brickWidth + gap) - gap;
        int startX = (getWidth() - totalBricksWidth) / 2, startY = 60;

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {
                if (bricks[row][col]) {
                    float hue = (hueShift + (row * 0.1f) + (col * 0.02f)) % 1f;
                    g2.setColor(Color.getHSBColor(hue, 0.7f, 1.0f));
                    int x = startX + col * (brickWidth + gap);
                    int y = startY + row * (brickHeight + gap);
                    g2.fillRect(x, y, brickWidth, brickHeight);
                }
            }
        }

        //=== PLATFORM ===
        g2.setColor(Color.LIGHT_GRAY);
        g2.fillRoundRect(paddleX, paddleY, paddleWidth, paddleHeight, 10, 10);

        //=== BALL ===
        g2.setColor(new Color(255, 255, 200));
        g2.fillOval(ballX, ballY, ballSize, ballSize);

        // FPS Counter
        g2.setColor(Color.WHITE);
        g2.drawString("FPS: " + fps, 10, 20);

        // Score
        g2.drawString("Score: " + score, 400, 20);
        
        //=== DIM BACKGROUND WHEN PAUSED ===
        if (paused) {
            g2.setColor(new Color(0, 0, 0, 150));
            g2.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        frameCount++;
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastFpsUpdateTime > 1000) {
            fps = frameCount;
            frameCount = 0;
            lastFpsUpdateTime = currentTime;
        }

        if(paused) return;

        hueShift += 0.01f;
        if (hueShift > 1f) hueShift = 0f;

        // Ball movement
        ballX += ballVelX;
        ballY += ballVelY;

        // Wall collision
        if (ballX <= 0 || ballX >= getWidth() - ballSize) {
            ballVelX = -ballVelX;
        }
        if (ballY <= 0) {
            ballVelY = -ballVelY;
        }

        Rectangle ballRect = new Rectangle(ballX, ballY, ballSize, ballSize);

        // Brick collision
        int brickWidth = 50, brickHeight = 20, gap = 6;
        int totalBricksWidth = columns * (brickWidth + gap) - gap;
        int startX = (getWidth() - totalBricksWidth) / 2, startY = 60;

        brick_collision_detection:
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {
                if (bricks[row][col]) {
                    int brickX = startX + col * (brickWidth + gap);
                    int brickY = startY + row * (brickHeight + gap);
                    Rectangle brickRect = new Rectangle(brickX, brickY, brickWidth, brickHeight);
                    if (ballRect.intersects(brickRect)) {
                        bricks[row][col] = false;
                        score += 50;
                        Rectangle intersection = ballRect.intersection(brickRect);
                        if (intersection.height > intersection.width) {
                            ballVelX = -ballVelX; // Side collision
                        } else {
                            ballVelY = -ballVelY; // Top/bottom collision
                        }

                        break brick_collision_detection;
                    }
                }
            }
        }

        // Paddle collision
        Rectangle paddleRect = new Rectangle(paddleX, paddleY, paddleWidth, paddleHeight);

        if (ballRect.intersects(paddleRect) && ballVelY > 0) {
            ballVelY = -ballVelY; // reverse vertical velocity

            // Adjust horizontal velocity based on where the ball hit the paddle
            int ballCenterX = ballX + ballSize / 2;
            int paddleCenterX = paddleX + paddleWidth / 2;
            int diff = ballCenterX - paddleCenterX;

            ballVelX += diff / 10;

            // Clamp the horizontal velocity to a reasonable range
            if (ballVelX > 4) {
                ballVelX = 4;
            }
            if (ballVelX < -4) {
                ballVelX = -4;
            }
            // prevent the ball from getting stuck in a vertical path
            if (ballVelX == 0) {
                ballVelX = 1;
            }
        }

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
            requestFocusInWindow();
        }
    }

    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}
}

