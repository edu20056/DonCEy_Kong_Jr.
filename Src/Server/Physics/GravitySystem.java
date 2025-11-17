package Physics;

import Entities.Player;
import Utils.Coords;

/**
 * Handles gravity physics for entities in the game.
 * Applies falling mechanics with acceleration and frame-based timing.
 * The system uses a delay mechanism to optimize performance by applying
 * gravity only every few frames while maintaining smooth falling behavior.
 * 
 * @author [Your Name or Team Name]
 * @version 1.1
 * @since 1.0
 */
public class GravitySystem {
    // ===== GRAVITY CONFIGURATION CONSTANTS =====
    
    /**
     * Number of frames to wait between gravity applications.
     * Higher values make gravity feel slower, lower values make it more responsive.
     */
    private static final int GRAVITY_DELAY = 0;
    
    /**
     * Initial falling speed when player first starts falling.
     * Increased from 1.5f to 2.0f for faster initial fall.
     */
    private static final float INITIAL_FALL_SPEED = 2.0f;
    
    /**
     * Maximum falling speed the player can reach.
     * Increased from 3.0f to 4.5f for more dramatic falling.
     */
    private static final float MAX_FALL_SPEED = 5.5f;
    
    /**
     * Rate at which falling speed increases each frame.
     * Increased from 0.1f to 0.15f for quicker acceleration.
     */
    private static final float FALL_ACCELERATION = 1.5f;
    
    // ===== INSTANCE VARIABLES =====
    
    /**
     * Reference to the collision system for validating movements.
     */
    private final CollisionSystem collisionSystem;
    
    /**
     * Counter to track frames between gravity applications.
     */
    private int gravityCounter = 0;
    
    /**
     * Current falling speed of the player, increases with acceleration.
     */
    private float fallSpeed = INITIAL_FALL_SPEED;
    
    // ===== CONSTRUCTOR =====
    
    /**
     * Constructs a GravitySystem with the specified collision system.
     * 
     * @param collisionSystem The collision system used to validate falling movements
     * @throws IllegalArgumentException if collisionSystem is null
     */
    public GravitySystem(CollisionSystem collisionSystem) {
        if (collisionSystem == null) {
            throw new IllegalArgumentException("CollisionSystem cannot be null");
        }
        this.collisionSystem = collisionSystem;
    }
    
    // ===== PUBLIC API =====
    
    /**
     * Applies gravity physics to the player if conditions are met.
     * Only applies gravity every GRAVITY_DELAY frames for performance optimization.
     * Resets falling speed when player is on ground or vine.
     * 
     * @param player The player entity to apply gravity to
     */
    public void applyGravity(Player player) {
        // Skip gravity application if player is ineligible
        if (shouldSkipGravity(player)) {
            resetFallSpeed();
            return;
        }
        
        // Apply gravity only on designated frames (performance optimization)
        if (!shouldApplyThisFrame()) {
            return;
        }
        
        // Apply falling physics or reset speed based on player state
        if (shouldFall(player)) {
            applyFalling(player);
        } else {
            resetFallSpeed();
        }
        
        // Update player state after gravity application
        collisionSystem.updatePlayerState(player, null, null);
    }
    
    // ===== PRIVATE HELPER METHODS =====
    
    /**
     * Determines if gravity should be skipped for the player.
     * Gravity is skipped if player is null, dead, or climbing.
     * 
     * @param player The player to check
     * @return true if gravity should be skipped, false otherwise
     */
    private boolean shouldSkipGravity(Player player) {
        return player == null || player.isDead();
    }
    
    /**
     * Determines if gravity should be applied in the current frame.
     * Uses frame counting to apply gravity only every GRAVITY_DELAY frames.
     * 
     * @return true if gravity should be applied this frame, false otherwise
     */
    private boolean shouldApplyThisFrame() {
        gravityCounter++;
        if (gravityCounter < GRAVITY_DELAY) {
            return false;
        }
        gravityCounter = 0;
        return true;
    }
    
    /**
     * Determines if the player should be falling.
     * Player falls when not on ground and not on vine.
     * 
     * @param player The player to check
     * @return true if player should fall, false otherwise
     */
    private boolean shouldFall(Player player) {
        return !player.isOnGround() && !player.isOnVine();
    }
    
    /**
     * Applies falling physics to the player.
     * Increases fall speed with acceleration up to maximum, then moves player downward.
     * 
     * @param player The player to apply falling physics to
     */
    private void applyFalling(Player player) {
        // Increase fall speed with acceleration, capped at maximum
        fallSpeed = Math.min(fallSpeed + FALL_ACCELERATION, MAX_FALL_SPEED);
        
        // Calculate position below player
        Coords below = new Coords(player.getPosition().getX(), 
                                 player.getPosition().getY() + 1);
        
        // Move player downward if the position is valid
        if (collisionSystem.canMoveTo(below)) {
            player.setPosition(below);
        }
    }
    
    /**
     * Resets the falling speed to initial value.
     * Called when player lands on ground or grabs a vine.
     */
    private void resetFallSpeed() {
        fallSpeed = INITIAL_FALL_SPEED;
    }
    
    // ===== DEBUG/UTILITY METHODS =====
    
    /**
     * Gets the current fall speed for debugging purposes.
     * 
     * @return The current falling speed value
     */
    public float getCurrentFallSpeed() {
        return fallSpeed;
    }
    
    /**
     * Gets the gravity delay value for debugging purposes.
     * 
     * @return The gravity delay in frames
     */
    public int getGravityDelay() {
        return GRAVITY_DELAY;
    }
}
