package modelo;

import java.util.ArrayList;
import java.util.List;

public class Sensor {

    private String idSensor;
    private String parcela;
    private double vSeco;
    private double vSat;
    private boolean estado;
    private List<Lectura> lecturas;

    public Sensor(String idSensor, String parcela, double vSeco, double vSat) {
        this.idSensor = idSensor;
        this.parcela = parcela;
        this.vSeco = vSeco;
        this.vSat = vSat;
        this.estado = true;
        this.lecturas = new ArrayList<>();
    }

    public boolean calibrar(double nuevoVSeco, double nuevoVSat) {
        if (nuevoVSeco > nuevoVSat && (nuevoVSeco - nuevoVSat) >= 0.5) {
            this.vSeco = nuevoVSeco;
            this.vSat = nuevoVSat;
            this.estado = true;
            return true;
        }
        return false;
    }

    public boolean registrarLectura(Lectura lectura) {
        if (lectura == null || !this.estado) {
            return false;
        }
        if (lectura.getNivelHumedad() < 0.0 || lectura.getNivelHumedad() > 100.0) {
            return false;
        }
        return this.lecturas.add(lectura);
    }

    public boolean eliminarLecturaPorId(String idLectura) {
        for (int i = 0; i < this.lecturas.size(); i++) {
            if (this.lecturas.get(i).getId().equalsIgnoreCase(idLectura)) {
                this.lecturas.remove(i);
                return true;
            }
        }
        return false;
    }

    public Lectura buscarLecturaPorId(String idLectura) {
        for (Lectura lectura : this.lecturas) {
            if (lectura.getId().equalsIgnoreCase(idLectura)) {
                return lectura;
            }
        }
        return null;
    }

    public double calcularPromedio() {
        if (this.lecturas.isEmpty()) {
            return 0.0;
        }
        double suma = 0.0;
        for (Lectura lectura : this.lecturas) {
            suma += lectura.getNivelHumedad();
        }
        return suma / this.lecturas.size();
    }

    public double calcularPendienteTendencia() {
        int cantidad = this.lecturas.size();
        if (cantidad < 2) {
            return 0.0;
        }
        double primera = this.lecturas.get(0).getNivelHumedad();
        double ultima = this.lecturas.get(cantidad - 1).getNivelHumedad();
        return (ultima - primera) / (cantidad - 1);
    }

    public double predecirSiguienteHumedad() {
        int cantidad = this.lecturas.size();
        if (cantidad == 0) {
            return 50.0;
        }
        if (cantidad == 1) {
            return this.lecturas.get(0).getNivelHumedad();
        }
        double ultima = this.lecturas.get(cantidad - 1).getNivelHumedad();
        double prediccion = ultima + calcularPendienteTendencia();
        if (prediccion < 0.0) {
            return 0.0;
        } else if (prediccion > 100.0) {
            return 100.0;
        }
        return prediccion;
    }

    public List<Lectura> getLecturas() {
        return this.lecturas;
    }

    public Lectura getUltimaLectura() {
        if (this.lecturas.isEmpty()) {
            return null;
        }
        return this.lecturas.get(this.lecturas.size() - 1);
    }

    public String getIdSensor() {
        return this.idSensor;
    }

    public String getParcela() {
        return this.parcela;
    }

    public boolean isEstado() {
        return this.estado;
    }

    public String mostrarInformacion() {
        return String.format("Sensor: %s | Parcela: %s | Lecturas: %d | Promedio: %.2f%%",
                this.idSensor, this.parcela, this.lecturas.size(), calcularPromedio());
    }
}