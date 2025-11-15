// Entities/Entity.java
package Entities;

import Utils.Coords;

public abstract class Entity {
    protected Coords position;
    
    public Entity(int x, int y) {
        this.position = new Coords(x, y);
    }
    
    public Coords getPosition() { return position; }
    public void setPosition(Coords position) { this.position = position; }
    public int getX() { return position.getX(); }
    public int getY() { return position.getY(); }
}
