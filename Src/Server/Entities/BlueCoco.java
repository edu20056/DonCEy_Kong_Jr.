package Entities;

import Utils.Coords;
import World.World;
import World.TileType;

/**
 * Blue cocodrile enemy that moves vertically on vines and falls when reaching the end.
 * This cocodrile starts on a vine, moves downward until the vine ends, then falls to the ground.
 */

public class BlueCoco extends Coco {
    private boolean onVine = true;
    private boolean falling = false;
    
    /**
     * Constructs a new BlueCoco at the specified coordinates with custom speed.
     * 
     * @param x The initial X coordinate in the game world
     * @param y The initial Y coordinate in the game world
     * @param movementSpeed The movement speed (higher = faster)
     */

    public BlueCoco(int x, int y, int movementSpeed) {
        super(x, y, "AZUL", movementSpeed);
    }

    /**
     * Gets the facing direction of the blue cocodrile.
     * Blue cocodriles always face downward during movement.
     * 
     * @return true indicating the cocodrile is facing downward
     */

    @Override
    public boolean getIsFacingDown() {
        return true;
    }
    
    /**
     * Updates the blue cocodrile's state and position.
     * Handles vine movement, falling behavior, and deactivation.
     * The cocodrile moves downward on vines, falls when vines end,
     * and deactivates when it reaches solid ground.
     * 
     * @param world The game world for collision detection and tile checking
     */

    @Override
    public void update(World world) {
        if (!isActive()) return;
        
        if (!shouldMove()) {
            return;
        }
        
        if (onVine) {
            Coords below = new Coords(getX(), getY() + 1);
            
            if (canMoveTo(below, world) && world.getTile(below) == TileType.VINE) {
                setPosition(below);
            } else {
                onVine = false;
                falling = true;
            }
        } else if (falling) {
            Coords below = new Coords(getX(), getY() + 1);
            
            if (canMoveTo(below, world)) {
                setPosition(below);
                
                Coords belowBelow = new Coords(getX(), getY() + 1);
                if (!canMoveTo(belowBelow, world)) {
                    falling = false;
                }
            } else {
                falling = false;
            }
        }
  
        if (!onVine && !falling) {
            setActive(false);
        }
    }
}
