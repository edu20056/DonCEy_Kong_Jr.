import java.net.Socket;
import java.util.*;

import Network.Server;
import World.World;
import World.TileType;
import Entities.Player;
import Physics.CollisionSystem;
import Physics.GravitySystem;
import Utils.Coords;

public class Main {
    private static World world;
    private static Player player1;
    private static Player player2;
    private static CollisionSystem collisionSystem;
    private static GravitySystem gravitySystem;
    
    // Posiciones iniciales (ajusta según tu mapa)
    private static final Coords SPAWN_J1 = new Coords(2, 1);
    private static final Coords SPAWN_J2 = new Coords(8, 1);

    public static void renderWorld(World world, Player player1, Player player2) {
        // Limpiar consola
        System.out.print("\033[H\033[2J");
        System.out.flush();
        
        System.out.println("=== MUNDO DEL JUEGO ===");
        
        for (int y = 0; y < world.getHeight(); y++) {
            for (int x = 0; x < world.getWidth(); x++) {
                Coords currentPos = new Coords(x, y);
                boolean playerPrinted = false;
                
                // Verificar jugador 1
                if (player1 != null && !player1.isDead() && 
                    player1.getPosition().getX() == x && player1.getPosition().getY() == y) {
                    System.out.print(player1.isFacingRight() ? "→" : "←");
                    playerPrinted = true;
                } 
                // Verificar jugador 2
                else if (player2 != null && !player2.isDead() && 
                         player2.getPosition().getX() == x && player2.getPosition().getY() == y) {
                    System.out.print(player2.isFacingRight() ? "►" : "◄");
                    playerPrinted = true;
                }
                
                // Si no hay jugador en esta posición, mostrar el tile del mundo
                if (!playerPrinted) {
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
        
        // Información de estado
        System.out.println("\n=== ESTADO DE JUGADORES ===");
        if (player1 != null) {
            Coords player1Pos = player1.getPosition();
            System.out.printf("J1: Pos(%d,%d) %s %s %s %s %s%n", 
                player1Pos.getX(), player1Pos.getY(),
                player1.isFacingRight() ? "→" : "←",
                player1.isOnGround() ? "[SUELO]" : "[AIRE]",
                player1.isOnVine() ? "[ENREDADERA]" : "",
                player1.isClimbing() ? "[ESCALANDO]" : "",
                player1.isDead() ? "[MUERTO]" : "");
        }
        if (player2 != null) {
            Coords player2Pos = player2.getPosition();
            System.out.printf("J2: Pos(%d,%d) %s %s %s %s %s%n", 
                player2Pos.getX(), player2Pos.getY(),
                player2.isFacingRight() ? "→" : "←",
                player2.isOnGround() ? "[SUELO]" : "[AIRE]",
                player2.isOnVine() ? "[ENREDADERA]" : "",
                player2.isClimbing() ? "[ESCALANDO]" : "",
                player2.isDead() ? "[MUERTO]" : "");
        }
        System.out.println("Esperando movimientos de clientes...");
    }

    public static void main(String[] args) {
        // Inicializar el juego
        inicializarJuego();
        
        Server servidor = new Server();
        servidor.iniciar();
        
        // Thread para procesar mensajes entrantes en tiempo real
        Thread procesadorMensajes = new Thread(() -> {
            while (true) {
                procesarMensajesEntrantes(servidor);
                try {
                    Thread.sleep(100); // Pequeña pausa para no saturar la CPU
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        procesadorMensajes.setDaemon(true);
        procesadorMensajes.start();

        // Thread para actualizar física y renderizar
        Thread gameLoop = new Thread(() -> {
            while (true) {
                actualizarJuego();
                renderWorld(world, player1, player2);
                try {
                    Thread.sleep(200); // 5 FPS para que sea visible
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        gameLoop.setDaemon(true);
        gameLoop.start();

        ejecutarInterfazUsuario(servidor);
    }
    
    private static void inicializarJuego() {
        try {
            world = new World("World/Levels/lvl1.txt");
            collisionSystem = new CollisionSystem(world);
            gravitySystem = new GravitySystem(collisionSystem);
            
            player1 = new Player(SPAWN_J1.getX(), SPAWN_J1.getY());
            player2 = new Player(SPAWN_J2.getX(), SPAWN_J2.getY());
            
            // Actualizar estado inicial de los jugadores
            collisionSystem.updatePlayerState(player1);
            collisionSystem.updatePlayerState(player2);
            
            System.out.println("✓ Juego inicializado correctamente");
            System.out.println("✓ Mundo cargado: " + world.getWidth() + "x" + world.getHeight());
        } catch (Exception e) {
            System.err.println("Error al inicializar el juego: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void actualizarJuego() {
        if (gravitySystem != null) {
            if (player1 != null && !player1.isDead()) gravitySystem.applyGravity(player1);
            if (player2 != null && !player2.isDead()) gravitySystem.applyGravity(player2);
        }
        if (collisionSystem != null) {
            if (player1 != null) collisionSystem.updatePlayerState(player1);
            if (player2 != null) collisionSystem.updatePlayerState(player2);
        }
    }

    private static void procesarMensajesEntrantes(Server servidor) {
        // Verificar si se debe instancear un jugador
        if (servidor.J1_ING == false && servidor.getJugadoresSize() == 1){
            System.out.println("Se debe instancear jugador 1");
            servidor.J1_ING = true;
        } 

        if (servidor.J2_ING == false && servidor.getJugadoresSize() == 2){
            System.out.println("Se debe instancear jugador 2");
            servidor.J2_ING = true;
        }

        // Procesar mensajes de J1
        if (!servidor.mensajes_j1.isEmpty()) {
            String mensaje = servidor.mensajes_j1.remove(0); // Obtener y remover el primer mensaje
            Socket s1 = servidor.getSocketJugador(servidor.J1_NAME);
            if (s1 != null) {
                procesarMovimientoJugador(mensaje, player1, servidor.J1_NAME, servidor );
            }    
            
            List<int[]> entidadesRandom = generarListaRandom(1);
            List<int[]> frutasRandom    = generarListaRandom(1);

            String json1 = Main.generarJSON(player1.getPosition().getX(), player1.getPosition().getY(), entidadesRandom, frutasRandom);

            servidor.enviarA(s1, json1);
            servidor.enviarAMisEspectadores(servidor.J1_NAME, json1);
            System.out.println("✓ Mensaje de J1 procesado: " + mensaje);
            
        }

        // Procesar mensajes de J2
        if (!servidor.mensajes_j2.isEmpty()) {
            String mensaje = servidor.mensajes_j2.remove(0); // Obtener y remover el primer mensaje
            Socket s2 = servidor.getSocketJugador(servidor.J2_NAME);
            if (s2 != null) {
                procesarMovimientoJugador(mensaje, player2, servidor.J2_NAME, servidor );
            }

            List<int[]> entidadesRandom = generarListaRandom(3);
            List<int[]> frutasRandom    = generarListaRandom(5);

            String json2 = Main.generarJSON(player2.getPosition().getX(), player2.getPosition().getY(), entidadesRandom, frutasRandom);

            // para que por cada frame se manden cambios, esto debe estar fuera del if en un if (servidor.J2_ING == true)
            servidor.enviarA(s2, json2);
            servidor.enviarAMisEspectadores(servidor.J2_NAME, json2);
            System.out.println("✓ Mensaje de J2 procesado: " + mensaje);
            
        }
        
    }

    private static void procesarMovimientoJugador(String mensaje, Player jugador, String nombreJugador, Server servidor) {
        if (jugador == null || jugador.isDead()) return;
        
        try {
            int movimiento = Integer.parseInt(mensaje);
            String accion = "";
            boolean movimientoExitoso = false;
            
            switch (movimiento) {
                case 1 -> { // ARRIBA - Salta si está en suelo, sube si está en liana
                    if (jugador.isOnGround()) {
                        // SALTO - usa el método jump
                        Coords posAntes = jugador.getPosition();
                        jugador.jump(gravitySystem, collisionSystem);
                        Coords posDespues = jugador.getPosition();
                        boolean saltoExitoso = !posAntes.equals(posDespues);
                    
                        if (saltoExitoso) {
                            accion = nombreJugador + " SALTÓ desde el suelo";
                        } else {
                            accion = nombreJugador + " intentó saltar pero no pudo (obstáculo arriba)";
                        }
                    } else if (jugador.isOnVine()) {
                        // SUBIR - usa moveUp cuando está en liana
                        Coords posAntes = jugador.getPosition();
                        jugador.moveUp(collisionSystem);
                        Coords posDespues = jugador.getPosition();
                        movimientoExitoso = !posAntes.equals(posDespues);
                    
                        if (movimientoExitoso) {
                            accion = nombreJugador + " SUBIÓ por la liana";
                        } else {
                            accion = nombreJugador + " intentó subir pero no pudo (obstáculo arriba)";
                        }
                    } else {
                        accion = nombreJugador + " no puede moverse arriba (no está en suelo ni en liana)";
                    }
                }
                case 2 -> { // Derecha
                    Coords posAntes = jugador.getPosition();
                    jugador.moveRight(collisionSystem);
                    Coords posDespues = jugador.getPosition();
                    movimientoExitoso = !posAntes.equals(posDespues);
                    accion = nombreJugador + " se movió DERECHA";
                }
                case 3 -> { // Abajo
                    Coords posAntes = jugador.getPosition();
                    jugador.moveDown(collisionSystem);
                    Coords posDespues = jugador.getPosition();
                    movimientoExitoso = !posAntes.equals(posDespues);
                    accion = nombreJugador + " se movió ABAJO";
                }
                case 4 -> { // Izquierda
                    Coords posAntes = jugador.getPosition();
                    jugador.moveLeft(collisionSystem);
                    Coords posDespues = jugador.getPosition();
                    movimientoExitoso = !posAntes.equals(posDespues);
                    accion = nombreJugador + " se movió IZQUIERDA";
                }
                default -> accion = nombreJugador + " acción desconocida: " + movimiento;
            }
            
            // Actualizar estado después del movimiento
            collisionSystem.updatePlayerState(jugador);
            
            // Enviar confirmación y estado actualizado
            String estadoActual = obtenerEstadoJugador(jugador, nombreJugador);
            String mensajeCompleto = accion + " | " + estadoActual;
            
            // Enviar al jugador que realizó la acción
            Socket socket = servidor.getSocketJugador(nombreJugador);
            if (socket != null) {
                servidor.enviarA(socket, mensajeCompleto);
            }
            
            // Enviar a espectadores
            servidor.enviarAMisEspectadores(nombreJugador, mensajeCompleto);
            
        } catch (NumberFormatException e) {
            System.err.println("Error: mensaje no es un número válido: " + mensaje);
        }
    }
    
    private static String obtenerEstadoJugador(Player jugador, String nombre) {
        if (jugador == null) return nombre + ": NO INICIALIZADO";
        
        return String.format("%s: Pos(%d,%d) %s%s%s%s", 
            nombre,
            jugador.getPosition().getX(), 
            jugador.getPosition().getY(),
            jugador.isOnGround() ? "SUELO " : "AIRE ",
            jugador.isClimbing() ? "ESCALANDO " : "",
            jugador.isOnVine() ? "ENREDADERA " : "",
            jugador.isDead() ? "MUERTO " : "");
    }

    private static void ejecutarInterfazUsuario(Server servidor) {
        try (Scanner sc = new Scanner(System.in)) {
            while (true) {
                System.out.println("\n=== MENÚ SERVIDOR ===");
                System.out.println("1. Forzar respawn de jugadores");
                System.out.println("2. Mostrar estado detallado");
                System.out.println("3. Enviar mensaje manual");
                System.out.println("4. Cerrar servidor");
                System.out.print("Selecciona opción: ");
                
                String opcion = sc.nextLine();

                switch (opcion) {
                    case "1" -> {
                        if (player1 != null) player1.respawn(SPAWN_J1);
                        if (player2 != null) player2.respawn(SPAWN_J2);
                        System.out.println("✓ Jugadores respawneados");
                    }
                    case "2" -> {
                        System.out.println("\n=== ESTADO DETALLADO ===");
                        System.out.println("J1: " + obtenerEstadoJugador(player1, "J1"));
                        System.out.println("J2: " + obtenerEstadoJugador(player2, "J2"));
                        System.out.println("Mundo: " + world.getWidth() + "x" + world.getHeight());
                    }
                    case "3" -> servidor.opcionEnviarMensaje(sc);
                    case "4" -> {
                        servidor.cerrarServidor();
                        System.out.println("¡Servidor cerrado!");
                        return;
                    }
                    default -> System.out.println("Opción no válida.");
                }
            }
        }
    }

    public static String generarJSON(int jx, int jy, List<int[]> entidades, List<int[]> frutas) {

        StringBuilder sb = new StringBuilder();
        sb.append("{");

        // Jugador
        sb.append("\"jugador\": {");
        sb.append("\"x\": ").append(jx).append(", ");
        sb.append("\"y\": ").append(jy);
        sb.append("},");

        // Entidades
        sb.append("\"entidades\": [");
        for (int i = 0; i < entidades.size(); i++) {
            int[] e = entidades.get(i);
            sb.append("{\"tipo\": \"entidad\", \"x\": ").append(e[0])
            .append(", \"y\": ").append(e[1]).append("}");
            if (i < entidades.size() - 1) sb.append(",");
        }
        sb.append("],");

        // Frutas
        sb.append("\"frutas\": [");
        for (int i = 0; i < frutas.size(); i++) {
            int[] f = frutas.get(i);
            sb.append("{\"tipo\": \"fruta\", \"x\": ").append(f[0])
            .append(", \"y\": ").append(f[1]).append("}");
            if (i < frutas.size() - 1) sb.append(",");
        }
        sb.append("]");

        sb.append("}");

        return sb.toString();
    }

    // FUNCION PARA PRUEBAS
    public static List<int[]> generarListaRandom(int cantidad) {
        Random r = new Random();
        List<int[]> lista = new ArrayList<>();

        for (int i = 0; i < cantidad; i++) {
            int x = r.nextInt(300);  // random 0-299
            int y = r.nextInt(300);
            lista.add(new int[]{x, y});
        }

        return lista;
    }

}
