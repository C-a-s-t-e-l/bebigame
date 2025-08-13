// Player.java
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.List;

public class Player {
    private enum PlayerState { IDLE, WALKING, JUMPING, FALLING, ATTACKING }
    private PlayerState currentState;

    private int x, y, width = 50, height = 60;
    private double velX = 0, velY = 0, speed = 5.0, jumpStrength = 16.0, gravity = 0.6;
    private boolean onGround = false, movingLeft = false, movingRight = false, facingRight = true;
    
    private int health = 3;
    private boolean isInvincible = false;
    private int invincibilityTimer = 0;
    private final int INVINCIBILITY_DURATION = 120;
    
    private int attackTimer = 0;
    private final int ATTACK_DURATION = 25;
    private final int ATTACK_COOLDOWN = 15;
    private final int ATTACK_WIDTH = 50, ATTACK_HEIGHT = 20;

    private BufferedImage idleSprite, jumpSprite, attackSprite;
    private BufferedImage[] walkSprites;
    private int animationTick = 0, animationIndex = 0;
    private final int ANIMATION_SPEED = 12; // Adjusted for a better feel

    private String characterPrefix;

    public Player(int x, int y, String characterPrefix) {
        this.x = x;
        this.y = y;
        this.characterPrefix = characterPrefix;
        this.currentState = PlayerState.IDLE;
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
        idleSprite = loadSprite(characterPrefix + "__0000_idle_1.png");
        jumpSprite = loadSprite(characterPrefix + "__0027_jump_1.png");
        attackSprite = loadSprite(characterPrefix + "__0031_attack_1.png");
        walkSprites = new BufferedImage[2];
        walkSprites[0] = loadSprite(characterPrefix + "__0006_walk_1.png");
        walkSprites[1] = loadSprite(characterPrefix + "__0007_walk_2.png");
    }

    public void update(List<Platform> platforms) {
        if (health <= 0) return;
        handleMovement();
        handleCollision(platforms);
        updateState(); // This method is now much smarter
        updateAnimationTick(); // This method is now simpler
        handleTimers();
    }
    
    private void handleMovement() {
        if (currentState == PlayerState.ATTACKING && attackTimer > ATTACK_COOLDOWN) {
            velX = 0;
        } else {
            if (movingLeft) { velX = -speed; facingRight = false; }
            else if (movingRight) { velX = speed; facingRight = true; }
            else { velX = 0; }
        }
        x += velX;
        if (x < 0) x = 0;
        if (x > GamePanel.WORLD_WIDTH - width) x = GamePanel.WORLD_WIDTH - width;
        velY += gravity;
        y += velY;
    }

    private void handleCollision(List<Platform> platforms) {
        onGround = false;
        for (Platform platform : platforms) {
            if (getBounds().intersects(platform.getBounds())) {
                if (velY > 0 && (y + height - velY) <= platform.getY()) {
                    y = platform.getY() - height;
                    velY = 0;
                    onGround = true;
                } else if (velX != 0) {
                    x -= velX;
                }
            }
        }
    }

    // --- NEW ROBUST STATE MACHINE ---
    private void updateState() {
        // The attack state is controlled by its own timer, so we leave it alone.
        if (currentState == PlayerState.ATTACKING) {
            return;
        }

        // Check for state transitions when on the ground
        if (onGround) {
            if (velX == 0) {
                // If we weren't already idle, change to idle and reset animation
                if (currentState != PlayerState.IDLE) {
                    currentState = PlayerState.IDLE;
                    animationIndex = 0;
                }
            } else {
                // If we weren't already walking, change to walking and reset animation
                if (currentState != PlayerState.WALKING) {
                    currentState = PlayerState.WALKING;
                    animationIndex = 0;
                }
            }
        } 
        // Check for state transitions when in the air
        else {
            if (velY < 0) { // Moving up
                if (currentState != PlayerState.JUMPING) {
                    currentState = PlayerState.JUMPING;
                    animationIndex = 0;
                }
            } else { // Moving down
                if (currentState != PlayerState.FALLING) {
                    currentState = PlayerState.FALLING;
                    animationIndex = 0;
                }
            }
        }
    }

    // --- NEW SIMPLIFIED ANIMATION LOGIC ---
    private void updateAnimationTick() {
        animationTick++;
        if (animationTick >= ANIMATION_SPEED) {
            animationTick = 0;
            // Only advance the animation frame if we are in a state that has one.
            if (currentState == PlayerState.WALKING) {
                animationIndex++;
                if (walkSprites != null && animationIndex >= walkSprites.length) {
                    animationIndex = 0;
                }
            }
            // For other states (IDLE, JUMP, etc.), the animation index is fixed at 0
            // and is set during the state transition.
        }
    }
    
    private void handleTimers() {
        if (currentState == PlayerState.ATTACKING) {
            attackTimer--;
            if (attackTimer <= 0) {
                currentState = PlayerState.IDLE; // Attack is over, return to idle
                animationIndex = 0;
            }
        }
        if (isInvincible) {
            invincibilityTimer--;
            if (invincibilityTimer <= 0) isInvincible = false;
        }
    }

    public void jump() {
        if (onGround && health > 0 && currentState != PlayerState.ATTACKING) {
            velY = -jumpStrength;
            // The state will be changed to JUMPING by the updateState() method
        }
    }

    public void attack() {
        if (health > 0 && currentState != PlayerState.ATTACKING) {
            currentState = PlayerState.ATTACKING;
            animationIndex = 0;
            attackTimer = ATTACK_DURATION;
        }
    }

    public void takeDamage(int amount) {
        if (!isInvincible) {
            this.health -= amount;
            this.isInvincible = true;
            this.invincibilityTimer = INVINCIBILITY_DURATION;
            if (health <= 0) health = 0;
        }
    }
    
    public void draw(Graphics2D g2) {
        BufferedImage imageToDraw = null;
        if (walkSprites != null && walkSprites[0] != null) {
            switch (currentState) {
                case ATTACKING: imageToDraw = attackSprite; break;
                case JUMPING: case FALLING: imageToDraw = jumpSprite; break;
                case WALKING: imageToDraw = walkSprites[animationIndex]; break;
                case IDLE: default: imageToDraw = idleSprite; break;
            }
        }

        if (isInvincible && health > 0 && invincibilityTimer % 10 < 5) return;

        if (imageToDraw != null) {
            if (facingRight) g2.drawImage(imageToDraw, x, y, width, height, null);
            else g2.drawImage(imageToDraw, x + width, y, -width, height, null);
        } else {
            g2.setColor(Color.MAGENTA);
            g2.fillRect(x, y, width, height);
        }
    }
    
    public int getX() { return x; }
    public int getHealth() { return health; }
    public boolean isAttacking() { return currentState == PlayerState.ATTACKING; }
    public Rectangle getBounds() { return new Rectangle(x, y, width, height); }
    public Rectangle getAttackBox() {
        return isAttacking() ? (facingRight ? new Rectangle(x + width, y + height / 4, ATTACK_WIDTH, ATTACK_HEIGHT) : new Rectangle(x - ATTACK_WIDTH, y + height / 4, ATTACK_WIDTH, ATTACK_HEIGHT)) : new Rectangle(0,0,0,0);
    }
    public void setMovingLeft(boolean movingLeft) { this.movingLeft = movingLeft; }
    public void setMovingRight(boolean movingRight) { this.movingRight = movingRight; }
}