package Entities;

import Utils.Coords;
import World.World;
import World.TileType;

/**
 * Red cocodrile enemy that moves vertically on vines in a patrolling pattern.
 * This cocodrile moves up and down on vines, changing direction after reaching
 * a maximum distance or when encountering obstacles.
 */

public class RedCoco extends Coco {
    private boolean movingUp = true;
    private int distanceTraveled = 0;

    public RedCoco(int x, int y, int movementSpeed) {
        super(x, y, "ROJO", movementSpeed);
    }
    
    // --- GETTERS ---

    public boolean isMovingUp() { return movingUp; }
    public int getDistanceTraveled() { return distanceTraveled; }
    
    /**
     * Gets the facing direction of the red cocodrile.
     * Red cocodriles face downward when moving down, upward when moving up.
     * 
     * @return true if facing downward, false if facing upward
     */

    @Override
    public boolean getIsFacingDown() {
        return !movingUp;
    }

    /**
     * Updates the red cocodrile's state and position.
     * Handles vertical patrolling movement on vines, direction changes,
     * and deactivation when leaving vines.
     * 
     * @param world The game world for collision detection and tile checking
     */

    @Override
    public void update(World world) {
        if (!isActive()) return;
        
        // Only move based on speed timing
        if (!shouldMove()) {
            return;
        }
        
        // Verify it's still on a vine
        if (!isOnVine(world)) {
            setActive(false);
            return;
        }
        
        // Determine movement direction
        int directionY = movingUp ? -1 : 1;
        Coords newPos = new Coords(getX(), getY() + directionY);
        
        // Verify if it can move and if the new position is a vine
        if (canMoveTo(newPos, world) && world.getTile(newPos) == TileType.VINE) {
            setPosition(newPos);
            distanceTraveled++;
            
        } else {
            // If cannot move, change direction
            movingUp = !movingUp;
            distanceTraveled = 0;
        }
    }

    private int getFramesBetweenMoves() {
        switch (movementSpeed) {
            case 1: return 7;
            case 2: return 5;
            case 3: return 3;
            case 4: return 2;
            case 5: return 1;
            default: return 0;
        }
    }
}
