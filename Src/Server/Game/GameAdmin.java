package Game;

import java.util.Scanner;
import java.util.Iterator;
import java.util.List;

import Entities.Coco;
import Entities.RedCoco;
import Entities.BlueCoco;
import Entities.Fruit;
import Utils.Coords;

/**
 * Administrative controller for managing game inputs and modifying GameData.
 * Game administrators to manipulate game elements such as: 
 * crocodiles and fruits...
 */

public class GameAdmin {
    private Scanner scanner;
    private GameData player1GameData;
    private GameData player2GameData;
    private boolean player1Active;
    private boolean player2Active;

    public GameAdmin() {
        this.scanner = new Scanner(System.in);
        this.player1GameData = null;
        this.player2GameData = null;
        this.player1Active = false;
        this.player2Active = false;
    }
    
    /**
     * Displays the main administrative menu and handles user input.
     * The menu runs in a loop until the user chooses to exit.
     */

    public void displayMenu() {
        boolean exit = false;

        while (!exit) {
            clearConsole();

            System.out.println("┌─────────────────────────────────────────────┐");
            System.out.println("│               DonCEy Kong Jr                │");
            System.out.println("└─────────────────────────────────────────────┘");
            System.out.println("[1] Agregar Cocodrilo");
            System.out.println("[2] Agregar Fruta");
            System.out.println("[3] Remover Fruta");
            System.out.println("[4] Mostrar Lianas Disponibles");
            System.out.println("[5] Terminar");
            System.out.println("└─────────────────────────────────────────────┘");
            
            System.out.print("\n[DonkeyKongJr@Admin ~]$ ");

            try {
                int option = scanner.nextInt();
                scanner.nextLine();
                
                switch (option) {
                    case 1: addCrocodile(); break;
                    case 2: addFruit(); break;
                    case 3: removeFruit(); break;
                    case 4: showAvailableVines(); break;
                    case 5: exit = true; break;
                    default:
                        System.out.println("(¬_¬) Opción inválida...");
                        waitForEnter();
                }
            } catch (Exception e) {
                System.out.println("(¬_¬) Opción inválida...");
                scanner.nextLine();
                waitForEnter();
            }
        }
    }
    
    /**
     * Updates the game data references and player activity status.
     * This method should be called when player connections change or
     * when new game data becomes available.
     *
     * @param player1GameData the game data for player 1
     * @param player2GameData the game data for player 2
     * @param player1Active whether player 1 is currently active
     * @param player2Active whether player 2 is currently active
     */

    public void updateGameData(GameData player1GameData, GameData player2GameData, 
                              boolean player1Active, boolean player2Active) {
        this.player1GameData = player1GameData;
        this.player2GameData = player2GameData;
        this.player1Active = player1Active;
        this.player2Active = player2Active;
    }
    
    /**
     * Displays all available vine positions for the selected player.
     */

    private void showAvailableVines() {
        clearConsole();
        try {
            System.out.println("┌─────────────────────────────────────────────┐");
            System.out.println("│               DonCEy Kong Jr                │");
            System.out.println("└─────────────────────────────────────────────┘");
            System.out.println("───────────── LIANAS DISPONIBLES ─────────────");
            
            if (!arePlayersActive()) {
                System.out.println("(✖╭╮✖) No hay jugadores activos...");
                waitForEnter();
                return;
            }
            
            int player = selectPlayer();
            if (player == 0) {
                waitForEnter();
                return;
            }

            GameData gameData = (player == 1) ? player1GameData : player2GameData;
            String playerName = (player == 1) ? "Jugador 1" : "Jugador 2";
            
            List<Coords> validPositions = gameData.world.getValidEntityPositions();
            List<Coords> availablePositions = new java.util.ArrayList<>();
            
            for (Coords pos : validPositions) {
                boolean occupied = false;
                
                for (Fruit fruit : gameData.fruits) {
                    if (fruit.getPosition().getX() == pos.getX() && fruit.getPosition().getY() == pos.getY()) {
                        occupied = true;
                        break;
                    }
                }
                
                if (!occupied) {
                    availablePositions.add(pos);
                }
            }
            
            System.out.println("\n✅ Lianas Disponibles:");
            printPositionsGrid(availablePositions);
        
        } catch (Exception e) {
            System.out.println("(⊙_⊙ ✖ ) Oops... Sucedió un error!!!");
            System.out.println("!Error: " + e.getMessage());
            scanner.nextLine();
        }
        waitForEnter();
    }
    
