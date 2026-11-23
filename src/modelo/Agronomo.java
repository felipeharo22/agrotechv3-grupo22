package modelo;

import java.util.ArrayList;
import java.util.List;

public class Agronomo {

    private int id;
    private String nombre;
    private String correo;
    private String contrasenia;
    private int intentosFallidos;
    private List<Terreno> terrenos;

    public Agronomo(int id, String nombre, String correo, String contrasenia) {
        this.id = id;
        this.nombre = nombre;
        this.correo = correo;
        this.contrasenia = contrasenia;
        this.intentosFallidos = 0;
        this.terrenos = new ArrayList<>();
    }

    public boolean agregarTerreno(Terreno terreno) {
        if (terreno == null || buscarTerreno(terreno.getCodigo()) != null) {
            return false;
        }
        return this.terrenos.add(terreno);
    }

    public Terreno buscarTerreno(String codigo) {
        for (Terreno terreno : this.terrenos) {
            if (terreno.getCodigo().equalsIgnoreCase(codigo)) {
                return terreno;
            }
        }
        return null;
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

    public List<Terreno> getTerrenos() {
        return this.terrenos;
    }

    public String mostrarInformacion() {
        return String.format("Agrónomo #%d: %s (%s) | Terrenos: %d | Bloqueado: %s",
                this.id, this.nombre, this.correo, this.terrenos.size(), estaBloqueado() ? "SÍ" : "NO");
    }
}