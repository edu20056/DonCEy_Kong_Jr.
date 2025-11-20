package Game;

import Entities.Coco;
import Entities.RedCoco;
import Entities.BlueCoco;
import Entities.Fruit;
import Utils.Coords;
import java.util.Scanner;
import java.util.Iterator;

/**
 * Administrative controller for managing game inputs and modifying GameData.
 * Provides a clean interface for console interactions and game modifications.
 */
public class GameAdmin {
    private Scanner scanner;
    private boolean menuActivo;
    private GameData gameDataJ1;
    private GameData gameDataJ2;
    private boolean j1Activo;
    private boolean j2Activo;
    private Thread menuThread;
    
    /**
     * Constructs a GameAdmin instance
     */
    public GameAdmin() {
        this.scanner = new Scanner(System.in);
        this.menuActivo = true; // Siempre activo
        this.gameDataJ1 = null;
        this.gameDataJ2 = null;
        this.j1Activo = false;
        this.j2Activo = false;
        this.menuThread = null;
        
        // Iniciar el menú inmediatamente
        startInteractiveMenu();
    }
    
    /**
     * Starts the interactive admin menu in a separate thread
     */
    public void startInteractiveMenu() {
        // Si ya hay un hilo corriendo, no hacer nada
        if (menuThread != null && menuThread.isAlive()) {
            return;
        }
        
        this.menuActivo = true;
        this.menuThread = new Thread(() -> {
            mostrarMenuInteractivo();
        });
        this.menuThread.setDaemon(true);
        this.menuThread.start();
        System.out.println("🎮 Menú administrador INICIADO");
    }
    
    /**
     * Stops the admin menu
     */
    public void stopMenu() {
        this.menuActivo = false;
        System.out.println("🔴 Menú administrador DETENIDO");
    }
    
    /**
     * Completely restarts the admin menu
     */
    public void restartMenu() {
        System.out.println("🔄 Reiniciando menú administrador...");
        stopMenu();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {}
        startInteractiveMenu();
    }
    
    /**
     * Checks if the admin menu is active
     */
    public boolean isMenuActive() {
        return menuActivo;
    }
    
    /**
     * Updates the game data references
     */
    public void updateGameData(GameData gameDataJ1, GameData gameDataJ2, boolean j1Activo, boolean j2Activo) {
        this.gameDataJ1 = gameDataJ1;
        this.gameDataJ2 = gameDataJ2;
        this.j1Activo = j1Activo;
        this.j2Activo = j2Activo;
        
        // Si el menú no está activo pero debería estarlo, reactivarlo
        if (!menuActivo) {
            System.out.println("🎮 Reactivando menú administrador...");
            restartMenu();
        }
    }
    
    // ========== PUBLIC ADMIN METHODS ==========
    
    /**
     * Displays the current game status for both players
     */
    public void mostrarEstadoActual() {
        limpiarConsola();
        System.out.println("=== ESTADO ACTUAL DEL JUEGO ===");
        
        System.out.println("JUGADOR 1: " + (j1Activo ? "🟢 ACTIVO" : "🔴 INACTIVO"));
        if (j1Activo && gameDataJ1 != null) {
            System.out.println("  - Nivel: " + gameDataJ1.lvl);
            System.out.println("  - Posición: (" + gameDataJ1.player.getPosition().getX() + 
                             "," + gameDataJ1.player.getPosition().getY() + ")");
            System.out.println("  - Puntos: " + gameDataJ1.player.getPoints());
            System.out.println("  - Cocodrilos: " + gameDataJ1.crocodiles.size());
            System.out.println("  - Frutas: " + gameDataJ1.fruits.size());
            System.out.println("  - Estado: " + (gameDataJ1.player.isDead() ? "💀 MUERTO" : "❤️ VIVO"));
            
            // Mostrar frutas disponibles
            if (!gameDataJ1.fruits.isEmpty()) {
                System.out.println("  - Frutas disponibles:");
                for (int i = 0; i < gameDataJ1.fruits.size(); i++) {
                    Fruit fruta = gameDataJ1.fruits.get(i);
                    System.out.println("    " + fruta.getType() + " en (" + 
                                     fruta.getPosition().getX() + "," + fruta.getPosition().getY() + ")");
                }
            }
        } else {
            System.out.println("  - Esperando conexión...");
        }
        
        System.out.println("JUGADOR 2: " + (j2Activo ? "🟢 ACTIVO" : "🔴 INACTIVO"));
        if (j2Activo && gameDataJ2 != null) {
            System.out.println("  - Nivel: " + gameDataJ2.lvl);
            System.out.println("  - Posición: (" + gameDataJ2.player.getPosition().getX() + 
                             "," + gameDataJ2.player.getPosition().getY() + ")");
            System.out.println("  - Puntos: " + gameDataJ2.player.getPoints());
            System.out.println("  - Cocodrilos: " + gameDataJ2.crocodiles.size());
            System.out.println("  - Frutas: " + gameDataJ2.fruits.size());
            System.out.println("  - Estado: " + (gameDataJ2.player.isDead() ? "💀 MUERTO" : "❤️ VIVO"));
            
            // Mostrar frutas disponibles
            if (!gameDataJ2.fruits.isEmpty()) {
                System.out.println("  - Frutas disponibles:");
                for (int i = 0; i < gameDataJ2.fruits.size(); i++) {
                    Fruit fruta = gameDataJ2.fruits.get(i);
                    System.out.println("    " + fruta.getType() + " en (" + 
                                     fruta.getPosition().getX() + "," + fruta.getPosition().getY() + ")");
                }
            }
        } else {
            System.out.println("  - Esperando conexión...");
        }
        
        System.out.println("Menú administrador: " + (menuActivo ? "🟢 ACTIVO" : "🔴 INACTIVO"));
        
        esperarEnter();
    }
    
