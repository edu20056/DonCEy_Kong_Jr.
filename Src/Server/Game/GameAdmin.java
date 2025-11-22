package Game;

import Entities.Coco;
import Entities.RedCoco;
import Entities.BlueCoco;
import Entities.Fruit;
import Utils.Coords;
import java.util.Scanner;
import java.util.Iterator;
import java.util.List;

/**
 * Administrative controller for managing game inputs and modifying GameData.
 */
public class GameAdmin {
    private Scanner scanner;
    private GameData gameDataJ1;
    private GameData gameDataJ2;
    private boolean j1Activo;
    private boolean j2Activo;
    
    public GameAdmin() {
        this.scanner = new Scanner(System.in);
        this.gameDataJ1 = null;
        this.gameDataJ2 = null;
        this.j1Activo = false;
        this.j2Activo = false;
    }
    
    public void mostrarMenu() {
        boolean salir = false;

        while (!salir) {
            limpiarConsola();
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
            System.out.print("Seleccione una opción: ");

            try {
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
                        salir = true;
                        System.out.println("Saliendo del menú administrativo...");
                        break;
                    default:
                        System.out.println("Opción inválida.");
                        esperarEnter();
                }
            } catch (Exception e) {
                System.out.println("Error: entrada inválida.");
                scanner.nextLine();
                esperarEnter();
            }
        }
    }
    
    public void updateGameData(GameData gameDataJ1, GameData gameDataJ2, boolean j1Activo, boolean j2Activo) {
        this.gameDataJ1 = gameDataJ1;
        this.gameDataJ2 = gameDataJ2;
        this.j1Activo = j1Activo;
        this.j2Activo = j2Activo;
    }
    
    private void mostrarEstadoActual() {
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
            
            if (!gameDataJ1.fruits.isEmpty()) {
                System.out.println("  - Frutas disponibles:");
                for (Fruit fruta : gameDataJ1.fruits) {
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
            
            if (!gameDataJ2.fruits.isEmpty()) {
                System.out.println("  - Frutas disponibles:");
                for (Fruit fruta : gameDataJ2.fruits) {
                    System.out.println("    " + fruta.getType() + " en (" + 
                                     fruta.getPosition().getX() + "," + fruta.getPosition().getY() + ")");
                }
            }
        } else {
            System.out.println("  - Esperando conexión...");
        }
        
        esperarEnter();
    }
    
    private void agregarCocodrilo() {
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
            
            GameData gameData = (jugador == 1) ? gameDataJ1 : gameDataJ2;
            
            // Mostrar posiciones válidas disponibles
            List<Coords> posicionesValidas = gameData.world.getValidEntityPositions();
            System.out.println("🌿 Posiciones válidas en lianas (" + posicionesValidas.size() + " disponibles):");
            for (int i = 0; i < Math.min(posicionesValidas.size(), 15); i++) {
                Coords pos = posicionesValidas.get(i);
                System.out.println("  (" + pos.getX() + ", " + pos.getY() + ")");
            }
            if (posicionesValidas.size() > 15) {
                System.out.println("  ... y " + (posicionesValidas.size() - 15) + " más");
            }
            
            System.out.println("Tipos de cocodrilo:");
            System.out.println("1. Rojo (se mueve verticalmente en lianas)");
            System.out.println("2. Azul (cae de las lianas)");
            System.out.print("Seleccione tipo: ");
            int tipo = scanner.nextInt();
            scanner.nextLine();
            
            if (tipo < 1 || tipo > 2) {
                System.out.println("❌ Tipo inválido.");
                esperarEnter();
                return;
            }
            
            System.out.print("Posición X: ");
            int x = scanner.nextInt();
            System.out.print("Posición Y: ");
            int y = scanner.nextInt();
            scanner.nextLine();
            
            // VERIFICAR SI LA POSICIÓN ES VÁLIDA (EN LIANA)
            Coords posicion = new Coords(x, y);
            boolean posicionValida = false;
            for (Coords posValida : posicionesValidas) {
                if (posValida.getX() == x && posValida.getY() == y) {
                    posicionValida = true;
                    break;
                }
            }
            
            if (!posicionValida) {
                System.out.println("❌ Posición inválida. Debe estar en una liana.");
                System.out.println("💡 Use una de las posiciones mostradas arriba.");
                esperarEnter();
                return;
            }
            
            System.out.print("Velocidad (1-5, donde 1 es lento y 5 es rápido): ");
            int velocidad = scanner.nextInt();
            scanner.nextLine();
            
            if (velocidad < 1 || velocidad > 5) {
                System.out.println("❌ Velocidad debe estar entre 1 y 5.");
                esperarEnter();
                return;
            }
            
            Coco nuevoCoco;
            if (tipo == 1) {
                nuevoCoco = new RedCoco(x, y, velocidad);
            } else {
                nuevoCoco = new BlueCoco(x, y, velocidad);
            }
            
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
            scanner.nextLine();
        }
        esperarEnter();
    }
    
    private void agregarFruta() {
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
            
            GameData gameData = (jugador == 1) ? gameDataJ1 : gameDataJ2;
            
            // Mostrar posiciones válidas y disponibles
            List<Coords> posicionesValidas = gameData.world.getValidEntityPositions();
            List<Coords> posicionesDisponibles = new java.util.ArrayList<>();
            
            // Filtrar posiciones que no tienen frutas
            for (Coords pos : posicionesValidas) {
                boolean ocupada = false;
                for (Fruit fruta : gameData.fruits) {
                    if (fruta.getPosition().getX() == pos.getX() && fruta.getPosition().getY() == pos.getY()) {
                        ocupada = true;
                        break;
                    }
                }
                if (!ocupada) {
                    posicionesDisponibles.add(pos);
                }
            }
            
            System.out.println("🌿 Posiciones disponibles en lianas (" + posicionesDisponibles.size() + " de " + posicionesValidas.size() + "):");
            for (int i = 0; i < Math.min(posicionesDisponibles.size(), 15); i++) {
                Coords pos = posicionesDisponibles.get(i);
                System.out.println("  (" + pos.getX() + ", " + pos.getY() + ")");
            }
            if (posicionesDisponibles.size() > 15) {
                System.out.println("  ... y " + (posicionesDisponibles.size() - 15) + " más");
            }
            
            // Sugerir una posición aleatoria disponible
            if (!posicionesDisponibles.isEmpty()) {
                Coords sugerencia = posicionesDisponibles.get((int)(Math.random() * posicionesDisponibles.size()));
                System.out.println("💡 Sugerencia: Posición disponible en (" + sugerencia.getX() + ", " + sugerencia.getY() + ")");
            }
            
            System.out.println("Tipos de fruta:");
            System.out.println("1. BANANA");
            System.out.println("2. STRAWBERRY"); 
            System.out.println("3. ORANGE");
            System.out.print("Seleccione tipo: ");
            int tipo = scanner.nextInt();
            scanner.nextLine();
            
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
            
            System.out.print("Puntos que otorga la fruta: ");
            int pts = scanner.nextInt();
            System.out.print("Posición X: ");
            int x = scanner.nextInt();
            System.out.print("Posición Y: ");
            int y = scanner.nextInt();
            scanner.nextLine();
            
            // VERIFICAR SI LA POSICIÓN ES VÁLIDA (EN LIANA)
            Coords posicion = new Coords(x, y);
            boolean posicionValida = false;
            for (Coords posValida : posicionesValidas) {
                if (posValida.getX() == x && posValida.getY() == y) {
                    posicionValida = true;
                    break;
                }
            }
            
            if (!posicionValida) {
                System.out.println("❌ Posición inválida. Debe estar en una liana.");
                System.out.println("💡 Use una de las posiciones disponibles mostradas arriba.");
                esperarEnter();
                return;
            }
            
            // VERIFICAR SI LA POSICIÓN ESTÁ OCUPADA POR OTRA FRUTA
            boolean posicionOcupada = false;
            for (Fruit fruta : gameData.fruits) {
                if (fruta.getPosition().getX() == x && fruta.getPosition().getY() == y) {
                    posicionOcupada = true;
                    break;
                }
            }
            
            if (posicionOcupada) {
                System.out.println("❌ Posición ocupada. Ya hay una fruta en (" + x + ", " + y + ")");
                System.out.println("💡 Use una posición disponible de la lista.");
                esperarEnter();
                return;
            }
            
            Fruit nuevaFruta = new Fruit(x, y, tipoFruta);
            nuevaFruta.setPoints(pts);

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
            scanner.nextLine();
        }
        esperarEnter();
    }
    
    private void eliminarFruta() {
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
            
            System.out.println("Frutas disponibles para " + nombreJugador + ":");
            for (Fruit fruta : gameData.fruits) {
                System.out.println("  - " + fruta.getType() + " en (" + 
                                 fruta.getPosition().getX() + "," + fruta.getPosition().getY() + ")");
            }
            
            System.out.print("Ingrese coordenada X de la fruta a eliminar: ");
            int x = scanner.nextInt();
            System.out.print("Ingrese coordenada Y de la fruta a eliminar: ");
            int y = scanner.nextInt();
            scanner.nextLine();
            
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
            scanner.nextLine();
        }
        esperarEnter();
    }
    
    private void cambiarNivel() {
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
            scanner.nextLine();
            
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
    
    private void resetearJugador() {
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
    
    private int seleccionarJugador() {
        System.out.println("Seleccionar jugador:");
        System.out.println("1. Jugador 1" + (j1Activo ? " 🟢 ACTIVO" : " 🔴 INACTIVO"));
        System.out.println("2. Jugador 2" + (j2Activo ? " 🟢 ACTIVO" : " 🔴 INACTIVO"));
        System.out.print("Seleccione jugador (0 para cancelar): ");
        
        int jugador = scanner.nextInt();
        scanner.nextLine();
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
    
    private void limpiarConsola() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }
    
    private void esperarEnter() {
        System.out.println("\nPresione Enter para continuar...");
        try {
            System.in.read();
            scanner.nextLine();
        } catch (Exception e) {
        }
    }
    
    public void cleanup() {
        if (scanner != null) {
            scanner.close();
        }
    }
}
