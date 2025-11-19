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

public class Main {
    // Constantes
    private static final Coords SPAWN_J1 = new Coords(0, 0);
    private static final Coords SPAWN_J2 = new Coords(8, 3);
    private static final int GAME_LOOP_DELAY = 175;
    private static final String LEVEL_PATH = "World/Levels/lvl1.txt";
    
    // GameData containers para cada jugador (reemplaza las variables individuales)
    private static GameData gameDataJ1 = null;
    private static GameData gameDataJ2 = null;
    
    private static Server servidor;
    private static AdapterJSON adapter;
    
    // Control de estado (se mantienen para compatibilidad)
    private static boolean j1Activo = false;
    private static boolean j2Activo = false;

    // Scanner para entrada de consola
    private static Scanner scanner = new Scanner(System.in);
    private static boolean menuActivo = true;

    // ========== INICIALIZACIÓN ==========

    public static void main(String[] args) {
        
        adapter = new AdapterJSON();
        servidor = new Server();
        servidor.iniciar();
        
        System.out.println("=== SERVIDOR INICIADO ===");
        System.out.println("Esperando conexiones de clientes...");
        System.out.println("Los mundos se crearán cuando los jugadores se conecten");
        
        // Iniciar hilo del menú interactivo
        Thread menuThread = new Thread(() -> {
            mostrarMenuInteractivo();
        });
        menuThread.setDaemon(true);
        menuThread.start();
        
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

    // ========== MENÚ INTERACTIVO ==========

    private static void mostrarMenuInteractivo() {
        while (menuActivo) {
            try {
                Thread.sleep(2000); // Esperar 2 segundos entre menús
                mostrarOpcionesMenu();
                
                if (System.in.available() > 0) {
                    int opcion = scanner.nextInt();
                    scanner.nextLine(); // Limpiar buffer
                    
                    switch (opcion) {
                        case 1:
                            agregarCocodrilo();
                            break;
                        case 2:
                            agregarFruta();
                            break;
                        case 4:
                            menuActivo = false;
                            System.out.println("Menú desactivado.");
                            break;
                        default:
                            System.out.println("Opción inválida.");
                    }
                }
            } catch (Exception e) {
                // Ignorar excepciones de entrada/salida
            }
        }
    }

    private static void mostrarOpcionesMenu() {
        System.out.println("\n=== MENÚ INTERACTIVO ===");
        System.out.println("1. Agregar Cocodrilo");
        System.out.println("2. Agregar Fruta");
        System.out.println("3. Mostrar Estado Actual");
        System.out.println("4. Salir del Menú");
        System.out.print("Seleccione una opción: ");
    }

    private static void agregarCocodrilo() {
        try {
            System.out.println("\n--- AGREGAR COCODRILO ---");
            
            // Seleccionar jugador
            int jugador = seleccionarJugador();
            if (jugador == 0) return;
            
            // Seleccionar tipo de cocodrilo
            System.out.println("Tipos de cocodrilo:");
            System.out.println("1. Rojo (lento)");
            System.out.println("2. Azul (rápido)");
            System.out.print("Seleccione tipo: ");
            int tipo = scanner.nextInt();
            
            if (tipo < 1 || tipo > 2) {
                System.out.println("Tipo inválido.");
                return;
            }
            
            // Ingresar posición
            System.out.print("Posición X: ");
            int x = scanner.nextInt();
            System.out.print("Posición Y: ");
            int y = scanner.nextInt();
            
            // Ingresar velocidad
            System.out.print("Velocidad (1=lento, 2=rápido): ");
            int velocidad = scanner.nextInt();
            
            // Crear cocodrilo
            Coco nuevoCoco;
            if (tipo == 1) {
                nuevoCoco = new RedCoco(x, y, velocidad);
            } else {
                nuevoCoco = new BlueCoco(x, y, velocidad);
            }
            
            // Agregar al jugador correspondiente usando GameData
            if (jugador == 1 && j1Activo && gameDataJ1 != null) {
                gameDataJ1.addCrocodile(nuevoCoco);
                System.out.println("✅ Cocodrilo agregado al Jugador 1 en (" + x + "," + y + ")");
            } else if (jugador == 2 && j2Activo && gameDataJ2 != null) {
                gameDataJ2.addCrocodile(nuevoCoco);
                System.out.println("✅ Cocodrilo agregado al Jugador 2 en (" + x + "," + y + ")");
            } else {
                System.out.println("❌ No se pudo agregar el cocodrilo - Jugador no activo");
            }
            
        } catch (Exception e) {
            System.out.println("❌ Error al agregar cocodrilo: " + e.getMessage());
            scanner.nextLine(); // Limpiar buffer en caso de error
        }
    }

    private static void agregarFruta() {
        try {
            System.out.println("\n--- AGREGAR FRUTA ---");
            
            // Seleccionar jugador
            int jugador = seleccionarJugador();
            if (jugador == 0) return;
            
            // Seleccionar tipo de fruta
            System.out.println("Tipos de fruta:");
            System.out.println("1. BANANA");
            System.out.println("2. STRAWBERRY");
            System.out.println("3. ORANGE");
            System.out.print("Seleccione tipo: ");
            int tipo = scanner.nextInt();
            
            String tipoFruta;
            switch (tipo) {
                case 1: tipoFruta = "BANANA"; break;
                case 2: tipoFruta = "STRAWBERRY"; break;
                case 3: tipoFruta = "ORANGE"; break;
                default:
                    System.out.println("Tipo inválido.");
                    return;
            }
            
            // Ingresar posición
            System.out.print("Posición X: ");
            int x = scanner.nextInt();
            System.out.print("Posición Y: ");
            int y = scanner.nextInt();
            
            // Crear fruta
            Fruit nuevaFruta = new Fruit(x, y, tipoFruta);
            
            // Agregar al jugador correspondiente usando GameData
            if (jugador == 1 && j1Activo && gameDataJ1 != null) {
                gameDataJ1.addFruit(nuevaFruta);
                System.out.println("✅ Fruta " + tipoFruta + " agregada al Jugador 1 en (" + x + "," + y + ")");
            } else if (jugador == 2 && j2Activo && gameDataJ2 != null) {
                gameDataJ2.addFruit(nuevaFruta);
                System.out.println("✅ Fruta " + tipoFruta + " agregada al Jugador 2 en (" + x + "," + y + ")");
            } else {
                System.out.println("❌ No se pudo agregar la fruta - Jugador no activo");
            }
            
        } catch (Exception e) {
            System.out.println("❌ Error al agregar fruta: " + e.getMessage());
            scanner.nextLine(); // Limpiar buffer en caso de error
        }
    }

    private static int seleccionarJugador() {
        System.out.println("Seleccionar jugador:");
        System.out.println("1. Jugador 1" + (j1Activo ? " (ACTIVO)" : " (INACTIVO)"));
        System.out.println("2. Jugador 2" + (j2Activo ? " (ACTIVO)" : " (INACTIVO)"));
        System.out.print("Seleccione jugador (0 para cancelar): ");
        
        int jugador = scanner.nextInt();
        if (jugador == 0) {
            System.out.println("Operación cancelada.");
            return 0;
        }
        
        if (jugador == 1 && !j1Activo) {
            System.out.println("❌ Jugador 1 no está activo.");
            return 0;
        }
        
        if (jugador == 2 && !j2Activo) {
            System.out.println("❌ Jugador 2 no está activo.");
            return 0;
        }
        
        if (jugador < 1 || jugador > 2) {
            System.out.println("❌ Jugador inválido.");
            return 0;
        }
        
        return jugador;
    }

    // ========== GESTIÓN DE JUGADORES ==========

    private static void gestionarJugadores() {
        gestionarConexionJugador1();
        gestionarConexionJugador2();
        limpiarJugadoresDesconectados();
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
            if (servidor.getJugadoresSize() == 1 && !j2Activo || 
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
                servidor.getSpectadoresSize(servidor.J2_NAME)
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
        // Verificar si J1 estaba activo pero ahora está desconectado
        if (servidor.J1_ING && servidor.J1_desc) {
            servidor.J1_desc = false;
            System.out.println("🔌 Jugador 1 desconectado, liberando recursos...");
            limpiarJugador1();
        }
        
        // Verificar si J2 estaba activo pero ahora está desconectado
        if (j2Activo && servidor.J2_desc) {
            servidor.J2_desc = false;
            System.out.println("🔌 Jugador 2 desconectado, liberando recursos...");
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
        // Usar los métodos de GameData para actualizar cocodrilos
        if (j1Activo && gameDataJ1 != null) {
            gameDataJ1.updateCrocodiles();
        }
        
        if (j2Activo && gameDataJ2 != null) {
            gameDataJ2.updateCrocodiles();
        }
    }

    private static void actualizarFisicaJugadores() {
        // Usar los métodos de GameData para actualizar física
        if (j1Activo && gameDataJ1 != null) {
            gameDataJ1.updatePhysics();
        }
        
        if (j2Activo && gameDataJ2 != null) {
            gameDataJ2.updatePhysics();
        }
    }

    // ========== PROCESAMIENTO DE MENSAJES ==========

    private static void procesarMensajesEntrantes() {
        // Solo procesar mensajes si el jugador está activo
        if (j1Activo && !servidor.mensajes_j1.isEmpty()) {
            String mensaje = servidor.mensajes_j1.remove(0);
            procesarMovimientoJugador(mensaje, gameDataJ1, servidor.J1_NAME);
            enviarDatosJugador(servidor.J1_NAME, gameDataJ1);
        }

        if (j2Activo && !servidor.mensajes_j2.isEmpty()) {
            String mensaje = servidor.mensajes_j2.remove(0);
            procesarMovimientoJugador(mensaje, gameDataJ2, servidor.J2_NAME);
            enviarDatosJugador(servidor.J2_NAME, gameDataJ2);
        }
    }

    private static void procesarMovimientoJugador(String mensaje, GameData gameData, String nombreJugador) {
        if (gameData == null || gameData.player == null || gameData.player.isDead() || 
            gameData.collisionSystem == null || gameData.gravitySystem == null) return;
        
        try {
            int movimiento = Integer.parseInt(mensaje);
            String accion = "";
            
            switch (movimiento) {

            case 1: // ARRIBA/SALTO
                if (gameData.player.isOnVine()) {
                    // Movimiento en enredadera - movimiento gradual de 1 bloque
                    Coords newPos = gameData.player.calculateMoveUp();
                    if (gameData.collisionSystem.canMoveTo(newPos)) {
                        gameData.player.applyMovement(newPos, gameData.player.isFacingRight());
                        accion = "SUBIÓ por la liana";
                    } else {
                        accion = "no puede subir (obstáculo)";
                    }
                } else if (gameData.player.isOnGround()) {
                    // Salto normal desde el suelo
                    Coords[] jumpPositions = gameData.player.calculateJumpPositions();
                    Coords jumpTarget = null;
                    
                    // Buscar la máxima altura alcanzable
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
                    
                default:
                    accion = "acción desconocida: " + movimiento;
            }
            
            // Actualizar estado del jugador después del movimiento usando GameData
            gameData.collisionSystem.updatePlayerState(gameData.player, gameData.crocodiles, gameData.fruits);
            
            // Enviar confirmación
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

    // ========== RENDERIZADO ==========
    private static void limpiarConsola() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    // ========== COMUNICACIÓN ==========

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

    public static List<int[]> generarListaRandom(int cantidad) {
        Random r = new Random();
        List<int[]> lista = new ArrayList<>();

        for (int i = 0; i < cantidad; i++) {
            int x = r.nextInt(300);
            int y = r.nextInt(300);
            lista.add(new int[]{x, y});
        }
        return lista;
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
}
