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
        if (dead || !climbing) return;
        Coords newPos = new Coords(position.getX(), position.getY() - 1);
        if (collision.canMoveTo(newPos) || collision.isOnLadder(newPos)) {
            position = newPos;
        }
        collision.updatePlayerState(this);
    }
    
    public void moveDown(CollisionSystem collision) {
        if (dead || !climbing) return;
        Coords newPos = new Coords(position.getX(), position.getY() + 1);
        if (collision.canMoveTo(newPos) || collision.isOnLadder(newPos)) {
            position = newPos;
        }
        collision.updatePlayerState(this);
    }
    
    public void jump(GravitySystem gravity, CollisionSystem collision) {
        if (dead) return;
        if (onGround || onVine) {
            Coords jumpPos = new Coords(position.getX(), position.getY() - 1);
            if (collision.canMoveTo(jumpPos)) {
                position = jumpPos;
                onGround = false;
            }
        }
    }
    
    public void toggleClimbing(CollisionSystem collision) {
        if (dead) return;
        if (onVine) {
            climbing = !climbing;
        }
    }
    
    public void die() {
        dead = true;
    }
    
    public void respawn(Coords spawnPoint) {
        position = spawnPoint;
        dead = false;
        onGround = false;
        climbing = false;
        onVine = false;
        facingRight = true;
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
