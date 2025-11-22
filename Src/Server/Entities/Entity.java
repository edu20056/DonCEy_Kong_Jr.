package Entities;

import Utils.Coords;

/**
 * Abstract base class for all entities in the game world.
 * Common to all game entities such as players, enemies, and items.
 */

public abstract class Entity {
    
    /**
     * Holds the current position of the entity in the game world coordinates.
     * Uses a Coords object to represent the (x, y) position.
     */

    protected Coords position;
    public Entity(int x, int y) {
        this.position = new Coords(x, y);
    }
    
    // --- GETTERS AND SETTERS --- //
    
    public Coords getPosition() { return position; }
    public void setPosition(Coords position) { this.position = position; }
    public int getX() { return position.getX(); }
    public int getY() { return position.getY(); }
}
