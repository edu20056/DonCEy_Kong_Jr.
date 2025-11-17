package Utils;

import java.util.Objects;

/**
 * Represents a pair of two-dimensional coordinates (x, y)
 * use in game grid. 
 */

public class Coords {
    private final int x;
    private final int y;
    
    /**
     * Constructs a new coordinate with the specified values.
     * 
     * @param x Coordinate on the horizontal axis
     * @param y Coordinate on the vertical axis
     */

    public Coords(int x, int y) {
        this.x = x;
        this.y = y;
    }
    

    // --- GETTERS ---

    public int getX() { return x; }
    public int getY() { return y; }

    /**
     * Compares this coordinate to the specified object for equality.
     * Two coordinates are equal if they have the same x and y values.
     * 
     * @param obj The object to compare with this coordinate
     * @return true if the objects are equal, false otherwise
     */
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Coords coords = (Coords) obj;
        return x == coords.x && y == coords.y;
    }

    /**
     * Calculates the hash code for this coordinate.
     * 
     * @return The calculated hash code for this coordinate
     */

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
    
}
