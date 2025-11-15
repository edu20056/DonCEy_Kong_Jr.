// Entities/Player.java
package Entities;

import Utils.Coords;
import Physics.CollisionSystem;
import Physics.GravitySystem;

public class Player {
    private Coords position;
    private boolean facingRight;
    private boolean onGround;
    private boolean climbing;
    private boolean onVine;
    private boolean dead;
    
    public Player(int x, int y) {
        this.position = new Coords(x, y);
        this.facingRight = true;
        this.onGround = false;
        this.climbing = false;
        this.onVine = false;
        this.dead = false;
    }
    
    public void moveLeft(CollisionSystem collision) {
        if (dead) return;
        Coords newPos = new Coords(position.getX() - 1, position.getY());
        if (collision.canMoveTo(newPos) || (climbing && collision.isOnLadder(newPos))) {
            position = newPos;
            facingRight = false;
        }
        collision.updatePlayerState(this);
    }
    
    public void moveRight(CollisionSystem collision) {
        if (dead) return;
        Coords newPos = new Coords(position.getX() + 1, position.getY());
        if (collision.canMoveTo(newPos) || (climbing && collision.isOnLadder(newPos))) {
            position = newPos;
            facingRight = true;
        }
        collision.updatePlayerState(this);
    }
    
    public void moveUp(CollisionSystem collision) {
        if (dead) return;
        // Permitir movimiento hacia arriba si está escalando O si está en una enredadera
        if (climbing || onVine) {
            Coords newPos = new Coords(position.getX(), position.getY() - 1);
            if (collision.canMoveTo(newPos)) {
                position = newPos;
            }
        }
    }

    public void jump(GravitySystem gravity, CollisionSystem collision) {
        if (dead) return;

        if (onGround || onVine) {
            // Salto de 2 bloques de altura
            Coords jumpPos1 = new Coords(position.getX(), position.getY() - 1);
            Coords jumpPos2 = new Coords(position.getX(), position.getY() - 2);
        
            if (collision.canMoveTo(jumpPos1) && collision.canMoveTo(jumpPos2)) {
                position = jumpPos2; // Salto alto
            } else if (collision.canMoveTo(jumpPos1)) {
                position = jumpPos1; // Salto normal
            }
            onGround = false;
        }
        collision.updatePlayerState(this);
    }

    public void moveDown(CollisionSystem collision) {
        if (dead) return;
        // Permitir movimiento hacia abajo si está escalando O si está en una enredadera
        if (climbing || onVine) {
            Coords newPos = new Coords(position.getX(), position.getY() + 1);
            if (collision.canMoveTo(newPos) || collision.isOnLadder(newPos)) {
                position = newPos;
            }
        }
        collision.updatePlayerState(this);
    }
    
    public void toggleClimbing(CollisionSystem collision) {
        if (dead) return;
        if (onVine) {
            climbing = !climbing;
            System.out.println("Escalando: " + climbing);
        } else {
            climbing = false; // No puede escalar si no está en una enredadera
        }
    }
    
    public void die() {
        if (!dead) {
            dead = true;
            System.out.println("¡El jugador ha muerto!");
        }
    }
    
    public void respawn(Coords spawnPoint) {
        position = spawnPoint;
        dead = false;
        onGround = false;
        climbing = false;
        onVine = false;
        facingRight = true;
        System.out.println("¡Jugador respawneado en " + spawnPoint + "!");
    }
    
    // Getters
    public Coords getPosition() { return position; }
    public boolean isFacingRight() { return facingRight; }
    public boolean isOnGround() { return onGround; }
    public boolean isClimbing() { return climbing; }
    public boolean isOnVine() { return onVine; }
    public boolean isDead() { return dead; }
    
    // Setters
    public void setPosition(Coords position) { this.position = position; }
    public void setOnGround(boolean onGround) { this.onGround = onGround; }
    public void setClimbing(boolean climbing) { this.climbing = climbing; }
    public void setOnVine(boolean onVine) { this.onVine = onVine; }
}
