// Entities/BlueCoco.java
package Entities;

import Utils.Coords;
import World.World;
import World.TileType;

public class BlueCoco extends Coco {
    private boolean enLiana = true;
    private boolean cayendo = false;
    
    public BlueCoco(int x, int y) {
        super(x, y, "AZUL");
        System.out.println("Cocodrilo Azul creado en: " + x + ", " + y);
    }

    @Override
    public boolean getIsFacingDown() {
        return true;
    }
    
    @Override
    public void actualizar(World world) {
        if (!isActivo()) return;
        
        if (enLiana) {
            // Está en liana - bajar gradualmente
            Coords abajo = new Coords(getX(), getY() + 1);
            
            if (puedeMoverseA(abajo, world) && world.getTile(abajo) == TileType.VINE) {
                // Sigue en liana, bajar
                setPosition(abajo);
            } else {
                // Ya no hay liana abajo, empezar a caer
                enLiana = false;
                cayendo = true;
                System.out.println("Cocodrilo Azul empezó a caer desde: " + getPosition());
            }
        } else if (cayendo) {
            // Está cayendo - caer hasta tocar el suelo
            Coords abajo = new Coords(getX(), getY() + 1);
            
            if (puedeMoverseA(abajo, world)) {
                setPosition(abajo);
                
                // Verificar si llegó al suelo
                Coords bajoAbajo = new Coords(getX(), getY() + 1);
                if (!puedeMoverseA(bajoAbajo, world)) {
                    // Tocó el suelo, dejar de caer
                    cayendo = false;
                    System.out.println("Cocodrilo Azul llegó al suelo en: " + getPosition());
                }
            } else {
                // Chocó con algo, dejar de caer
                cayendo = false;
            }
        }
        
        // Si no está en liana ni cayendo, se desactiva
        if (!enLiana && !cayendo) {
            setActivo(false);
        }
    }
}
