package Network;

import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
    private static final int PORT = 5000;
    private static final int MAX_JUGADORES = 2;
    private static final int MAX_ESPECTADORES_POR_JUGADOR = 2;
    private final Map<String, Socket> jugadores = new HashMap<>();
    private final Map<String, List<Socket>> espectadoresPorJugador = new HashMap<>();
    public String J1_NAME = "";
    public String J2_NAME = "";
    public final List<String> mensajes_j1 = new ArrayList<>();
    public final List<String> mensajes_j2 = new ArrayList<>();

        
    public Server() {}

    public void iniciar() {
        new Thread(() -> this.iniciarServidor()).start();
        // o más simple:
        new Thread(this::iniciarServidor).start();
    }

    /** ----------------------------- */
    /**         INICIO SERVIDOR       */
    /** ----------------------------- */
    private void iniciarServidor() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Servidor iniciado en el puerto " + PORT);
            System.out.println("Límites: " + MAX_JUGADORES + " jugadores, " + MAX_ESPECTADORES_POR_JUGADOR + " espectadores por jugador");

            while (true) {
                Socket socket = serverSocket.accept();
                new Thread(new ClientHandler(socket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** ----------------------------- */
    /**   FUNCIONES MODULARES         */
    /** ----------------------------- */

    // Muestra los jugadores y las opciones principales
    public void mostrarMenuPrincipal() {
        System.out.println("\n====== MENÚ DEL SERVIDOR ======");
        System.out.println("Jugadores conectados:");
        if (jugadores.isEmpty()) {
            System.out.println("  (ninguno)");
        } else {
            jugadores.keySet().forEach(j -> {
                int count = espectadoresPorJugador.getOrDefault(j, Collections.emptyList()).size();
                System.out.println("  - " + j + " (" + count + "/" + MAX_ESPECTADORES_POR_JUGADOR + " espectadores)");
            });
        }

        System.out.println("\nOpciones:");
        System.out.println("1. Enviar mensaje a un jugador");
        System.out.println("2. Ver espectadores de un jugador");
        System.out.println("3. Salir");
        System.out.print("Seleccione una opción: ");
    }

    /** Opción 1: enviar mensaje a un jugador */
    public void opcionEnviarMensaje(Scanner sc) {
        if (jugadores.isEmpty()) {
            System.out.println("No hay jugadores conectados.");
            return;
        }

        System.out.print("Ingrese nombre del jugador destino: ");
        String destino = sc.nextLine();

        Socket jugadorSocket = jugadores.get(destino);
        if (jugadorSocket == null) {
            System.out.println("Jugador no encontrado.");
            return;
        }

        System.out.print("Mensaje para " + destino + ": ");
        String mensaje = sc.nextLine();

        enviarMensajeJugador(destino, mensaje);
    }

    /** Opción 2: ver espectadores de un jugador */
    public void opcionVerEspectadores(Scanner sc) {
        System.out.print("Ingrese nombre del jugador: ");
        String jugador = sc.nextLine();
        mostrarEspectadoresDe(jugador);
    }

    /** Opción 3: cerrar servidor */
    public void cerrarServidor() {
        System.out.println("Servidor cerrado.");
        System.exit(0);
    }

    /** ----------------------------- */
    /**   FUNCIONES REUTILIZABLES     */
    /** ----------------------------- */

    // Enviar mensaje a jugador y sus espectadores
    public void enviarMensajeJugador(String jugador, String mensaje) {
        Socket jugadorSocket = jugadores.get(jugador);
        if (jugadorSocket == null) {
            System.out.println("Jugador no encontrado: " + jugador);
            return;
        }

        String texto = "[SERVIDOR → " + jugador + "]: " + mensaje;
        enviarA(jugadorSocket, texto);
        enviarAMisEspectadores(jugador, texto);
        System.out.println("Mensaje enviado a " + jugador);
    }

    // Mostrar espectadores conectados a un jugador
    public void mostrarEspectadoresDe(String jugador) {
        List<Socket> espectadores = espectadoresPorJugador.get(jugador);
        if (espectadores == null || espectadores.isEmpty()) {
            System.out.println("No hay espectadores conectados a " + jugador);
        } else {
            System.out.println("Espectadores conectados a " + jugador + ": " + espectadores.size());
        }
    }

    /** ----------------------------- */
    /**     CLASE CLIENT HANDLER      */
    /** ----------------------------- */
    class ClientHandler implements Runnable {
        private final Socket socket;
        private BufferedReader in;
        private PrintWriter out;
        private String tipo;
        private String jugadorAsociado;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                // ENVIAR MENSAJE DE BIENVENIDA INMEDIATAMENTE
                out.println("Conexión Exitosa.");
                out.flush();

                String line = in.readLine();
                if (line == null) return;

                if (line.startsWith("PLAYER")) {
                    // VALIDACIÓN: Verificar límite de jugadores
                    if (jugadores.size() >= MAX_JUGADORES) {
                        out.println("ERROR: El servidor está lleno. Máximo " + MAX_JUGADORES + " jugadores permitidos.");
                        socket.close();
                        return;
                    }
                    
                    tipo = "PLAYER";
                    String[] parts = line.split(" ", 2);
                    if (parts.length < 2) {
                        out.println("ERROR: Formato incorrecto. Usa: PLAYER <nombre>");
                        socket.close();
                        return;
                    }
                    jugadorAsociado = parts[1].trim();
                    
                    // Verificar si el nombre ya existe
                    if (jugadores.containsKey(jugadorAsociado)) {
                        out.println("ERROR: El nombre de jugador ya está en uso");
                        socket.close();
                        return;
                    }
                    
                    // Jugador entrante es jugador 1
                    if (jugadores.size() == 0) { 
                        J1_NAME = jugadorAsociado;
                    }

                    else {
                        J2_NAME = jugadorAsociado;
                    }

                    jugadores.put(jugadorAsociado, socket);
                    espectadoresPorJugador.putIfAbsent(jugadorAsociado, new ArrayList<>());
                    out.println("OK: Jugador registrado como: " + jugadorAsociado);
                    out.println("Puede comenzar a enviar mensajes.");
                    System.out.println("Jugador conectado: " + jugadorAsociado);
                    manejarJugador();

                } else if (line.startsWith("SPECTATOR")) {
                    tipo = "SPECTATOR";
                    manejarEspectador();

                } else {
                    out.println("ERROR: Tipo desconocido. Usa: PLAYER <nombre> o SPECTATOR");
                    socket.close();
                }

            } catch (IOException e) {
                System.out.println("Cliente desconectado: " + (jugadorAsociado != null ? jugadorAsociado : "sin registrar"));
            } finally {
                limpiar();
            }
        }

        /** ----------------------------- */
        /**        LÓGICA DE JUGADOR      */
        /** ----------------------------- */
        private void manejarJugador() throws IOException {
            String msg;
            while ((msg = in.readLine()) != null) {
                System.out.println("[" + jugadorAsociado + "]: " + msg);
                // Jugador asociado es jugador 1
                if (jugadorAsociado == J1_NAME) {
                    mensajes_j1.add(msg);
                }
                else { // jugador asociado es jugador 2
                    mensajes_j2.add(msg);
                }
                // Reenviar mensaje al propio jugador (confirmación)
                // enviarA(jugadores.get(jugadorAsociado), "[Tú]: " + mensajeProcesado);

                // Reenviar mensaje a sus espectadores
                // enviarAMisEspectadores(jugadorAsociado, "[" + jugadorAsociado + "]: " + mensajeProcesado);
            }
        }

        /** ----------------------------- */
        /**      LÓGICA DE ESPECTADOR     */
        /** ----------------------------- */
        private void manejarEspectador() throws IOException {
            // Mostrar lista de jugadores disponibles
            if (jugadores.isEmpty()) {
                out.println("ERROR: No hay jugadores disponibles para espectear.");
                out.println("Conecte al menos un jugador primero.");
                socket.close();
                return;
            }

            out.println("Jugadores disponibles:");
            for (String jugador : jugadores.keySet()) {
                List<Socket> espectadores = espectadoresPorJugador.get(jugador);
                int numEspectadores = (espectadores != null) ? espectadores.size() : 0;
                out.println("- " + jugador + " (" + numEspectadores + "/" + MAX_ESPECTADORES_POR_JUGADOR + " espectadores)");
            }
            out.println("END_PLAYERS_LIST");
            out.println("Por favor, escriba el nombre exacto del jugador que desea espectear:");
            out.flush();

            String elegido = in.readLine();
            if (elegido == null) {
                socket.close();
                return;
            }
            elegido = elegido.trim();

            // Verificar si el jugador existe
            if (!jugadores.containsKey(elegido)) {
                out.println("ERROR: Jugador '" + elegido + "' no encontrado.");
                out.println("Conexión cerrada.");
                socket.close();
                return;
            }

            // VALIDACIÓN: Verificar límite de espectadores para este jugador
            List<Socket> espectadoresActuales = espectadoresPorJugador.get(elegido);
            if (espectadoresActuales != null && espectadoresActuales.size() >= MAX_ESPECTADORES_POR_JUGADOR) {
                out.println("ERROR: El jugador " + elegido + " ya tiene el máximo de " + MAX_ESPECTADORES_POR_JUGADOR + " espectadores.");
                out.println("Por favor, espere a que se libere un espacio o elija otro jugador.");
                socket.close();
                return;
            }

            jugadorAsociado = elegido;
            espectadoresPorJugador.putIfAbsent(jugadorAsociado, new ArrayList<>());
            
            // Agregar este socket a la lista de espectadores
            synchronized (espectadoresPorJugador) {
                espectadoresPorJugador.get(jugadorAsociado).add(socket);
            }

            out.println("OK: Conectado como espectador de " + jugadorAsociado);
            out.println("=== Ahora recibirá todos los mensajes de " + jugadorAsociado + " ===");
            out.flush();
            System.out.println("Nuevo espectador conectado a " + jugadorAsociado + 
                             " (Total: " + espectadoresPorJugador.get(jugadorAsociado).size() + 
                             "/" + MAX_ESPECTADORES_POR_JUGADOR + ")");

            // El espectador ahora escucha mensajes continuamente
            try {
                while (true) {
                    String mensaje = in.readLine();
                    if (mensaje == null) {
                        break; // Conexión cerrada
                    }
                    // Los espectadores no envían mensajes, solo reciben
                    // Ignorar cualquier mensaje que envíen
                }
            } catch (IOException e) {
                // Conexión cerrada
            }
        }

        /** ----------------------------- */
        /**           LIMPIEZA            */
        /** ----------------------------- */
        private void limpiar() {
            try {
                if ("PLAYER".equals(tipo) && jugadorAsociado != null) {
                    jugadores.remove(jugadorAsociado);
                    // Notificar a espectadores
                    List<Socket> espectadores = espectadoresPorJugador.remove(jugadorAsociado);
                    if (espectadores != null) {
                        for (Socket s : espectadores) {
                            try {
                                enviarA(s, "El jugador " + jugadorAsociado + " se ha desconectado");
                                s.close();
                            } catch (IOException ignored) {}
                        }
                    }
                    System.out.println("Jugador desconectado: " + jugadorAsociado);
                } else if ("SPECTATOR".equals(tipo) && jugadorAsociado != null) {
                    List<Socket> lista = espectadoresPorJugador.get(jugadorAsociado);
                    if (lista != null) {
                        synchronized (espectadoresPorJugador) {
                            lista.remove(socket);
                        }
                    }
                    System.out.println("Espectador desconectado de " + jugadorAsociado);
                }
                socket.close();
            } catch (IOException ignored) {
            }
        }
    }

    /** ----------------------------- */
    /**     MÉTODOS AUXILIARES        */
    /** ----------------------------- */
    public void enviarAMisEspectadores(String jugador, String mensaje) {
        List<Socket> espectadores = espectadoresPorJugador.get(jugador);
        if (espectadores == null) return;

        synchronized (espectadoresPorJugador) {
            List<Socket> copia = new ArrayList<>(espectadores);
            for (Socket s : copia) {
                if (!s.isClosed()) {
                    enviarA(s, mensaje);
                }
            }
        }
    }

    public void enviarA(Socket s, String mensaje) {
        try {
            PrintWriter pw = new PrintWriter(s.getOutputStream(), true);
            pw.println(mensaje);
        } catch (IOException ignored) {
            // Socket probablemente cerrado
        }
    }

    // Obtener Socket por nombre
    public Socket getSocketJugador(String nombreJugador) {
        return jugadores.get(nombreJugador);
    }

}