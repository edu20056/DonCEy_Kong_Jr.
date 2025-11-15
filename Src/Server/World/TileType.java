package World;

/**
 * Types of tiles in the game world.
 *
 * Each tile type has specific properties that affect gameplay.
 * Such as:
 *
 * @param symbol      Char that represents the tile in map gird
 * @param isSolid     Availability of the player to pass through the object
 * @param isDeadly    Kills player on contact
 * @param isClimbable Availability of the player to move verticality 
 */

public enum TileType {
    EMPTY('_', false, false, false),
    VINE('H', false, false, true),
    PLATFORM('=', true, false, true),
    WATER('~', false, true, false),
    GOAL('X', false, false, false);
    
    private final char symbol;
    private final boolean isSolid;
    private final boolean isDeadly;
    private final boolean isClimbable;
    
    TileType(char symbol, boolean isSolid, boolean isDeadly, boolean isClimbable) {
        this.symbol = symbol;
        this.isSolid = isSolid;
        this.isDeadly = isDeadly;
        this.isClimbable = isClimbable;
    }
    
    public char getSymbol() { return symbol; }
    public boolean isSolid() { return isSolid; }
    public boolean isDeadly() { return isDeadly; }
    public boolean isClimbable() { return isClimbable; }
    
    public static TileType fromChar(char c) {
        for (TileType type : values()) {
            if (type.symbol == c) {
                return type;
            }
        }
        return EMPTY;
    }
}
