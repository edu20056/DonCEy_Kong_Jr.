package Entities;

import Utils.Coords;
import World.World;
import World.TileType;

/**
 * Abstract base class for all cocodrile enemies in the game.
 * Handles basic movement, activation state, and world interaction.
 */

public abstract class Coco extends Entity {
    protected boolean active;
    protected String type;
    protected boolean isFacingDown;
    protected int movementSpeed;
    protected int movementCounter;

    /**
     * Constructs a new Coco enemy at the specified coordinates.
     * 
     * @param x The initial X coordinate in the game world
     * @param y The initial Y coordinate in the game world
     * @param type The type identifier for this cocodrile
     */

    public Coco(int x, int y, String type, int movementSpeed) {
        super(x, y);
        this.active = true;
        this.type = type;
        this.isFacingDown = true;
        this.movementSpeed = movementSpeed;
        this.movementCounter = 0;
    }
    
    /**
     * Abstract method to update the cocodrile's state and position.
     * 
     * @param world The game world for collision detection and tile checking
     */

    public abstract void update(World world);
    
    /**
     * Gets the current facing direction of the cocodrile.
     * 
     * @return true if facing downward, false if facing upward
     */

    public abstract boolean getIsFacingDown();

    // --- GETTERS AND SETTERS ---

    public boolean isActive() { return active; }
    public String getType() { return type; }
    public void setActive(boolean active) { this.active = active; }
    public int getMovementSpeed() { return movementSpeed; }
    public void setMovementSpeed(int movementSpeed) { this.movementSpeed = movementSpeed; }

    /**
     * Determines whether the coco object should 
     * move to the next position.
     */

    protected boolean shouldMove() {
        movementCounter++;
        
        int framesBetweenMoves;
        switch (movementSpeed) {
            case 1: framesBetweenMoves = 7; break;
            case 2: framesBetweenMoves = 5; break;
            case 3: framesBetweenMoves = 3; break;
            case 4: framesBetweenMoves = 2; break;
            case 5: framesBetweenMoves = 1; break;
            default: framesBetweenMoves = 0; break;
        }
        
        if (movementCounter >= framesBetweenMoves) {
            movementCounter = 0;
            return true;
        }
        return false;
    }

    /**
     * Checks if the cocodrile is currently standing on a vine tile.
     * 
     * @param world The game world to check tile types
     * @return true if the current position contains a vine tile
     */
    
    protected boolean isOnVine(World world) {
        return world.getTile(getPosition()) == TileType.VINE;
    }
    
    /**
     * Checks if the cocodrile can move to the specified coordinates.
     * Verifies world bounds and tile solidity.
     * 
     * @param coords The target coordinates to check
     * @param world The game world for bounds and tile checking
     * @return true if the position is within bounds and not solid
     */

    protected boolean canMoveTo(Coords coords, World world) {
        if (!world.isWithinBounds(coords)) {
            return false;
        }
        TileType tile = world.getTile(coords);
        return !tile.isSolid();
    }
}
