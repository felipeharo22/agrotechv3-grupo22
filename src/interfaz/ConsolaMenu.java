package interfaz;

import java.util.Scanner;
import modelo.AccionAgronomica;
import modelo.Agronomo;
import modelo.Lectura;
import modelo.Sensor;
import modelo.Terreno;
import negocio.CsvDatabase;

public class ConsolaMenu {

    private final Scanner scanner;
    private final CsvDatabase database;
    private final Terreno terreno;
    private final Agronomo agronomo;

    public ConsolaMenu(Scanner scanner, CsvDatabase database, Terreno terreno, Agronomo agronomo) {
        this.scanner = scanner;
        this.database = database;
        this.terreno = terreno;
        this.agronomo = agronomo;
    }

    public void start() {
        printBanner();

        if (!handleLogin()) {
            return;
        }

        boolean running = true;
        while (running) {
            printMenuOptions();
            int choice = readIntOption("Seleccione una opción: ", 0, 6);
            System.out.println();

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
                case 0:
                    running = false;
                    System.out.println("Saliendo del Sistema de Monitoreo de Humedad. ¡Hasta pronto!");
                    break;
                default:
                    System.out.println("Opción inválida. Intente nuevamente.");
                    break;
            }
            System.out.println();
        }
    }

    private boolean handleLogin() {
        System.out.println("--- AUTENTICACIÓN DE AGRÓNOMO (RF11) ---");
        System.out.println("Usuario registrado: " + this.agronomo.getNombre() + " (" + this.agronomo.getCorreo() + ")");

        while (!this.agronomo.estaBloqueado()) {
            System.out.print("Ingrese contraseña (por defecto 'palma2026'): ");
            String pass = this.scanner.nextLine().trim();

            if (this.agronomo.autenticar(pass)) {
                System.out.println(">> Acceso concedido. Bienvenido, " + this.agronomo.getNombre() + "!");
                System.out.println(">> " + this.terreno.mostrarInformacion());
                System.out.println();
                return true;
            } else {
                int restantes = 3 - this.agronomo.getIntentosFallidos();
                if (restantes > 0) {
                    System.out.println(">> Contraseña incorrecta. Intentos restantes: " + restantes);
                }
            }
        }

        System.out.println(">> ERROR: Cuenta bloqueada temporalmente por 3 intentos fallidos.");
        return false;
    }

    private void printBanner() {
        System.out.println("==================================================================");
        System.out.println("        OIL PALM (Elaeis guineensis) SOIL MOISTURE SYSTEM         ");
        System.out.println("               CSV-Driven Database & Decision Engine              ");
        System.out.println("==================================================================");
    }

    private void printMenuOptions() {
        System.out.println("------------------------- MENÚ PRINCIPAL -------------------------");
        System.out.println(" 1. Ver todas las lecturas registradas en el terreno (CSV)");
        System.out.println(" 2. Registrar nueva lectura de sensor (y guardar en CSV)");
        System.out.println(" 3. Editar/actualizar humedad de una lectura en CSV");
        System.out.println(" 4. Eliminar una lectura del terreno y CSV");
        System.out.println(" 5. Analizar un sensor (Estado, Tendencia, Predicción y Acción)");
        System.out.println(" 6. Mostrar reporte consolidado de todos los sensores");
        System.out.println(" 0. Salir");
        System.out.println("------------------------------------------------------------------");
    }

    private void handleListAllReadings() {
        Sensor[] sensores = this.terreno.getSensores();
        if (sensores.length == 0) {
            System.out.println(">> No hay sensores ni lecturas registradas en el archivo CSV.");
            return;
        }

        int total = 0;
        System.out.println("======================== REGISTROS EN BASE CSV ========================");
        System.out.printf("%-10s %-18s %-15s %-12s%n", "ID Reg.", "Fecha / Hora", "Humedad (%)", "ID Sensor");
        System.out.println("-----------------------------------------------------------------------");
        for (int i = 0; i < sensores.length; i++) {
            Sensor s = sensores[i];
            Lectura[] lecturas = s.getLecturas();
            for (int j = 0; j < lecturas.length; j++) {
                Lectura l = lecturas[j];
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

        System.out.print("Ingrese Fecha/Hora (ej. 2026-11-01 08:30): ");
        String date = this.scanner.nextLine().trim();
        if (date.isEmpty()) {
            date = "2026-11-01 08:00";
        }

        double humidity = readDoubleOption("Ingrese Nivel de Humedad (%): ", 0.0, 100.0);

        System.out.print("Ingrese ID del Sensor (ej. SEN-PALM-01): ");
        String sensorId = this.scanner.nextLine().trim().toUpperCase();
        if (sensorId.isEmpty()) {
            sensorId = "SEN-PALM-01";
        }

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
        System.out.print("Ingrese ID de Lectura a modificar (ej. REC-001): ");
        String id = this.scanner.nextLine().trim();

        Sensor[] sensores = this.terreno.getSensores();
        Lectura encontrada = null;

        for (int i = 0; i < sensores.length; i++) {
            encontrada = sensores[i].buscarLecturaPorId(id);
            if (encontrada != null) {
                break;
            }
        }

        if (encontrada == null) {
            System.out.printf(">> No se encontró la lectura con ID '%s'.%n", id);
            return;
        }

        System.out.println("Registro actual: " + encontrada.mostrarInformacion());
        double newHumidity = readDoubleOption("Ingrese Nuevo Nivel de Humedad (%): ", 0.0, 100.0);

        encontrada.setNivelHumedad(newHumidity);
        if (this.database.guardarTerrenoEnCsv(this.terreno)) {
            System.out.printf(">> Lectura %s actualizada a %.2f%% en el CSV!%n", id, newHumidity);
        } else {
            System.out.println(">> Error al actualizar el archivo CSV.");
        }
    }

    private void handleDeleteReading() {
        System.out.println("--- ELIMINAR LECTURA DEL TERRENO ---");
        System.out.print("Ingrese ID de Lectura a eliminar: ");
        String id = this.scanner.nextLine().trim();

        Sensor[] sensores = this.terreno.getSensores();
        boolean eliminado = false;

        for (int i = 0; i < sensores.length; i++) {
            if (sensores[i].eliminarLecturaPorId(id)) {
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
        Sensor[] sensores = this.terreno.getSensores();
        if (sensores.length == 0) {
            System.out.println(">> No hay sensores registrados en el terreno.");
            return;
        }

        System.out.print("Sensores disponibles: ");
        for (int i = 0; i < sensores.length; i++) {
            System.out.printf("[%s] ", sensores[i].getIdSensor());
        }
        System.out.println();

        System.out.print("Ingrese ID del Sensor a analizar: ");
        String sensorId = this.scanner.nextLine().trim().toUpperCase();

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
        System.out.printf(" Total Lecturas:       %d registros%n", sensor.getLecturas().length);
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
        Sensor[] sensores = this.terreno.getSensores();
        if (sensores.length == 0) {
            System.out.println(">> No hay sensores registrados en el terreno.");
            return;
        }

        System.out.println("============================= RESUMEN DEL TERRENO =============================");
        System.out.println(this.terreno.mostrarInformacion());
        System.out.println("-------------------------------------------------------------------------------");
        System.out.printf("%-14s %-9s %-12s %-10s %-12s %-12s %-12s%n",
                "ID Sensor", "Lecturas", "Actual (%)", "Estado", "Tendencia", "Predicción", "Prioridad");
        System.out.println("-------------------------------------------------------------------------------");

        for (int i = 0; i < sensores.length; i++) {
            Sensor s = sensores[i];
            Lectura latest = s.getUltimaLectura();
            if (latest == null) {
                continue;
            }
            String state = this.terreno.clasificarHumedad(latest.getNivelHumedad());
            double slope = s.calcularPendienteTendencia();
            double forecast = s.predecirSiguienteHumedad();
            AccionAgronomica accion = this.terreno.evaluarTerreno(s.getIdSensor());

            System.out.printf("%-14s %-9d %-12.2f %-10s %-12.3f %-12.2f %-12s%n",
                    s.getIdSensor(), s.getLecturas().length, latest.getNivelHumedad(),
                    state, slope, forecast, accion.getPrioridad());
        }
        System.out.println("===============================================================================");
    }

    private int readIntOption(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String input = this.scanner.nextLine().trim();
            try {
                int value = Integer.parseInt(input);
                if (value >= min && value <= max) {
                    return value;
                }
                System.out.printf(">> Ingrese un número entre %d y %d.%n", min, max);
            } catch (NumberFormatException e) {
                System.out.println(">> Entrada inválida. Ingrese un número entero.");
            }
        }
    }

    private double readDoubleOption(String prompt, double min, double max) {
        while (true) {
            System.out.print(prompt);
            String input = this.scanner.nextLine().trim();
            try {
                double value = Double.parseDouble(input);
                if (value >= min && value <= max) {
                    return value;
                }
                System.out.printf(">> Ingrese un valor entre %.1f y %.1f.%n", min, max);
            } catch (NumberFormatException e) {
                System.out.println(">> Entrada inválida. Ingrese un número decimal válido.");
            }
        }
    }
}