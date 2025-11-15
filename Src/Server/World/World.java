package World;

import Utils.Coords;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class World {
    private final Map<Coords, TileType> tiles;
    private int width;
    private int height;
    
    public World(String filename) {
        tiles = new HashMap<>();
        loadWorldFromFile(filename);
    }
    
    private void loadWorldFromFile(String filename) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            int y = 0;
            
            while ((line = reader.readLine()) != null) {
                if (width == 0) {
                    width = line.length();
                }
                
                for (int x = 0; x < line.length(); x++) {
                    char symbol = line.charAt(x);
                    TileType tileType = TileType.fromChar(symbol);
                    Coords coords = new Coords(x, y);
                    tiles.put(coords, tileType);
                }
                y++;
            }
            height = y;
            
            System.out.println("World loaded: " + width + "x" + height);
            
        } catch (IOException e) {
            System.err.println("Error loading world file: " + e.getMessage());
        }
    }
    
    public boolean isWithinBounds(Coords coords) {
        return coords.getX() >= 0 && coords.getX() < width && 
               coords.getY() >= 0 && coords.getY() < height;
    }
    
    public TileType getTile(Coords coords) {
        return tiles.getOrDefault(coords, TileType.EMPTY);
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }
}
