package Physics;

import Entities.Player;
import Utils.Coords;

public class GravitySystem {
    private CollisionSystem collisionSystem;
    
    public GravitySystem(CollisionSystem collisionSystem) {
        this.collisionSystem = collisionSystem;
    }
    
    public void applyGravity(Player player) {
        // No aplicar gravedad si está en el suelo o trepando
        if (player.isOnGround() || player.isClimbing()) {
            return;
        }
        
        // Aplicar gravedad (caer 1 posición hacia abajo)
        Coords currentPos = player.getPosition();
        Coords newPos = new Coords(currentPos.getX(), currentPos.getY() + 1);
        
        if (collisionSystem.canMoveTo(newPos)) {
            player.setPosition(newPos);
        }
        
        // Actualizar estado después de la gravedad
        collisionSystem.updatePlayerState(player);
    }
}
