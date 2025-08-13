// Enemy.java
import java.awt.*;

public class Enemy {
    private int x, y;
    private int width = 40;
    private int height = 40;

    private int speed = 2;
    private int direction = 1; // 1 for right, -1 for left

    // Patrol area
    private int patrolStartX;
    private int patrolEndX;

    public Enemy(int x, int y, int patrolStart, int patrolEnd) {
        this.x = x;
        this.y = y;
        this.patrolStartX = patrolStart;
        this.patrolEndX = patrolEnd;
    }

    public void update() {
        x += speed * direction;
        
        // If enemy reaches the end of its patrol range, turn around
        if (x <= patrolStartX || x + width >= patrolEndX) {
            direction *= -1; // Reverse direction
        }
    }

    public void draw(Graphics2D g2) {
        g2.setColor(Color.RED);
        g2.fillRect(x, y, width, height);
        
        // Optional: Draw patrol range for debugging
        // g2.setColor(Color.YELLOW);
        // g2.drawLine(patrolStartX, y + height + 5, patrolEndX, y + height + 5);
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }
}