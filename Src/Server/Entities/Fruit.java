// Entities/Fruit.java
package Entities;

import Utils.Coords;

public class Fruit extends Entity {
    private boolean activa;
    private final int puntos;
    private final String tipo;
    
    public Fruit(int x, int y, String tipo, int puntos) {
        super(x, y);
        this.activa = true;
        this.tipo = tipo;
        this.puntos = puntos;
    }
    
    // Constructor simplificado con puntos por defecto según el tipo
    public Fruit(int x, int y, String tipo) {
        this(x, y, tipo, obtenerPuntosPorTipo(tipo));
    }
    
    private static int obtenerPuntosPorTipo(String tipo) {
        switch (tipo.toUpperCase()) {
            case "MANZANA": return 10;
            case "BANANA": return 15;
            case "UVA": return 5;
            case "FRUTILLA": return 20;
            case "NARANJA": return 8;
            default: return 10;
        }
    }
    
    public boolean isActiva() {
        return activa;
    }
    
    public void setActiva(boolean activa) {
        this.activa = activa;
    }
    
    public int getPuntos() {
        return puntos;
    }
    
    public String getTipo() {
        return tipo;
    }
    
    public void recolectar() {
        this.activa = false;
        System.out.println("¡Fruta " + tipo + " recolectada! +" + puntos + " puntos");
    }
    
    @Override
    public String toString() {
        return "Fruit{" +
                "tipo='" + tipo + '\'' +
                ", puntos=" + puntos +
                ", activa=" + activa +
                ", posición=" + getPosition() +
                '}';
    }
}
