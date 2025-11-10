package Physics;

import World.World;
import World.TileType;
import Utils.Coords;
import Entities.Player;

public class CollisionSystem {
    private World world;
    
    public CollisionSystem(World world) {
        this.world = world;
    }
    
    public boolean canMoveTo(Coords position) {
        if (!world.isWithinBounds(position)) {
            return false;
        }
        
        TileType tile = world.getTile(position);
        return !tile.isSolid();
    }
    
    public boolean isOnLadder(Coords position) {
        if (!world.isWithinBounds(position)) return false;
        TileType tile = world.getTile(position);
        return tile == TileType.VINE;
    }
    
    public boolean isOnGround(Coords position) {
        Coords below = new Coords(position.getX(), position.getY() + 1);
        if (!world.isWithinBounds(below)) return false;
        
        TileType tileBelow = world.getTile(below);
        return tileBelow.isSolid() || tileBelow == TileType.PLATFORM;
    }
    
    public boolean isInWater(Coords position) {
        if (!world.isWithinBounds(position)) return false;
        TileType tile = world.getTile(position);
        return tile == TileType.WATER;
    }
    
    public void updatePlayerState(Player player) {
        Coords pos = player.getPosition();
        
        if (isInWater(pos)) {
            player.die();
            return;
        }
        
        boolean onGround = isOnGround(pos);
        boolean onLadder = isOnLadder(pos);
        
        player.setOnGround(onGround);
        player.setOnVine(onLadder);
        
        if (!onLadder) {
            player.setClimbing(false);
        }
    }
}
