package interfaz;

import java.io.Console;
import java.util.Scanner;

public class Validador {

    public static int leerEntero(Scanner scanner, String mensaje, int min, int max) {
        while (true) {
            System.out.print(mensaje);
            String texto = scanner.nextLine().trim();
            try {
                int numero = Integer.parseInt(texto);
                if (numero >= min && numero <= max) {
                    return numero;
                }
                System.out.println(">> Ingrese un número entre " + min + " y " + max + ".");
            } catch (NumberFormatException e) {
                System.out.println(">> Entrada inválida. Ingrese solo números enteros.");
            }
        }
    }

    public static double leerDecimal(Scanner scanner, String mensaje, double min, double max) {
        while (true) {
            System.out.print(mensaje);
            String texto = scanner.nextLine().trim();
            try {
                double numero = Double.parseDouble(texto);
                if (numero >= min && numero <= max) {
                    return numero;
                }
                System.out.println(">> Ingrese un valor entre " + min + " y " + max + ".");
            } catch (NumberFormatException e) {
                System.out.println(">> Entrada inválida. Ingrese un número (use punto para decimales, ej. 12.5).");
            }
        }
    }

    public static String leerTexto(Scanner scanner, String mensaje) {
        while (true) {
            System.out.print(mensaje);
            String texto = scanner.nextLine().trim();
            if (!texto.isEmpty()) {
                return texto;
            }
            System.out.println(">> El campo no puede estar vacío.");
        }
    }

    public static String leerNombre(Scanner scanner, String mensaje) {
        while (true) {
            String texto = leerTexto(scanner, mensaje);
            if (texto.matches("[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+")) {
                return texto;
            }
            System.out.println(">> El nombre solo puede tener letras y espacios.");
        }
    }

    public static String leerCorreo(Scanner scanner, String mensaje) {
        while (true) {
            String texto = leerTexto(scanner, mensaje);
            if (texto.contains("@") && texto.contains(".") && !texto.contains(" ")) {
                return texto;
            }
            System.out.println(">> Correo inválido. Ejemplo: usuario@correo.com");
        }
    }

    public static String leerContrasenia(Scanner scanner, String mensaje) {
        Console consola = System.console();
        if (consola == null) {
            System.out.println(">> Aviso: esta consola no permite ocultar la contraseña (ejecute desde la terminal para ocultarla).");
            return leerTexto(scanner, mensaje);
        }
        while (true) {
            char[] clave = consola.readPassword(mensaje);
            if (clave != null && clave.length > 0) {
                return new String(clave);
            }
            System.out.println(">> El campo no puede estar vacío.");
        }
    }

    public static String leerFecha(Scanner scanner, String mensaje) {
        while (true) {
            String texto = leerTexto(scanner, mensaje);
            if (texto.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}")) {
                return texto;
            }
            System.out.println(">> Formato inválido. Use AAAA-MM-DD HH:MM (ej. 2026-11-01 08:30).");
        }
    }
}