    /**
     * Adds a crocodile to the specified player's game
     */
    public void agregarCocodrilo() {
        limpiarConsola();
        try {
            System.out.println("--- AGREGAR COCODRILO ---");
            
            if (!hayJugadoresActivos()) {
                System.out.println("❌ No hay jugadores activos para agregar cocodrilos.");
                esperarEnter();
                return;
            }
            
            int jugador = seleccionarJugador();
            if (jugador == 0) {
                esperarEnter();
                return;
            }
            
            System.out.println("Tipos de cocodrilo:");
            System.out.println("1. Rojo (se mueve verticalmente en lianas)");
            System.out.println("2. Azul (cae de las lianas)");
            System.out.print("Seleccione tipo: ");
            int tipo = scanner.nextInt();
            
            if (tipo < 1 || tipo > 2) {
                System.out.println("❌ Tipo inválido.");
                esperarEnter();
                return;
            }
            
            System.out.print("Posición X: ");
            int x = scanner.nextInt();
            System.out.print("Posición Y: ");
            int y = scanner.nextInt();
            
            System.out.print("Velocidad (1-5, donde 1 es lento y 5 es rápido): ");
            int velocidad = scanner.nextInt();
            
            // Validar velocidad
            if (velocidad < 1 || velocidad > 5) {
                System.out.println("❌ Velocidad debe estar entre 1 y 5.");
                esperarEnter();
                return;
            }
            
            Coco nuevoCoco;
            if (tipo == 1) {
                nuevoCoco = new RedCoco(x, y, velocidad);
                System.out.println("✅ Cocodrilo ROJO creado");
            } else {
                nuevoCoco = new BlueCoco(x, y, velocidad);
                System.out.println("✅ Cocodrilo AZUL creado");
            }
            
            // Agregar al jugador correspondiente
            if (jugador == 1 && j1Activo && gameDataJ1 != null) {
                gameDataJ1.addCrocodile(nuevoCoco);
                System.out.println("✅ Cocodrilo agregado al Jugador 1 en (" + x + "," + y + ") con velocidad " + velocidad);
            } else if (jugador == 2 && j2Activo && gameDataJ2 != null) {
                gameDataJ2.addCrocodile(nuevoCoco);
                System.out.println("✅ Cocodrilo agregado al Jugador 2 en (" + x + "," + y + ") con velocidad " + velocidad);
            } else {
                System.out.println("❌ No se pudo agregar el cocodrilo - Jugador no activo");
            }
            
        } catch (Exception e) {
            System.out.println("❌ Error al agregar cocodrilo: " + e.getMessage());
            scanner.nextLine(); // Limpiar buffer
        }
        esperarEnter();
    }
    
