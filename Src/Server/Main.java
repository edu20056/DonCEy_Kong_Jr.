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

public class Main {
    // Constantes
    private static final Coords SPAWN_J1 = new Coords(0, 0);
    private static final Coords SPAWN_J2 = new Coords(8, 3);
    private static final int GAME_LOOP_DELAY = 175;
    private static final String LEVEL_PATH = "World/Levels/lvl1.txt";
    
    // Jugadores y sus sistemas (inicialmente null)
    private static Player player1 = null;
    private static Player player2 = null;
    private static World world1 = null;
    private static World world2 = null;
    private static CollisionSystem collisionSystem1 = null;
    private static CollisionSystem collisionSystem2 = null;
    private static GravitySystem gravitySystem1 = null;
    private static GravitySystem gravitySystem2 = null;
    
    private static Server servidor;
    private static AdapterJSON adapter;
    
    // Control de estado
    private static boolean j1Activo = false;
    private static boolean j2Activo = false;
    
    // Entidades por jugador (inicialmente listas vacías)
    private static List<Coco> cocodrilosJ1 = new ArrayList<>();
    private static List<Coco> cocodrilosJ2 = new ArrayList<>();
    private static List<Fruit> frutasJ1 = new ArrayList<>();
    private static List<Fruit> frutasJ2 = new ArrayList<>();

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
                        case 3:
                            mostrarEstadoActual();
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
            
