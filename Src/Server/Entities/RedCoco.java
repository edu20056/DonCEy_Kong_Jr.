// Entities/RedCoco.java
package Entities;

import Utils.Coords;
import World.World;
import World.TileType;

public class RedCoco extends Coco {
    private boolean moviendoseArriba = true;
    private final int distanciaMaxima = 3;
    private int distanciaRecorrida = 0;
    
    public RedCoco(int x, int y) {
        super(x, y, "ROJO");
        System.out.println("Cocodrilo Rojo creado en: " + x + ", " + y);
    }

    @Override
    public boolean getIsFacingDown() {
        return !moviendoseArriba;
    }

    @Override
    public void actualizar(World world) {
        if (!isActivo()) return;
        
        // Verificar que todavía esté en una liana
        if (!estaSobreLiana(world)) {
            setActivo(false);
            return;
        }
        
        // Determinar dirección del movimiento
        int direccionY = moviendoseArriba ? -1 : 1;
        Coords nuevaPos = new Coords(getX(), getY() + direccionY);
        
        // Verificar si puede moverse y si la nueva posición es una liana
        if (puedeMoverseA(nuevaPos, world) && world.getTile(nuevaPos) == TileType.VINE) {
            setPosition(nuevaPos);
            distanciaRecorrida++;
            
            // Cambiar dirección si alcanzó la distancia máxima
            if (distanciaRecorrida >= distanciaMaxima) {
                moviendoseArriba = !moviendoseArriba;
                distanciaRecorrida = 0;
            }
        } else {
            // Si no puede moverse, cambiar dirección
            moviendoseArriba = !moviendoseArriba;
            distanciaRecorrida = 0;
        }
    }
}
