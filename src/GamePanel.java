// GamePanel.java
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GamePanel extends JPanel implements Runnable, KeyListener {
    
    private static final String PLAYER_CHARACTER_PREFIX = "armor";
    private static final String ENEMY_CHARACTER_PREFIX = "predatormask";

    private enum GameState { PLAYING, GAME_OVER }
    private GameState gameState;

    public static final int SCREEN_WIDTH = 800;
    public static final int SCREEN_HEIGHT = 600;
    public static final int WORLD_WIDTH = 3200;
    private final int FPS = 60;

    private Player player;
    private List<Platform> platforms;
    private List<Enemy> enemies;
    private int cameraX = 0;
    private Thread gameThread;

    // --- NEW: Input Buffer Flags ---
    // These flags will store the state of the keys, smoothed out over frames.
    private boolean leftPressed;
    private boolean rightPressed;

    public GamePanel() {
        this.setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        this.setBackground(new Color(135, 206, 235));
        this.setDoubleBuffered(true);
        this.addKeyListener(this);
        this.setFocusable(true);
        startGame();
    }

    public void startGame() {
        gameState = GameState.PLAYING;
        player = new Player(100, 400, PLAYER_CHARACTER_PREFIX);
        platforms = new ArrayList<>();
        enemies = new ArrayList<>();
        initializeLevel();
    }

    private void initializeLevel() {
        platforms.add(new Platform(0, 550, WORLD_WIDTH, 50));
        platforms.add(new Platform(300, 450, 150, 20));
        platforms.add(new Platform(550, 350, 150, 20));
        platforms.add(new Platform(800, 250, 100, 20));
        platforms.add(new Platform(1100, 400, 200, 20));
        platforms.add(new Platform(1400, 300, 150, 20));
        platforms.add(new Platform(1800, 450, 250, 20));
        enemies.add(new Enemy(1850, 500, 1800, 2000, ENEMY_CHARACTER_PREFIX));
        enemies.add(new Enemy(1150, 350, 1100, 1250, ENEMY_CHARACTER_PREFIX));
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
        while (gameThread != null) {
            long currentTime = System.nanoTime();
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
        if (gameState == GameState.PLAYING) {
            // --- NEW: Handle Input Before Updating Player ---
            // We tell the player object the STABLE state of the keys for this frame.
            player.setMovingLeft(leftPressed);
            player.setMovingRight(rightPressed);
            
            // Now update the player with the clean input
            player.update(platforms);

            for (Enemy enemy : enemies) {
                enemy.update();
            }
            checkCollisions();
            updateCamera();
            if (player.getHealth() <= 0) {
                gameState = GameState.GAME_OVER;
            }
        }
    }

    private void checkCollisions() {
        Rectangle playerBounds = player.getBounds();
        Rectangle playerAttackBox = player.getAttackBox();
        Iterator<Enemy> enemyIterator = enemies.iterator();
        while (enemyIterator.hasNext()) {
            Enemy enemy = enemyIterator.next();
            Rectangle enemyBounds = enemy.getBounds();
            if (player.isAttacking() && playerAttackBox.intersects(enemyBounds)) {
                enemyIterator.remove();
            } else if (playerBounds.intersects(enemyBounds)) {
                player.takeDamage(1);
            }
        }
    }

    private void updateCamera() {
        cameraX = player.getX() - SCREEN_WIDTH / 2;
        if (cameraX < 0) cameraX = 0;
        if (cameraX > WORLD_WIDTH - SCREEN_WIDTH) cameraX = WORLD_WIDTH - SCREEN_WIDTH;
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.translate(-cameraX, 0);
        for (Platform platform : platforms) platform.draw(g2);
        for (Enemy enemy : enemies) enemy.draw(g2);
        player.draw(g2);
        g2.translate(cameraX, 0);
        drawUI(g2);
        if (gameState == GameState.GAME_OVER) {
            drawGameOverScreen(g2);
        }
        g2.dispose();
    }
    
   
    // (Helper methods for UI are unchanged, so they are omitted here for brevity)
    private void drawUI(Graphics2D g2) { g2.setColor(Color.WHITE); g2.setFont(new Font("Arial", Font.BOLD, 20)); g2.drawString("Health: " + player.getHealth(), 20, 30); g2.drawString("Enemies: " + enemies.size(), 20, 55); }
    private void drawGameOverScreen(Graphics2D g2) { g2.setColor(new Color(0, 0, 0, 150)); g2.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT); g2.setColor(Color.RED); g2.setFont(new Font("Arial", Font.BOLD, 50)); String gameOverText = "GAME OVER"; int textWidth = g2.getFontMetrics().stringWidth(gameOverText); g2.drawString(gameOverText, (SCREEN_WIDTH - textWidth) / 2, SCREEN_HEIGHT / 2 - 20); g2.setColor(Color.WHITE); g2.setFont(new Font("Arial", Font.PLAIN, 20)); String restartText = "Press 'R' to Restart"; textWidth = g2.getFontMetrics().stringWidth(restartText); g2.drawString(restartText, (SCREEN_WIDTH - textWidth) / 2, SCREEN_HEIGHT / 2 + 20); }

    // --- NEW: KeyListener now only updates the buffer flags ---
    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if (gameState == GameState.PLAYING) {
            if (code == KeyEvent.VK_A || code == KeyEvent.VK_LEFT) {
                leftPressed = true;
            }
            if (code == KeyEvent.VK_D || code == KeyEvent.VK_RIGHT) {
                rightPressed = true;
            }
            // Jump and Attack are single events, so we can call them directly
            if (code == KeyEvent.VK_W || code == KeyEvent.VK_UP || code == KeyEvent.VK_SPACE) {
                player.jump();
            }
            if (code == KeyEvent.VK_J) {
                player.attack();
            }
        }
        if (code == KeyEvent.VK_R && gameState == GameState.GAME_OVER) {
            startGame();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_A || code == KeyEvent.VK_LEFT) {
            leftPressed = false;
        }
        if (code == KeyEvent.VK_D || code ==  KeyEvent.VK_RIGHT) {
            rightPressed = false;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}
}