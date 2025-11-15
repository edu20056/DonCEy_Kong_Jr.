// World/WorldRenderer.java
package World;

import Entities.Player;
import Utils.Coords;

public class WorldRenderer {
    private final World world;
    
    public WorldRenderer(World world) {
        this.world = world;
    }
    
    public String renderWorld(Player player1, Player player2) {
        StringBuilder sb = new StringBuilder();
        
        // Limpiar consola (opcional)
        sb.append("\033[H\033[2J");
        sb.append("=== MUNDO DEL JUEGO ===\n");
        
        int width = world.getWidth();
        int height = world.getHeight();
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Coords current = new Coords(x, y);
                char symbol = ' ';
                
                // Verificar jugadores primero (tienen prioridad en la visualización)
                if (player1 != null && !player1.isDead() && 
                    player1.getPosition().equals(current)) {
                    symbol = player1.isFacingRight() ? '▶' : '◀';
                } else if (player2 != null && !player2.isDead() && 
                          player2.getPosition().equals(current)) {
                    symbol = player2.isFacingRight() ? '►' : '◄';
                } else {
                    // Mostrar tile del mundo según tu enum TileType
                    TileType tile = world.getTile(current);
                    switch (tile) {
                        case EMPTY -> symbol = ' ';
                        case PLATFORM -> symbol = '█';
                        case VINE -> symbol = '≈';
                        case WATER -> symbol = '~';
                        case GOAL -> symbol = 'X';
                        default -> symbol = tile.getSymbol();
                    }
                }
                
                sb.append(symbol);
            }
            sb.append('\n');
        }
        
        // Información del estado de los jugadores
        sb.append("\n=== ESTADO DE JUGADORES ===\n");
        if (player1 != null) {
            sb.append(String.format("J1: Pos(%d,%d) %s %s %s %s\n", 
                player1.getPosition().getX(), player1.getPosition().getY(),
                player1.isOnGround() ? "[SUELO]" : "[AIRE]",
                player1.isClimbing() ? "[ESCALANDO]" : "",
                player1.isOnVine() ? "[EN ENREDADERA]" : "",
                player1.isDead() ? "[MUERTO]" : ""));
        }
        if (player2 != null) {
            sb.append(String.format("J2: Pos(%d,%d) %s %s %s %s\n", 
                player2.getPosition().getX(), player2.getPosition().getY(),
                player2.isOnGround() ? "[SUELO]" : "[AIRE]",
                player2.isClimbing() ? "[ESCALANDO]" : "",
                player2.isOnVine() ? "[EN ENREDADERA]" : "",
                player2.isDead() ? "[MUERTO]" : ""));
        }
        
        return sb.toString();
    }
}