    /**
     * Prints positions in a organized grid format for better readability.
     * 
     * @param positions list of coordinates to display
     */

    private void printPositionsGrid(List<Coords> positions) {
        if (positions.isEmpty()) {
            System.out.println("(✖╭╮✖) No hay posiciones disponibles...");
            return;
        }
        
        positions.sort((a, b) -> {
            if (a.getY() != b.getY()) {
                return Integer.compare(a.getY(), b.getY());
            }
            return Integer.compare(a.getX(), b.getX());
        });
        
        int maxY = positions.stream().mapToInt(Coords::getY).max().orElse(0);
        int yWidth = String.valueOf(maxY).length();
        
        System.out.println("\n  " + " ".repeat(yWidth) + "Y │ X disponibles");
        System.out.println("   " + "─".repeat(yWidth) + "─┼────────────────");
        
        int currentY = -1;
        
        for (Coords pos : positions) {
            if (pos.getY() != currentY) {
                if (currentY != -1) System.out.println();
                System.out.printf("   %-" + yWidth + "d │ ", pos.getY());
                currentY = pos.getY();
            }
            System.out.printf("%2d ", pos.getX());
        }
        System.out.println();
    }

    /**
     * Adds a crocodile to the selected player's game world.
     * Prompts for crocodile type, position, and speed.
     * Validates that the position is on a valid vine and within bounds.
     */

    private void addCrocodile() {
        clearConsole();
        try {
            System.out.println("┌─────────────────────────────────────────────┐");
            System.out.println("│               DonCEy Kong Jr                │");
            System.out.println("└─────────────────────────────────────────────┘");
            System.out.println("────────────── AGREGAR COCODRILO ──────────────");
            
            if (!arePlayersActive()) {
                System.out.println("(✖╭╮✖) No hay jugadores activos...");
                waitForEnter();
                return;
            }
            
            int player = selectPlayer();
            if (player == 0) {
                waitForEnter();
                return;
            }

            GameData gameData = (player == 1) ? player1GameData : player2GameData;

            List<Coords> validPositions = gameData.world.getValidEntityPositions();

            System.out.println("\nSeleccione el tipo de Cocodrilo:");
            System.out.println("[1] Rojo");
            System.out.println("[2] Azul");
            
            System.out.print("\n[DonkeyKongJr@Admin ~]$ ");
            
            int type = scanner.nextInt();
            scanner.nextLine();
            if (type < 1 || type > 2) {
                System.out.println("(¬_¬) Opción inválida...");
                waitForEnter();
                return;
            }
            
            System.out.print("\nCordenada X     > ");
            int x = scanner.nextInt();
            System.out.print("Cordenada Y     > ");
            int y = scanner.nextInt();
            scanner.nextLine();
            System.out.print("Velocidad (1-5) > ");
            int speed = scanner.nextInt();
            scanner.nextLine();
            
            Coords position = new Coords(x, y);
            boolean validPosition = false;
            for (Coords validPos : validPositions) {
                if (validPos.getX() == x && validPos.getY() == y) {
                    validPosition = true;
                    break;
                }
            }
            
            if (!validPosition) {
                System.out.println("(¬_¬) Posición inválida...");
                System.out.println("(⚠︎  Debe de ser una liana)");
                System.out.println("(💡 Use la opción 4 para ver lianas disponibles)");
                waitForEnter();
                return;
            }
            
            if (speed < 1 || speed > 5) {
                System.out.println("(¬_¬) Posición inválida...");
                System.out.println("(⚠︎  La velocidad debe de estar dentro del rango)");
                waitForEnter();
                return;
            }
            
            Coco newCrocodile;
            if (type == 1) { newCrocodile = new RedCoco(x, y, speed);
            } else { newCrocodile = new BlueCoco(x, y, speed); }
            
            if (player == 1 && player1Active && player1GameData != null) {
                player1GameData.addCrocodile(newCrocodile);
                System.out.println("(-‿◦) Operación exitosa"); 
            } else if (player == 2 && player2Active && player2GameData != null) {
                player2GameData.addCrocodile(newCrocodile);
                System.out.println("(-‿◦) Operación exitosa"); 
            } else {
                System.out.println("(⊙_⊙ ✖ ) Oops... Sucedió un error!!!");
            }
            
        } catch (Exception e) {
            System.out.println("(⊙_⊙ ✖ ) Oops... Sucedió un error!!!");
            System.out.println("!Error: " + e.getMessage());
            scanner.nextLine();
        }
        waitForEnter();
    }
    
