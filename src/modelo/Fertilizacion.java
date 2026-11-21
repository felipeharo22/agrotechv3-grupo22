package modelo;

public class Fertilizacion extends AccionAgronomica {

    private String tipoFertilizante;
    private double gramosPorPalma;

    public Fertilizacion(int codigo, String prioridad, String mensaje, String tipoFertilizante, double gramosPorPalma) {
        super(codigo, prioridad, mensaje);
        this.tipoFertilizante = tipoFertilizante;
        this.gramosPorPalma = gramosPorPalma;
    }

    public boolean validarHumedadOptima(double humedadActual) {
        return humedadActual >= 45.0 && humedadActual <= 75.0;
    }

    @Override
    public String mostrarInformacion() {
        return String.format("[%s] FERTILIZACIÓN (#%d): %s | Insumo: %s (%.0f g/palma)",
                getPrioridad(), getCodigo(), getMensaje(), this.tipoFertilizante, this.gramosPorPalma);
    }
}