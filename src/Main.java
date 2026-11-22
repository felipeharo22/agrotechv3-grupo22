import interfaz.ConsolaMenu;
import java.util.Scanner;
import modelo.Agronomo;
import modelo.Terreno;
import negocio.CsvDatabase;

public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("--- DATOS DEL TERRENO ---");
        System.out.print("Código del terreno: ");
        String codigo = scanner.nextLine();
        System.out.print("Tipo de tierra: ");
        String tipoTierra = scanner.nextLine();
        System.out.print("Área en hectáreas (ej. 12.5): ");
        double area = Double.parseDouble(scanner.nextLine());
        System.out.print("Umbral seco (%) (ej. 40): ");
        double umbralSeco = Double.parseDouble(scanner.nextLine());
        System.out.print("Umbral de exceso (%) (ej. 80): ");
        double umbralExceso = Double.parseDouble(scanner.nextLine());

        System.out.println();
        System.out.println("--- DATOS DEL AGRÓNOMO ---");
        System.out.print("ID del agrónomo (número): ");
        int id = Integer.parseInt(scanner.nextLine());
        System.out.print("Nombre: ");
        String nombre = scanner.nextLine();
        System.out.print("Correo: ");
        String correo = scanner.nextLine();
        System.out.print("Contraseña: ");
        String contrasenia = scanner.nextLine();
        System.out.println();

        Terreno terreno = new Terreno(codigo, tipoTierra, area, umbralSeco, umbralExceso);
        Agronomo agronomo = new Agronomo(id, nombre, correo, contrasenia);
        CsvDatabase database = new CsvDatabase("data/readings.csv");

        if (!database.cargarTerrenoDesdeCsv(terreno)) {
            System.out.println(">> No se encontró el archivo CSV. Se empezará sin lecturas.");
        }

        ConsolaMenu menu = new ConsolaMenu(scanner, database, terreno, agronomo);
        menu.start();
    }
}