    /**
     * Adds a fruit to the selected player's game world.
     * Prompts for fruit type, position, and point value.
     * Validates position availability and displays suggested positions.
     */

    private void addFruit() {
        clearConsole();
        try {
            System.out.println("┌─────────────────────────────────────────────┐");
            System.out.println("│               DonCEy Kong Jr                │");
            System.out.println("└─────────────────────────────────────────────┘");
            System.out.println("──────────────── AGREGAR FRUTA ────────────────");
            
            if (!arePlayersActive()) {
                System.out.println("(✖╭╮✖) No hay jugadores activos...");
                waitForEnter();
                return;
            }
            
            int player = selectPlayer();
            if (player == 0) {
                waitForEnter();
                return;
            }

            GameData gameData = (player == 1) ? player1GameData : player2GameData;
            List<Coords> validPositions = gameData.world.getValidEntityPositions();
            List<Coords> availablePositions = new java.util.ArrayList<>();
            
            for (Coords pos : validPositions) {
                boolean occupied = false;
                for (Fruit fruit : gameData.fruits) {
                    if (fruit.getPosition().getX() == pos.getX() && fruit.getPosition().getY() == pos.getY()) {
                        occupied = true;
                        break;
                    }
                }
                if (!occupied) {
                    availablePositions.add(pos);
                }
            }

            System.out.println("\nSeleccione el tipo de Fruta:");
            System.out.println("[1] BANANA");
            System.out.println("[2] FRESA");
            System.out.println("[3] NARANJA");
            
            System.out.print("\n[DonkeyKongJr@Admin ~]$ ");
            
            int type = scanner.nextInt();
            scanner.nextLine();
            
            String fruitType;
            switch (type) {
                case 1: fruitType = "BANANA"; break;
                case 2: fruitType = "STRAWBERRY"; break;
                case 3: fruitType = "ORANGE"; break;
                default:
                    System.out.println("(¬_¬) Opción inválida...");
                    waitForEnter();
                    return;
            }
            
            System.out.print("\nCordenada X     > ");
            int x = scanner.nextInt();
            System.out.print("Cordenada Y     > ");
            int y = scanner.nextInt();
            scanner.nextLine();
            System.out.print("Puntos          > ");
            int points = scanner.nextInt();
            scanner.nextLine();
            
            Coords position = new Coords(x, y);
            boolean validPosition = false;
            for (Coords validPos : validPositions) {
                if (validPos.getX() == x && validPos.getY() == y) {
                    validPosition = true;
                    break;
                }
            }
            
            if (!validPosition) {
                System.out.println("(¬_¬) Posición inválida...");
                System.out.println("(⚠︎  Debe de ser una liana)");
                System.out.println("(💡 Use la opción 4 para ver lianas disponibles)");
                waitForEnter();
                return;
            }
            
            boolean positionOccupied = false;
            for (Fruit fruit : gameData.fruits) {
                if (fruit.getPosition().getX() == x && fruit.getPosition().getY() == y) {
                    positionOccupied = true;
                    break;
                }
            }
            
            if (positionOccupied) {
                System.out.println("(¬_¬) Posición ocupada...");
                System.out.println("(⚠︎  Ya existe una fruta en esta posición)");
                waitForEnter();
                return;
            }
            
            if (points < 1) {
                System.out.println("(¬_¬) Puntos inválidos...");
                System.out.println("(⚠︎  Los puntos deben ser mayores a 0)");
                waitForEnter();
                return;
            }
            
            Fruit newFruit = new Fruit(x, y, fruitType);
            newFruit.setPoints(points);

            if (player == 1 && player1Active && player1GameData != null) {
                player1GameData.addFruit(newFruit);
                System.out.println("(-‿◦) Operación exitosa"); 
            } else if (player == 2 && player2Active && player2GameData != null) {
                player2GameData.addFruit(newFruit);
                System.out.println("(-‿◦) Operación exitosa"); 
            } else {
                System.out.println("(⊙_⊙ ✖ ) Oops... Sucedió un error!!!");
            }
            
        } catch (Exception e) {
            System.out.println("(⊙_⊙ ✖ ) Oops... Sucedió un error!!!");
            System.out.println("!Error: " + e.getMessage());
            scanner.nextLine();
        }
        waitForEnter();
    }

