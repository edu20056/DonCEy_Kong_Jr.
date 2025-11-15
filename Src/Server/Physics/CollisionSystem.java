package Physics;

import World.World;
import World.TileType;
import Entities.Player;
import Utils.Coords;

public class CollisionSystem {
    private final World world;
    
    public CollisionSystem(World world) {
        this.world = world;
    }
    
    public boolean canMoveTo(Coords coords) {
        if (!world.isWithinBounds(coords)) {
            return false;
        }
        
        TileType tile = world.getTile(coords);
        return !tile.isSolid();
    }
    
    public boolean isOnLadder(Coords coords) {
        if (!world.isWithinBounds(coords)) {
            return false;
        }
        
        TileType tile = world.getTile(coords);
        return tile.isClimbable();
    }
    
    public boolean isOnVine(Coords coords) {
        if (!world.isWithinBounds(coords)) {
            return false;
        }
        
        TileType tile = world.getTile(coords);
        return tile == TileType.VINE;
    }

    public void updatePlayerState(Player player) {
        if (player == null) return;
    
        Coords playerPos = player.getPosition();
    
        // 1. Verificar si está en una enredadera
        boolean onVine = isOnVine(playerPos);
        player.setOnVine(onVine);
    
        // 2. Verificar si está en el suelo - método más robusto
        boolean onGround = false;
        if (world.isWithinBounds(playerPos)) {
            // Verificar el tile directamente debajo del jugador
            Coords below = new Coords(playerPos.getX(), playerPos.getY() + 1);
            if (world.isWithinBounds(below)) {
                TileType tileBelow = world.getTile(below);
                onGround = tileBelow.isSolid();
            }
        }

        player.setOnGround(onGround);
    
        // 3. Verificar muerte
        if (world.isWithinBounds(playerPos)) {
            TileType currentTile = world.getTile(playerPos);
            if (currentTile.isDeadly() && !player.isDead()) {
                player.die();
                System.out.println("¡Jugador murió por: " + currentTile + "!");
            }
        }
    }
}
