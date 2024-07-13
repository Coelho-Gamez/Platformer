package imagetest;

import processing.core.PApplet;
import processing.core.PImage;
import processing.event.KeyEvent;
import java.util.ArrayList;

public class ImageTest extends PApplet {

    PImage character;
    PImage bg;
    float speed = 10;
    float dashMultiplier = 3.0f; // Dash speed multiplier
    float x = 0;
    float y = 1080;
    float horizontalSpeed = 0;
    float jumpSpeed = -15; // Initial jump speed
    double gravity = 0.7;   // Gravity effect
    boolean moveForward = false;
    boolean moveBackward = false;
    boolean isDashing = false;
    boolean lastDirectionRight = true; // Track the last direction faced
    boolean shiftPressed = false;
    int dashCooldown = 130; // Cooldown in frames
    int dashCooldownTimer = 0;
    int dashDuration = 24; // Maximum duration of dash in frames
    int currentDashFrame = 0; // Counter for current dash frames
    int shiftTimer = -1; // Timer to fix double inputs
    int jumpCount = 0;
    int maxJumps = 2; // Allow double jump
    StatusBar statusBar;

    ArrayList<Platform> platforms;
    ArrayList<BPlatform> bplatforms;
    ArrayList<Wall> walls; // ArrayList to hold wall objects



    public static void main(String[] args) {
        PApplet.main("imagetest.ImageTest"); // Correctly specify the package and class name
    }

    public void settings() {
        size(1920, 1080);
    }

    public void setup() {
        character = loadImage("Images/obama.png");
        bg = loadImage("Images/background4Tiles.png");

        // Resize character image
        character.resize(100, 100);

        // Initialize status bar
        statusBar = new StatusBar(40, 6, color(0, 255, 0), color(255, 0, 0));

        // Initialize platforms
        platforms = new ArrayList<>();
        bplatforms = new ArrayList<>();
        walls = new ArrayList<>();

        //Add platforms
        platforms.add(new Platform(200, 650, 200, 1));  // Raised platform
        bplatforms.add(new BPlatform(200, 650, 200, 20));  // Raised platform
        bplatforms.add(new BPlatform(800, 500, 200, 20));  // Raised platform
        platforms.add(new Platform(800, 500, 200, 1));  // Raised platform
        platforms.add(new Platform(800, 200, 200, 1));  // Raised platform
        bplatforms.add(new BPlatform(800, 200, 200, 20));  // Raised platform
        platforms.add(new Platform(200, 200, 200, 1));  // Raised platform
        bplatforms.add(new BPlatform(200, 200, 200, 20));  // Raised platform
        platforms.add(new Platform(800, 800, 200, 1));  // Raised platform
        bplatforms.add(new BPlatform(800, 800, 200, 20));  // Raised platform
        platforms.add(new Platform(1300, 950, 200, 1));  // Raised platform
        bplatforms.add(new BPlatform(1300, 950, 200, 20));  // Raised platform
        platforms.add(new Platform(1300, 400, 200, 1));  // Raised platform
        bplatforms.add(new BPlatform(1300, 400, 200, 20));  // Raised platform

        // Add floor platform
        platforms.add(new Platform(0, 1040, width, 100)); // Full width floor platform

        // Add walls (left and right)
        walls.add(new Wall(-10, 0, 10, height)); // Left wall
        walls.add(new Wall(width, 0, 10, height)); // Right wall
    }

    public void draw() {
        // Draw background image
        background(bg);

        // Draw platforms
        for (Platform platform : platforms) {
            platform.display();
        }
        for (BPlatform bplatform : bplatforms) {
            bplatform.display();
        }

        // Draw walls
        for (Wall wall : walls) {
            wall.display(this);
        }

        // Move and control the character
        moveCharacter();
        drawCharacter();

        // Update and draw status bar
        statusBar.update(isDashing);
        statusBar.display(x, y); // Pass character position to statusBar

        // Dash cooldown timer
        if (dashCooldownTimer > 0) {
            dashCooldownTimer--;
        }

        // Handle dash effect duration
        if (isDashing) {
            currentDashFrame++;
            if (currentDashFrame >= dashDuration) {
                isDashing = false;
                currentDashFrame = 0;
                dashCooldownTimer = dashCooldown; // Reset dash cooldown for next dash
            }
        }
    }

