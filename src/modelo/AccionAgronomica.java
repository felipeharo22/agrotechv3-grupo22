package modelo;

public class AccionAgronomica {

    private int codigo;
    private String prioridad;
    private String mensaje;

    public AccionAgronomica(int codigo, String prioridad, String mensaje) {
        this.codigo = codigo;
        this.prioridad = prioridad;
        this.mensaje = mensaje;
    }

    public int getCodigo() {
        return this.codigo;
    }

    public String getPrioridad() {
        return this.prioridad;
    }

    public String getMensaje() {
        return this.mensaje;
    }

    public String mostrarInformacion() {
        return String.format("[%s] Acción #%d: %s", this.prioridad, this.codigo, this.mensaje);
    }

    @Override
    public String toString() {
        return mostrarInformacion();
    }
}