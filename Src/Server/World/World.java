package World;

import Utils.Coords;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents the game world composed of tiles arranged in a 2D grid.
 * The world is loaded from a text file where each character represents a tile type.
 */

public class World {
    private final Map<Coords, TileType> grid;
    private int width;
    private int height;
    
    /**
     * Constructs a new World by loading tile data from the specified file.
     *
     * @param filename the path to the file containing world data
     */

    public World(String filename) {
        grid = new HashMap<>();
        loadWorldFromFile(filename);
    }

    // --- GETTERS ---
    
    public int getWidth() { return width; }
    public int getHeight() { return height; }


    /**
     * Loads world data from a text file where each character represents a tile.
     *
     * @param filename the path to the world file
     */

    private void loadWorldFromFile(String filename) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            int y = 0;
            
            while ((line = reader.readLine()) != null) {
                if (width == 0) { width = line.length(); }
                
                for (int x = 0; x < line.length(); x++) {
                    char symbol = line.charAt(x);
                    TileType tileType = TileType.fromChar(symbol);
                    Coords coords = new Coords(x, y);
                    grid.put(coords, tileType);
                }
                y++;
            }
            height = y;
            
        } catch (IOException e) {
            System.err.println("Error loading world file: " + e.getMessage());
        }
    }
    
    /**
     * Checks if the given coordinates are within the world boundaries.
     *
     * @param coords the coordinates to check
     * @return true if coordinates are within bounds, false otherwise
     */

    public boolean isWithinBounds(Coords coords) {
        return coords.getX() >= 0 && coords.getX() < width && 
               coords.getY() >= 0 && coords.getY() < height;
    }
    
    /**
     * Gets the tile type at the specified coordinates.
     * Returns EMPTY if coordinates are out of bounds or tile not found.
     *
     * @param coords the coordinates to query
     * @return the TileType at specified coordinates, or EMPTY if not found/out of bounds
     */

    public TileType getTile(Coords coords) {
        return grid.getOrDefault(coords, TileType.EMPTY);
    }
}
