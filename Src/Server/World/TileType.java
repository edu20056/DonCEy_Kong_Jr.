package World;

/**
 * Types of tiles in the game world.
 * Each tile type has specific properties that affect gameplay.
 */

public enum TileType {
    EMPTY('_', false, false, false),
    VINE('H', false, false, true),
    PLATFORM('=', true, false, true),
    WATER('~', false, true, false),
    GOAL('X', false, false, false);
    
    // Tile properties
    private final char symbol;        // Character representation in map files
    private final boolean isSolid;    // Blocks entity movement if true
    private final boolean isDeadly;   // Causes instant death on contact if true  
    private final boolean isClimbable; // Allows vertical movement if true
    
    /**
     * Constructs a TileType with specified properties.
     *
     * @param symbol character that represents this tile in map grid files
     * @param isSolid determines if entities can pass through this tile
     * @param isDeadly kills player on contact if true
     * @param isClimbable allows player to move vertically when on this tile
     */

    TileType(char symbol, boolean isSolid, boolean isDeadly, boolean isClimbable) {
        this.symbol = symbol;
        this.isSolid = isSolid;
        this.isDeadly = isDeadly;
        this.isClimbable = isClimbable;
    }
    
    // --- GETTER METHODS ---
    
    public char getSymbol() { return symbol; }
    public boolean isSolid() { return isSolid; }
    public boolean isDeadly() { return isDeadly; }
    public boolean isClimbable() { return isClimbable; }
    
    
    /**
     * Converts a character to its corresponding TileType.
     *
     * @param c the character to convert to a TileType
     * @return the TileType associated with the character, or EMPTY if no match found
     */

    public static TileType fromChar(char c) {
        for (TileType type : values()) {
            if (type.symbol == c) {
                return type;
            }
        }
        return EMPTY;
    }
}