    /**
     * Adds a fruit to the specified player's game
     */
    public void agregarFruta() {
        limpiarConsola();
        try {
            System.out.println("--- AGREGAR FRUTA ---");
            
            if (!hayJugadoresActivos()) {
                System.out.println("❌ No hay jugadores activos para agregar frutas.");
                esperarEnter();
                return;
            }
            
            int jugador = seleccionarJugador();
            if (jugador == 0) {
                esperarEnter();
                return;
            }
            
            System.out.println("Tipos de fruta:");
            System.out.println("1. BANANA");
            System.out.println("2. STRAWBERRY"); 
            System.out.println("3. ORANGE");
            System.out.print("Seleccione tipo: ");
            int tipo = scanner.nextInt();
            
            String tipoFruta;
            switch (tipo) {
                case 1: 
                    tipoFruta = "BANANA"; 
                    break;
                case 2: 
                    tipoFruta = "STRAWBERRY"; 
                    break;
                case 3: 
                    tipoFruta = "ORANGE"; 
                    break;
                default:
                    System.out.println("❌ Tipo inválido.");
                    esperarEnter();
                    return;
            }
            
            System.out.print("Posición X: ");
            int x = scanner.nextInt();
            System.out.print("Posición Y: ");
            int y = scanner.nextInt();
            
            Fruit nuevaFruta = new Fruit(x, y, tipoFruta);
            
            // Agregar al jugador correspondiente
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
            scanner.nextLine(); // Limpiar buffer
        }
        esperarEnter();
    }
    
    /**
     * Removes a fruit from the specified player's game by coordinates
     */
    public void eliminarFruta() {
        limpiarConsola();
        try {
            System.out.println("--- ELIMINAR FRUTA ---");
            
            if (!hayJugadoresActivos()) {
                System.out.println("❌ No hay jugadores activos para eliminar frutas.");
                esperarEnter();
                return;
            }
            
            int jugador = seleccionarJugador();
            if (jugador == 0) {
                esperarEnter();
                return;
            }
            
            GameData gameData = (jugador == 1) ? gameDataJ1 : gameDataJ2;
            String nombreJugador = (jugador == 1) ? "Jugador 1" : "Jugador 2";
            
            if (gameData == null || gameData.fruits.isEmpty()) {
                System.out.println("❌ " + nombreJugador + " no tiene frutas para eliminar.");
                esperarEnter();
                return;
            }
            
            // Mostrar frutas disponibles
            System.out.println("Frutas disponibles para " + nombreJugador + ":");
            for (Fruit fruta : gameData.fruits) {
                System.out.println("  - " + fruta.getType() + " en (" + 
                                 fruta.getPosition().getX() + "," + fruta.getPosition().getY() + ")");
            }
            
            System.out.print("Ingrese coordenada X de la fruta a eliminar: ");
            int x = scanner.nextInt();
            System.out.print("Ingrese coordenada Y de la fruta a eliminar: ");
            int y = scanner.nextInt();
            
            // Buscar y eliminar la fruta en las coordenadas especificadas
            boolean frutaEncontrada = false;
            Iterator<Fruit> iterator = gameData.fruits.iterator();
            while (iterator.hasNext()) {
                Fruit fruta = iterator.next();
                if (fruta.getPosition().getX() == x && fruta.getPosition().getY() == y) {
                    iterator.remove();
                    System.out.println("✅ Fruta " + fruta.getType() + " eliminada de " + nombreJugador + " en (" + x + "," + y + ")");
                    frutaEncontrada = true;
                    break;
                }
            }
            
            if (!frutaEncontrada) {
                System.out.println("❌ No se encontró ninguna fruta en las coordenadas (" + x + "," + y + ") para " + nombreJugador);
            }
            
        } catch (Exception e) {
            System.out.println("❌ Error al eliminar fruta: " + e.getMessage());
            scanner.nextLine(); // Limpiar buffer
        }
        esperarEnter();
    }
    
    /**
     * Forces a level change for the specified player
     */
    public void cambiarNivel() {
        limpiarConsola();
        try {
            System.out.println("--- CAMBIAR NIVEL ---");
            
            if (!hayJugadoresActivos()) {
                System.out.println("❌ No hay jugadores activos para cambiar nivel.");
                esperarEnter();
                return;
            }
            
            int jugador = seleccionarJugador();
            if (jugador == 0) {
                esperarEnter();
                return;
            }
            
            System.out.print("Ingrese el nuevo nivel: ");
            int nuevoNivel = scanner.nextInt();
            
            if (nuevoNivel < 1) {
                System.out.println("❌ El nivel debe ser mayor o igual a 1.");
                esperarEnter();
                return;
            }
            
            if (jugador == 1 && j1Activo && gameDataJ1 != null) {
                gameDataJ1.lvl = nuevoNivel;
                System.out.println("✅ Jugador 1 cambiado al nivel " + nuevoNivel);
            } else if (jugador == 2 && j2Activo && gameDataJ2 != null) {
                gameDataJ2.lvl = nuevoNivel;
                System.out.println("✅ Jugador 2 cambiado al nivel " + nuevoNivel);
            } else {
                System.out.println("❌ No se pudo cambiar el nivel - Jugador no activo");
            }
            
        } catch (Exception e) {
            System.out.println("❌ Error al cambiar nivel: " + e.getMessage());
            scanner.nextLine();
        }
        esperarEnter();
    }
    
