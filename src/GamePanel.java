// GamePanel.java
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GamePanel extends JPanel implements Runnable, KeyListener {

    // Screen and World Settings
    public static final int SCREEN_WIDTH = 800;
    public static final int SCREEN_HEIGHT = 600;
    public static final int WORLD_WIDTH = 3200; // The world is now 4x wider than the screen
    private final int FPS = 60;

    // Game Objects
    private Player player;
    private List<Platform> platforms;
    private List<Enemy> enemies;

    // Camera
    private int cameraX = 0;

    // Game Loop
    private Thread gameThread;

    public GamePanel() {
        this.setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        this.setBackground(new Color(135, 206, 235)); // Sky blue background
        this.setDoubleBuffered(true);
        this.addKeyListener(this);
        this.setFocusable(true);

        initializeGameObjects();
    }

    private void initializeGameObjects() {
        player = new Player(100, 400);
        platforms = new ArrayList<>();
        enemies = new ArrayList<>();
        
        // Create a long ground platform for the larger world
        platforms.add(new Platform(0, 550, WORLD_WIDTH, 50));

        // Add various platforms throughout the world
        platforms.add(new Platform(300, 450, 150, 20));
        platforms.add(new Platform(550, 350, 150, 20));
        platforms.add(new Platform(800, 250, 100, 20));
        platforms.add(new Platform(1100, 400, 200, 20));
        platforms.add(new Platform(1400, 300, 150, 20));
        platforms.add(new Platform(1800, 450, 250, 20));
        platforms.add(new Platform(2200, 350, 100, 20));
        platforms.add(new Platform(2500, 250, 150, 20));

        // Add enemies on some platforms
        enemies.add(new Enemy(1850, 400, 1800, 2000));
        enemies.add(new Enemy(2525, 200, 2500, 2600));
        enemies.add(new Enemy(1150, 350, 1100, 1250));
    }

    public void startGameThread() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void run() {
        double drawInterval = 1000000000.0 / FPS;
        double delta = 0;
        long lastTime = System.nanoTime();
        long currentTime;

        while (gameThread != null) {
            currentTime = System.nanoTime();
            delta += (currentTime - lastTime) / drawInterval;
            lastTime = currentTime;

            if (delta >= 1) {
                update();
                repaint();
                delta--;
            }
        }
    }

    public void update() {
        player.update(platforms);
        
        for (Enemy enemy : enemies) {
            enemy.update();
        }

        checkCollisions();
        updateCamera();
    }

    private void checkCollisions() {
        Rectangle playerAttackBox = player.getAttackBox();

        // Use an iterator to safely remove enemies while iterating
        Iterator<Enemy> enemyIterator = enemies.iterator();
        while (enemyIterator.hasNext()) {
            Enemy enemy = enemyIterator.next();
            
            // Check if player's attack hits an enemy
            if (player.isAttacking() && playerAttackBox.intersects(enemy.getBounds())) {
                enemyIterator.remove(); // Enemy is defeated
            }
            // Note: A real game would have health, damage, etc.
        }
    }

    private void updateCamera() {
        // The camera tries to center on the player
        cameraX = player.getX() - SCREEN_WIDTH / 2;

        // Keep the camera within the world bounds
        if (cameraX < 0) {
            cameraX = 0;
        }
        if (cameraX > WORLD_WIDTH - SCREEN_WIDTH) {
            cameraX = WORLD_WIDTH - SCREEN_WIDTH;
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // --- CAMERA TRANSLATION START ---
        // Everything drawn after this point will be shifted by the camera's position
        g2.translate(-cameraX, 0);

        // Draw Game Objects (in world space)
        for (Platform platform : platforms) {
            platform.draw(g2);
        }
        for (Enemy enemy : enemies) {
            enemy.draw(g2);
        }
        player.draw(g2);
        
        // --- CAMERA TRANSLATION END ---
        // Reset the translation to draw UI elements in screen space if needed later
        g2.translate(cameraX, 0);

        // Example of a UI element (like a score or health bar)
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 20));
        g2.drawString("Enemies Remaining: " + enemies.size(), 20, 30);
        g2.drawString("Controls: A/D/Arrows to Move, W/Space to Jump, J to Attack", 20, 60);

        g2.dispose();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_A || code == KeyEvent.VK_LEFT) player.setMovingLeft(true);
        if (code == KeyEvent.VK_D || code == KeyEvent.VK_RIGHT) player.setMovingRight(true);
        if (code == KeyEvent.VK_W || code == KeyEvent.VK_UP || code == KeyEvent.VK_SPACE) player.jump();
        if (code == KeyEvent.VK_J) player.attack();
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_A || code == KeyEvent.VK_LEFT) player.setMovingLeft(false);
        if (code == KeyEvent.VK_D || code == KeyEvent.VK_RIGHT) player.setMovingRight(false);
    }

    @Override
    public void keyTyped(KeyEvent e) {}
}