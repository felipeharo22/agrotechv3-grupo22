import interfaz.ConsolaMenu;
import interfaz.Validador;
import java.util.Scanner;
import modelo.Agronomo;
import modelo.Terreno;
import negocio.CsvDatabase;

public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("--- DATOS DEL TERRENO ---");
        String codigo = Validador.leerTexto(scanner, "Código del terreno: ");
        System.out.println("Tipos de suelo:");
        System.out.println(" 1. Arenoso");
        System.out.println(" 2. Arcilloso");
        System.out.println(" 3. Humífero");
        System.out.println(" 4. Rocoso");
        int tipoSuelo = Validador.leerEntero(scanner, "Seleccione el tipo de suelo (1-4): ", 1, 4);
        double area = Validador.leerDecimal(scanner, "Área en hectáreas (ej. 12.5): ", 0.1, 100000);

        System.out.println();
        System.out.println("--- DATOS DEL AGRÓNOMO ---");
        int id = Validador.leerEntero(scanner, "ID del agrónomo (número): ", 1, 99999);
        String nombre = Validador.leerNombre(scanner, "Nombre: ");
        String correo = Validador.leerCorreo(scanner, "Correo: ");
        String contrasenia = Validador.leerTexto(scanner, "Contraseña: ");
        System.out.println();

        Terreno terreno = new Terreno(codigo, tipoSuelo, area);
        Agronomo agronomo = new Agronomo(id, nombre, correo, contrasenia);
        CsvDatabase database = new CsvDatabase("data/readings.csv");

        if (!database.cargarTerrenoDesdeCsv(terreno)) {
            System.out.println(">> No se encontró el archivo CSV. Se empezará sin lecturas.");
        }

        ConsolaMenu menu = new ConsolaMenu(scanner, database, terreno, agronomo);
        menu.start();
    }
}