    /**
     * Resets a player to spawn point
     */
    public void resetearJugador() {
        limpiarConsola();
        try {
            System.out.println("--- RESETEAR JUGADOR ---");
            
            if (!hayJugadoresActivos()) {
                System.out.println("❌ No hay jugadores activos para resetear.");
                esperarEnter();
                return;
            }
            
            int jugador = seleccionarJugador();
            if (jugador == 0) {
                esperarEnter();
                return;
            }
            
            if (jugador == 1 && j1Activo && gameDataJ1 != null) {
                gameDataJ1.player.respawn(gameDataJ1.spawnPoint);
                System.out.println("✅ Jugador 1 reseteado a posición inicial");
            } else if (jugador == 2 && j2Activo && gameDataJ2 != null) {
                gameDataJ2.player.respawn(gameDataJ2.spawnPoint);
                System.out.println("✅ Jugador 2 reseteado a posición inicial");
            } else {
                System.out.println("❌ No se pudo resetear - Jugador no activo");
            }
            
        } catch (Exception e) {
            System.out.println("❌ Error al resetear jugador: " + e.getMessage());
            scanner.nextLine();
        }
        esperarEnter();
    }
    
    // ========== PRIVATE METHODS ==========
    
    private void mostrarMenuInteractivo() {
        System.out.println("🎮 Menú administrador ACTIVO");
        
        while (menuActivo) {
            try {
                limpiarConsola();
                mostrarOpcionesMenu();
                
                // Leer input del usuario
                System.out.print("Seleccione una opción: ");
                if (scanner.hasNextInt()) {
                    int opcion = scanner.nextInt();
                    scanner.nextLine(); // Limpiar buffer
                    procesarOpcionMenu(opcion);
                } else {
                    // Si no es un número, limpiar el buffer y continuar
                    scanner.nextLine();
                    System.out.println("❌ Por favor ingrese un número válido.");
                    Thread.sleep(1000);
                }
                
            } catch (Exception e) {
                System.out.println("⚠️  Error en menú: " + e.getMessage());
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ie) {}
            }
        }
        
        System.out.println("🔴 Menú administrador DETENIDO");
    }
    
    private void mostrarOpcionesMenu() {
        System.out.println("=== MENÚ ADMINISTRADOR ===");
        System.out.println("Jugadores: " + 
            (j1Activo ? "J1🟢" : "J1🔴") + " " + 
            (j2Activo ? "J2🟢" : "J2🔴"));
        System.out.println("1. Agregar Cocodrilo");
        System.out.println("2. Agregar Fruta");
        System.out.println("3. Eliminar Fruta");
        System.out.println("4. Mostrar Estado Actual");
        System.out.println("5. Cambiar Nivel");
        System.out.println("6. Resetear Jugador");
        System.out.println("7. Salir del Menú");
        System.out.println(); // Línea en blanco para separar
    }
    
    private void procesarOpcionMenu(int opcion) {
        switch (opcion) {
            case 1:
                agregarCocodrilo();
                break;
            case 2:
                agregarFruta();
                break;
            case 3:
                eliminarFruta();
                break;
            case 4:
                mostrarEstadoActual();
                break;
            case 5:
                cambiarNivel();
                break;
            case 6:
                resetearJugador();
                break;
            case 7:
                menuActivo = false;
                System.out.println("👋 Menú administrador desactivado.");
                break;
            default:
                System.out.println("❌ Opción inválida. Por favor seleccione 1-7.");
                esperarEnter();
        }
    }
    
    private int seleccionarJugador() {
        System.out.println("Seleccionar jugador:");
        System.out.println("1. Jugador 1" + (j1Activo ? " 🟢 ACTIVO" : " 🔴 INACTIVO"));
        System.out.println("2. Jugador 2" + (j2Activo ? " 🟢 ACTIVO" : " 🔴 INACTIVO"));
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
    
    private boolean hayJugadoresActivos() {
        return j1Activo || j2Activo;
    }
    
    /**
     * Clears the console screen
     */
    private void limpiarConsola() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }
    
    /**
     * Waits for user to press Enter
     */
    private void esperarEnter() {
        System.out.println("\nPresione Enter para continuar...");
        try {
            System.in.read();
            scanner.nextLine(); // Limpiar el buffer
        } catch (Exception e) {
            // Ignorar excepciones
        }
    }
    
    /**
     * Clean up resources
     */
    public void cleanup() {
        this.menuActivo = false;
        if (scanner != null) {
            scanner.close();
        }
    }
}
