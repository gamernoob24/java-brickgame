import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class BrickGamePanel extends JPanel implements ActionListener, KeyListener {
    private final Main mainApp;
    private float hueShift = 0f;
    private Timer timer;
    private boolean paused = false;
    private PauseMenu pauseMenu;
    private GameOverMenu gameOverMenu;
    private GameOverMenu gameWonMenu;
    private int score = 0;
    private boolean gameOver = false;
    private int paddleX = 200;     // start position
    private int paddleY = 500;     // fixed vertical position
    private final int paddleHeight = 15;
    private int paddleWidth = 100;
    private final int originalPaddleWidth = 100;

    private final int ballSize = 24;

    private final int rows = 6, columns = 8;
    private boolean[][] bricks;

    private int frameCount = 0;
    private long lastFpsUpdateTime = 0;
    private int fps = 0;

    // Balls
    private static class Ball {
        double x, y;
        double vx, vy;
        int size;
        boolean isOriginal = false;
        Ball(double x, double y, double vx, double vy, int size, boolean isOriginal) {
            this.x = x; this.y = y; this.vx = vx; this.vy = vy; this.size = size; this.isOriginal = isOriginal;
        }
    }
    private final List<Ball> balls = new ArrayList<>();

    // Modifiers
    private enum ModCategory { POINT, SPEED, PADDLE, MULTIBALL }
    private static class ModifierDef {
        ModCategory category;
        double value;
        String display;
        ModifierDef(ModCategory c, double v, String d) { category = c; value = v; display = d; }
    }
    private final ModifierDef[] modifierPool;
    private static class FallingModifier {
        int x, y, w, h;
        int vy;
        ModifierDef def;
        FallingModifier(int x, int y, int w, int h, int vy, ModifierDef def) {
            this.x = x; this.y = y; this.w = w; this.h = h; this.vy = vy; this.def = def;
        }
        Rectangle rect() { return new Rectangle(x, y, w, h); }
    }
    private FallingModifier fallingModifier = null;
    private boolean modifierActive = false;
    private ModifierDef activeModifierDef = null;
    private long modifierEndTime = 0L;
    private double pointMultiplier = 1.0;
    private double speedMultiplier = 1.0;
    private int addedBallsCount = 0;
    private final List<Ball> addedBalls = new ArrayList<>();
    private final Random random = new Random();

    private int bricksDestroyedCounter = 0;

    public BrickGamePanel(Main mainApp) {
        this.mainApp = mainApp;
        modifierPool = new ModifierDef[] {
                new ModifierDef(ModCategory.POINT, 0.5, "×0.5 POINTS"),
                new ModifierDef(ModCategory.POINT, 2.0, "×2 POINTS"),
                new ModifierDef(ModCategory.POINT, 4.0, "×4 POINTS"),
                new ModifierDef(ModCategory.SPEED, 0.5, "×0.5 SPEED"),
                new ModifierDef(ModCategory.SPEED, 2.0, "×2 SPEED"),
                new ModifierDef(ModCategory.SPEED, 1.5, "×1.5 SPEED"),
                new ModifierDef(ModCategory.PADDLE, 0.5, "×0.5 PADDLE SIZE"),
                new ModifierDef(ModCategory.PADDLE, 1.5, "×1.5 PADDLE SIZE"),
                new ModifierDef(ModCategory.PADDLE, 2.0, "×2 PADDLE SIZE"),
                new ModifierDef(ModCategory.MULTIBALL, 1.0, "+1 BALL"),
                new ModifierDef(ModCategory.MULTIBALL, 2.0, "+2 BALLS"),
                new ModifierDef(ModCategory.MULTIBALL, 3.0, "+3 BALLS")
        };

        
        setBackground(new Color(20, 24, 58));
        setLayout(null); // absolute positioning for overlay

        bricks = new boolean[rows][columns];
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {
                bricks[row][col] = true;
            }
        }

        // initial ball
        balls.clear();
        balls.add(new Ball(244, 475, 5, -3, ballSize, true));

        timer = new Timer(16, this); // ~60 FPS
        timer.start();

        addKeyListener(this);
        setFocusable(true);
        requestFocusInWindow();

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                if (paused || gameOver) return;

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

    public void stopTimer() {
        timer.stop();
    }

    @Override
    protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    Graphics2D g2 = (Graphics2D) g;
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    // Calculate scaling based on current panel size
    int panelWidth = getWidth();
    int panelHeight = getHeight();
    
    //=== BRICKS ===
    int brickWidth = panelWidth / 10;  // Dynamically scale brick width
    int brickHeight = panelHeight / 30; // Dynamically scale brick height
    int gap = Math.max(4, panelWidth / 100); // Scale gap too
    int totalBricksWidth = columns * (brickWidth + gap) - gap;
    int startX = (panelWidth - totalBricksWidth) / 2;
    int startY = panelHeight / 10; // Start bricks at 10% from top

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

    //=== PLATFORM (PADDLE) ===
    g2.setColor(Color.LIGHT_GRAY);
    g2.fillRoundRect(paddleX, paddleY, paddleWidth, paddleHeight, 10, 10);

    //=== BALLS ===
    g2.setColor(new Color(255, 255, 200));
    for (Ball b : balls) {
        g2.fillOval((int)b.x, (int)b.y, b.size, b.size);
    }

    //=== FALLING MODIFIER ===
    if (fallingModifier != null) {
        g2.setColor(new Color(200, 200, 255));
        g2.fillRect(fallingModifier.x, fallingModifier.y, fallingModifier.w, fallingModifier.h);
        g2.setColor(Color.BLACK);
        g2.setFont(new Font("Impact", Font.PLAIN, 18));
        FontMetrics fm = g2.getFontMetrics();
        String q = "?";
        int tx = fallingModifier.x + (fallingModifier.w - fm.stringWidth(q)) / 2;
        int ty = fallingModifier.y + (fallingModifier.h + fm.getAscent()) / 2 - 3;
        g2.drawString(q, tx, ty);
    }

    // FPS Counter
    g2.setColor(Color.WHITE);
    g2.drawString("FPS: " + fps, 10, 20);

    // Score
    g2.drawString("Score: " + score, panelWidth - 120, 20);

    // Active modifier display
    if (modifierActive && activeModifierDef != null) {
        long remainingMs = modifierEndTime - System.currentTimeMillis();
        long remainingSec = Math.max(0, (remainingMs + 999) / 1000);
        String txt = activeModifierDef.display + " - " + remainingSec + "S REMAINING";
        FontMetrics fm = g2.getFontMetrics();
        int tw = fm.stringWidth(txt);
        g2.setColor(Color.WHITE);
        g2.drawString(txt, (panelWidth - tw) / 2, 40);
    }

    //=== DIM BACKGROUND WHEN PAUSED ===
    if (paused) {
        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRect(0, 0, panelWidth, panelHeight);
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

        // Move balls
        Iterator<Ball> it = balls.iterator();
        while (it.hasNext()) {
            Ball b = it.next();
            b.x += b.vx;
            b.y += b.vy;

            if (b.x <= 0) { b.x = 0; b.vx = -b.vx; }
            if (b.x >= getWidth() - b.size) { b.x = getWidth() - b.size; b.vx = -b.vx; }
            if (b.y <= 0) { b.y = 0; b.vy = -b.vy; }
        }

        // Check if all balls are off bottom -> remove them; if none left -> game over
        Iterator<Ball> remIt = balls.iterator();
        while (remIt.hasNext()) {
            Ball b = remIt.next();
            if (b.y >= getHeight()) {
                remIt.remove();
            }
        }
        if (balls.isEmpty()) {
            gameOver = true;
            timer.stop();
            showGameOverMenu();
            return;
        }

        // Brick collision
        int panelWidth = getWidth();
        int panelHeight = getHeight();
        int brickWidth = panelWidth / 10;
        int brickHeight = panelHeight / 30;
        int gap = Math.max(4, panelWidth / 100);
        int totalBricksWidth = columns * (brickWidth + gap) - gap;
        int startX = (panelWidth - totalBricksWidth) / 2;
        int startY = panelHeight / 10;
        boolean anyBrickLeft = false;

        for (Ball b : new ArrayList<>(balls)) {
            Rectangle ballRect = new Rectangle((int)b.x, (int)b.y, b.size, b.size);
            boolean collided = false;
            brick_collision_detection:
            for (int row = 0; row < rows; row++) {
                for (int col = 0; col < columns; col++) {
                    if (bricks[row][col]) {
                        anyBrickLeft = true;
                        int brickX = startX + col * (brickWidth + gap);
                        int brickY = startY + row * (brickHeight + gap);
                        Rectangle brickRect = new Rectangle(brickX, brickY, brickWidth, brickHeight);
                        if (ballRect.intersects(brickRect)) {
                            bricks[row][col] = false;
                            int points = (int)Math.round(50 * pointMultiplier);
                            score += points;
                            bricksDestroyedCounter++;
                            Rectangle intersection = ballRect.intersection(brickRect);
                            if (intersection.height > intersection.width) {
                                b.vx = -b.vx;
                            } else {
                                b.vy = -b.vy;
                            }
                            collided = true;

                            if (bricksDestroyedCounter % 6 == 0) {
                                if (!modifierActive && fallingModifier == null) {
                                    int fx = brickX + brickWidth / 2 - 16;
                                    int fy = brickY + brickHeight / 2 - 16;
                                    spawnFallingModifier(fx, fy);
                                }
                            }
                            break brick_collision_detection;
                        }
                    }
                }
            }
            if (collided) {
                // continue to next ball
            }
        }

        if (!anyBrickLeft) {
            timer.stop();
            showGameWonMenu();
            return;
        }

        // Paddle collision
        Rectangle paddleRect = new Rectangle(paddleX, paddleY, paddleWidth, paddleHeight);

        for (Ball b : balls) {
            Rectangle ballRect = new Rectangle((int)b.x, (int)b.y, b.size, b.size);
            if (ballRect.intersects(paddleRect) && b.vy > 0) {
                b.vy = -b.vy;

                int ballCenterX = (int)b.x + b.size / 2;
                int paddleCenterX = paddleX + paddleWidth / 2;
                int diff = ballCenterX - paddleCenterX;

                b.vx += diff / 10.0;

                if (b.vx > 4) b.vx = 4;
                if (b.vx < -4) b.vx = -4;
                if (Math.abs(b.vx) < 0.1) b.vx = Math.signum(b.vx) == 0 ? 1 : Math.signum(b.vx);
            }
        }

        // Falling modifier movement & collision
        if (fallingModifier != null) {
            fallingModifier.y += fallingModifier.vy;
            if (fallingModifier.y > getHeight()) {
                fallingModifier = null;
            } else {
                if (fallingModifier.rect().intersects(paddleRect)) {
                    activateModifier(fallingModifier.def);
                    fallingModifier = null;
                }
            }
        }

        // Active modifier timeout
        if (modifierActive && System.currentTimeMillis() >= modifierEndTime) {
            deactivateModifier();
        }

        repaint();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            if (!gameOver) {
                togglePause();
            }
        }
    }

    private void spawnFallingModifier(int x, int y) {
        ModifierDef def = modifierPool[random.nextInt(modifierPool.length)];
        fallingModifier = new FallingModifier(x, y, 32, 32, 6, def);
    }

    private void activateModifier(ModifierDef def) {
        if (modifierActive) return;
        modifierActive = true;
        activeModifierDef = def;
        modifierEndTime = System.currentTimeMillis() + 10_000L;
        score += 100;
        switch (def.category) {
            case POINT:
                pointMultiplier = def.value;
                break;
            case SPEED:
                double factor = def.value;
                for (Ball b : balls) {
                    b.vx *= factor;
                    b.vy *= factor;
                }
                speedMultiplier = factor;
                break;
            case PADDLE:
                paddleWidth = Math.max(20, (int)(originalPaddleWidth * def.value));
                break;
            case MULTIBALL:
                int add = (int)def.value;
                addedBallsCount = add;
                for (int i = 0; i < add; i++) {
                    Ball src = balls.get(0);
                    double nx = src.x + (i+1) * 10;
                    double ny = src.y;
                    double nvx = -src.vx * (i % 2 == 0 ? 1 : -1);
                    double nvy = src.vy;
                    Ball nb = new Ball(nx, ny, nvx == 0 ? 1 : nvx, nvy == 0 ? -1 : nvy, src.size, false);
                    balls.add(nb);
                    addedBalls.add(nb);
                }
                break;
        }
    }

    private void deactivateModifier() {
        modifierActive = false;
        if (activeModifierDef != null) {
            switch (activeModifierDef.category) {
                case POINT:
                    pointMultiplier = 1.0;
                    break;
                case SPEED:
                    double factor = speedMultiplier;
                    if (factor != 0) {
                        for (Ball b : balls) {
                            b.vx /= factor;
                            b.vy /= factor;
                        }
                    }
                    speedMultiplier = 1.0;
                    break;
                case PADDLE:
                    paddleWidth = originalPaddleWidth;
                    break;
                case MULTIBALL:
                    for (Ball b : new ArrayList<>(addedBalls)) {
                        balls.remove(b);
                    }
                    addedBalls.clear();
                      addedBallsCount = 0;
                    break;
            }
        }
        activeModifierDef = null;
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

    private void showGameOverMenu() {
        if (gameOverMenu == null) {
            gameOverMenu = new GameOverMenu(this, mainApp, score, false);
            int w = 400, h = 300;
            gameOverMenu.setBounds((getWidth() - w) / 2, (getHeight() - h) / 2, w, h);
            add(gameOverMenu, Integer.valueOf(2)); // top layer
            gameOverMenu.setVisible(true);
            repaint();
        }
    }

    private void showGameWonMenu() {
        if (gameWonMenu == null) {
            gameWonMenu = new GameOverMenu(this, mainApp, score, true);
            int w = 400, h = 300;
            gameWonMenu.setBounds((getWidth() - w) / 2, (getHeight() - h) / 2, w, h);
            add(gameWonMenu, Integer.valueOf(2));
            gameWonMenu.setVisible(true);
            repaint();
        }
    }

    public void restartGame() {
        // Remove game over menu
        if (gameOverMenu != null) {
            remove(gameOverMenu);
            gameOverMenu = null;
        }
        if (gameWonMenu != null) {
            remove(gameWonMenu);
            gameWonMenu = null;
        }

        // Reset game state
        gameOver = false;
        score = 0;
        paddleX = 200;
        paddleWidth = originalPaddleWidth;
        pointMultiplier = 1.0;
        speedMultiplier = 1.0;
        activeModifierDef = null;
        modifierActive = false;
        fallingModifier = null;
        modifierEndTime = 0L;
        bricksDestroyedCounter = 0;
        addedBalls.clear();
        balls.clear();
        balls.add(new Ball(244, 475, 5, -3, ballSize, true));

        // Reset bricks
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {
                bricks[row][col] = true;
            }
        }

        // Restart timer
        timer.start();
        requestFocusInWindow();
        repaint();
    }

    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}
}