            // Agregar al jugador correspondiente
            if (jugador == 1 && j1Activo) {
                cocodrilosJ1.add(nuevoCoco);
                System.out.println("✅ Cocodrilo agregado al Jugador 1 en (" + x + "," + y + ")");
            } else if (jugador == 2 && j2Activo) {
                cocodrilosJ2.add(nuevoCoco);
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
            
            // Agregar al jugador correspondiente
            if (jugador == 1 && j1Activo) {
                frutasJ1.add(nuevaFruta);
                System.out.println("✅ Fruta " + tipoFruta + " agregada al Jugador 1 en (" + x + "," + y + ")");
            } else if (jugador == 2 && j2Activo) {
                frutasJ2.add(nuevaFruta);
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

    private static void mostrarEstadoActual() {
        System.out.println("\n=== ESTADO ACTUAL ===");
        
        System.out.println("JUGADOR 1: " + (j1Activo ? "ACTIVO" : "INACTIVO"));
        if (j1Activo && player1 != null) {
            System.out.println("  - Posición: (" + player1.getPosition().getX() + "," + player1.getPosition().getY() + ")");
            System.out.println("  - Puntos: " + player1.getPoints());
            System.out.println("  - Cocodrilos: " + cocodrilosJ1.size());
            System.out.println("  - Frutas: " + frutasJ1.size());
        }
        
        System.out.println("JUGADOR 2: " + (j2Activo ? "ACTIVO" : "INACTIVO"));
        if (j2Activo && player2 != null) {
            System.out.println("  - Posición: (" + player2.getPosition().getX() + "," + player2.getPosition().getY() + ")");
            System.out.println("  - Puntos: " + player2.getPoints());
            System.out.println("  - Cocodrilos: " + cocodrilosJ2.size());
            System.out.println("  - Frutas: " + frutasJ2.size());
        }
    }

    // ========== GESTIÓN DE JUGADORES ==========

    private static void gestionarJugadores() {
        gestionarConexionJugador1();
        gestionarConexionJugador2();
        limpiarJugadoresDesconectados();
    }

    private static void gestionarConexionJugador1() {
        // Solo crear jugador 1 si hay al menos 1 jugador conectado y no está activo
        if (j1Activo) {
            Socket s1 = servidor.getSocketJugador(servidor.J1_NAME);

            if (s1 == null || s1.isClosed()) {
                return;                    
            }

            String json1 = adapter.generarJSON(player1, frutasJ1, cocodrilosJ1, servidor.J1_NAME, servidor.getSpectadoresSize(servidor.J1_NAME));
            servidor.enviarA(s1, json1);
            servidor.enviarAMisEspectadores(servidor.J1_NAME, json1);
        }
        else {
            if (servidor.getJugadoresSize() == 1 && !j2Activo || // Se conecta por primera vez J1 (aunque antes se pudo haber conectado y luego desconectado)
                servidor.getJugadoresSize() == 2 && j2Activo){ // Se conecta J1 luego de haberse desconectado mientras J2 estaba jugando.
                try {
                    System.out.println("🔄 Inicializando mundo para Jugador 1...");
                    
                    // Crear mundo y sistemas para J1
                    world1 = new World(LEVEL_PATH);
                    collisionSystem1 = new CollisionSystem(world1);
                    gravitySystem1 = new GravitySystem(collisionSystem1);
                    player1 = new Player(SPAWN_J1.getX(), SPAWN_J1.getY());
                    
                    // Inicializar estado del jugador
                    collisionSystem1.updatePlayerState(player1, null, null);
                    
                    // Inicializar entidades
                    inicializarCocodrilosJ1();
                    inicializarFrutasJ1();
                    
                    j1Activo = true;
                    servidor.J1_ING = true;
                    
                    System.out.println("✅ Jugador 1 instanciado con su propio mundo");
                    System.out.println("   - Mundo: " + world1.getWidth() + "x" + world1.getHeight());
                    System.out.println("   - Cocodrilos: " + cocodrilosJ1.size());
                    System.out.println("   - Frutas: " + frutasJ1.size());
                    

                } catch (Exception e) {
                    System.err.println("❌ Error al crear mundo para J1: " + e.getMessage());
                    limpiarJugador1();
                }
            }
        }

        if (!j1Activo && servidor.getJugadoresSize() >= 1 ) {
            
        }
    }

    private static void gestionarConexionJugador2() {
        // Solo crear jugador 2 si hay al menos 2 jugadores conectados y no está activo
        if (j2Activo) {
            Socket s2 = servidor.getSocketJugador(servidor.J2_NAME);

            if (s2 == null || s2.isClosed()) {
                servidor.J2_desc = true;   
                return;                    
            }

            String json2 = adapter.generarJSON(player2, frutasJ2, cocodrilosJ2, servidor.J2_NAME, servidor.getSpectadoresSize(servidor.J2_NAME));
            servidor.enviarA(s2, json2);
            servidor.enviarAMisEspectadores(servidor.J2_NAME, json2);
        }
        if (!j2Activo && servidor.getJugadoresSize() >= 2) {
            try {
                System.out.println("🔄 Inicializando mundo para Jugador 2...");
                
                // Crear mundo y sistemas para J2
                world2 = new World(LEVEL_PATH);
                collisionSystem2 = new CollisionSystem(world2);
                gravitySystem2 = new GravitySystem(collisionSystem2);
                player2 = new Player(SPAWN_J2.getX(), SPAWN_J2.getY());
                
                // Inicializar estado del jugador (usando el nuevo método sin parámetros)
                collisionSystem2.updatePlayerState(player2, null, null);
                
                // Inicializar entidades
                inicializarCocodrilosJ2();
                inicializarFrutasJ2();
                
                j2Activo = true;
                servidor.J2_ING = true;
                
                System.out.println("✅ Jugador 2 instanciado con su propio mundo");
                System.out.println("   - Mundo: " + world2.getWidth() + "x" + world2.getHeight());
                System.out.println("   - Cocodrilos: " + cocodrilosJ2.size());
                System.out.println("   - Frutas: " + frutasJ2.size());
                
            } catch (Exception e) {
                System.err.println("❌ Error al crear mundo para J2: " + e.getMessage());
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
        player1 = null;
        world1 = null;
        collisionSystem1 = null;
        gravitySystem1 = null;
        cocodrilosJ1.clear();
        frutasJ1.clear();
        j1Activo = false;
        servidor.J1_ING = false;
        servidor.J1_desc = false;
        System.out.println("🗑️  Recursos de Jugador 1 liberados");
    }

    private static void limpiarJugador2() {
        player2 = null;
        world2 = null;
        collisionSystem2 = null;
        gravitySystem2 = null;
        cocodrilosJ2.clear();
        frutasJ2.clear();
        j2Activo = false;
        servidor.J2_ING = false;
        servidor.J2_desc = false;
        System.out.println("🗑️  Recursos de Jugador 2 liberados");
    }

    // ========== INICIALIZACIÓN DE ENTIDADES ==========

    private static void inicializarCocodrilosJ1() {
        cocodrilosJ1.clear();
        // Crear cocodrilos con velocidad en el constructor
        cocodrilosJ1.add(new RedCoco(11, 6, 1)); // Rojo lento
        cocodrilosJ1.add(new BlueCoco(0, 6, 2)); // Azul rápido
    }

    private static void inicializarCocodrilosJ2() {
        cocodrilosJ2.clear();
        // Crear cocodrilos con velocidad en el constructor
        cocodrilosJ2.add(new RedCoco(11, 8, 1)); // Rojo lento
        cocodrilosJ2.add(new BlueCoco(0, 3, 2)); // Azul rápido
    }

    private static void inicializarFrutasJ1() {
        frutasJ1.clear();
        frutasJ1.add(new Fruit(3, 4, "BANANA"));
        frutasJ1.add(new Fruit(7, 12, "STRAWBERRY"));
        frutasJ1.add(new Fruit(6, 6, "ORANGE"));
    }

    private static void inicializarFrutasJ2() {
        frutasJ2.clear();
        frutasJ2.add(new Fruit(3, 4, "BANANA"));
        frutasJ2.add(new Fruit(7, 12, "STRAWBERRY"));
        frutasJ2.add(new Fruit(6, 6, "ORANGE"));
    }

    // ========== ACTUALIZACIÓN DEL JUEGO ==========

    private static void actualizarJuego() {
        actualizarCocodrilos();
        actualizarFisicaJugadores();
    }

    private static void actualizarCocodrilos() {
        // Solo actualizar cocodrilos si el jugador está activo
        if (j1Activo && world1 != null) {
            for (Coco cocodrilo : cocodrilosJ1) {
                if (cocodrilo.isActive()) {
                    cocodrilo.update(world1);
                }
            }
            cocodrilosJ1.removeIf(c -> !c.isActive());
        }
        
        if (j2Activo && world2 != null) {
            for (Coco cocodrilo : cocodrilosJ2) {
                if (cocodrilo.isActive()) {
                    cocodrilo.update(world2);
                }
            }
            cocodrilosJ2.removeIf(c -> !c.isActive());
        }
    }

    private static void actualizarFisicaJugadores() {
        // Solo actualizar física si el jugador está activo y tiene sistemas
        if (j1Activo && gravitySystem1 != null && player1 != null && !player1.isDead()) {
            gravitySystem1.applyGravity(player1);
            // Usar el nuevo método unificado con listas (pueden ser null)
            collisionSystem1.updatePlayerState(player1, cocodrilosJ1, frutasJ1);
        }
        
        if (j2Activo && gravitySystem2 != null && player2 != null && !player2.isDead()) {
            gravitySystem2.applyGravity(player2);
            // Usar el nuevo método unificado con listas (pueden ser null)
            collisionSystem2.updatePlayerState(player2, cocodrilosJ2, frutasJ2);
        }
    }

    // ========== PROCESAMIENTO DE MENSAJES ==========

    private static void procesarMensajesEntrantes() {
        // Solo procesar mensajes si el jugador está activo
        if (j1Activo && !servidor.mensajes_j1.isEmpty()) {
            String mensaje = servidor.mensajes_j1.remove(0);
            procesarMovimientoJugador(mensaje, player1, collisionSystem1, gravitySystem1, cocodrilosJ1, frutasJ1, servidor.J1_NAME);
            enviarDatosJugador(servidor.J1_NAME, player1, frutasJ1, cocodrilosJ1);
        }

        if (j2Activo && !servidor.mensajes_j2.isEmpty()) {
            String mensaje = servidor.mensajes_j2.remove(0);
            procesarMovimientoJugador(mensaje, player2, collisionSystem2, gravitySystem2, cocodrilosJ2, frutasJ2, servidor.J2_NAME);
            enviarDatosJugador(servidor.J2_NAME, player2, frutasJ2, cocodrilosJ2);
        }
    }

    private static void procesarMovimientoJugador(String mensaje, Player jugador, 
                                                 CollisionSystem collision, GravitySystem gravity, 
                                                 List<Coco> cocodrilos, List<Fruit> frutas, String nombreJugador) {
        if (jugador == null || jugador.isDead() || collision == null || gravity == null) return;
        
        try {
            int movimiento = Integer.parseInt(mensaje);
            String accion = "";
            
            switch (movimiento) {

            case 1: // ARRIBA/SALTO
                if (jugador.isOnVine()) {
                    // Movimiento en enredadera - movimiento gradual de 1 bloque
                    Coords newPos = jugador.calculateMoveUp();
                    if (collision.canMoveTo(newPos)) {
                        jugador.applyMovement(newPos, jugador.isFacingRight());
                        accion = "SUBIÓ por la liana";
                    } else {
                        accion = "no puede subir (obstáculo)";
                    }
                } else if (jugador.isOnGround()) {
                    // Salto normal desde el suelo
                    Coords[] jumpPositions = jugador.calculateJumpPositions();
                    Coords jumpTarget = null;
                    
                    // Buscar la máxima altura alcanzable
                    int maxAltura = 0;
                    for (int i = 0; i < jumpPositions.length; i++) {
                        if (collision.canMoveTo(jumpPositions[i])) {
                            maxAltura = i + 1;
                        } else {
                            break;
                        }
                    }
                    
                    if (maxAltura > 0) {
                        jumpTarget = jumpPositions[maxAltura - 1];
                        jugador.applyJump(jumpTarget);
                        accion = "SALTÓ " + maxAltura + " bloques de altura";
                    } else {
                        accion = "no puede saltar (obstáculo arriba)";
                    }
                } else {
                    accion = "no puede moverse arriba (en el aire)";
                }
                break;

                case 2: // Derecha
                    // NUEVO: Usar el sistema de colisión para movimiento horizontal
                    Coords rightPos = jugador.calculateMoveRight();
                    if (collision.canMoveTo(rightPos)) {
                        jugador.applyMovement(rightPos, true);
                        accion = "se movió DERECHA";
                    } else {
                        accion = "no puede moverse derecha (obstáculo)";
                    }
                    break;
                    
                case 3: // Abajo
                    // NUEVO: Usar el sistema de colisión para movimiento vertical
                    if (jugador.isOnVine()) {
                        Coords downPos = jugador.calculateMoveDown();
                        if (collision.canMoveTo(downPos)) {
                            jugador.applyMovement(downPos, jugador.isFacingRight());
                            accion = "se movió ABAJO";
                        } else {
                            accion = "no puede bajar (obstáculo)";
                        }
                    } else {
                        accion = "no puede moverse abajo (no está escalando)";
                    }
                    break;
                    
                case 4: // Izquierda
                    // NUEVO: Usar el sistema de colisión para movimiento horizontal
                    Coords leftPos = jugador.calculateMoveLeft();
                    if (collision.canMoveTo(leftPos)) {
                        jugador.applyMovement(leftPos, false);
                        accion = "se movió IZQUIERDA";
                    } else {
                        accion = "no puede moverse izquierda (obstáculo)";
                    }
                    break;
                    
                default:
                    accion = "acción desconocida: " + movimiento;
            }
            
            // Actualizar estado del jugador después del movimiento
            collision.updatePlayerState(jugador, cocodrilos, frutas);
            
            // Enviar confirmación
            String estadoActual = obtenerEstadoJugador(jugador, nombreJugador);
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

    private static void enviarDatosJugador(String nombreJugador, Player jugador, List<Fruit> frutas, List<Coco> cocos) {
        if (jugador == null) return;
        
        Socket socket = servidor.getSocketJugador(nombreJugador);
        if (socket != null) {
            String json = adapter.generarJSON(jugador, frutas, cocos, nombreJugador, servidor.getSpectadoresSize(nombreJugador));
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
