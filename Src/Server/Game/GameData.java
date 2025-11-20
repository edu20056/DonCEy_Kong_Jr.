package Game;

import Entities.Player;
import Entities.Coco;
import Entities.RedCoco;
import Entities.BlueCoco;
import Entities.Fruit;
import World.World;
import Physics.CollisionSystem;
import Physics.GravitySystem;
import Utils.Coords;
import java.util.ArrayList;
import java.util.List;

/**
 * Data container for all player-related information.
 * Groups together player, world, systems, and entities.
 * Designed to work seamlessly with the existing Main.java structure.
 */

public class GameData {
    public Player player;
    public World world;
    public CollisionSystem collisionSystem;
    public GravitySystem gravitySystem;
    public List<Coco> crocodiles;
    public List<Fruit> fruits;
    public boolean isActive;
    public Coords spawnPoint;
    public int lvl;
    
    /**
     * Constructs a GameData instance for a player.
     * 
     * @param spawn The spawn coordinates for the player
     */

    public GameData(Coords spawn) {
        this.spawnPoint = spawn;
        this.player = new Player(spawn.getX(), spawn.getY());
        this.crocodiles = new ArrayList<>();
        this.fruits = new ArrayList<>();
        this.isActive = false;
        this.lvl = 1;
    }


    // --- GETTERS AND SETTERS --- //

    public int getLvl() { return lvl; }

    /**
     * Initializes the player world and systems.
     * Equivalent to the initialization code in Main.java for each player.
     * 
     * @param levelPath The path to the level file
     */

    public void initializeWorld(String levelPath) {
        try {
            this.world = new World(levelPath);
            this.collisionSystem = new CollisionSystem(world);
            this.gravitySystem = new GravitySystem(collisionSystem);
            
            this.collisionSystem.updatePlayerState(player, null, null);
            this.isActive = true;
            this.lvl = 1;

            initializeLevelEntities();
            
        } catch (Exception e) {
            this.isActive = false;
        }
    }

    private void initializeLevelEntities() {
        crocodiles.clear();
        fruits.clear();
        
        crocodiles.add(new RedCoco(11, 6, lvl));
        crocodiles.add(new BlueCoco(0, 6, lvl));
    
        fruits.add(new Fruit(3, 4, "BANANA"));
        fruits.add(new Fruit(7, 12, "STRAWBERRY"));
        fruits.add(new Fruit(6, 6, "ORANGE"));
    }
    
    public void cleanup() {
        this.player = new Player(spawnPoint.getX(), spawnPoint.getY());
        this.world = null;
        this.collisionSystem = null;
        this.gravitySystem = null;
        this.crocodiles.clear();
        this.fruits.clear();
        this.isActive = false;
        this.lvl = 1;
    }

    /**
     * Advances to the next level, increasing difficulty.
     * Called when player reaches the goal.
     */
   
    public void newLevel(int levelNumber) {
      
        lvl = levelNumber;

        // Increase difficulty
        increaseCrocodileSpeed();

        // Reset player position
        player.respawn(spawnPoint);
        
        
        // Initialize new level entities
        initializeLevelEntities();

        // Update collision system
        if (collisionSystem != null) {
            collisionSystem.updatePlayerState(player, null, null);
        }
    }
    
    /**
     * Increases crocodile movement speed based on current level.
     * Implements progressive difficulty.
     */
    
    private void increaseCrocodileSpeed() {
        for (Coco crocodile : crocodiles) {
            crocodile.setMovementSpeed(lvl);
        }
    }

    /**
     * Updates player physics and game state.
     * Equivalent to actualizarFisicaJugadores() in Main.java.
     */
    
    public void updatePhysics() {
        if (isActive && gravitySystem != null && player != null && !player.isDead()) {
            
            // Check if player reached the goal
            if (collisionSystem.checkWinCollision(player)) {
                player.incLives();
                newLevel(++lvl);
                return;
            }

            // Apply gravity
            gravitySystem.applyGravity(player);
            
            // Update collision state 
            collisionSystem.updatePlayerState(player, crocodiles, fruits);
       
            // Check if player lives after deadly collision.
            if (player.isDead() && player.getLives() > 0) {
                player.decLives();
                newLevel(lvl);
                return;
            }
        }
    }
    
    /**
     * Updates all crocodile entities.
     * Equivalent to actualizarCocodrilos() in Main.java.
     */

    public void updateCrocodiles() {
        if (isActive && world != null) {
            for (Coco crocodile : crocodiles) {
                if (crocodile.isActive()) {
                    crocodile.update(world);
                }
            }
            crocodiles.removeIf(c -> !c.isActive());
        }
    }
    
    /**
     * Adds a crocodile to the game (for admin menu functionality).
     * 
     * @param crocodile The crocodile to add
     */

    public void addCrocodile(Coco crocodile) {
        if (isActive && crocodile != null) {
            crocodiles.add(crocodile);
        }
    }
    
    /**
     * Adds a fruit to the game (for admin menu functionality).
     * 
     * @param fruit The fruit to add
     */

    public void addFruit(Fruit fruit) {
        if (isActive && fruit != null) {
            fruits.add(fruit);
        }
    }
}
