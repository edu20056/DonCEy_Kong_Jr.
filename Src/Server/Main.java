import World.TileType;
import World.World;
import Entities.Player;
import Physics.CollisionSystem;
import Physics.GravitySystem;
import Utils.Coords;
import Network.Server;

public class Main {

    public static void renderWorld(World world, Player player) {
        Coords playerPos = player.getPosition();
        
        for (int y = 0; y < world.getHeight(); y++) {
            for (int x = 0; x < world.getWidth(); x++) {
                Coords currentPos = new Coords(x, y);
                
                if (playerPos.getX() == x && playerPos.getY() == y) {
                    if (player.isFacingRight()) {
                        System.out.print("→");
                    } else {
                        System.out.print("←");
                    }
                } else {
                    TileType tile = world.getTile(currentPos);
                    switch (tile) {
                        case EMPTY:
                            System.out.print(" ");
                            break;
                        case VINE:
                            System.out.print("H");
                            break;
                        case PLATFORM:
                            System.out.print("=");
                            break;
                        case WATER:
                            System.out.print("~");
                            break;
                        case GOAL:
                            System.out.print("X");
                            break;
                        default:
                            System.out.print("?");
                    }
                }
            }
            System.out.println();
        }
        
        System.out.println();
        System.out.println("Posición: (" + playerPos.getX() + ", " + playerPos.getY() + ")");
        System.out.println("Dirección: " + (player.isFacingRight() ? "DERECHA" : "IZQUIERDA"));
        System.out.println("En suelo: " + (player.isOnGround() ? "SÍ" : "NO"));
        System.out.println("En liana: " + (player.isOnVine() ? "SÍ" : "NO"));
        System.out.println("Trepando: " + (player.isClimbing() ? "SÍ" : "NO"));
    }

    public static void main(String[] args) {

        Server servidor = new Server();
        servidor.iniciar();
        Server.menuServidor(); 

        System.out.print("\033[H\033[2J");
        System.out.flush();
        System.out.println("┌─────────────────────────────────────────────┐");
        System.out.println("│                DONKEY KONG JR               │");
        System.out.println("└─────────────────────────────────────────────┘");
        System.out.println();

        World world = new World("World/Levels/lvl1.txt");
        Player player = new Player(0, 19);
        CollisionSystem collision = new CollisionSystem(world);
        GravitySystem gravity = new GravitySystem(collision);

        player.moveRight(collision);
        player.jump(gravity, collision); 

        renderWorld(world, player);
    }
}
