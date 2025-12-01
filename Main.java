import javax.swing.*;

public class Main {
    private JFrame window;
    private MainMenu mainMenu;
    private BrickGamePanel gamePanel;

   public Main() {
    window = new JFrame("Java Brick Game");
    window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    window.setSize(500, 600);
    window.setResizable(true); // Changed from false to true
    window.setLocationRelativeTo(null);

    showMainMenu();
    window.setVisible(true);
}

    public void showMainMenu() {
        if (gamePanel != null) window.remove(gamePanel);
        mainMenu = new MainMenu(this);
        window.setContentPane(mainMenu);
        window.revalidate();
        window.repaint();
    }

    public void startGame() {
        window.remove(mainMenu);
        gamePanel = new BrickGamePanel(this);
        window.setContentPane(gamePanel);
        window.revalidate();
        gamePanel.requestFocusInWindow();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::new);
    }
}