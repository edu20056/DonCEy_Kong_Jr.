package Physics;

import Entities.Player;
import Utils.Coords;

public class GravitySystem {
    private final CollisionSystem collisionSystem;
    
    public GravitySystem(CollisionSystem collisionSystem) {
        this.collisionSystem = collisionSystem;
    }
    
    public void applyGravity(Player player) {
        if (player == null || player.isDead() || player.isClimbing()) {
            return;
        }
        
        // Solo aplicar gravedad si no está en el suelo y no está en una liana
        if (!player.isOnGround() && !player.isOnVine()) {
            Coords below = new Coords(player.getPosition().getX(), player.getPosition().getY() + 1);
            if (collisionSystem.canMoveTo(below)) {
                player.setPosition(below);
            }
        }
        
        // Actualizar estado después de aplicar gravedad
        collisionSystem.updatePlayerState(player);
    }
}
