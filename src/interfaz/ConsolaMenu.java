package interfaz;

import java.util.List;
import java.util.Scanner;
import modelo.AccionAgronomica;
import modelo.Agronomo;
import modelo.Lectura;
import modelo.Sensor;
import modelo.Terreno;
import negocio.CsvDatabase;

public class ConsolaMenu {

    private final Scanner scanner;
    private final Agronomo agronomo;
    private CsvDatabase database;
    private Terreno terreno;

    public ConsolaMenu(Scanner scanner, Agronomo agronomo) {
        this.scanner = scanner;
        this.agronomo = agronomo;
        this.database = null;
        this.terreno = null;
    }

    public void start() {
        printBanner();

        if (this.agronomo.getTerrenos().isEmpty()) {
            System.out.println(">> Aún no tiene terrenos registrados. Registre su primer terreno (opción 7).");
            System.out.println();
        } else {
            activarTerreno(this.agronomo.getTerrenos().get(0));
        }

        boolean running = true;
        while (running) {
            printMenuOptions();
            int choice = Validador.leerEntero(this.scanner, "Seleccione una opción: ", 0, 8);
            System.out.println();

            if (choice >= 1 && choice <= 6 && this.terreno == null) {
                System.out.println(">> Primero registre o seleccione un terreno (opciones 7 u 8).");
                System.out.println();
                continue;
            }

            switch (choice) {
                case 1:
                    handleListAllReadings();
                    break;
                case 2:
                    handleAddReading();
                    break;
                case 3:
                    handleEditReading();
                    break;
                case 4:
                    handleDeleteReading();
                    break;
                case 5:
                    handleAnalyzeSensor();
                    break;
                case 6:
                    handleSummaryReport();
                    break;
                case 7:
                    handleRegisterTerrain();
                    break;
                case 8:
                    handleSelectTerrain();
                    break;
                case 0:
                    running = false;
                    System.out.println(">> Sesión cerrada. Hasta pronto, " + this.agronomo.getNombre() + ".");
                    break;
                default:
                    System.out.println("Opción inválida. Intente nuevamente.");
                    break;
            }
            System.out.println();
        }
    }

    private void handleRegisterTerrain() {
        System.out.println("--- REGISTRAR NUEVO TERRENO ---");
        String codigo = Validador.leerTexto(this.scanner, "Código del terreno (ej. LOTE-01): ").toUpperCase();
        if (this.agronomo.buscarTerreno(codigo) != null) {
            System.out.println(">> Error: Ya tiene un terreno registrado con el código " + codigo + ".");
            return;
        }

        System.out.println("Tipos de suelo:");
        System.out.println(" 1. Arenoso");
        System.out.println(" 2. Arcilloso");
        System.out.println(" 3. Humífero");
        System.out.println(" 4. Rocoso");
        int tipoSuelo = Validador.leerEntero(this.scanner, "Seleccione el tipo de suelo (1-4): ", 1, 4);
        double area = Validador.leerDecimal(this.scanner, "Área en hectáreas (ej. 12.5): ", 0.1, 100000);

        Terreno nuevo = new Terreno(codigo, tipoSuelo, area);
        this.agronomo.agregarTerreno(nuevo);
        activarTerreno(nuevo);
        if (!this.database.cargarTerrenoDesdeCsv(nuevo)) {
            System.out.println(">> No se encontró el archivo CSV del terreno. Se empezará sin lecturas.");
        }
        System.out.println(">> Terreno registrado correctamente y seleccionado como terreno de trabajo.");
        System.out.println(">> " + nuevo.mostrarInformacion());
    }

    private void handleSelectTerrain() {
        List<Terreno> terrenos = this.agronomo.getTerrenos();
        if (terrenos.isEmpty()) {
            System.out.println(">> No tiene terrenos registrados. Use la opción 7.");
            return;
        }

        System.out.println("--- SUS TERRENOS ---");
        for (Terreno t : terrenos) {
            System.out.println(" - " + t.mostrarInformacion());
        }
        String codigo = Validador.leerTexto(this.scanner, "Ingrese el código del terreno: ").toUpperCase();
        Terreno elegido = this.agronomo.buscarTerreno(codigo);
        if (elegido == null) {
            System.out.printf(">> No se encontró el terreno '%s'.%n", codigo);
            return;
        }
        activarTerreno(elegido);
        System.out.println(">> Terreno de trabajo: " + elegido.mostrarInformacion());
    }

    private void activarTerreno(Terreno t) {
        this.terreno = t;
        this.database = new CsvDatabase("data/readings_" + t.getCodigo() + ".csv");
    }

