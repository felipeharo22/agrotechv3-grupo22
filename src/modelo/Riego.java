package modelo;

public class Riego extends AccionAgronomica {

    private double litrosPorPalma;

    public Riego(int codigo, String prioridad, String mensaje, double litrosPorPalma) {
        super(codigo, prioridad, mensaje);
        this.litrosPorPalma = litrosPorPalma;
    }

    public double getLitrosPorPalma() {
        return this.litrosPorPalma;
    }

    public double calcularLaminaAgua(double deficitPorcentaje) {
        if (deficitPorcentaje <= 0) {
            return 0.0;
        }
        return deficitPorcentaje * 3.5;
    }

    @Override
    public String mostrarInformacion() {
        return String.format("[%s] RIEGO (#%d): %s | Dosis: %.0f L/palma",
                getPrioridad(), getCodigo(), getMensaje(), this.litrosPorPalma);
    }
}