import javax.swing.*;

public class Main {
    private final JFrame window;
    private MainMenu mainMenu;
    private BrickGamePanel gamePanel;
    private final AudioPlayer audioPlayer;

    public Main() {
        window = new JFrame("Brick Battle Royale 2D");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setSize(500, 600);
        window.setResizable(true);
        window.setLocationRelativeTo(null);

        audioPlayer = new AudioPlayer();
        audioPlayer.playMusic("background.wav");

        showMainMenu();
        window.setVisible(true);
    }

    public void showMainMenu() {
        if (gamePanel != null) {
            gamePanel.stopTimer(); // Stop game timer
            window.remove(gamePanel);
            gamePanel = null;
        }
        mainMenu = new MainMenu(this);
        window.setContentPane(mainMenu);
        window.revalidate();
        window.repaint();
        mainMenu.requestFocusInWindow();
    }

    public void startGame() {
        if (mainMenu != null) {
            mainMenu.stopTimer(); // Stop menu timer
            window.remove(mainMenu);
            mainMenu = null;
        }
        gamePanel = new BrickGamePanel(this);
        window.setContentPane(gamePanel);
        window.revalidate();
        gamePanel.requestFocusInWindow();
    }

    public void playSound(String soundFile) {
        audioPlayer.playSound(soundFile);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::new);
    }
}