    private void printBanner() {
        System.out.println("==================================================================");
        System.out.println("        OIL PALM (Elaeis guineensis) SOIL MOISTURE SYSTEM         ");
        System.out.println("               CSV-Driven Database & Decision Engine              ");
        System.out.println("==================================================================");
    }

    private void printMenuOptions() {
        System.out.println("------------------------- MENÚ PRINCIPAL -------------------------");
        System.out.println(" Usuario: " + this.agronomo.getNombre() + " | Terreno de trabajo: "
                + (this.terreno == null ? "(ninguno)" : this.terreno.getCodigo()));
        System.out.println("------------------------------------------------------------------");
        System.out.println(" 1. Ver todas las lecturas del terreno");
        System.out.println(" 2. Registrar nueva lectura de sensor");
        System.out.println(" 3. Editar humedad de una lectura");
        System.out.println(" 4. Eliminar una lectura");
        System.out.println(" 5. Analizar un sensor");
        System.out.println(" 6. Mostrar reporte consolidado de todos los sensores");
        System.out.println(" 7. Registrar nuevo terreno");
        System.out.println(" 8. Seleccionar terreno de trabajo");
        System.out.println(" 0. Cerrar sesión");
        System.out.println("------------------------------------------------------------------");
    }

    private void handleListAllReadings() {
        List<Sensor> sensores = this.terreno.getSensores();
        if (sensores.isEmpty()) {
            System.out.println(">> No hay sensores ni lecturas registradas en el archivo CSV.");
            return;
        }

        int total = 0;
        System.out.println("======================== REGISTROS EN BASE CSV ========================");
        System.out.printf("%-10s %-18s %-15s %-12s%n", "ID Reg.", "Fecha / Hora", "Humedad (%)", "ID Sensor");
        System.out.println("-----------------------------------------------------------------------");
        for (Sensor s : sensores) {
            for (Lectura l : s.getLecturas()) {
                System.out.printf("%-10s %-18s %-15.2f %-12s%n",
                        l.getId(), l.getFecha(), l.getNivelHumedad(), s.getIdSensor());
                total++;
            }
        }
        System.out.println("-----------------------------------------------------------------------");
        System.out.printf("Total de lecturas: %d%n", total);
        System.out.println("=======================================================================");
    }

    private void handleAddReading() {
        System.out.println("--- AGREGAR NUEVA LECTURA AL TERRENO ---");
        String newId = this.database.generarSiguienteId(this.terreno);
        System.out.printf("ID de Lectura generado: %s%n", newId);

        String date = Validador.leerFecha(this.scanner, "Ingrese Fecha/Hora (ej. 2026-11-01 08:30): ");
        double humidity = Validador.leerDecimal(this.scanner, "Ingrese Nivel de Humedad (%): ", 0.0, 100.0);
        String sensorId = Validador.leerTexto(this.scanner, "Ingrese ID del Sensor (ej. SEN-PALM-01): ").toUpperCase();

        Sensor sensor = this.terreno.buscarSensor(sensorId);
        if (sensor == null) {
            sensor = new Sensor(sensorId, "Parcela-" + sensorId, 2.8, 1.2);
            this.terreno.agregarSensor(sensor);
        }

        Lectura nuevaLectura = new Lectura(newId, date, humidity);
        if (sensor.registrarLectura(nuevaLectura) && this.database.guardarTerrenoEnCsv(this.terreno)) {
            System.out.printf(">> Lectura %s agregada exitosamente al sensor %s y guardada en CSV!%n", newId, sensorId);
        } else {
            System.out.println(">> Error: No se pudo registrar la lectura en el archivo CSV.");
        }
    }

    private void handleEditReading() {
        System.out.println("--- EDITAR LECTURA EN EL TERRENO ---");
        String id = Validador.leerTexto(this.scanner, "Ingrese ID de Lectura a modificar (ej. REC-001): ");

        Lectura encontrada = null;
        for (Sensor s : this.terreno.getSensores()) {
            encontrada = s.buscarLecturaPorId(id);
            if (encontrada != null) {
                break;
            }
        }

        if (encontrada == null) {
            System.out.printf(">> No se encontró la lectura con ID '%s'.%n", id);
            return;
        }

        System.out.println("Registro actual: " + encontrada.mostrarInformacion());
        double newHumidity = Validador.leerDecimal(this.scanner, "Ingrese Nuevo Nivel de Humedad (%): ", 0.0, 100.0);

        encontrada.setNivelHumedad(newHumidity);
        if (this.database.guardarTerrenoEnCsv(this.terreno)) {
            System.out.printf(">> Lectura %s actualizada a %.2f%% en el CSV!%n", id, newHumidity);
        } else {
            System.out.println(">> Error al actualizar el archivo CSV.");
        }
    }

