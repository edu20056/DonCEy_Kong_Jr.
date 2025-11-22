// Entities/Fruit.java
package Entities;

import Utils.Coords;

/**
 * Represents a collectible fruit item in the game world.
 * Fruits provide points when collected by the player and can have different types
 * with varying point values. Each fruit has an active state that determines
 * whether it can be collected.
 */

public class Fruit extends Entity {
    private boolean active;
    private int points;
    private final String type;
    
    /**
     * Constructs a new Fruit at the specified coordinates with custom point value.
     * 
     * @param x The X coordinate in the game world
     * @param y The Y coordinate in the game world
     * @param type The type identifier for this fruit
     * @param points The point value awarded when collected
     */

    public Fruit(int x, int y, String type, int points) {
        super(x, y);
        this.active = true;
        this.type = type;
        this.points = points;
    }
    
    /**
     * Constructs a new Fruit at the specified coordinates with default point value based on type.
     * The point value is automatically determined by the fruit type.
     * 
     * @param x The X coordinate in the game world
     * @param y The Y coordinate in the game world
     * @param type The type identifier for this fruit
     */

    public Fruit(int x, int y, String type) {
        this(x, y, type, getPointsByType(type));
    }
    
    /**
     * Determines the default point value for a given fruit type.
     * 
     * @param type The fruit type identifier
     * @return The default point value for the specified type
     */

    private static int getPointsByType(String type) {
        switch (type.toUpperCase()) {
            case "BANANA": return 100;
            case "STRAWBERRY": return 25;
            case "ORANGE": return 50;
            default: return 10;
        }
    }

    // --- GETTERS AND SETTERS --- //

    public void setPoints(int pts) { this.points = pts;  }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public int getPoints() { return points; }
    public String getType() { return type; }
    public void collect() { this.active = false; }
}
