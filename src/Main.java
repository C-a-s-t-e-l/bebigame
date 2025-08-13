// Main.java
import javax.swing.JFrame;

public class Main {
    public static void main(String[] args) {
        JFrame window = new JFrame("Simple Platformer");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);

        // Create the game panel and add it to the window
        GamePanel gamePanel = new GamePanel();
        window.add(gamePanel);

        // Pack the window to fit the preferred size of the GamePanel
        window.pack();

        window.setLocationRelativeTo(null); // Center the window
        window.setVisible(true);

        // Start the game loop
        gamePanel.startGameThread();
    }
}