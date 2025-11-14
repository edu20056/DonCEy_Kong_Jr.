import java.net.Socket;
import java.util.*;

import Network.Server;

public class Main {
    // lista_jugador 1 = [jugador, [entidades], [frutas], mapa]
    // lista_jugador 2 = [jugador, [entidades], [frutas], mapa]
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
        // Verificar si se debe instancear un jugador
        if (servidor.J1_ING == false && servidor.getJugadoresSize() == 1){
            System.out.println("Se debe instancear jugador 1");
            servidor.J1_ING = true;
        } 

        if (servidor.J2_ING == false && servidor.getJugadoresSize() == 2){
            System.out.println("Se debe instancear jugador 2");
            servidor.J2_ING = true;
        } 

        // Procesar mensajes de J1
        if (!servidor.mensajes_j1.isEmpty()) {
            String mensaje = servidor.mensajes_j1.remove(0); // Obtener y remover el primer mensaje
            Socket s1 = servidor.getSocketJugador(servidor.J1_NAME);
            if (s1 != null) {
                String map1 = "";
                int movimiento = Integer.parseInt(mensaje);
                switch (movimiento) {
                    case 1:
                        map1 = "Jugador 1 se movio arriba";
                        break;
                    case 2:
                        map1 = "Jugador 1 se movia hacia derecha";
                        break;
                    case 3:
                        map1 = "Jugador 1 se movio abajo";
                        break;
                    case 4:
                        map1 = "Jugador 1 se movio hacia izquierda";
                        break;
                    default:
                        break;
                }
                List<int[]> entidadesRandom = generarListaRandom(3);
                List<int[]> frutasRandom    = generarListaRandom(5);

                String json1 = Main.generarJSON(12, 12, entidadesRandom, frutasRandom);

                System.out.println(map1);
                servidor.enviarA(s1, json1);
                servidor.enviarAMisEspectadores(servidor.J1_NAME, json1);
                System.out.println("✓ Mensaje de J1 procesado: " + mensaje);
            }
        }

        // Procesar mensajes de J2
        if (!servidor.mensajes_j2.isEmpty()) {
            String mensaje = servidor.mensajes_j2.remove(0); // Obtener y remover el primer mensaje
            Socket s2 = servidor.getSocketJugador(servidor.J2_NAME);
            if (s2 != null) {
                String map2 = "";
                int movimiento = Integer.parseInt(mensaje);
                switch (movimiento) { // Esto debe se como jugador2.move(1,2,3 ó 4)
                    case 1:
                        map2 = "Jugador 2 se movio arriba";
                        break;
                    case 2:
                        map2 = "Jugador 2 se movia hacia derecha";
                        break;
                    case 3:
                        map2 = "Jugador 2 se movio abajo";
                        break;
                    case 4:
                        map2 = "Jugador 2 se movio hacia izquierda";
                        break;
                    default:
                        break;
                }

                List<int[]> entidadesRandom = generarListaRandom(3);
                List<int[]> frutasRandom    = generarListaRandom(5);

                String json2 = Main.generarJSON(12, 12, entidadesRandom, frutasRandom);

                System.out.println(map2);

                // para que por cada frame se manden cambios, esto debe estar fuera del if en un if (servidor.J2_ING == true)
                servidor.enviarA(s2, json2);
                servidor.enviarAMisEspectadores(servidor.J2_NAME, json2);
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
                    case "1" -> servidor.opcionEnviarMensaje(sc); // Esto debe cambiarse para poder modicar matriz con una funcion y luego enviar mensaje de matriz 
                    case "2" -> {
                        servidor.cerrarServidor();
                        return; // Salir del método
                    }
                    default -> System.out.println("Opción no válida.");
                }
            }
        }
    }

    public static String generarJSON(int jx, int jy, List<int[]> entidades, List<int[]> frutas) {

        StringBuilder sb = new StringBuilder();
        sb.append("{");

        // Jugador
        sb.append("\"jugador\": {");
        sb.append("\"x\": ").append(jx).append(", ");
        sb.append("\"y\": ").append(jy);
        sb.append("},");

        // Entidades
        sb.append("\"entidades\": [");
        for (int i = 0; i < entidades.size(); i++) {
            int[] e = entidades.get(i);
            sb.append("{\"tipo\": \"entidad\", \"x\": ").append(e[0])
            .append(", \"y\": ").append(e[1]).append("}");
            if (i < entidades.size() - 1) sb.append(",");
        }
        sb.append("],");

        // Frutas
        sb.append("\"frutas\": [");
        for (int i = 0; i < frutas.size(); i++) {
            int[] f = frutas.get(i);
            sb.append("{\"tipo\": \"fruta\", \"x\": ").append(f[0])
            .append(", \"y\": ").append(f[1]).append("}");
            if (i < frutas.size() - 1) sb.append(",");
        }
        sb.append("]");

        sb.append("}");

        return sb.toString();
    }


    // FUNCION PARA PRUEBAS
    public static List<int[]> generarListaRandom(int cantidad) {
        Random r = new Random();
        List<int[]> lista = new ArrayList<>();

        for (int i = 0; i < cantidad; i++) {
            int x = r.nextInt(300);  // random 0-299
            int y = r.nextInt(300);
            lista.add(new int[]{x, y});
        }

        return lista;
    }

}