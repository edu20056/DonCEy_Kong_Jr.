// Entities/Coco.java
package Entities;

import Utils.Coords;
import World.World;
import World.TileType;

public abstract class Coco extends Entity {
    protected boolean activo;
    protected String tipo;
    protected boolean isFacingDown;

    public Coco(int x, int y, String tipo) {
        super(x, y);
        this.activo = true;
        this.tipo = tipo;
        this.isFacingDown = true;
    }
    
    public abstract void actualizar(World world);
    public abstract boolean getIsFacingDown();

    public boolean isActivo() { 
        return activo; 
    }
    
    public String getTipo() { 
        return tipo; 
    }
    
    public void setActivo(boolean activo) { 
        this.activo = activo; 
    }
    
    protected boolean estaSobreLiana(World world) {
        return world.getTile(getPosition()) == TileType.VINE;
    }
    
    protected boolean puedeMoverseA(Coords coords, World world) {
        if (!world.isWithinBounds(coords)) {
            return false;
        }
        TileType tile = world.getTile(coords);
        return !tile.isSolid();
    }
}
