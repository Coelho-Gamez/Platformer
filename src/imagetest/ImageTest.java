package imagetest;

import processing.core.PApplet;
import processing.core.PImage;
import processing.event.Event;
import processing.event.KeyEvent;
import java.util.ArrayList;

public class ImageTest extends PApplet {
    PImage platformImage; // New variable for the platform image


    PImage attack2SpriteSheet, attack3SpriteSheet;
    int attack2FrameCount = 8;
    int attack3FrameCount = 8;
    PImage floorImage;
    PImage idleSpriteSheet, moveSpriteSheet, jumpSpriteSheet, doubleJumpSpriteSheet;
    PImage bg;
    PImage dashSpriteSheet, dashEffectSpriteSheet;
    int dashFrameCount = 6; // Number of frames in the dash animation
    int idleFrameCount = 4; // Assuming idle has 4 frames
    int moveFrameCount = 6; // Run has 6 frames
    int jumpFrameCount = 4; // Assuming jump has 4 frames
    int doubleJumpFrameCount = 6; // Double jump has 6 frames

    int currentFrame = 0;
    int frameWidth, frameHeight;

    float speed = 10;
    float dashMultiplier = 3.0f;
    float x = 0;
    float y = 940;
    float horizontalSpeed = 0;
    float jumpSpeed = 0;
    double gravity = 0.6;
    boolean moveForward = false;
    boolean moveBackward = false;
    boolean isDashing = false;
    boolean isJumping = false;
    boolean lastDirectionRight = true;
    int dashCooldown = 130;
    int dashCooldownTimer = 0;
    int dashDuration = 24;
    int currentDashFrame = 0;
    int jumpCount = 0;
    int maxJumps = 2;
    float stamina = 100;
    boolean shiftPressed = false;
    boolean isAttacking = false;
    int currentAttack = 0; //

    int attackFrameDelay = 5;
    int attackFrameDelayCounter = 0;
    int tintTimer = 0; // Timer for tint duration
    int dashAnimationFrame = 0;
    int dashAnimationFrameDelay = 5;
    int dashAnimationFrameDelayCounter = 0;

    Camera camera;
    HealthBar healthBar;


    ArrayList<Platform> platforms;
    ArrayList<Wall> walls;




    Platform floorPlatform;

    public static void main(String[] args) {
        PApplet.main("imagetest.ImageTest");
    }

    public void settings() {
        size(1920, 1080);
    }

    public void setup() {
        // Load images
        idleSpriteSheet = loadImage("Images/2 Punk/Punk_idlev2.png");
        moveSpriteSheet = loadImage("Images/2 Punk/Punk_runv2.png");
        jumpSpriteSheet = loadImage("Images/2 Punk/Punk_jumpv2.png");
        doubleJumpSpriteSheet = loadImage("Images/2 Punk/Punk_doublejumpv2.png");
        bg = loadImage("Images/background4Tiles.png");
        platformImage = loadImage("Images/platform.png"); // Load the platform image



        attack2SpriteSheet = loadImage("Images/2 Punk/Punk_attack2v2.png");
        attack3SpriteSheet = loadImage("Images/2 Punk/Punk_attack3v2.png");
        floorImage = loadImage("Images/flunderarea.png"); // Load the floor image

        dashEffectSpriteSheet = loadImage("Images/1 Magic/10.png");
        dashEffectSpriteSheet.resize(500, 100);
        dashSpriteSheet = loadImage("Images/2/Dashv2.png");
        dashSpriteSheet.resize(600, 100); // Adjust size as needed
        platformImage.resize(150, 20); // Resize to match the typical platform size


        attack2SpriteSheet.resize(800, 100); // Adjust size as needed
        attack3SpriteSheet.resize(800, 100); // Adjust size as needed



        // Resize sprite sheets as necessary
        idleSpriteSheet.resize(400, 100);
        moveSpriteSheet.resize(600, 100); // 6 frames
        jumpSpriteSheet.resize(400, 100);
        doubleJumpSpriteSheet.resize(600, 100); // 6 frames

        camera = new Camera(width / 2, height / 2);

        platforms = new ArrayList<>();
        walls = new ArrayList<>();


        addInitialPlatforms();


        floorPlatform = new Platform(-1000, 1040, 1300, 200, floorImage, true);
        platforms.add(floorPlatform);

        healthBar = new HealthBar(x-1000, height - 500, 70, 10, 100);
    }

    public void draw() {
        camera.update(x, y);
        background(bg);

        for (Platform platform : platforms) {
            platform.display();
        }

        moveCharacter();
        drawCharacter();

        if (isDashing) {
            currentDashFrame++;
            if (currentDashFrame >= dashDuration) {
                isDashing = false;
                currentDashFrame = 0;
                dashCooldownTimer = dashCooldown;
            }
        }

        if (dashCooldownTimer > 0) {
            dashCooldownTimer--;
        }


        healthBar.display(); // Draw the health bar
    }

