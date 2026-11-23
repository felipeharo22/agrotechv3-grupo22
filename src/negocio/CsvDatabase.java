package negocio;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Scanner;
import modelo.Lectura;
import modelo.Sensor;
import modelo.Terreno;

public class CsvDatabase {

    private final String rutaArchivo;

    public CsvDatabase(String rutaArchivo) {
        this.rutaArchivo = rutaArchivo;
    }

    public boolean cargarTerrenoDesdeCsv(Terreno terreno) {
        Scanner lector;
        try {
            lector = new Scanner(new File(this.rutaArchivo));
        } catch (FileNotFoundException e) {
            return false;
        }

        if (lector.hasNextLine()) {
            lector.nextLine();
        }

        while (lector.hasNextLine()) {
            String linea = lector.nextLine().trim();
            if (linea.isEmpty()) {
                continue;
            }
            String[] partes = linea.split(",");
            if (partes.length == 4) {
                String id = partes[0].trim();
                String fecha = partes[1].trim();
                double humedad = Double.parseDouble(partes[2].trim());
                String idSensor = partes[3].trim();

                Sensor sensor = terreno.buscarSensor(idSensor);
                if (sensor == null) {
                    sensor = new Sensor(idSensor, "Parcela-" + idSensor, 2.8, 1.2);
                    terreno.agregarSensor(sensor);
                }
                sensor.registrarLectura(new Lectura(id, fecha, humedad));
            }
        }
        lector.close();
        return true;
    }

    public boolean guardarTerrenoEnCsv(Terreno terreno) {
        PrintWriter escritor;
        try {
            escritor = new PrintWriter(new File(this.rutaArchivo));
        } catch (FileNotFoundException e) {
            return false;
        }

        escritor.println("id,date,humidityLevel,sensorId");
        for (Sensor sensor : terreno.getSensores()) {
            for (Lectura lectura : sensor.getLecturas()) {
                escritor.println(lectura.toCsvLine(sensor.getIdSensor()));
            }
        }
        escritor.close();
        return true;
    }

    public String generarSiguienteId(Terreno terreno) {
        int mayor = 0;
        for (Sensor sensor : terreno.getSensores()) {
            for (Lectura lectura : sensor.getLecturas()) {
                String id = lectura.getId();
                if (id.startsWith("REC-")) {
                    int numero = Integer.parseInt(id.substring(4));
                    if (numero > mayor) {
                        mayor = numero;
                    }
                }
            }
        }
        return String.format("REC-%03d", mayor + 1);
    }
}