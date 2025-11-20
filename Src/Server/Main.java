import java.net.Socket;
import java.util.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import Network.Server;
import Network.AdapterJSON;
import World.World;
import World.TileType;
import Entities.Player;
import Entities.Coco;
import Entities.RedCoco;
import Entities.BlueCoco;
import Physics.CollisionSystem;
import Physics.GravitySystem;
import Utils.Coords;
import Entities.Fruit;
import Game.GameData;
import Game.GameAdmin;

public class Main {
    // Constantes
    private static final Coords SPAWN_J1 = new Coords(0, 19);
    private static final Coords SPAWN_J2 = new Coords(0, 19);
    private static final int GAME_LOOP_DELAY = 125;
    private static final String LEVEL_PATH = "World/Levels/lvl1.txt";
    
    // GameData containers para cada jugador
    private static GameData gameDataJ1 = null;
    private static GameData gameDataJ2 = null;
    
    private static Server servidor;
    private static AdapterJSON adapter;
    private static GameAdmin gameAdmin;
    
    // Control de estado
    private static boolean j1Activo = false;
    private static boolean j2Activo = false;

    // ========== INICIALIZACIÓN ==========

    public static void main(String[] args) {
        
        adapter = new AdapterJSON();
        servidor = new Server();
        
        // Inicializar GameAdmin (NO iniciar menú todavía)
        gameAdmin = new GameAdmin();
        
        servidor.iniciar();
        
        System.out.println("=== SERVIDOR INICIADO ===");
        System.out.println("Esperando conexiones de clientes...");
        System.out.println("Los mundos se crearán cuando los jugadores se conecten");
        
        // NO iniciar el menú aquí - se iniciará automáticamente cuando se conecten jugadores
        
        Thread gameThread = new Thread(() -> {
            while (true) {
                gestionarJugadores();
                procesarMensajesEntrantes();
                actualizarJuego();
                
                try {
                    Thread.sleep(GAME_LOOP_DELAY);
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        gameThread.setDaemon(true);
        gameThread.start();
    }

    // ========== GESTIÓN DE JUGADORES ==========

    private static void gestionarJugadores() {
        boolean estadoAnteriorJ1 = j1Activo;
        boolean estadoAnteriorJ2 = j2Activo;
        
        gestionarConexionJugador1();
        gestionarConexionJugador2();
        limpiarJugadoresDesconectados();
        
        // Verificar si hubo cambios de estado
        boolean huboCambios = (estadoAnteriorJ1 != j1Activo) || (estadoAnteriorJ2 != j2Activo);
        
        if (huboCambios) {
            System.out.println("🔄 Cambio de estado - J1: " + estadoAnteriorJ1 + "→" + j1Activo + 
                             ", J2: " + estadoAnteriorJ2 + "→" + j2Activo);
        }
        
        // Actualizar GameAdmin con los estados actuales (SIEMPRE)
        gameAdmin.updateGameData(gameDataJ1, gameDataJ2, j1Activo, j2Activo);
    }

    private static void gestionarConexionJugador1() {
        if (j1Activo) {
            Socket s1 = servidor.getSocketJugador(servidor.J1_NAME);

            if (s1 == null || s1.isClosed()) {
                return;                    
            }

            String json1 = adapter.generarJSON(
                gameDataJ1.player, 
                gameDataJ1.fruits, 
                gameDataJ1.crocodiles, 
                servidor.J1_NAME, 
                servidor.getSpectadoresSize(servidor.J1_NAME)
            );
            servidor.enviarA(s1, json1);
            servidor.enviarAMisEspectadores(servidor.J1_NAME, json1);
        }
        else {
            if (servidor.getJugadoresSize() >= 1 && !j2Activo || 
                servidor.getJugadoresSize() == 2 && j2Activo) {
                try {
                    System.out.println("🔄 Inicializando mundo para Jugador 1...");
                    
                    // Crear GameData para J1
                    gameDataJ1 = new GameData(SPAWN_J1);
                    gameDataJ1.initializeWorld(LEVEL_PATH);
                    
                    j1Activo = true;
                    servidor.J1_ING = true;
                    
                    System.out.println("✅ Jugador 1 CONECTADO y listo");

                } catch (Exception e) {
                    System.err.println("❌ Error al crear GameData para J1: " + e.getMessage());
                    limpiarJugador1();
                }
            }
        }
    }

    private static void gestionarConexionJugador2() {
        if (j2Activo) {
            Socket s2 = servidor.getSocketJugador(servidor.J2_NAME);

            if (s2 == null || s2.isClosed()) {
                servidor.J2_desc = true;   
                return;                    
            }

            String json2 = adapter.generarJSON(
                gameDataJ2.player, 
                gameDataJ2.fruits, 
                gameDataJ2.crocodiles, 
                servidor.J2_NAME, 
                servidor.getSpectadoresSize(servidor.J2_NAME)
            );
            servidor.enviarA(s2, json2);
            servidor.enviarAMisEspectadores(servidor.J2_NAME, json2);
        }
        if (!j2Activo && servidor.getJugadoresSize() >= 2) {
            try {
                System.out.println("🔄 Inicializando mundo para Jugador 2...");
                
                // Crear GameData para J2
                gameDataJ2 = new GameData(SPAWN_J2);
                gameDataJ2.initializeWorld(LEVEL_PATH);
                
                j2Activo = true;
                servidor.J2_ING = true;
                
                System.out.println("✅ Jugador 2 CONECTADO y listo");
                
            } catch (Exception e) {
                System.err.println("❌ Error al crear GameData para J2: " + e.getMessage());
                limpiarJugador2();
            }
        }
    }

    private static void limpiarJugadoresDesconectados() {
        // Verificar si J1 estaba activo pero ahora está desconectado
        if (servidor.J1_ING && servidor.J1_desc) {
            servidor.J1_desc = false;
            System.out.println("🔌 Jugador 1 DESCONECTADO, liberando recursos...");
            limpiarJugador1();
        }
        
        // Verificar si J2 estaba activo pero ahora está desconectado
        if (j2Activo && servidor.J2_desc) {
            servidor.J2_desc = false;
            System.out.println("🔌 Jugador 2 DESCONECTADO, liberando recursos...");
            limpiarJugador2();
        }
    }

    private static void limpiarJugador1() {
        if (gameDataJ1 != null) {
            gameDataJ1.cleanup();
        }
        gameDataJ1 = null;
        j1Activo = false;
        servidor.J1_ING = false;
        servidor.J1_desc = false;
        System.out.println("🗑️  Recursos de Jugador 1 liberados");
    }

    private static void limpiarJugador2() {
        if (gameDataJ2 != null) {
            gameDataJ2.cleanup();
        }
        gameDataJ2 = null;
        j2Activo = false;
        servidor.J2_ING = false;
        servidor.J2_desc = false;
        System.out.println("🗑️  Recursos de Jugador 2 liberados");
    }

    // ========== ACTUALIZACIÓN DEL JUEGO ==========

    private static void actualizarJuego() {
        actualizarCocodrilos();
        actualizarFisicaJugadores();
    }

    private static void actualizarCocodrilos() {
        if (j1Activo && gameDataJ1 != null) {
            gameDataJ1.updateCrocodiles();
        }
        
        if (j2Activo && gameDataJ2 != null) {
            gameDataJ2.updateCrocodiles();
        }
    }

    private static void actualizarFisicaJugadores() {
        if (j1Activo && gameDataJ1 != null) {
            gameDataJ1.updatePhysics();
        }
        
        if (j2Activo && gameDataJ2 != null) {
            gameDataJ2.updatePhysics();
        }
    }

    // ========== PROCESAMIENTO DE MENSAJES ==========

    private static void procesarMensajesEntrantes() {
        if (j1Activo && !servidor.mensajes_j1.isEmpty()) {
            String mensaje = servidor.mensajes_j1.remove(0);
            procesarMovimientoJugador(mensaje, gameDataJ1, servidor.J1_NAME, 1);
            enviarDatosJugador(servidor.J1_NAME, gameDataJ1);
        }

        if (j2Activo && !servidor.mensajes_j2.isEmpty()) {
            String mensaje = servidor.mensajes_j2.remove(0);
            procesarMovimientoJugador(mensaje, gameDataJ2, servidor.J2_NAME, 2);
            enviarDatosJugador(servidor.J2_NAME, gameDataJ2);
        }
    }

    private static void procesarMovimientoJugador(String mensaje, GameData gameData, String nombreJugador, int posJug) {
        if (gameData == null || gameData.player == null || 
            gameData.collisionSystem == null || gameData.gravitySystem == null) return;
        
        try {
            int movimiento = Integer.parseInt(mensaje);
            String accion = "";
            
            switch (movimiento) {

            case 1: // ARRIBA/SALTO
                if (gameData.player.isOnVine()) {
                    Coords newPos = gameData.player.calculateMoveUp();
                    if (gameData.collisionSystem.canMoveTo(newPos)) {
                        gameData.player.applyMovement(newPos, gameData.player.isFacingRight());
                        accion = "SUBIÓ por la liana";
                    } else {
                        accion = "no puede subir (obstáculo)";
                    }
                } else if (gameData.player.isOnGround()) {
                    Coords[] jumpPositions = gameData.player.calculateJumpPositions();
                    Coords jumpTarget = null;
                    
                    int maxAltura = 0;
                    for (int i = 0; i < jumpPositions.length; i++) {
                        if (gameData.collisionSystem.canMoveTo(jumpPositions[i])) {
                            maxAltura = i + 1;
                        } else {
                            break;
                        }
                    }
                    
                    if (maxAltura > 0) {
                        jumpTarget = jumpPositions[maxAltura - 1];
                        gameData.player.applyJump(jumpTarget);
                        accion = "SALTÓ " + maxAltura + " bloques de altura";
                    } else {
                        accion = "no puede saltar (obstáculo arriba)";
                    }
                } else {
                    accion = "no puede moverse arriba (en el aire)";
                }
                break;

                case 2: // Derecha
                    Coords rightPos = gameData.player.calculateMoveRight();
                    if (gameData.collisionSystem.canMoveTo(rightPos)) {
                        gameData.player.applyMovement(rightPos, true);
                        accion = "se movió DERECHA";
                    } else {
                        accion = "no puede moverse derecha (obstáculo)";
                    }
                    break;
                    
                case 3: // Abajo
                    if (gameData.player.isOnVine()) {
                        Coords downPos = gameData.player.calculateMoveDown();
                        if (gameData.collisionSystem.canMoveTo(downPos)) {
                            gameData.player.applyMovement(downPos, gameData.player.isFacingRight());
                            accion = "se movió ABAJO";
                        } else {
                            accion = "no puede bajar (obstáculo)";
                        }
                    } else {
                        accion = "no puede moverse abajo (no está escalando)";
                    }
                    break;
                    
                case 4: // Izquierda
                    Coords leftPos = gameData.player.calculateMoveLeft();
                    if (gameData.collisionSystem.canMoveTo(leftPos)) {
                        gameData.player.applyMovement(leftPos, false);
                        accion = "se movió IZQUIERDA";
                    } else {
                        accion = "no puede moverse izquierda (obstáculo)";
                    }
                    break;
                case 5:
                    if (posJug == 1) {
                        if (gameDataJ1.player.isDead()) {
                            gameDataJ1.newLevel(1);
                        }
                    }
                    else {
                        if (gameDataJ2.player.isDead()) {
                            gameDataJ2.newLevel(1);
                        }
                    }
                    break;
                default:
                    accion = "acción desconocida: " + movimiento;
            }
            
            gameData.collisionSystem.updatePlayerState(gameData.player, gameData.crocodiles, gameData.fruits);
            
            String estadoActual = obtenerEstadoJugador(gameData.player, nombreJugador);
            String mensajeCompleto = nombreJugador + " " + accion + " | " + estadoActual;
            
            Socket socket = servidor.getSocketJugador(nombreJugador);
            if (socket != null) {
                servidor.enviarA(socket, mensajeCompleto);
            }
            
            servidor.enviarAMisEspectadores(nombreJugador, mensajeCompleto);
            
        } catch (NumberFormatException e) {
            System.err.println("Error: mensaje inválido: " + mensaje);
        }
    }

    private static void enviarDatosJugador(String nombreJugador, GameData gameData) {
        if (gameData == null || gameData.player == null) return;
        
        Socket socket = servidor.getSocketJugador(nombreJugador);
        if (socket != null) {
            String json = adapter.generarJSON(
                gameData.player, 
                gameData.fruits, 
                gameData.crocodiles, 
                nombreJugador, 
                servidor.getSpectadoresSize(nombreJugador)
            );
            servidor.enviarA(socket, json);
            servidor.enviarAMisEspectadores(nombreJugador, json);
        }
    }

    private static String obtenerEstadoJugador(Player jugador, String nombre) {
        if (jugador == null) return nombre + ": NO INICIALIZADO";
        
        return String.format("%s: Pos(%d,%d) Puntos:%d %s%s%s%s", 
            nombre,
            jugador.getPosition().getX(), 
            jugador.getPosition().getY(),
            jugador.getPoints(),
            jugador.isOnGround() ? "SUELO " : "AIRE ",
            "",
            jugador.isOnVine() ? "ENREDADERA " : "",
            jugador.isDead() ? "MUERTO " : "");
    }
    
    /**
     * Clean up resources
     */
    public static void cleanup() {
        if (gameAdmin != null) {
            gameAdmin.cleanup();
        }
    }
}
