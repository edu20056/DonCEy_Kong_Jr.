package Network;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Servidor principal que gestiona conexiones de jugadores y espectadores
 * para una aplicación de red multijugador.
 * 
 * <p>El servidor permite hasta {@value #MAX_JUGADORES} jugadores y 
 * {@value #MAX_ESPECTADORES_POR_JUGADOR} espectadores por jugador.
 * Los clientes pueden conectarse como jugadores o espectadores, y los
 * espectadores pueden ver los mensajes de los jugadores a los que siguen.</p>
 * 
 */
public class Server {
    /** Puerto en el que el servidor escucha conexiones */
    private static final int PORT = 5000;
    
    /** Número máximo de jugadores permitidos en el servidor */
    private static final int MAX_JUGADORES = 2;
    
    /** Número máximo de espectadores permitidos por jugador */
    private static final int MAX_ESPECTADORES_POR_JUGADOR = 2;
    
    /** Mapa que almacena los jugadores conectados (nombre -> socket) */
    private final Map<String, Socket> jugadores = new HashMap<>();
    
    /** Mapa que almacena los espectadores por jugador (nombre jugador -> lista de sockets espectadores) */
    private final Map<String, List<Socket>> espectadoresPorJugador = new HashMap<>();
    
    /** Bandera que indica si el jugador 1 ha ingresado al juego */
    public boolean J1_ING = false;
    
    /** Bandera que indica si el jugador 2 ha ingresado al juego */
    public boolean J2_ING = false;
    
    /** Bandera que indica si el jugador 1 se ha desconectado */
    public boolean J1_desc = false;
    
    /** Bandera que indica si el jugador 2 se ha desconectado */
    public boolean J2_desc = false;
    
    /** Nombre del jugador 1 */
    public String J1_NAME = "";
    
    /** Nombre del jugador 2 */
    public String J2_NAME = "";
    
    /** Lista de mensajes enviados por el jugador 1 */
    public final List<String> mensajes_j1 = new ArrayList<>();
    
    /** Lista de mensajes enviados por el jugador 2 */
    public final List<String> mensajes_j2 = new ArrayList<>();

    /**
     * Constructor por defecto del servidor.
     */
    public Server() {}

    /**
     * Inicia el servidor en un hilo separado.
     * Este método no bloquea el hilo principal.
     */
    public void iniciar() {
        new Thread(() -> this.iniciarServidor()).start();
        // Alternativa más simple usando method reference:
        new Thread(this::iniciarServidor).start();
    }

    /** ----------------------------- */
    /**         INICIO SERVIDOR       */
    /** ----------------------------- */
    
    /**
     * Inicia el servidor y comienza a aceptar conexiones de clientes.
     * 
     * <p>Este método crea un ServerSocket en el puerto especificado y
     * escucha continuamente nuevas conexiones. Por cada cliente que se
     * conecta, crea un nuevo hilo ClientHandler para gestionar la comunicación.</p>
     * 
     * @see ClientHandler
     */
    private void iniciarServidor() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Servidor iniciado en el puerto " + PORT);
            System.out.println("Límites: " + MAX_JUGADORES + " jugadores, " + 
                             MAX_ESPECTADORES_POR_JUGADOR + " espectadores por jugador");

            // Bucle principal que acepta conexiones continuamente
            while (true) {
                Socket socket = serverSocket.accept();
                new Thread(new ClientHandler(socket)).start();
            }
        } catch (IOException e) {
            System.err.println("Error al iniciar el servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /** ----------------------------- */
    /**     CLASE CLIENT HANDLER      */
    /** ----------------------------- */
    
    /**
     * Clase interna que maneja la comunicación con un cliente individual.
     * 
     * <p>Cada instancia de ClientHandler se ejecuta en su propio hilo
     * y gestiona todo el ciclo de vida de la conexión con un cliente,
     * incluyendo registro, procesamiento de mensajes y limpieza.</p>
     */
    class ClientHandler implements Runnable {
        /** Socket de conexión con el cliente */
        private final Socket socket;
        
        /** Stream de entrada para leer mensajes del cliente */
        private BufferedReader in;
        
        /** Stream de salida para enviar mensajes al cliente */
        private PrintWriter out;
        
        /** Tipo de cliente: "PLAYER" o "SPECTATOR" */
        private String tipo;
        
        /** Nombre del jugador asociado (para espectadores, el jugador que están viendo) */
        private String jugadorAsociado;

        /**
         * Constructor del ClientHandler.
         * 
         * @param socket Socket de conexión con el cliente
         */
        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        /**
         * Método principal que ejecuta el hilo del cliente.
         * 
         * <p>Gestiona todo el ciclo de vida del cliente:
         * 1. Establece los streams de comunicación
         * 2. Registra al cliente según su tipo (jugador o espectador)
         * 3. Procesa mensajes según el tipo de cliente
         * 4. Realiza limpieza cuando el cliente se desconecta</p>
         */
        public void run() {
            try {
                // Configurar streams de entrada y salida
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                // Enviar mensaje de bienvenida inmediatamente
                out.println("Conexión Exitosa.");
                out.flush();

                // Leer el primer mensaje del cliente para determinar su tipo
                String line = in.readLine();
                if (line == null) return;

                if (line.startsWith("PLAYER")) {
                    manejarRegistroJugador(line);
                } else if (line.startsWith("SPECTATOR")) {
                    tipo = "SPECTATOR";
                    manejarEspectador();
                } else {
                    out.println("ERROR: Tipo desconocido. Usa: PLAYER <nombre> o SPECTATOR");
                    socket.close();
                }

            } catch (IOException e) {
                System.out.println("Cliente desconectado: " + 
                    (jugadorAsociado != null ? jugadorAsociado : "sin registrar"));
            } finally {
                limpiar();
            }
        }

        /**
         * Maneja el registro de un nuevo jugador.
         * 
         * @param line Línea de comando recibida del cliente (formato: "PLAYER nombre")
         * @throws IOException Si ocurre un error de E/S durante el registro
         */
        private void manejarRegistroJugador(String line) throws IOException {
            // Validar límite de jugadores
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
            
            // Asignar jugador a J1 o J2
            if (!J1_ING) {
                J1_NAME = jugadorAsociado;
                J1_ING = true;
            } else if (!J2_ING) {
                J2_NAME = jugadorAsociado;
                J2_ING = true;
            } else {
                out.println("ERROR: Máximo de jugadores alcanzado!");
                socket.close();
                return;
            }

            // Registrar jugador en las estructuras de datos
            jugadores.put(jugadorAsociado, socket);
            espectadoresPorJugador.putIfAbsent(jugadorAsociado, new ArrayList<>());
            out.println("OK: Jugador registrado como: " + jugadorAsociado);
            out.println("Puede comenzar a enviar mensajes.");
            manejarJugador();
        }

        /** ----------------------------- */
        /**        LÓGICA DE JUGADOR      */
        /** ----------------------------- */
        
        /**
         * Maneja la comunicación con un jugador registrado.
         * 
         * <p>Este método lee continuamente mensajes del jugador y los
         * almacena en la lista correspondiente (J1 o J2). También se
         * encarga de enviar los mensajes a los espectadores del jugador.</p>
         * 
         * @throws IOException Si ocurre un error de E/S durante la lectura
         */
        private void manejarJugador() throws IOException {
            String msg;
            while ((msg = in.readLine()) != null) {
                if (!msg.isEmpty()) {  // Verifica que no sea vacío
                    // Almacenar mensaje en la lista correspondiente
                    if (jugadorAsociado.equals(J1_NAME)) {
                        mensajes_j1.add(msg);
                    } else {
                        mensajes_j2.add(msg);
                    }
                    
                    // Enviar mensaje a todos los espectadores del jugador
                    enviarAMisEspectadores(jugadorAsociado, 
                        "[" + jugadorAsociado + "]: " + msg);
                }
            }
        }

        /** ----------------------------- */
        /**      LÓGICA DE ESPECTADOR     */
        /** ----------------------------- */
        
        /**
         * Maneja el registro y comunicación con un espectador.
         * 
         * <p>Este método muestra la lista de jugadores disponibles,
         * permite al espectador elegir uno para seguir, y luego
         * comienza a recibir y mostrar los mensajes de ese jugador.</p>
         * 
         * @throws IOException Si ocurre un error de E/S durante el registro
         */
        private void manejarEspectador() throws IOException {
            // Verificar que hay jugadores disponibles
            if (jugadores.isEmpty()) {
                out.println("ERROR: No hay jugadores disponibles para espectear.");
                out.println("Conecte al menos un jugador primero.");
                socket.close();
                return;
            }

            mostrarListaJugadores();
            
            // Leer y procesar la selección del espectador
            String input = in.readLine();
            if (input == null) {
                socket.close();
                return;
            }

            int indiceElegido = procesarSeleccionEspectador(input);
            if (indiceElegido == -1) {
                socket.close();
                return;
            }

            List<String> nombres = new ArrayList<>(jugadores.keySet());
            String elegido = nombres.get(indiceElegido);

            // Validar límite de espectadores para el jugador seleccionado
            if (!validarLimiteEspectadores(elegido)) {
                socket.close();
                return;
            }

            completarRegistroEspectador(elegido);
        }

        /**
         * Muestra la lista de jugadores disponibles al espectador.
         */
        private void mostrarListaJugadores() {
            out.println("Jugadores disponibles:");
            List<String> nombres = new ArrayList<>(jugadores.keySet());
            for (int i = 0; i < nombres.size(); i++) {
                String jugador = nombres.get(i);
                List<Socket> espectadores = espectadoresPorJugador.get(jugador);
                int numEspectadores = (espectadores != null) ? espectadores.size() : 0;
                out.println((i + 1) + ". " + jugador + " (" + numEspectadores + 
                           "/" + MAX_ESPECTADORES_POR_JUGADOR + " espectadores)");
            }
            out.println("END_PLAYERS_LIST");
            out.println("Por favor, escriba el número del jugador que desea espectear:");
            out.flush();
        }

        /**
         * Procesa la selección numérica del espectador.
         * 
         * @param input Entrada del espectador (número como string)
         * @return Índice del jugador seleccionado, o -1 si la selección es inválida
         */
        private int procesarSeleccionEspectador(String input) {
            int indiceElegido;
            try {
                indiceElegido = Integer.parseInt(input.trim()) - 1;
            } catch (NumberFormatException e) {
                out.println("ERROR: Debe ingresar un número válido.");
                return -1;
            }

            List<String> nombres = new ArrayList<>(jugadores.keySet());
            if (indiceElegido < 0 || indiceElegido >= nombres.size()) {
                out.println("ERROR: Número fuera de rango.");
                return -1;
            }

            return indiceElegido;
        }

        /**
         * Valida que el jugador seleccionado no haya alcanzado su límite de espectadores.
         * 
         * @param jugador Nombre del jugador a validar
         * @return true si el jugador puede aceptar más espectadores, false en caso contrario
         */
        private boolean validarLimiteEspectadores(String jugador) {
            List<Socket> espectadoresActuales = espectadoresPorJugador.get(jugador);
            if (espectadoresActuales != null && 
                espectadoresActuales.size() >= MAX_ESPECTADORES_POR_JUGADOR) {
                out.println("ERROR: El jugador " + jugador + 
                           " ya tiene el máximo de " + MAX_ESPECTADORES_POR_JUGADOR + " espectadores.");
                out.println("Por favor, espere a que se libere un espacio o elija otro jugador.");
                return false;
            }
            return true;
        }

        /**
         * Completa el registro del espectador y comienza a seguir al jugador seleccionado.
         * 
         * @param jugadorElegido Nombre del jugador que el espectador va a seguir
         * @throws IOException Si ocurre un error de E/S durante el registro
         */
        private void completarRegistroEspectador(String jugadorElegido) throws IOException {
            jugadorAsociado = jugadorElegido;
            espectadoresPorJugador.putIfAbsent(jugadorAsociado, new ArrayList<>());

            // Agregar este socket a la lista de espectadores del jugador
            synchronized (espectadoresPorJugador) {
                espectadoresPorJugador.get(jugadorAsociado).add(socket);
            }

            out.println("OK: Conectado como espectador de " + jugadorAsociado);
            out.println("=== Ahora recibirá todos los mensajes de " + jugadorAsociado + " ===");
            out.flush();

            // El espectador solo recibe mensajes, no los envía
            try {
                while (true) {
                    String mensaje = in.readLine();
                    if (mensaje == null) break; // Conexión cerrada
                    // Los espectadores no envían mensajes, solo reciben
                }
            } catch (IOException e) {
                // Ignorar excepción por cierre de conexión
            }
        }

        /** ----------------------------- */
        /**           LIMPIEZA            */
        /** ----------------------------- */
        
        /**
         * Realiza la limpieza de recursos cuando un cliente se desconecta.
         * 
         * <p>Este método:
         * - Elimina jugadores de las estructuras de datos
         * - Notifica a los espectadores cuando un jugador se desconecta
         * - Cierra sockets y libera recursos
         * - Actualiza banderas de estado</p>
         */
        private void limpiar() {
            try {
                if ("PLAYER".equals(tipo) && jugadorAsociado != null) {
                    // Eliminar jugador y notificar a sus espectadores
                    jugadores.remove(jugadorAsociado);
                    List<Socket> espectadores = espectadoresPorJugador.remove(jugadorAsociado);
                    if (espectadores != null) {
                        for (Socket s : espectadores) {
                            try {
                                enviarA(s, "El jugador " + jugadorAsociado + " se ha desconectado");
                                s.close();
                            } catch (IOException ignored) {}
                        }
                    }
                    
                    // Actualizar banderas de estado
                    if (jugadorAsociado.equals(J1_NAME)) {
                        J1_desc = true;
                        J1_ING = false;
                    } else if (jugadorAsociado.equals(J2_NAME)) {
                        J2_desc = true;
                        J2_ING = false;
                    }
                    
                } else if ("SPECTATOR".equals(tipo) && jugadorAsociado != null) {
                    // Eliminar espectador de la lista del jugador
                    List<Socket> lista = espectadoresPorJugador.get(jugadorAsociado);
                    if (lista != null) {
                        synchronized (espectadoresPorJugador) {
                            lista.remove(socket);
                        }
                    }
                }
                socket.close();
            } catch (IOException ignored) {
                // Ignorar excepción durante limpieza
            }
        }
    }

    /** ----------------------------- */
    /**     MÉTODOS AUXILIARES        */
    /** ----------------------------- */
    
    /**
     * Envía un mensaje a todos los espectadores de un jugador específico.
     * 
     * @param jugador Nombre del jugador cuyos espectadores recibirán el mensaje
     * @param mensaje Mensaje a enviar a los espectadores
     */
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

    /**
     * Envía un mensaje a un socket específico.
     * 
     * @param s Socket de destino
     * @param mensaje Mensaje a enviar
     */
    public void enviarA(Socket s, String mensaje) {
        try {
            PrintWriter pw = new PrintWriter(s.getOutputStream(), true);
            pw.println(mensaje);
        } catch (IOException ignored) {
            // Socket probablemente cerrado
        }
    }

    /**
     * Obtiene el socket de un jugador por su nombre.
     * 
     * @param nombreJugador Nombre del jugador
     * @return Socket del jugador, o null si no existe
     */
    public Socket getSocketJugador(String nombreJugador) {
        return jugadores.get(nombreJugador);
    }

    /**
     * Obtiene el número de espectadores de un jugador específico.
     * 
     * @param nombreJugador Nombre del jugador
     * @return Número de espectadores del jugador
     */
    public int getSpectadoresSize(String nombreJugador) {
        List<Socket> spList = espectadoresPorJugador.get(nombreJugador);
        return (spList != null) ? spList.size() : 0;
    }

    /**
     * Obtiene el número de jugadores actualmente conectados.
     * 
     * @return Número de jugadores conectados
     */
    public int getJugadoresSize() {
        return this.jugadores.size();
    }

    /**
     * Método principal para ejecutar el servidor de forma independiente.
     * 
     * @param args Argumentos de línea de comandos (no se utilizan)
     */
    public static void main(String[] args) {
        Server servidor = new Server();
        servidor.iniciar();
        System.out.println("Servidor iniciado. Presiona Ctrl+C para detenerlo.");
    }
}