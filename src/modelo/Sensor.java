package modelo;

public class Sensor {

    private String idSensor;
    private String parcela;
    private double vSeco;
    private double vSat;
    private boolean estado;
    private Lectura[] lecturas;
    private int cantidadLecturas;

    public Sensor(String idSensor, String parcela, double vSeco, double vSat) {
        this.idSensor = idSensor;
        this.parcela = parcela;
        this.vSeco = vSeco;
        this.vSat = vSat;
        this.estado = true;
        this.lecturas = new Lectura[50];
        this.cantidadLecturas = 0;
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
        if (this.cantidadLecturas >= this.lecturas.length) {
            expandirArreglo();
        }
        this.lecturas[this.cantidadLecturas++] = lectura;
        return true;
    }

    public boolean eliminarLecturaPorId(String idLectura) {
        int indice = -1;
        for (int i = 0; i < this.cantidadLecturas; i++) {
            if (this.lecturas[i].getId().equalsIgnoreCase(idLectura)) {
                indice = i;
                break;
            }
        }
        if (indice == -1) {
            return false;
        }
        for (int i = indice; i < this.cantidadLecturas - 1; i++) {
            this.lecturas[i] = this.lecturas[i + 1];
        }
        this.lecturas[--this.cantidadLecturas] = null;
        return true;
    }

    public Lectura buscarLecturaPorId(String idLectura) {
        for (int i = 0; i < this.cantidadLecturas; i++) {
            if (this.lecturas[i].getId().equalsIgnoreCase(idLectura)) {
                return this.lecturas[i];
            }
        }
        return null;
    }

    public double calcularPromedio() {
        if (this.cantidadLecturas == 0) {
            return 0.0;
        }
        double suma = 0.0;
        for (int i = 0; i < this.cantidadLecturas; i++) {
            suma += this.lecturas[i].getNivelHumedad();
        }
        return suma / this.cantidadLecturas;
    }

    public double calcularPendienteTendencia() {
        if (this.cantidadLecturas < 2) {
            return 0.0;
        }
        double primera = this.lecturas[0].getNivelHumedad();
        double ultima = this.lecturas[this.cantidadLecturas - 1].getNivelHumedad();
        return (ultima - primera) / (this.cantidadLecturas - 1);
    }

    public double predecirSiguienteHumedad() {
        if (this.cantidadLecturas == 0) {
            return 50.0;
        }
        if (this.cantidadLecturas == 1) {
            return this.lecturas[0].getNivelHumedad();
        }
        double ultima = this.lecturas[this.cantidadLecturas - 1].getNivelHumedad();
        double prediccion = ultima + calcularPendienteTendencia();
        if (prediccion < 0.0) {
            return 0.0;
        } else if (prediccion > 100.0) {
            return 100.0;
        }
        return prediccion;
    }

    private void expandirArreglo() {
        Lectura[] nuevo = new Lectura[this.lecturas.length * 2];
        for (int i = 0; i < this.cantidadLecturas; i++) {
            nuevo[i] = this.lecturas[i];
        }
        this.lecturas = nuevo;
    }

    public Lectura[] getLecturas() {
        Lectura[] copia = new Lectura[this.cantidadLecturas];
        for (int i = 0; i < this.cantidadLecturas; i++) {
            copia[i] = this.lecturas[i];
        }
        return copia;
    }

    public Lectura getUltimaLectura() {
        if (this.cantidadLecturas == 0) {
            return null;
        }
        return this.lecturas[this.cantidadLecturas - 1];
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
                this.idSensor, this.parcela, this.cantidadLecturas, calcularPromedio());
    }
}