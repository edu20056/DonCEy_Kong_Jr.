package Network;

import java.util.List;
import Entities.Coco;
import Entities.Fruit;
import Entities.Player;
import Utils.Coords;

/**
 * Clase adaptadora para generar representaciones JSON del estado del juego.
 * 
 * <p>Esta clase se encarga de convertir los objetos del juego (jugadores, frutas, cocos)
 * en formato JSON para facilitar la comunicación entre el servidor y los clientes.</p>
 * 
 */
public class AdapterJSON {
    
    /**
     * Constructor por defecto del adaptador JSON.
     */
    public AdapterJSON() {}
    
    /**
     * Genera una representación JSON del estado actual del juego.
     * 
     * <p>El JSON generado contiene tres secciones principales:
     * <ol>
     *   <li><b>jugador</b>: Información del jugador (posición, puntos, estado, etc.)</li>
     *   <li><b>entidades</b>: Lista de cocos en el juego con sus propiedades</li>
     *   <li><b>frutas</b>: Lista de frutas activas con sus características</li>
     * </ol>
     * </p>
     * 
     * @param player El jugador principal del que se generará la información
     * @param frutas Lista de frutas presentes en el juego (puede ser null)
     * @param cocos Lista de cocos presentes en el juego
     * @param playerName Nombre del jugador para identificarlo
     * @param espectadores Número de espectadores viendo al jugador
     * @param vidas Número de vidas restantes del jugador
     * @return Una cadena en formato JSON que representa el estado del juego
     * 
     * @see Player
     * @see Fruit
     * @see Coco
     * @see Coords
     */
    public String generarJSON(Player player, List<Fruit> frutas, List<Coco> cocos, 
                             String playerName, int espectadores, int vidas) {
        
        StringBuilder sb = new StringBuilder();
        sb.append("{");

        // Sección JUGADOR
        generarSeccionJugador(sb, player, playerName, espectadores, vidas);
        sb.append(",");

        // Sección ENTIDADES (Cocos)
        generarSeccionEntidades(sb, cocos);
        sb.append(",");

        // Sección FRUTAS
        generarSeccionFrutas(sb, frutas);

        sb.append("}");
        return sb.toString();
    }

    /**
     * Genera la sección del jugador en el JSON.
     * 
     * <p>Incluye las siguientes propiedades:
     * <ul>
     *   <li><b>x</b>: Posición X del jugador</li>
     *   <li><b>y</b>: Posición Y del jugador</li>
     *   <li><b>puntos</b>: Puntos acumulados por el jugador</li>
     *   <li><b>climbing</b>: Indica si el jugador está escalando</li>
     *   <li><b>right</b>: Indica la dirección del jugador (true = derecha)</li>
     *   <li><b>name</b>: Nombre del jugador</li>
     *   <li><b>spect</b>: Número de espectadores</li>
     *   <li><b>dead</b>: Indica si el jugador está muerto</li>
     *   <li><b>lives</b>: Número de vidas restantes</li>
     * </ul>
     * </p>
     * 
     * @param sb StringBuilder donde se construye el JSON
     * @param player Jugador del que se obtiene la información
     * @param playerName Nombre del jugador
     * @param espectadores Número de espectadores
     * @param vidas Número de vidas restantes
     */
    private void generarSeccionJugador(StringBuilder sb, Player player, String playerName, 
                                      int espectadores, int vidas) {
        sb.append("\"jugador\": {");
        sb.append("\"x\": ").append(player.getX()).append(", ");
        sb.append("\"y\": ").append(player.getY()).append(", ");
        sb.append("\"puntos\": ").append(player.getPoints()).append(", ");
        sb.append("\"climbing\": ").append(player.isOnVine()).append(", "); 
        sb.append("\"right\": ").append(player.isFacingRight()).append(", "); 
        sb.append("\"name\": \"").append(escaparComillas(playerName)).append("\", ");
        sb.append("\"spect\": ").append(espectadores).append(", ");
        sb.append("\"dead\": ").append(player.isDead()).append(", ");
        sb.append("\"lives\": ").append(vidas); 
        sb.append("}");
    }

