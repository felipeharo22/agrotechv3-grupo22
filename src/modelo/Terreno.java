package modelo;


public class Terreno {

    private String codigo;
    private String tipoTierra;
    private double areaHectareas;
    private double umbralSeco;
    private double umbralExceso;
    private Sensor[] sensores;
    private int cantidadSensores;

    public Terreno(String codigo, String tipoTierra, double areaHectareas, double umbralSeco, double umbralExceso) {
        this.codigo = codigo;
        this.tipoTierra = tipoTierra;
        this.areaHectareas = areaHectareas;
        this.umbralSeco = umbralSeco;
        this.umbralExceso = umbralExceso;
        this.sensores = new Sensor[20];
        this.cantidadSensores = 0;
    }

    public boolean agregarSensor(Sensor sensor) {
        if (sensor == null || buscarSensor(sensor.getIdSensor()) != null) {
            return false;
        }
        if (this.cantidadSensores >= this.sensores.length) {
            Sensor[] nuevo = new Sensor[this.sensores.length * 2];
            for (int i = 0; i < this.cantidadSensores; i++) {
                nuevo[i] = this.sensores[i];
            }
            this.sensores = nuevo;
        }
        this.sensores[this.cantidadSensores++] = sensor;
        return true;
    }

    public Sensor buscarSensor(String idSensor) {
        for (int i = 0; i < this.cantidadSensores; i++) {
            if (this.sensores[i].getIdSensor().equalsIgnoreCase(idSensor)) {
                return this.sensores[i];
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

    public Sensor[] getSensores() {
        Sensor[] copia = new Sensor[this.cantidadSensores];
        for (int i = 0; i < this.cantidadSensores; i++) {
            copia[i] = this.sensores[i];
        }
        return copia;
    }

    public String mostrarInformacion() {
        return String.format("Terreno: %s | Tierra: %s | Área: %.1f ha | Umbrales: [%.1f%% - %.1f%%] | Sensores: %d",
                this.codigo, this.tipoTierra, this.areaHectareas, this.umbralSeco, this.umbralExceso, this.cantidadSensores);
    }
}