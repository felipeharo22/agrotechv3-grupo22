package modelo;

import java.util.Locale;

public class Lectura {

    private String id;
    private String fecha;
    private double nivelHumedad;

    public Lectura(String id, String fecha, double nivelHumedad) {
        this.id = id;
        this.fecha = fecha;
        this.nivelHumedad = nivelHumedad;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFecha() {
        return this.fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public double getNivelHumedad() {
        return this.nivelHumedad;
    }

    public void setNivelHumedad(double nivelHumedad) {
        this.nivelHumedad = nivelHumedad;
    }

    public String toCsvLine(String idSensor) {
        return String.format(Locale.US, "%s,%s,%.2f,%s",
                this.id, this.fecha, this.nivelHumedad, idSensor);
    }

    public String mostrarInformacion() {
        return String.format("[%s] Fecha: %s | Humedad: %.2f%%",
                this.id, this.fecha, this.nivelHumedad);
    }

    @Override
    public String toString() {
        return mostrarInformacion();
    }
}