    private void handleDeleteReading() {
        System.out.println("--- ELIMINAR LECTURA DEL TERRENO ---");
        String id = Validador.leerTexto(this.scanner, "Ingrese ID de Lectura a eliminar: ");

        boolean eliminado = false;
        for (Sensor s : this.terreno.getSensores()) {
            if (s.eliminarLecturaPorId(id)) {
                eliminado = true;
                break;
            }
        }

        if (eliminado && this.database.guardarTerrenoEnCsv(this.terreno)) {
            System.out.printf(">> Lectura %s eliminada exitosamente del CSV.%n", id);
        } else {
            System.out.printf(">> No se encontró la lectura '%s' o no se pudo eliminar.%n", id);
        }
    }

    private void handleAnalyzeSensor() {
        List<Sensor> sensores = this.terreno.getSensores();
        if (sensores.isEmpty()) {
            System.out.println(">> No hay sensores registrados en el terreno.");
            return;
        }

        System.out.print("Sensores disponibles: ");
        for (Sensor s : sensores) {
            System.out.printf("[%s] ", s.getIdSensor());
        }
        System.out.println();

        String sensorId = Validador.leerTexto(this.scanner, "Ingrese ID del Sensor a analizar: ").toUpperCase();

        Sensor sensor = this.terreno.buscarSensor(sensorId);
        if (sensor == null || sensor.getUltimaLectura() == null) {
            System.out.printf(">> No existen lecturas para el sensor '%s'.%n", sensorId);
            return;
        }

        Lectura latest = sensor.getUltimaLectura();
        String state = this.terreno.clasificarHumedad(latest.getNivelHumedad());
        double avg = sensor.calcularPromedio();
        double slope = sensor.calcularPendienteTendencia();
        double predicted = sensor.predecirSiguienteHumedad();
        AccionAgronomica accion = this.terreno.evaluarTerreno(sensorId);

        System.out.println("==================== REPORTE DE ANÁLISIS DE SENSOR ====================");
        System.out.printf(" Sensor:               %s (%s)%n", sensor.getIdSensor(), sensor.getParcela());
        System.out.printf(" Total Lecturas:       %d registros%n", sensor.getLecturas().size());
        System.out.printf(" Última Fecha/Hora:    %s%n", latest.getFecha());
        System.out.printf(" Humedad Actual:       %.2f%%%n", latest.getNivelHumedad());
        System.out.printf(" Estado del Suelo:     [%s]%n", state);
        System.out.printf(" Promedio Histórico:   %.2f%%%n", avg);
        System.out.printf(" Pendiente Tendencia:  %.3f%% por intervalo (%s)%n",
                slope, (slope < 0 ? "SECÁNDOSE" : "HUMEDECIENDO/ESTABLE"));
        System.out.printf(" Predicción Siguiente: %.2f%% (Estado Proyectado: %s)%n",
                predicted, this.terreno.clasificarHumedad(predicted));
        System.out.println("-----------------------------------------------------------------------");
        System.out.printf(" Acción Agronómica:    %s%n", accion.mostrarInformacion());
        System.out.println("=======================================================================");
    }

    private void handleSummaryReport() {
        List<Sensor> sensores = this.terreno.getSensores();
        if (sensores.isEmpty()) {
            System.out.println(">> No hay sensores registrados en el terreno.");
            return;
        }

        System.out.println("============================= RESUMEN DEL TERRENO =============================");
        System.out.println(this.terreno.mostrarInformacion());
        System.out.println("-------------------------------------------------------------------------------");
        System.out.printf("%-14s %-9s %-12s %-10s %-12s %-12s %-12s%n",
                "ID Sensor", "Lecturas", "Actual (%)", "Estado", "Tendencia", "Predicción", "Prioridad");
        System.out.println("-------------------------------------------------------------------------------");

        for (Sensor s : sensores) {
            Lectura latest = s.getUltimaLectura();
            if (latest == null) {
                continue;
            }
            String state = this.terreno.clasificarHumedad(latest.getNivelHumedad());
            double slope = s.calcularPendienteTendencia();
            double forecast = s.predecirSiguienteHumedad();
            AccionAgronomica accion = this.terreno.evaluarTerreno(s.getIdSensor());

            System.out.printf("%-14s %-9d %-12.2f %-10s %-12.3f %-12.2f %-12s%n",
                    s.getIdSensor(), s.getLecturas().size(), latest.getNivelHumedad(),
                    state, slope, forecast, accion.getPrioridad());
        }
        System.out.println("===============================================================================");
    }
}