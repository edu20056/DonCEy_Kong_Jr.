package Physics;

import Entities.Player;
import Utils.Coords;

public class GravitySystem {
    private final CollisionSystem collisionSystem;
    private int gravityCounter = 0;
    private final int GRAVITY_DELAY = 2; // Frames entre cada aplicación de gravedad
    private float fallSpeed = 0.5f; // Velocidad inicial de caída
    private final float MAX_FALL_SPEED = 2.0f; // Velocidad máxima
    
    public GravitySystem(CollisionSystem collisionSystem) {
        this.collisionSystem = collisionSystem;
    }
    
    public void applyGravity(Player player) {
        if (player == null || player.isDead() || player.isClimbing()) {
            return;
        }
        
        // Solo aplicar gravedad cada X frames
        gravityCounter++;
        if (gravityCounter < GRAVITY_DELAY) {
            return;
        }
        gravityCounter = 0;
        
        if (!player.isOnGround() && !player.isOnVine()) {
            // Aumentar gradualmente la velocidad de caída
            fallSpeed = Math.min(fallSpeed + 0.1f, MAX_FALL_SPEED);
            
            Coords below = new Coords(player.getPosition().getX(), player.getPosition().getY() + 1);
            if (collisionSystem.canMoveTo(below)) {
                player.setPosition(below);
            }
        } else {
            // Resetear velocidad cuando toca el suelo
            fallSpeed = 0.5f;
        }
        
        collisionSystem.updatePlayerState(player);
    }
}