    public void drawCharacter() {
        PImage currentSpriteSheet = idleSpriteSheet; // Default value
        int currentFrameCount = idleFrameCount; // Default value

        if (isAttacking) {
            switch (currentAttack) {
                case 2:
                    currentSpriteSheet = attack2SpriteSheet;
                    currentFrameCount = attack2FrameCount;
                    break;
                case 3:
                    currentSpriteSheet = attack3SpriteSheet;
                    currentFrameCount = attack3FrameCount;
                    break;
                default:
                    break;
            }
        } else if (isDashing) {
            currentSpriteSheet = dashSpriteSheet;
            currentFrameCount = dashFrameCount;
            if (dashAnimationFrameDelayCounter >= dashAnimationFrameDelay) {
                dashAnimationFrame = (dashAnimationFrame + 1) % dashFrameCount;
                dashAnimationFrameDelayCounter = 0;
            } else {
                dashAnimationFrameDelayCounter++;
            }
        } else {
            if (jumpCount == 2) {
                currentSpriteSheet = doubleJumpSpriteSheet;
                currentFrameCount = doubleJumpFrameCount;
            } else if (isJumping) {
                currentSpriteSheet = jumpSpriteSheet;
                currentFrameCount = jumpFrameCount;
            } else if (moveForward || moveBackward) {
                currentSpriteSheet = moveSpriteSheet;
                currentFrameCount = moveFrameCount;
            } else {
                currentSpriteSheet = idleSpriteSheet;
                currentFrameCount = idleFrameCount;
            }
        }

        frameWidth = currentSpriteSheet.width / currentFrameCount;
        frameHeight = currentSpriteSheet.height;

        // Animate the character
        if (attackFrameDelayCounter >= attackFrameDelay) {
            currentFrame = (currentFrame + 1) % currentFrameCount;
            attackFrameDelayCounter = 0;
            if (currentFrame == 0 && isAttacking) {
                isAttacking = false; // End attack animation when cycle completes
            }
        } else {
            attackFrameDelayCounter++;
        }

        PImage currentSprite;
        if (isDashing) {
            currentSprite = dashSpriteSheet.get(dashAnimationFrame * frameWidth, 0, frameWidth, frameHeight);
        } else {
            currentSprite = currentSpriteSheet.get(currentFrame * frameWidth, 0, frameWidth, frameHeight);
        }

        if (tintTimer > 0) {
            tint(104, 104, 104); // Apply a grey tint with transparency during dashing
        } else {
            noTint(); // Clear the tint
        }

        if (moveForward || moveBackward) {
            if (moveForward) {
                image(currentSprite, camera.adjustX(x), camera.adjustY(y));
                lastDirectionRight = true;
            } else {
                pushMatrix();
                translate(camera.adjustX(x) + frameWidth, camera.adjustY(y));
                scale(-1, 1);
                image(currentSprite, 0, 0);
                popMatrix();
                lastDirectionRight = false;
            }
        } else {
            if (lastDirectionRight) {
                image(currentSprite, camera.adjustX(x), camera.adjustY(y));
            } else {
                pushMatrix();
                translate(camera.adjustX(x) + frameWidth, camera.adjustY(y));
                scale(-1, 1);
                image(currentSprite, 0, 0);
                popMatrix();
            }
        }

        // Draw health bar above the character
        healthBar.x = x + frameWidth / 2 - healthBar.width / 2;
        healthBar.y = y - healthBar.height - 10;
        healthBar.display();
    }

    public void moveCharacter() {
        float currentSpeed = isDashing ? speed * dashMultiplier : speed;

        // Handle horizontal movement
        if (moveForward) {
            horizontalSpeed = currentSpeed;
        } else if (moveBackward) {
            horizontalSpeed = -currentSpeed;
        } else {
            horizontalSpeed = 0;
        }

        // Update the horizontal position
        x += horizontalSpeed;

        boolean onPlatform = false;

        // Check collisions with platforms
        for (Platform platform : platforms) {
            if (platform.isColliding(x, y + 1, frameWidth, frameHeight)) {
                y = platform.y - frameHeight;
                onPlatform = true;
                jumpSpeed = 0;
                jumpCount = 0;
                isJumping = false;
                break;
            }
        }

        // Check collisions with walls
        for (Wall wall : walls) {
            if (wall.isColliding(x, y, frameWidth, frameHeight)) {
                if (wall.x > x) {
                    x = wall.x - frameWidth;
                } else {
                    x = wall.x + wall.w;
                }
            }
        }

        // Apply gravity if not on the platform
        if (!onPlatform) {
            jumpSpeed += gravity;
            y += jumpSpeed;
        } else {
            if (jumpSpeed > 0) {
                y = floor((float) (y + gravity));
            }
        }

        // Update stamina for dashing
        if (isDashing) {
            stamina -= 1;
            if (stamina <= 0) {
                isDashing = false;
                stamina = 0;
                dashCooldownTimer = dashCooldown;
            }
        } else {
            if (stamina < 100) {
                stamina += 0.5;
                if (stamina > 100) {
                    stamina = 100;
                }
            }
        }
    }