    public void drawCharacter() {
        if (moveForward || moveBackward) {
            // Determine facing direction
            if (moveForward) {
                // Draw character facing right
                image(character, x, y);
                lastDirectionRight = true;
            } else {
                // Draw character facing left (flipped horizontally)
                pushMatrix();
                translate(x + character.width, y);
                scale(-1, 1);
                image(character, 0, 0);
                popMatrix();
                lastDirectionRight = false;
            }
        } else {
            // Default: Draw character facing the last direction
            if (lastDirectionRight) {
                image(character, x, y);
            } else {
                pushMatrix();
                translate(x + character.width, y);
                scale(-1, 1);
                image(character, 0, 0);
                popMatrix();
            }
        }
    }

    public void moveCharacter() {
        // Horizontal movement
        float currentSpeed = isDashing ? speed * dashMultiplier : speed;
        if (moveForward) {
            horizontalSpeed = currentSpeed;
        } else if (moveBackward) {
            horizontalSpeed = -currentSpeed;
        } else {
            horizontalSpeed = 0;
        }
        x += horizontalSpeed;

        // Prevent character from moving out of bounds horizontally
        if (x < 0) {
            x = 0;
        } else if (x + character.width > width) {
            x = width - character.width;
        }

        // Apply gravity
        if (!isDashing && jumpSpeed == 0) {
            boolean onPlatform = false;
            for (Platform platform : platforms) {
                // Check if character is colliding with the platform from above
                if (platform.isColliding(x, y + 1, character.width, character.height)) {
                    y = platform.y + character.height;
                    onPlatform = true;
                    jumpSpeed = 0;
                    jumpCount = 0; // Reset jump count when landing
                    break;
                }
            }
            if (!onPlatform && y < 740) {
                y += gravity; // Apply gravity if not on platform
            }
        }

        // Check if character is jumping
        if (jumpSpeed != 0) {
            for (Platform platform : platforms) {
                if (platform.isColliding(x, y + jumpSpeed, character.width, character.height)) {
                    y = platform.y - character.height;
                    jumpSpeed = 0;
                    jumpCount = 0; // Reset jump count when landing
                    break;
                }
            }
            for (BPlatform bPlatform : bplatforms) {
                // Check if character is colliding with the platform from below
                if (bPlatform.isCollidingBottom(x, y + jumpSpeed, character.width, character.height)) {
                    y = bPlatform.y + character.height;
                    jumpSpeed = 0;
                    jumpCount = 0; // Reset jump count when landing
                    break;
                }
            }
            y += jumpSpeed;
            jumpSpeed += gravity; // Apply gravity to the jump speed
        }

        // Check for wall collision and bounce back if needed
        if (jumpCount == 1) {
            for (Platform platform : platforms) {
                if (platform.isColliding(x + horizontalSpeed, y, character.width, character.height)) {
                    horizontalSpeed = -horizontalSpeed; // Bounce back in the opposite direction
                    x += horizontalSpeed;
                    break;
                }
            }
            for (Wall wall : walls) {
                if (wall.isColliding(x, y, character.width, character.height)) {
                    horizontalSpeed = -horizontalSpeed; // Bounce back in the opposite direction
                    x += horizontalSpeed;
                    break;
                }
            }
        }
    }

