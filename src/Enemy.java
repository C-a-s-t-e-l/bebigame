// Enemy.java
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

public class Enemy {
    private int x, y, width = 50, height = 50;
    private int speed = 2, direction = 1;
    private int patrolStartX, patrolEndX;

    private BufferedImage[] walkSprites;
    private int animationTick = 0, animationIndex = 0;
    private final int ANIMATION_SPEED = 10;
    private boolean facingRight = true;
    private String characterPrefix;

    public Enemy(int x, int y, int patrolStart, int patrolEnd, String characterPrefix) {
        this.x = x;
        this.y = y;
        this.patrolStartX = patrolStart;
        this.patrolEndX = patrolEnd;
        this.characterPrefix = characterPrefix;
        loadSprites();
    }

    private BufferedImage loadSprite(String fileName) {
        try {
            String fullPath = "/resources/" + fileName;
            URL resourceUrl = getClass().getResource(fullPath);
            if (resourceUrl == null) {
                System.err.println("Cannot find sprite: " + fullPath);
                return null;
            }
            return ImageIO.read(resourceUrl);
        } catch (IOException e) {
            System.err.println("Failed to load sprite: " + fileName);
            e.printStackTrace();
            return null;
        }
    }

    private void loadSprites() {
        walkSprites = new BufferedImage[2];
        walkSprites[0] = loadSprite(characterPrefix + "__0005_turn_3.png");
        walkSprites[1] = loadSprite(characterPrefix + "__0006_walk_1.png");
    }

    public void update() {
        x += speed * direction;
        if (x <= patrolStartX) {
            direction = 1;
            facingRight = true;
        } else if (x + width >= patrolEndX) {
            direction = -1;
            facingRight = false;
        }
        animationTick++;
        if (animationTick >= ANIMATION_SPEED) {
            animationTick = 0;
            animationIndex++;
            if (walkSprites != null && animationIndex >= walkSprites.length) {
                animationIndex = 0;
            }
        }
    }

    public void draw(Graphics2D g2) {
        BufferedImage imageToDraw = null;
        if (walkSprites != null && walkSprites[0] != null) { // Check if sprites loaded
            imageToDraw = walkSprites[animationIndex];
        }

        if (imageToDraw != null) {
            if (facingRight) {
                 g2.drawImage(imageToDraw, x, y, width, height, null);
            } else {
                 g2.drawImage(imageToDraw, x + width, y, -width, height, null);
            }
        } else {
            g2.setColor(Color.RED); // Draw error color if sprite is missing
            g2.fillRect(x, y, width, height);
        }
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }
}