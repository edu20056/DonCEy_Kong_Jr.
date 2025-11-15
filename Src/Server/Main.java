import java.net.Socket;
import java.util.*;

import Network.Server;
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
    private static final Coords SPAWN_J1 = new Coords(2, 1);
    private static final Coords SPAWN_J2 = new Coords(8, 1);
    private static final int GAME_LOOP_DELAY = 200;
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
    
    // Control de estado
    private static boolean j1Activo = false;
    private static boolean j2Activo = false;
    
    // Entidades por jugador (inicialmente listas vacías)
    private static List<Coco> cocodrilosJ1 = new ArrayList<>();
    private static List<Coco> cocodrilosJ2 = new ArrayList<>();
    private static List<Fruit> frutasJ1 = new ArrayList<>();
    private static List<Fruit> frutasJ2 = new ArrayList<>();

    // ========== INICIALIZACIÓN ==========

    public static void main(String[] args) {
        servidor = new Server();
        servidor.iniciar();
        
        System.out.println("=== SERVIDOR INICIADO ===");
        System.out.println("Esperando conexiones de clientes...");
        System.out.println("Los mundos se crearán cuando los jugadores se conecten");
        
        Thread gameThread = new Thread(() -> {
            while (true) {
                gestionarJugadores();
                procesarMensajesEntrantes();
                actualizarJuego();
                renderWorld();
                
                try {
                    Thread.sleep(GAME_LOOP_DELAY);
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        gameThread.setDaemon(true);
        gameThread.start();

        ejecutarInterfazUsuario();
    }

    // ========== GESTIÓN DE JUGADORES ==========

    private static void gestionarJugadores() {
        gestionarConexionJugador1();
        gestionarConexionJugador2();
        limpiarJugadoresDesconectados();
    }

    private static void gestionarConexionJugador1() {
        // Solo crear jugador 1 si hay al menos 1 jugador conectado y no está activo
        if (!j1Activo && servidor.getJugadoresSize() >= 1) {
            try {
                System.out.println("🔄 Inicializando mundo para Jugador 1...");
                
                // Crear mundo y sistemas para J1
                world1 = new World(LEVEL_PATH);
                collisionSystem1 = new CollisionSystem(world1);
                gravitySystem1 = new GravitySystem(collisionSystem1);
                player1 = new Player(SPAWN_J1.getX(), SPAWN_J1.getY());
                
                // Inicializar estado del jugador
                collisionSystem1.updatePlayerState(player1);
                
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

    private static void gestionarConexionJugador2() {
        // Solo crear jugador 2 si hay al menos 2 jugadores conectados y no está activo
        if (!j2Activo && servidor.getJugadoresSize() >= 2) {
            try {
                System.out.println("🔄 Inicializando mundo para Jugador 2...");
                
                // Crear mundo y sistemas para J2
                world2 = new World(LEVEL_PATH);
                collisionSystem2 = new CollisionSystem(world2);
                gravitySystem2 = new GravitySystem(collisionSystem2);
                player2 = new Player(SPAWN_J2.getX(), SPAWN_J2.getY());
                
                // Inicializar estado del jugador
                collisionSystem2.updatePlayerState(player2);
                
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
        if (j1Activo && servidor.getSocketJugador(servidor.J1_NAME) == null) {
            System.out.println("🔌 Jugador 1 desconectado, liberando recursos...");
            limpiarJugador1();
        }
        
        // Verificar si J2 estaba activo pero ahora está desconectado
        if (j2Activo && servidor.getSocketJugador(servidor.J2_NAME) == null) {
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
        System.out.println("🗑️  Recursos de Jugador 2 liberados");
    }

    // ========== INICIALIZACIÓN DE ENTIDADES ==========

    private static void inicializarCocodrilosJ1() {
        cocodrilosJ1.clear();
        cocodrilosJ1.add(new RedCoco(11, 6));
        cocodrilosJ1.add(new BlueCoco(0, 6));
    }

    private static void inicializarCocodrilosJ2() {
        cocodrilosJ2.clear();
        cocodrilosJ2.add(new RedCoco(7, 3));
        cocodrilosJ2.add(new BlueCoco(6, 2));
    }

    private static void inicializarFrutasJ1() {
        frutasJ1.clear();
        frutasJ1.add(new Fruit(5, 2, "MANZANA"));
        frutasJ1.add(new Fruit(3, 4, "BANANA"));
        frutasJ1.add(new Fruit(7, 3, "FRUTILLA"));
        frutasJ1.add(new Fruit(2, 5, "UVA"));
        frutasJ1.add(new Fruit(6, 6, "NARANJA"));
    }

    private static void inicializarFrutasJ2() {
        frutasJ2.clear();
        frutasJ2.add(new Fruit(5, 2, "MANZANA"));
        frutasJ2.add(new Fruit(3, 4, "BANANA"));
        frutasJ2.add(new Fruit(7, 3, "FRUTILLA"));
        frutasJ2.add(new Fruit(2, 5, "UVA"));
        frutasJ2.add(new Fruit(6, 6, "NARANJA"));
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
                if (cocodrilo.isActivo()) {
                    cocodrilo.actualizar(world1);
                }
            }
            cocodrilosJ1.removeIf(c -> !c.isActivo());
        }
        
        if (j2Activo && world2 != null) {
            for (Coco cocodrilo : cocodrilosJ2) {
                if (cocodrilo.isActivo()) {
                    cocodrilo.actualizar(world2);
                }
            }
            cocodrilosJ2.removeIf(c -> !c.isActivo());
        }
    }

    private static void actualizarFisicaJugadores() {
        // Solo actualizar física si el jugador está activo y tiene sistemas
        if (j1Activo && gravitySystem1 != null && player1 != null && !player1.isDead()) {
            gravitySystem1.applyGravity(player1);
            collisionSystem1.updatePlayerState(player1, cocodrilosJ1, frutasJ1);
        }
        
        if (j2Activo && gravitySystem2 != null && player2 != null && !player2.isDead()) {
            gravitySystem2.applyGravity(player2);
            collisionSystem2.updatePlayerState(player2, cocodrilosJ2, frutasJ2);
        }
    }

    // ========== PROCESAMIENTO DE MENSAJES ==========

    private static void procesarMensajesEntrantes() {
        // Solo procesar mensajes si el jugador está activo
        if (j1Activo && !servidor.mensajes_j1.isEmpty()) {
            String mensaje = servidor.mensajes_j1.remove(0);
            procesarMovimientoJugador(mensaje, player1, collisionSystem1, gravitySystem1, cocodrilosJ1, frutasJ1, servidor.J1_NAME);
            enviarDatosJugador(servidor.J1_NAME, player1, frutasJ1);
        }

        if (j2Activo && !servidor.mensajes_j2.isEmpty()) {
            String mensaje = servidor.mensajes_j2.remove(0);
            procesarMovimientoJugador(mensaje, player2, collisionSystem2, gravitySystem2, cocodrilosJ2, frutasJ2, servidor.J2_NAME);
            enviarDatosJugador(servidor.J2_NAME, player2, frutasJ2);
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
                case 1: // ARRIBA
                    if (jugador.isOnGround()) {
                        jugador.jump(gravity, collision);
                        accion = "SALTÓ desde el suelo";
                    } else if (jugador.isOnVine()) {
                        jugador.moveUp(collision);
                        accion = "SUBIÓ por la liana";
                    } else {
                        accion = "no puede moverse arriba";
                    }
                    break;
                case 2: // Derecha
                    jugador.moveRight(collision);
                    accion = "se movió DERECHA";
                    break;
                case 3: // Abajo
                    jugador.moveDown(collision);
                    accion = "se movió ABAJO";
                    break;
                case 4: // Izquierda
                    jugador.moveLeft(collision);
                    accion = "se movió IZQUIERDA";
                    break;
                default:
                    accion = "acción desconocida: " + movimiento;
            }
            
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

    private static void enviarConfirmacionMovimiento(String nombreJugador, String accion, Player jugador) {
        String estadoActual = obtenerEstadoJugador(jugador, nombreJugador);
        String mensajeCompleto = nombreJugador + " " + accion + " | " + estadoActual;
        
        Socket socket = servidor.getSocketJugador(nombreJugador);
        if (socket != null) {
            servidor.enviarA(socket, mensajeCompleto);
        }
        servidor.enviarAMisEspectadores(nombreJugador, mensajeCompleto);
    }

    // ========== RENDERIZADO ==========

    public static void renderWorld() {
        limpiarConsola();
        
        StringBuilder sb = new StringBuilder();
        sb.append("=== SERVIDOR - VISTA GENERAL ===\n\n");
        
        sb.append("=== JUGADOR 1 ===\n");
        if (j1Activo && player1 != null && world1 != null) {
            renderMundoIndividual(sb, world1, player1, cocodrilosJ1, frutasJ1, "J1");
        } else {
            sb.append("NO CONECTADO\n");
        }
        
        sb.append("\n=== JUGADOR 2 ===\n");
        if (j2Activo && player2 != null && world2 != null) {
            renderMundoIndividual(sb, world2, player2, cocodrilosJ2, frutasJ2, "J2");
        } else {
            sb.append("NO CONECTADO\n");
        }
        
        sb.append("\nJugadores conectados: ").append(servidor.getJugadoresSize()).append("\n");
        sb.append("Esperando conexiones...\n");
        System.out.print(sb.toString());
    }
    
    private static void limpiarConsola() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }
    
    private static void renderMundoIndividual(StringBuilder sb, World world, Player jugador, 
                                             List<Coco> cocodrilos, List<Fruit> frutas, String nombre) {
        int width = world.getWidth();
        int height = world.getHeight();
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Coords currentPos = new Coords(x, y);
                char symbol = obtenerSimboloPosicion(currentPos, jugador, cocodrilos, frutas, world);
                sb.append(symbol);
            }
            sb.append('\n');
        }
        
        sb.append(obtenerInfoEstadoJugador(jugador, cocodrilos, frutas, nombre));
    }
    
    private static char obtenerSimboloPosicion(Coords pos, Player jugador, List<Coco> cocodrilos, List<Fruit> frutas, World world) {
        // Verificar jugador
        if (jugador != null && !jugador.isDead() && jugador.getPosition().equals(pos)) {
            return jugador.isFacingRight() ? '→' : '←';
        }
        
        // Verificar cocodrilos
        for (Coco cocodrilo : cocodrilos) {
            if (cocodrilo.isActivo() && cocodrilo.getPosition().equals(pos)) {
                return cocodrilo.getTipo().equals("ROJO") ? 'R' : 'A';
            }
        }
        
        // Verificar frutas
        for (Fruit fruta : frutas) {
            if (fruta.isActiva() && fruta.getPosition().equals(pos)) {
                return obtenerSimboloFruta(fruta.getTipo());
            }
        }
        
        // Mostrar tile del mundo
        TileType tile = world.getTile(pos);
        switch (tile) {
            case EMPTY: return ' ';
            case VINE: return 'H';
            case PLATFORM: return '=';
            case WATER: return '~';
            case GOAL: return 'X';
            default: return '?';
        }
    }
    
    private static char obtenerSimboloFruta(String tipo) {
        switch (tipo) {
            case "MANZANA": return 'M';
            case "BANANA": return 'B';
            case "FRUTILLA": return 'F';
            case "UVA": return 'U';
            case "NARANJA": return 'N';
            default: return '?';
        }
    }
    
    private static String obtenerInfoEstadoJugador(Player jugador, List<Coco> cocodrilos, List<Fruit> frutas, String nombre) {
        Coords pos = jugador.getPosition();
        StringBuilder info = new StringBuilder();
        
        info.append(String.format("Pos(%d,%d) ", pos.getX(), pos.getY()));
        info.append("Puntos: ").append(jugador.getPoints()).append(" | ");
        if (jugador.isOnGround()) info.append("[SUELO] ");
        if (jugador.isOnVine()) info.append("[ENREDADERA] ");
        if (jugador.isClimbing()) info.append("[ESCALANDO] ");
        if (jugador.isDead()) info.append("[MUERTO] ");
        info.append("\n");
            
        int cocodrilosActivos = (int) cocodrilos.stream().filter(Coco::isActivo).count();
        int frutasActivas = (int) frutas.stream().filter(Fruit::isActiva).count();
        info.append("Cocodrilos: ").append(cocodrilosActivos)
            .append(" | Frutas: ").append(frutasActivas).append("/").append(frutas.size())
            .append("\n");
        
        return info.toString();
    }

    // ========== COMUNICACIÓN ==========

    private static void enviarDatosJugador(String nombreJugador, Player jugador, List<Fruit> frutas) {
        if (jugador == null) return;
        
        Socket socket = servidor.getSocketJugador(nombreJugador);
        if (socket != null) {
            Coords pos = jugador.getPosition();
            String json = generarJSON(pos.getX(), pos.getY(), frutas);
            servidor.enviarA(socket, json);
            servidor.enviarAMisEspectadores(nombreJugador, json);
        }
    }

    public static String generarJSON(int jx, int jy, List<Fruit> frutas) {
        List<int[]> entidadesRandom = generarListaRandom(3);
        
        StringBuilder sb = new StringBuilder();
        sb.append("{");

        // Jugador
        sb.append("\"jugador\": {");
        sb.append("\"x\": ").append(jx).append(", ");
        sb.append("\"y\": ").append(jy).append(", ");
        sb.append("\"puntos\": ").append(jx); // Usar player.getPoints() cuando esté disponible
        sb.append("},");

        // Entidades
        sb.append("\"entidades\": [");
        for (int i = 0; i < entidadesRandom.size(); i++) {
            int[] e = entidadesRandom.get(i);
            sb.append("{\"tipo\": \"entidad\", \"x\": ").append(e[0])
            .append(", \"y\": ").append(e[1]).append("}");
            if (i < entidadesRandom.size() - 1) sb.append(",");
        }
        sb.append("],");

        // Frutas REALES
        sb.append("\"frutas\": [");
        if (frutas != null) {
            int frutasActivas = 0;
            for (Fruit fruta : frutas) {
                if (fruta.isActiva()) {
                    if (frutasActivas > 0) sb.append(",");
                    Coords pos = fruta.getPosition();
                    sb.append("{\"tipo\": \"").append(fruta.getTipo())
                      .append("\", \"x\": ").append(pos.getX())
                      .append(", \"y\": ").append(pos.getY())
                      .append(", \"puntos\": ").append(fruta.getPuntos())
                      .append("}");
                    frutasActivas++;
                }
            }
        }
        sb.append("]");

        sb.append("}");
        return sb.toString();
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
            jugador.isClimbing() ? "ESCALANDO " : "",
            jugador.isOnVine() ? "ENREDADERA " : "",
            jugador.isDead() ? "MUERTO " : "");
    }

    // ========== INTERFAZ DE USUARIO ==========

    private static void ejecutarInterfazUsuario() {
        try (Scanner sc = new Scanner(System.in)) {
            while (true) {
                System.out.println("\n=== MENÚ SERVIDOR ===");
                System.out.println("1. Forzar respawn de jugadores");
                System.out.println("2. Mostrar estado detallado");
                System.out.println("3. Recargar frutas");
                System.out.println("4. Enviar mensaje manual");
                System.out.println("5. Cerrar servidor");
                System.out.print("Selecciona opción: ");
                
                String opcion = sc.nextLine();

                switch (opcion) {
                    case "1" -> forzarRespawnJugadores();
                    case "2" -> mostrarEstadoDetallado();
                    case "3" -> recargarFrutas();
                    case "4" -> servidor.opcionEnviarMensaje(sc);
                    case "5" -> {
                        servidor.cerrarServidor();
                        System.out.println("¡Servidor cerrado!");
                        return;
                    }
                    default -> System.out.println("Opción no válida.");
                }
            }
        }
    }

    private static void forzarRespawnJugadores() {
        if (j1Activo && player1 != null) {
            player1.respawn(SPAWN_J1);
            System.out.println("✓ Jugador 1 respawneado");
        }
        if (j2Activo && player2 != null) {
            player2.respawn(SPAWN_J2);
            System.out.println("✓ Jugador 2 respawneado");
        }
    }

    private static void recargarFrutas() {
        if (j1Activo) {
            inicializarFrutasJ1();
            System.out.println("✓ Frutas recargadas para J1");
        }
        if (j2Activo) {
            inicializarFrutasJ2();
            System.out.println("✓ Frutas recargadas para J2");
        }
    }

    private static void mostrarEstadoDetallado() {
        System.out.println("\n=== ESTADO DETALLADO ===");
        System.out.println("J1: " + (j1Activo ? obtenerEstadoJugador(player1, "J1") : "NO CONECTADO"));
        System.out.println("J2: " + (j2Activo ? obtenerEstadoJugador(player2, "J2") : "NO CONECTADO"));
        System.out.println("Jugadores conectados: " + servidor.getJugadoresSize());
        
        if (j1Activo) {
            int cocodrilosActivosJ1 = (int) cocodrilosJ1.stream().filter(Coco::isActivo).count();
            int frutasActivasJ1 = (int) frutasJ1.stream().filter(Fruit::isActiva).count();
            System.out.println("Cocodrilos J1 activos: " + cocodrilosActivosJ1);
            System.out.println("Frutas J1 activas: " + frutasActivasJ1 + "/" + frutasJ1.size());
        }
        
        if (j2Activo) {
            int cocodrilosActivosJ2 = (int) cocodrilosJ2.stream().filter(Coco::isActivo).count();
            int frutasActivasJ2 = (int) frutasJ2.stream().filter(Fruit::isActiva).count();
            System.out.println("Cocodrilos J2 activos: " + cocodrilosActivosJ2);
            System.out.println("Frutas J2 activas: " + frutasActivasJ2 + "/" + frutasJ2.size());
        }
    }
}