    public void keyPressed(KeyEvent evt) {
        // Movement controls
        if (evt.getKeyCode() == 'D') { // 'D' key
            moveForward = true;
            moveBackward = false;
        } else if (evt.getKeyCode() == 'A') { // 'A' key
            moveBackward = true;
            moveForward = false;
        }

        // Dash control (Shift key)
        if (evt.getKeyCode() == 16 && dashCooldownTimer <= 0) { // Shift key
            dashCooldownTimer = dashCooldown; // Reset cooldown timer
            isDashing = true;
            shiftTimer = 20; // Set shiftTimer to detect double input
        }

        // Track if shift key is pressed
        if (evt.getKeyCode() == 16) { // Shift key
            shiftPressed = true;
        }

        // Jump control (W key)
        if (evt.getKeyCode() == 'W' || evt.getKeyCode() == 32) { // 'W' or Space key
            if (jumpCount < maxJumps) { // Allow jump if jumpCount is less than maxJumps
                jumpSpeed = -13; // Normal jump speed
                if (shiftPressed) {
                    jumpSpeed = -20; // Higher jump if Shift is pressed
                }
                jumpCount++; // Increment jump counter
            }
        }
    }

    public void keyReleased(KeyEvent evt) {
        // Movement controls
        if (evt.getKeyCode() == 'D') { // 'D' key
            moveForward = false;
        } else if (evt.getKeyCode() == 'A') { // 'A' key
            moveBackward = false;
        }

        // Dash control (release Shift key)
        if (evt.getKeyCode() == 16) { // Shift key
            isDashing = false;
            currentDashFrame = 0; // Reset current dash frame counter
            shiftPressed = false; // Reset shift key pressed state
        }
    }

    class Platform {
        float x, y, width, height;

        Platform(float x, float y, float width, float height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        void display() {
            fill(150, 75, 0); // Brown color for platforms
            rect(x, y, width, height);
        }

        boolean isColliding(float characterX, float characterY, float characterWidth, float characterHeight) {
            // Check if character is colliding with the platform
            return characterX + characterWidth > x && characterX < x + width &&
                    characterY + characterHeight >= y && characterY < y + height;
        }
    }

    class BPlatform {
        float x, y, width, height;

        BPlatform(float x, float y, float width, float height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        void display() {
            fill(150, 75, 0); // Brown color for platforms
            rect(x, y, width, height);
        }

        boolean isCollidingBottom(float characterX, float characterY, float characterWidth, float characterHeight) {
            // Check if character is colliding with the bottom of the platform
            return characterX + characterWidth > x && characterX < x + width &&
                    characterY + characterHeight >= y + height - 5 && characterY + characterHeight <= y + height + 85;
        }
    }

    class StatusBar {
        float x, y;
        float width, height;
        int barColor;
        int bgColor;

        float value = 0;
        float maxValue = 120; // Maximum value for status bar
        float offsetX, offsetY; // Offset from character position

        StatusBar(float width, float height, int barColor, int bgColor) {
            this.width = width;
            this.height = height;
            this.barColor = barColor;
            this.bgColor = bgColor;
            this.x = 20;
            this.y = height + 20;
            this.offsetX = 30; // Example offset from character position
            this.offsetY = -20; // Example offset from character position
        }

        void update(boolean isDashing) {
            if (!isDashing && value < maxValue) {
                value += 1; // Increase value over time if not dashing
            } else if (isDashing) {
                value = 0; // Reset value when dashing
            }
        }

        void display(float charX, float charY) {
            // Draw background
            fill(bgColor);
            rect(charX + offsetX, charY + offsetY, width, height);

            // Draw status bar
            float barWidth = map(value, 0, maxValue, 0, width);
            fill(barColor);
            rect(charX + offsetX, charY + offsetY, barWidth, height);
        }
    }

    static class Wall {
        float x, y, width, height;

        Wall(float x, float y, float width, float height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        void display(PApplet p) {
            p.fill(100); // Gray color for walls
            p.rect(x, y, width, height);
        }

        boolean isColliding(float characterX, float characterY, float characterWidth, float characterHeight) {
            // Check if character is colliding with the wall
            return characterX + characterWidth > x && characterX < x + width &&
                    characterY + characterHeight > y && characterY < y + height;
        }
    }
}
