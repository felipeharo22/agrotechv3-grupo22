package modelo;

public class Agronomo {

    private int id;
    private String nombre;
    private String correo;
    private String contrasenia;
    private int intentosFallidos;

    public Agronomo(int id, String nombre, String correo, String contrasenia) {
        this.id = id;
        this.nombre = nombre;
        this.correo = correo;
        this.contrasenia = contrasenia;
        this.intentosFallidos = 0;
    }

    public boolean estaBloqueado() {
        return this.intentosFallidos >= 3;
    }

    public boolean autenticar(String claveIngresada) {
        if (estaBloqueado()) {
            return false;
        }
        if (this.contrasenia.equals(claveIngresada)) {
            this.intentosFallidos = 0;
            return true;
        } else {
            this.intentosFallidos++;
            return false;
        }
    }

    public String getNombre() {
        return this.nombre;
    }

    public String getCorreo() {
        return this.correo;
    }

    public int getIntentosFallidos() {
        return this.intentosFallidos;
    }

    public String mostrarInformacion() {
        return String.format("Agrónomo #%d: %s (%s) | Bloqueado: %s",
                this.id, this.nombre, this.correo, estaBloqueado() ? "SÍ" : "NO");
    }
}