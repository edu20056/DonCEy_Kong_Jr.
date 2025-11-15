package Physics;
import java.util.*;

import World.World;
import World.TileType;
import Entities.Player;
import Utils.Coords;
import Entities.Coco;
import Entities.RedCoco;
import Entities.BlueCoco;
import Entities.Fruit;

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

    public boolean checkCocodriloCollision(Player player, List<Coco> cocodrilos) {
        if (player == null || player.isDead() || cocodrilos == null) {
            return false;
        }
        
        Coords playerPos = player.getPosition();
        
        for (Coco cocodrilo : cocodrilos) {
            if (cocodrilo.isActivo() && playerPos.equals(cocodrilo.getPosition())) {
                System.out.println("¡Colisión con cocodrilo " + cocodrilo.getTipo() + "!");
                return true;
            }
        }
        return false;
    }

    public boolean checkFruitCollision(Player player, List<Fruit> frutas) {
        if (player == null || player.isDead() || frutas == null) {
            return false;
        }

        Coords playerPos = player.getPosition();
        
        Iterator<Fruit> iterator = frutas.iterator();
        while (iterator.hasNext()) {
            Fruit fruta = iterator.next();
            if (fruta.isActiva() && playerPos.equals(fruta.getPosition())) {
                player.addPoints(fruta.getPuntos());
                System.out.println("¡Fruta recolectada! +" + fruta.getPuntos() + " puntos");
                iterator.remove(); // Eliminar la fruta recolectada
                return true;
            }
        }
        return false;
    }

    // Método principal actualizado
    public void updatePlayerState(Player player, List<Coco> cocodrilos, List<Fruit> frutas) {
        if (player == null) return;
    
        Coords playerPos = player.getPosition();
    
        // 1. Verificar si está en una enredadera
        boolean onVine = isOnVine(playerPos);
        player.setOnVine(onVine);
    
        // 2. Verificar si está en el suelo
        boolean onGround = false;
        if (world.isWithinBounds(playerPos)) {
            Coords below = new Coords(playerPos.getX(), playerPos.getY() + 1);
            if (world.isWithinBounds(below)) {
                TileType tileBelow = world.getTile(below);
                onGround = tileBelow.isSolid();
            }
        }

        player.setOnGround(onGround);
    
        // 3. Verificar muerte por tiles mortales (agua, etc.)
        if (world.isWithinBounds(playerPos)) {
            TileType currentTile = world.getTile(playerPos);
            if (currentTile.isDeadly() && !player.isDead()) {
                player.die();
                System.out.println("¡Jugador murió por: " + currentTile + "!");
                return; // Si murió por tile, no verificar cocodrilos
            }
        }
        
        // 4. Verificar colisión con cocodrilos
        if (checkCocodriloCollision(player, cocodrilos) && !player.isDead()) {
            player.die();
            System.out.println("¡Jugador murió por cocodrilo!");
            return; // Si murió por cocodrilo, no verificar frutas
        }

        // 5. Verificar colisión con frutas
        checkFruitCollision(player, frutas);
    }
    
    // Método sobrecargado para compatibilidad con cocodrilos solamente
    public void updatePlayerState(Player player, List<Coco> cocodrilos) {
        updatePlayerState(player, cocodrilos, null);
    }
    
    // Método sobrecargado para mantener compatibilidad con código existente
    public void updatePlayerState(Player player) {
        updatePlayerState(player, null, null);
    }
}
