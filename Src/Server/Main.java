import Network.Server;
import Network.AdapterJSON;
import Entities.Player;
import Entities.Coco;
import Entities.Fruit;
import Physics.CollisionSystem;
import Physics.GravitySystem;
import Utils.Coords;
import Game.GameData;
import Game.GameAdmin;

import java.net.Socket;
import java.util.*;

public class Main {
    private static final Coords SPAWN_J1 = new Coords(0, 19);
    private static final Coords SPAWN_J2 = new Coords(0, 19);
    private static final int GAME_LOOP_DELAY = 125;
    private static final String LEVEL_PATH = "World/Levels/lvl1.txt";
    
    private static GameData gameDataJ1 = null;
    private static GameData gameDataJ2 = null;
    
    private static Server servidor;
    private static AdapterJSON adapter;
    private static GameAdmin gameAdmin;
    
    private static boolean j1Activo = false;
    private static boolean j2Activo = false;

    public static void main(String[] args) {
        adapter = new AdapterJSON();
        servidor = new Server();
        gameAdmin = new GameAdmin();
        
        // Iniciar servidor en un hilo separado
        Thread serverThread = new Thread(() -> {
            servidor.iniciar();
        });
        serverThread.setDaemon(true);
        serverThread.start();
        
        // Esperar un momento para que el servidor inicie
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {}
        
        // Iniciar el juego en un hilo separado
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
        
        // Limpiar pantalla y mostrar menú en el hilo principal
        limpiarPantalla();
        gameAdmin.displayMenu();
    }

