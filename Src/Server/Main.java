import java.net.Socket;
import java.util.Scanner;
import Network.Server;

public class Main {
    
    public static void main(String[] args) {
        Server servidor = new Server();
        servidor.iniciar();
        
        // Thread para procesar mensajes entrantes en tiempo real
        Thread procesadorMensajes = new Thread(() -> {
            while (true) {
                procesarMensajesEntrantes(servidor);
                try {
                    Thread.sleep(100); // Pequeña pausa para no saturar la CPU
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        procesadorMensajes.setDaemon(true); // Para que se cierre cuando el main termine
        procesadorMensajes.start();

        ejecutarInterfazUsuario(servidor);
    }

    private static void procesarMensajesEntrantes(Server servidor) {
        // Procesar mensajes de J1
        if (!servidor.mensajes_j1.isEmpty()) {
            String mensaje = servidor.mensajes_j1.remove(0); // Obtener y remover el primer mensaje
            Socket s1 = servidor.getSocketJugador(servidor.J1_NAME);
            if (s1 != null) {
                String map1 = "Mapa1 para: " + mensaje;
                servidor.enviarA(s1, map1);
                servidor.enviarAMisEspectadores(servidor.J1_NAME, map1);
                System.out.println("✓ Mensaje de J1 procesado: " + mensaje);
            }
        }

        // Procesar mensajes de J2
        if (!servidor.mensajes_j2.isEmpty()) {
            String mensaje = servidor.mensajes_j2.remove(0); // Obtener y remover el primer mensaje
            Socket s2 = servidor.getSocketJugador(servidor.J2_NAME);
            if (s2 != null) {
                String map2 = "Mapa2 para: " + mensaje;
                servidor.enviarA(s2, map2);
                servidor.enviarAMisEspectadores(servidor.J2_NAME, map2);
                System.out.println("✓ Mensaje de J2 procesado: " + mensaje);
            }
        }
    }

    private static void ejecutarInterfazUsuario(Server servidor) {
        try (Scanner sc = new Scanner(System.in)) {
            while (true) {
                servidor.mostrarMenuPrincipal();

                String opcion = sc.nextLine();

                switch (opcion) {
                    case "1" -> servidor.opcionEnviarMensaje(sc);
                    case "2" -> {
                        servidor.cerrarServidor();
                        return; // Salir del método
                    }
                    default -> System.out.println("Opción no válida.");
                }
            }
        }
    }
}