    public void keyPressed(KeyEvent evt) {
        if (evt.getKeyCode() == 68) { // 'D' key
            this.moveForward = true;
            this.moveBackward = false;
        } else if (evt.getKeyCode() == 65) { // 'A' key
            this.moveBackward = true;
            this.moveForward = false;
        }

        if (evt.getKeyCode() == 16) { // Shift key
            this.shiftPressed = true;
            if (dashCooldownTimer == 0 && stamina > 0) {
                this.isDashing = true;
            }
        }

        if (evt.getKeyCode() == 32 || evt.getKeyCode() == 87) { // Space bar or 'W' key for jump
            if (jumpCount < maxJumps) {
                this.jumpSpeed = -15;
                this.y += jumpSpeed;
                jumpCount++;
                isJumping = true;
            }
        }

    }

    public void mousePressed() {
        if (mouseButton == LEFT) { // Left mouse button
            if (isOnGround()) { // Check if on ground before allowing attacks
                if (!isAttacking) {
                    isAttacking = true;
                    currentAttack = 2; // Attack 2

                }
            }
        } else if (mouseButton == RIGHT) { // Right mouse button
            if (isOnGround()) { // Check if on ground before allowing attacks
                if (!isAttacking) {
                    isAttacking = true;
                    currentAttack = 3; // Attack 3

                }
            }
        }
    }


    public void keyReleased(KeyEvent evt) {
        if (evt.getKeyCode() == 68) { // 'D' key
            this.moveForward = false;
        } else if (evt.getKeyCode() == 65) { // 'A' key
            this.moveBackward = false;
        }

        if (evt.getKeyCode() == 16) { // Shift key
            this.shiftPressed = false;
        }
    }

    public void addInitialPlatforms() {
        platforms.add(new Platform(-800, 800, 200, 30, platformImage));
        platforms.add(new Platform(-400, 600, 200, 30, platformImage));
        platforms.add(new Platform(0, 800, 200, 30, platformImage));
    }

    public void addInitialWalls() {

    }

    class Camera {
        float xOffset;
        float yOffset;

        Camera(float xOffset, float yOffset) {
            this.xOffset = xOffset;
            this.yOffset = yOffset;
        }

        void update(float x, float y) {
            xOffset = x - width / 2;
            yOffset = y - height / 2;
        }

        float adjustX(float x) {
            return x - xOffset;
        }

        float adjustY(float y) {
            return y - yOffset;
        }
    }

    class Platform {
        float x, y, w, h;
        PImage image;
        int color;
        boolean isFloor; // New flag to indicate if this is a floor platform

        // Constructor for platforms with an image
        Platform(float x, float y, float w, float h, PImage image) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            this.image = image;
            this.color = color(255, 255, 255); // Default color
            this.isFloor = false;
        }

        // Constructor for platforms with a color
        Platform(float x, float y, float w, float h, int color) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            this.color = color;
            this.image = null; // No image for this constructor
            this.isFloor = false;
        }

        // Constructor for floor platforms with an image
        Platform(float x, float y, float w, float h, PImage image, boolean isFloor) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            this.image = image;
            this.color = color(255, 255, 255); // Default color
            this.isFloor = isFloor;
        }

        void display() {
            if (isFloor) {
                if (image != null) {
                    imageMode(CORNER);
                    image(floorImage, camera.adjustX(x), camera.adjustY(y), w, h); // Display the floor image
                } else {
                    fill(color);
                    noStroke();
                    rect(camera.adjustX(x), camera.adjustY(y), w, h); // Display the colored platform
                }
            } else {
                if (image != null) {
                    imageMode(CORNER);
                    image(image, camera.adjustX(x), camera.adjustY(y), w, h); // Display the platform image
                } else {
                    fill(color);
                    noStroke();
                    rect(camera.adjustX(x), camera.adjustY(y), w, h); // Display the colored platform
                }
            }
        }

        boolean isColliding(float cx, float cy, float cw, float ch) {
            return cx < x + w && cx + cw > x && cy < y + h && cy + ch > y;
        }
    }

    boolean isOnGround() {
        for (Platform platform : platforms) {
            if (platform.isColliding(x, y + 1, frameWidth, frameHeight)) {
                return true;
            }
        }
        return false;
    }

    class Wall {
        float x, y, w, h;

        Wall(float x, float y, float w, float h) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
        }

        void display() {
            fill(0);
            noStroke();
            rect(camera.adjustX(x), camera.adjustY(y), w, h);
        }

        boolean isColliding(float cx, float cy, float cw, float ch) {
            return cx < x + w && cx + cw > x && cy < y + h && cy + ch > y;
        }
    }


    class HealthBar {
        float x, y;
        float width, height;
        float maxHealth;
        float currentHealth;

        HealthBar(float x, float y, float width, float height, float maxHealth) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.maxHealth = maxHealth;
            this.currentHealth = maxHealth;
        }



        void display() {
            fill(255, 0, 0); // Black border
            rect(camera.adjustX(x), camera.adjustY(y), width, height);
            fill(0, 255, 0); // Red fill
            float healthWidth = map(currentHealth, 0, maxHealth, 0, width);
            rect(camera.adjustX(x), camera.adjustY(y), healthWidth, height);
        }
    }

}