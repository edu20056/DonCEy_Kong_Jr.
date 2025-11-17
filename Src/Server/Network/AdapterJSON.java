package Network;

import java.util.List;

import Entities.Coco;
import Entities.Fruit;
import Entities.Player;
import Utils.Coords;

public class AdapterJSON {
        public String generarJSON(Player player, List<Fruit> frutas, List<Coco> cocos) {
        
        StringBuilder sb = new StringBuilder();
        sb.append("{");

        // Jugador {x,y,puntos,escalando?,derecha?}
        sb.append("\"jugador\": {");
        sb.append("\"x\": ").append(player.getX()).append(", ");
        sb.append("\"y\": ").append(player.getY()).append(", ");
        sb.append("\"puntos\": ").append(player.getPoints()).append(", "); // Usar player.getPoints() cuando esté disponible
        sb.append("\"climbing\": ").append(player.isOnVine()).append(", "); 
        sb.append("\"right\": ").append(player.isFacingRight()); 
        sb.append("},");

        // Entidades {tipo,x,y,abajo?}
        sb.append("\"entidades\": [");
        for (int i = 0; i < cocos.size(); i++) {
            Coco coco = cocos.get(i);
            String type = coco.getTipo();
            int x_pos = coco.getX();
            int y_pos = coco.getY();
            sb.append("{\"tipo\": \"").append(type).append("\", \"x\": ")
                .append(x_pos).append(", \"y\": ").append(y_pos)
                .append(", \"View\":").append(coco.getIsFacingDown()).append("}");
            if (i < cocos.size() - 1) sb.append(",");
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
}