    /**
     * Removes a fruit from the selected player's game world.
     * Displays available fruits and prompts for coordinates to remove.
     */

    private void removeFruit() {
        clearConsole();
        try {
            System.out.println("┌─────────────────────────────────────────────┐");
            System.out.println("│               DonCEy Kong Jr                │");
            System.out.println("└─────────────────────────────────────────────┘");
            System.out.println("──────────────── REMOVER FRUTA ────────────────");
            
            if (!arePlayersActive()) {
                System.out.println("(✖╭╮✖) No hay jugadores activos...");
                waitForEnter();
                return;
            }
            
            int player = selectPlayer();
            if (player == 0) {
                waitForEnter();
                return;
            }
            
            GameData gameData = (player == 1) ? player1GameData : player2GameData;
            String playerName = (player == 1) ? "Jugador 1" : "Jugador 2";
            
            if (gameData == null || gameData.fruits.isEmpty()) {
                System.out.println("(◕‿◕) No hay frutas para remover...");
                waitForEnter();
                return;
            }
            
            System.out.println("\nFrutas disponibles para " + playerName + ":");
            int index = 1;
            for (Fruit fruit : gameData.fruits) {
                System.out.println(fruit.getType() + " (" + 
                                 fruit.getPosition().getX() + "," + fruit.getPosition().getY() + ")");
                index++;
            }
            
            System.out.print("\nCordenada X     > ");
            int x = scanner.nextInt();
            System.out.print("Cordenada Y     > ");
            int y = scanner.nextInt();
            scanner.nextLine();
            
            boolean fruitFound = false;
            Iterator<Fruit> iterator = gameData.fruits.iterator();
            while (iterator.hasNext()) {
                Fruit fruit = iterator.next();
                if (fruit.getPosition().getX() == x && fruit.getPosition().getY() == y) {
                    iterator.remove();
                    System.out.println("(-‿◦) Fruta " + fruit.getType() + " removida de " + playerName + " en (" + x + "," + y + ")");
                    fruitFound = true;
                    break;
                }
            }
            
            if (!fruitFound) {
                System.out.println("(¬_¬) Fruta no encontrada...");
                System.out.println("(⚠︎  No existe fruta en las coordenadas especificadas)");
            }
            
        } catch (Exception e) {
            System.out.println("(⊙_⊙ ✖ ) Oops... Sucedió un error!!!");
            System.out.println("!Error: " + e.getMessage());
            scanner.nextLine();
        }
        waitForEnter();
    }

    /**
     * Prompts the user to select a player for administrative actions.
     * 
     * @return the selected player number (1 or 2), or 0 if canceled
     */

    private int selectPlayer() {
        System.out.println("Seleccionar Jugador:");
        System.out.println("1. Jugador 1" + (player1Active ? " (•‿•)" : " (_ _ ) Zzz"));
        System.out.println("2. Jugador 2" + (player2Active ? " (•‿•)" : " (_ _ ) Zzz"));
        
        System.out.print("\n[DonkeyKongJr@Admin ~]$ ");
        
        int player = scanner.nextInt();
        scanner.nextLine();
        if (player == 0) { return 0; }
        
        if (player == 1 && !player1Active) {
            System.out.println("( •̀ ᴖ •́ ) Jugador 1 no activo...");
            return 0;
        }
        
        if (player == 2 && !player2Active) {
            System.out.println("( •̀ ᴖ •́ ) Jugador 2 no activo...");
            return 0;
        }
        
        if (player < 1 || player > 2) {
            System.out.println("(¬_¬) Opción inválida...");
            return 0;
        }
        
        return player;
    }
    
    /**
     * Checks if there are any active players.
     * 
     * @return true if at least one player is active, false otherwise
     */
    
    private boolean arePlayersActive() {
        return player1Active || player2Active;
    }
    
    /**
     * Clears the console screen for better menu presentation.
     */
    
    private void clearConsole() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }
    
    /**
     * Waits for the user to press Enter before continuing.
     * Provides a pause for the user to read messages.
     */

    private void waitForEnter() {
        System.out.println("\nDoble <ENTER> para continuar...");
        try {
            System.in.read();
            scanner.nextLine();
        } catch (Exception e) {
        }
    }
    
    /**
     * Cleans up resources used by the GameAdmin.
     * Should be called when the admin interface is no longer needed.
     */
    
    public void cleanup() {
        if (scanner != null) {
            scanner.close();
        }
    }
}
