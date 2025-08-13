// Player.java
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.List;

public class Player {
    private int x, y;
    private int width = 50;  // Adjusted for sprite size
    private int height = 60; // Adjusted for sprite size

    private double velX = 0;
    private double velY = 0;
    private double speed = 4.0;
    private double jumpStrength = 15.0;
    private double gravity = 0.5;

    private boolean onGround = false;
    private boolean movingLeft = false;
    private boolean movingRight = false;
    private boolean facingRight = true;

    // Attack variables
    private boolean isAttacking = false;
    private int attackTimer = 0;
    private final int ATTACK_DURATION = 15; // in frames (1/4 second at 60fps)
    private final int ATTACK_WIDTH = 50;
    private final int ATTACK_HEIGHT = 20;

    // Sprite
    private BufferedImage sprite;

    public Player(int x, int y) {
        this.x = x;
        this.y = y;
        loadSprite();
    }

    private void loadSprite() {
        try {
            // --- REPLACE THIS URL WITH THE LINK TO YOUR OWN SPRITE ---
            sprite = ImageIO.read(new URL("https://i.imgur.com/gJc0mIq.png")); // A simple placeholder character
            // ---------------------------------------------------------
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed to load player sprite!");
            sprite = null; // Use a fallback if loading fails
        }
    }

    public void update(List<Platform> platforms) {
        // Horizontal Movement
        if (movingLeft) {
            velX = -speed;
            facingRight = false;
        } else if (movingRight) {
            velX = speed;
            facingRight = true;
        } else {
            velX = 0;
        }

        // Apply horizontal velocity
        x += velX;
        // Prevent player from going out of world bounds
        if (x < 0) x = 0;
        if (x > GamePanel.WORLD_WIDTH - width) x = GamePanel.WORLD_WIDTH - width;

        // Vertical Movement (Gravity)
        velY += gravity;
        y += velY;

        // Platform Collision
        onGround = false;
        for (Platform platform : platforms) {
            if (getBounds().intersects(platform.getBounds())) {
                // Check if landing on top of the platform from above
                if (velY > 0 && (y + height - velY) <= platform.getY()) {
                    y = platform.getY() - height;
                    velY = 0;
                    onGround = true;
                }
                // Simple side collision (stops player from going through platforms sideways)
                else if (velX != 0 && onGround == false) {
                     x -= velX; // Push back
                }
            }
        }
        
        // Update attack timer
        if (isAttacking) {
            attackTimer--;
            if (attackTimer <= 0) {
                isAttacking = false;
            }
        }
    }

    public void jump() {
        if (onGround) {
            velY = -jumpStrength;
            onGround = false;
        }
    }
    
    public void attack() {
        if (!isAttacking) {
            isAttacking = true;
            attackTimer = ATTACK_DURATION;
        }
    }

    public void draw(Graphics2D g2) {
        // Draw the sprite, flipping it based on direction
        if (sprite != null) {
            if (facingRight) {
                g2.drawImage(sprite, x, y, width, height, null);
            } else {
                // Classic trick to flip an image horizontally
                g2.drawImage(sprite, x + width, y, -width, height, null);
            }
        } else {
            // Fallback: draw a rectangle if the sprite failed to load
            g2.setColor(Color.CYAN);
            g2.fillRect(x, y, width, height);
        }

        // Draw attack visualization (useful for debugging)
        if (isAttacking) {
            g2.setColor(new Color(255, 255, 0, 128)); // Semi-transparent yellow
            g2.fill(getAttackBox());
        }
    }

    // Getters
    public int getX() { return x; }
    public boolean isAttacking() { return isAttacking; }
    public Rectangle getBounds() { return new Rectangle(x, y, width, height); }

    public Rectangle getAttackBox() {
        if (facingRight) {
            return new Rectangle(x + width, y + height / 4, ATTACK_WIDTH, ATTACK_HEIGHT);
        } else {
            return new Rectangle(x - ATTACK_WIDTH, y + height / 4, ATTACK_WIDTH, ATTACK_HEIGHT);
        }
    }

    // Setters for key input
    public void setMovingLeft(boolean movingLeft) { this.movingLeft = movingLeft; }
    public void setMovingRight(boolean movingRight) { this.movingRight = movingRight; }
}