    /**
     * Genera la sección de entidades (cocos) en el JSON.
     * 
     * <p>Cada entidad incluye las siguientes propiedades:
     * <ul>
     *   <li><b>tipo</b>: Tipo de la entidad (coco)</li>
     *   <li><b>x</b>: Posición X de la entidad</li>
     *   <li><b>y</b>: Posición Y de la entidad</li>
     *   <li><b>View</b>: Dirección de la entidad (true = hacia abajo)</li>
     * </ul>
     * </p>
     * 
     * @param sb StringBuilder donde se construye el JSON
     * @param cocos Lista de cocos a incluir en el JSON
     */
    private void generarSeccionEntidades(StringBuilder sb, List<Coco> cocos) {
        sb.append("\"entidades\": [");
        if (cocos != null) {
            for (int i = 0; i < cocos.size(); i++) {
                Coco coco = cocos.get(i);
                String type = coco.getType();
                int x_pos = coco.getX();
                int y_pos = coco.getY();
                
                sb.append("{\"tipo\": \"").append(type).append("\", \"x\": ")
                  .append(x_pos).append(", \"y\": ").append(y_pos)
                  .append(", \"View\":").append(coco.getIsFacingDown()).append("}");
                
                if (i < cocos.size() - 1) sb.append(",");
            }
        }
        sb.append("]");
    }

    /**
     * Genera la sección de frutas en el JSON.
     * 
     * <p>Solo incluye las frutas que están activas. Cada fruta incluye:
     * <ul>
     *   <li><b>tipo</b>: Tipo de fruta</li>
     *   <li><b>x</b>: Posición X de la fruta</li>
     *   <li><b>y</b>: Posición Y de la fruta</li>
     *   <li><b>puntos</b>: Puntos que otorga la fruta al ser recolectada</li>
     * </ul>
     * </p>
     * 
     * @param sb StringBuilder donde se construye el JSON
     * @param frutas Lista de frutas a incluir en el JSON (puede ser null)
     */
    private void generarSeccionFrutas(StringBuilder sb, List<Fruit> frutas) {
        sb.append("\"frutas\": [");
        if (frutas != null) {
            int frutasActivas = 0;
            for (Fruit fruta : frutas) {
                if (fruta.isActive()) {
                    if (frutasActivas > 0) sb.append(",");
                    
                    Coords pos = fruta.getPosition();
                    sb.append("{\"tipo\": \"").append(fruta.getType())
                      .append("\", \"x\": ").append(pos.getX())
                      .append(", \"y\": ").append(pos.getY())
                      .append(", \"puntos\": ").append(fruta.getPoints())
                      .append("}");
                    frutasActivas++;
                }
            }
        }
        sb.append("]");
    }

    /**
     * Escapa las comillas dobles en una cadena para evitar problemas en el JSON.
     * 
     * @param texto Texto original que puede contener comillas
     * @return Texto con las comillas escapadas
     */
    private String escaparComillas(String texto) {
        if (texto == null) return "";
        return texto.replace("\"", "\\\"");
    }
    
    /**
     * Genera un JSON de error para notificar problemas al cliente.
     * 
     * @param mensajeError Descripción del error ocurrido
     * @return JSON con información de error
     */
    public String generarJSONError(String mensajeError) {
        return "{" +
               "\"error\": true," +
               "\"mensaje\": \"" + escaparComillas(mensajeError) + "\"," +
               "\"jugador\": {}," +
               "\"entidades\": []," +
               "\"frutas\": []" +
               "}";
    }
    
    /**
     * Genera un JSON básico indicando que el juego no está disponible.
     * 
     * @return JSON indicando juego no disponible
     */
    public String generarJSONNoDisponible() {
        return "{" +
               "\"error\": true," +
               "\"mensaje\": \"Juego no disponible\"," +
               "\"jugador\": {}," +
               "\"entidades\": []," +
               "\"frutas\": []" +
               "}";
    }
    
    /**
     * Verifica si una cadena es un JSON válido generado por esta clase.
     * 
     * @param json Cadena a verificar
     * @return true si es un JSON válido, false en caso contrario
     */
    public boolean esJSONValido(String json) {
        if (json == null || json.trim().isEmpty()) return false;
        return json.startsWith("{") && json.endsWith("}");
    }
}