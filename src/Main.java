import interfaz.ConsolaMenu;
import interfaz.Validador;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import modelo.Agronomo;

public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        List<Agronomo> usuarios = new ArrayList<>();

        System.out.println("==================================================================");
        System.out.println("        OIL PALM (Elaeis guineensis) SOIL MOISTURE SYSTEM         ");
        System.out.println("==================================================================");

        boolean running = true;
        while (running) {
            System.out.println("------------------------- MENÚ DE INICIO -------------------------");
            System.out.println(" 1. Registrarse");
            System.out.println(" 2. Iniciar sesión");
            System.out.println(" 0. Salir");
            System.out.println("------------------------------------------------------------------");
            int opcion = Validador.leerEntero(scanner, "Seleccione una opción: ", 0, 2);
            System.out.println();

            switch (opcion) {
                case 1:
                    registrarUsuario(scanner, usuarios);
                    break;
                case 2:
                    Agronomo agronomo = iniciarSesion(scanner, usuarios);
                    if (agronomo != null) {
                        ConsolaMenu menu = new ConsolaMenu(scanner, agronomo);
                        menu.start();
                    }
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

    private static void registrarUsuario(Scanner scanner, List<Agronomo> usuarios) {
        System.out.println("--- REGISTRO DE USUARIO (PROPIETARIO / AGRÓNOMO) ---");
        int id = Validador.leerEntero(scanner, "ID del usuario (número): ", 1, 99999);
        String nombre = Validador.leerNombre(scanner, "Nombre: ");
        String correo = Validador.leerCorreo(scanner, "Correo: ");

        if (buscarUsuario(usuarios, correo) != null) {
            System.out.println(">> Error: Ya existe un usuario registrado con ese correo.");
            return;
        }

        String contrasenia = Validador.leerContrasenia(scanner, "Contraseña: ");
        usuarios.add(new Agronomo(id, nombre, correo, contrasenia));
        System.out.println(">> Usuario registrado correctamente. Ya puede iniciar sesión.");
    }

    private static Agronomo iniciarSesion(Scanner scanner, List<Agronomo> usuarios) {
        System.out.println("--- INICIO DE SESIÓN (RF11) ---");
        if (usuarios.isEmpty()) {
            System.out.println(">> No hay usuarios registrados. Regístrese primero (opción 1).");
            return null;
        }

        String correo = Validador.leerCorreo(scanner, "Correo: ");
        Agronomo agronomo = buscarUsuario(usuarios, correo);
        if (agronomo == null) {
            System.out.println(">> Usuario no encontrado.");
            return null;
        }

        while (!agronomo.estaBloqueado()) {
            String pass = Validador.leerContrasenia(scanner, "Contraseña: ");

            if (agronomo.autenticar(pass)) {
                System.out.println(">> Acceso concedido. Bienvenido, " + agronomo.getNombre() + "!");
                System.out.println(">> " + agronomo.mostrarInformacion());
                System.out.println();
                return agronomo;
            } else {
                int restantes = 3 - agronomo.getIntentosFallidos();
                if (restantes > 0) {
                    System.out.println(">> Contraseña incorrecta. Intentos restantes: " + restantes);
                }
            }
        }

        System.out.println(">> ERROR: Cuenta bloqueada temporalmente por 3 intentos fallidos.");
        return null;
    }

    private static Agronomo buscarUsuario(List<Agronomo> usuarios, String correo) {
        for (Agronomo agronomo : usuarios) {
            if (agronomo.getCorreo().equalsIgnoreCase(correo)) {
                return agronomo;
            }
        }
        return null;
    }
}
