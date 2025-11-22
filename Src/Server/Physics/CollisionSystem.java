package Physics;

import World.World;
import World.TileType;
import Entities.Player;
import Utils.Coords;
import Entities.Coco;
import Entities.Fruit;

import java.util.*;

/**
 * Handles all collision detection and resolution in the game.
 * Manages collisions between entities and the game world.
 */

public class CollisionSystem {
    private final World world;
    
    public CollisionSystem(World world) {
        this.world = world;
    }

    /**
     * Check win condition requirements.
     * Goal is reach.
     *
     * @param player extract players coords to check for collision
     * @return true if collision with goal, false otherwise.
     */
     
     public boolean checkWinCollision(Player player) {
        Coords playerPos = player.getPosition();

        return world.isWithinBounds(playerPos) && world.getTile(playerPos).isGoal();
    }

    /**
     * Checks if a position is valid for movement.
     */

    public boolean canMoveTo(Coords coords) {
        return world.isWithinBounds(coords) && !world.getTile(coords).isSolid();
    }
    
    /**
     * Checks if a position contains a vine tile.
     */
    
    public boolean isOnVine(Coords coords) {
        return world.isWithinBounds(coords) && world.getTile(coords) == TileType.VINE;
    }
    
    /**
     * Checks if a position contains a deadly tile.
     */
    
    public boolean isDeadlyTile(Coords coords) {
        return world.isWithinBounds(coords) && world.getTile(coords).isDeadly();
    }
    
    /**
     * Checks if a position is above solid ground.
     */
    
    public boolean isOnGround(Coords coords) {
        if (!world.isWithinBounds(coords)) return false;
        
        Coords below = new Coords(coords.getX(), coords.getY() + 1);
        return world.isWithinBounds(below) && world.getTile(below).isSolid();
    }

    /**
     * Comprehensive player state update with all collision checks.
     * Uses null-safe collections to avoid overloaded methods.
     */

    public void updatePlayerState(Player player, List<Coco> cocodrilos, List<Fruit> fruits) {
        if (player == null || player.isDead()) return;
    
        Coords playerPos = player.getPosition();
        
        // Update environmental states
        player.setOnVine(isOnVine(playerPos));
        player.setOnGround(isOnGround(playerPos));

        // Check for fatal collisions (stop if player dies)
        if (checkFatalCollisions(player, playerPos, cocodrilos)) {
            player.die();
            return;
        }
        
        // Check for collectibles (non-fatal)
        checkCollectibleCollisions(player, playerPos, fruits);
    }
    
    /**
     * Checks for collisions that can kill the player.
     * 
     * @return true if player died.
     */

    private boolean checkFatalCollisions(Player player, Coords playerPos, List<Coco> cocodrilos) {
        if (isDeadlyTile(playerPos)) {
            return true;
        }

        if (cocodrilos != null) {
            for (Coco cocodrilo : cocodrilos) {
                if (cocodrilo.isActive() && playerPos.equals(cocodrilo.getPosition())) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    /**
     * Checks for collectible collisions
     */

    private void checkCollectibleCollisions(Player player, Coords playerPos, List<Fruit> fruits) {
        if (fruits == null) return;
        
        Iterator<Fruit> iterator = fruits.iterator();
        while (iterator.hasNext()) {
            Fruit fruit = iterator.next();
            if (fruit.isActive() && playerPos.equals(fruit.getPosition())) {
                player.addPoints(fruit.getPoints());
                fruit.collect();
                iterator.remove();
            }
        }
    }
}