    private static void limpiarPantalla() {
        try {
            if (System.getProperty("os.name").contains("Windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                new ProcessBuilder("clear").inheritIO().start().waitFor();
            }
        } catch (Exception e) {
            for (int i = 0; i < 50; i++) {
                System.out.println();
            }
        }
    }

    private static void gestionarJugadores() {
        gestionarConexionJugador1();
        gestionarConexionJugador2();
        limpiarJugadoresDesconectados();
        gameAdmin.updateGameData(gameDataJ1, gameDataJ2, j1Activo, j2Activo);
    }

    private static void gestionarConexionJugador1() {
        if (j1Activo) {
            Socket s1 = servidor.getSocketJugador(servidor.J1_NAME);
            if (s1 == null || s1.isClosed()) return;

            String json1 = adapter.generarJSON(
                gameDataJ1.player, 
                gameDataJ1.fruits, 
                gameDataJ1.crocodiles, 
                servidor.J1_NAME, 
                servidor.getSpectadoresSize(servidor.J1_NAME),
                gameDataJ1.player.getLives());

            servidor.enviarA(s1, json1);
            servidor.enviarAMisEspectadores(servidor.J1_NAME, json1);
        } else {
            if (servidor.getJugadoresSize() >= 1 && !j2Activo || 
                servidor.getJugadoresSize() == 2 && j2Activo) {
                try {
                    gameDataJ1 = new GameData(SPAWN_J1);
                    gameDataJ1.initializeWorld(LEVEL_PATH);
                    j1Activo = true;
                    servidor.J1_ING = true;
                } catch (Exception e) {
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
                servidor.getSpectadoresSize(servidor.J2_NAME),
                gameDataJ2.player.getLives()
            );
            servidor.enviarA(s2, json2);
            servidor.enviarAMisEspectadores(servidor.J2_NAME, json2);
        }
        if (!j2Activo && servidor.getJugadoresSize() >= 2) {
            try {
                gameDataJ2 = new GameData(SPAWN_J2);
                gameDataJ2.initializeWorld(LEVEL_PATH);
                j2Activo = true;
                servidor.J2_ING = true;
            } catch (Exception e) {
                limpiarJugador2();
            }
        }
    }

    private static void limpiarJugadoresDesconectados() {
        if (servidor.J1_ING && servidor.J1_desc) {
            servidor.J1_desc = false;
            limpiarJugador1();
        }
        
        if (j2Activo && servidor.J2_desc) {
            servidor.J2_desc = false;
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
    }

    private static void limpiarJugador2() {
        if (gameDataJ2 != null) {
            gameDataJ2.cleanup();
        }
        gameDataJ2 = null;
        j2Activo = false;
        servidor.J2_ING = false;
        servidor.J2_desc = false;
    }

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
        // Actualizar saltos del jugador 1
        if (j1Activo && gameDataJ1 != null) {
            if (gameDataJ1.player.isJumping()) {
                gameDataJ1.player.updateJump();
                // Verificar colisiones EN CADA FRAME del salto
                gameDataJ1.collisionSystem.updatePlayerState(gameDataJ1.player, gameDataJ1.crocodiles, gameDataJ1.fruits);
            } else {
                gameDataJ1.updatePhysics();
            }
        }
        
        // Actualizar saltos del jugador 2
        if (j2Activo && gameDataJ2 != null) {
            if (gameDataJ2.player.isJumping()) {
                gameDataJ2.player.updateJump();
                // Verificar colisiones EN CADA FRAME del salto
                gameDataJ2.collisionSystem.updatePlayerState(gameDataJ2.player, gameDataJ2.crocodiles, gameDataJ2.fruits);
            } else {
                gameDataJ2.updatePhysics();
            }
        }
    }

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
            
            switch (movimiento) {
                case 1: // ARRIBA/SALTO
                    if (gameData.player.isJumping()) {
                        break; // Ignorar si ya está saltando
                    }
                    
                    if (gameData.player.isOnVine()) {
                        Coords newPos = gameData.player.calculateMoveUp();
                        if (gameData.collisionSystem.canMoveTo(newPos)) {
                            gameData.player.applyMovement(newPos, gameData.player.isFacingRight());
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
                        }
                    }
                    break;

                case 2: // Derecha
                    if (!gameData.player.isJumping()) {
                        Coords rightPos = gameData.player.calculateMoveRight();
                        if (gameData.collisionSystem.canMoveTo(rightPos)) {
                            gameData.player.applyMovement(rightPos, true);
                        }
                    }
                    // Ignorar el input durante el salto
                    break;
                case 3: // Abajo
                    if (gameData.player.isOnVine()) {
                        Coords downPos = gameData.player.calculateMoveDown();
                        if (gameData.collisionSystem.canMoveTo(downPos)) {
                            gameData.player.applyMovement(downPos, gameData.player.isFacingRight());
                        }
                    }
                    break;
                    
                case 4: // Izquierda
                   if (!gameData.player.isJumping()) {
                        Coords leftPos = gameData.player.calculateMoveLeft();
                        if (gameData.collisionSystem.canMoveTo(leftPos)) {
                            gameData.player.applyMovement(leftPos, false);
                        }
                    }
                    // Ignorar el input durante el salto
                    break;
                     
                case 5: // Respawn
                    if (posJug == 1) {
                        if (gameDataJ1.player.isDead()) {    
                            gameDataJ1.player.setLives(3);
                            gameDataJ1.newLevel(1);
                        }
                    } else {
                        if (gameDataJ2.player.isDead()) {
                            gameDataJ2.player.setLives(3);
                            gameDataJ2.newLevel(1);
                        }
                    }
                    break;
            }
            
            gameData.collisionSystem.updatePlayerState(gameData.player, gameData.crocodiles, gameData.fruits);
            
        } catch (NumberFormatException e) {
            // Invalid message format, ignore
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
                servidor.getSpectadoresSize(nombreJugador),
                gameData.player.getLives()
            );
            servidor.enviarA(socket, json);
            servidor.enviarAMisEspectadores(nombreJugador, json);
        }
    }
    
    public static void cleanup() {
        if (gameAdmin != null) {
            gameAdmin.cleanup();
        }
    }
}
