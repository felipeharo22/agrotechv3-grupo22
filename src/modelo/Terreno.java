package modelo;

import java.util.ArrayList;
import java.util.List;

public class Terreno {

    private String codigo;
    private String tipoTierra;
    private double areaHectareas;
    private double umbralSeco;
    private double umbralExceso;
    private List<Sensor> sensores;

    public Terreno(String codigo, int tipoSuelo, double areaHectareas) {
        this.codigo = codigo;
        this.areaHectareas = areaHectareas;
        this.sensores = new ArrayList<>();

        switch (tipoSuelo) {
            case 1:
                this.tipoTierra = "Arenoso";
                this.umbralSeco = 25.0;
                this.umbralExceso = 60.0;
                break;
            case 2:
                this.tipoTierra = "Arcilloso";
                this.umbralSeco = 45.0;
                this.umbralExceso = 85.0;
                break;
            case 3:
                this.tipoTierra = "Humífero";
                this.umbralSeco = 40.0;
                this.umbralExceso = 80.0;
                break;
            default:
                this.tipoTierra = "Rocoso";
                this.umbralSeco = 20.0;
                this.umbralExceso = 55.0;
                break;
        }
    }

    public boolean agregarSensor(Sensor sensor) {
        if (sensor == null || buscarSensor(sensor.getIdSensor()) != null) {
            return false;
        }
        return this.sensores.add(sensor);
    }

    public Sensor buscarSensor(String idSensor) {
        for (Sensor sensor : this.sensores) {
            if (sensor.getIdSensor().equalsIgnoreCase(idSensor)) {
                return sensor;
            }
        }
        return null;
    }

    public String clasificarHumedad(double humedad) {
        if (humedad < this.umbralSeco) {
            return "DRY";
        } else if (humedad > this.umbralExceso) {
            return "EXCESS";
        } else {
            return "OPTIMAL";
        }
    }

    public AccionAgronomica evaluarTerreno(String idSensor) {
        Sensor sensor = buscarSensor(idSensor);
        if (sensor == null || sensor.getUltimaLectura() == null) {
            return new AccionAgronomica(100, "LOW", "Sin registros de sensor para evaluar.");
        }

        double actual = sensor.getUltimaLectura().getNivelHumedad();
        double proyectada = sensor.predecirSiguienteHumedad();
        String estado = clasificarHumedad(actual);

        if (estado.equals("DRY")) {
            String msg = String.format("Humedad crítica baja (%.2f%%). Regar de inmediato.", actual);
            return new Riego(201, "HIGH", msg, 150.0);
        } else if (estado.equals("EXCESS")) {
            String msg = String.format("Exceso de humedad (%.2f%%). Revisar canales de drenaje; suspender riego.", actual);
            return new AccionAgronomica(301, "HIGH", msg);
        } else if (proyectada < this.umbralSeco) {
            String msg = String.format("Humedad óptima (%.2f%%) pero caerá a %.2f%%. Preparar riego preventivo.", actual, proyectada);
            return new Riego(202, "MEDIUM", msg, 120.0);
        } else {
            String msg = String.format("Humedad óptima (%.2f%%). Condición ideal para absorción radicular.", actual);
            return new Fertilizacion(401, "MEDIUM", msg, "NPK Palma 15-15-15", 500.0);
        }
    }

    public String getCodigo() {
        return this.codigo;
    }

    public List<Sensor> getSensores() {
        return this.sensores;
    }

    public String mostrarInformacion() {
        return String.format("Terreno: %s | Tierra: %s | Área: %.1f ha | Umbrales: [%.1f%% - %.1f%%] | Sensores: %d",
                this.codigo, this.tipoTierra, this.areaHectareas, this.umbralSeco, this.umbralExceso, this.sensores.size());
    }
}