package Entities;

import Utils.Coords;

/**
* Represents the main player character in the game, 
 * this class handles player positioning, directional facing, etc...
 */

public class Player extends Entity {
    private final int JUMP_DURATION = 4;
    final private int JUMP_STRENGTH = 3;
    private boolean facingRight;
    private boolean onGround;
    private boolean onVine;
    private boolean dead;
    private int points;
    private int lives;
    private boolean isJumping = false;
    private Coords jumpStartPosition;
    private Coords jumpTargetPosition;
    private int jumpProgress = 0;

    /**
     * Constructs a new Player entity at the specified coordinates.
     * Initializes with default state: facing right, alive, on ground with zero points.
     * 
     * @param x The initial X coordinate in the game world
     * @param y The initial Y coordinate in the game world
     */

    public Player(int x, int y) {
        super(x, y);
        this.facingRight = true;
        this.onGround = false;
        this.onVine = false;
        this.dead = false;
        this.points = 0;
        this.lives = 3;
    }

    // --- GETTERS AND SETTERS --- //

    public boolean isFacingRight() { return facingRight; }
    public boolean isOnGround() { return onGround; }
    public boolean isOnVine() { return onVine; }
    public boolean isDead() { return dead; }
    public int getPoints() { return points; }
    public int getLives() { return lives; }
    public boolean isJumping() { return isJumping; }

    public void setOnGround(boolean onGround) { this.onGround = onGround; }
    public void setOnVine(boolean onVine) { this.onVine = onVine; }
    public void setPoints(int pts) { this.points = pts; }
    public void setIsDead(boolean deadBool) { this.dead = deadBool; }
    public void setLives(int newLives) { this.lives = newLives; }
    public void incLives() { this.lives++; }
    public void decLives() { this.lives--; }

    /**
     * These methods calculate potential movements without validation
     * Used by external systems to check collision before applying movement
     * 
     * @return Coordinates representing the position after moving
     */

    public Coords calculateMoveLeft() {
        return new Coords(getX() - 1, getY());
    }
    public Coords calculateMoveRight() {
        return new Coords(getX() + 1, getY());
    }
    public Coords calculateMoveUp() {
        return new Coords(getX(), getY() - 1);
    }
    public Coords calculateMoveDown() {
        return new Coords(getX(), getY() + 1);
    }
    public Coords[] calculateJumpPositions() {
        Coords[] jumpPositions = new Coords[JUMP_STRENGTH];

        for (int i = 0; i < JUMP_STRENGTH; i++) {
            jumpPositions[i] = new Coords(getX(), getY() - (i + 1));
        }

        return jumpPositions;
    }

    /**
     * These methods apply validated movements to the player
     * Only processes movement if the player is alive.
     * 
     * @param newPos/jumpPos The validated new position to move to
     * @param moveRight The direction faced after movement
     */

    public void applyMovement(Coords newPos, boolean moveRight) {
        if (!dead) {
            setPosition(newPos);
            facingRight = moveRight;
        }
    }
   public void applyJump(Coords target) {
        if (!isJumping && isOnGround()) {
            isJumping = true;
            jumpStartPosition = new Coords(position.getX(), position.getY());
            jumpTargetPosition = target;
            jumpProgress = 0;
        }
    }
    
    /**
     * Updates player movement while jumping movement.
     */

    public void updateJump() {
        if (!isJumping) return;
        
        jumpProgress++;
        
        if (jumpProgress >= JUMP_DURATION) {
            position = new Coords(jumpTargetPosition.getX(), jumpTargetPosition.getY());
            isJumping = false;
            return;
        }
        
        int currentX = jumpStartPosition.getX();
        int currentY = jumpStartPosition.getY() - jumpProgress;
        
        position = new Coords(currentX, currentY);
    }

    public void cancelJump() {
        isJumping = false;
    }    
    
    /**
     * Kills the player, setting the dead state to true.
     */

    public void die() {
        if (!dead) {
            dead = true;
        }
    }
    
    /**
     * Respawns the player at the specified position with reset state.
     * Resets all state variables to their initial values.
     * 
     * @param spawnPoint The coordinates where the player should respawn
     */

    public void respawn(Coords spawnPoint) {
        setPosition(spawnPoint);
        dead = false;
        onGround = false;
        onVine = false;
        facingRight = true;
    }
    
    /**
     * Adds points to the player's current score.
     * 
     * @param pts The number of points to add to the current score
     */

    public void addPoints(int pts) {
        this.points += pts;
    }
    
    /**
     * Updates the player's environmental interaction states.
     * Typically called by collision detection systems.
     * 
     * @param onGround Whether the player is standing on solid ground
     * @param onVine Whether the player is standing on a climbable vine
     */

    public void updateEnvironmentalState(boolean onGround, boolean onVine) {
        this.onGround = onGround;
        this.onVine = onVine;
    }
}
