package negocio;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import modelo.Lectura;
import modelo.Sensor;
import modelo.Terreno;

/**
 * Lee el sensor de humedad del ESP32 por WiFi (HTTP) en segundo plano.
 *
 * Cada 5 s pide GET http://esp32-humedad.local/lectura, junta las lecturas nuevas
 * y cada cierto intervalo guarda UNA lectura promediada en el CSV del terreno,
 * usando los mismos métodos que el menú (registrarLectura + guardarTerrenoEnCsv).
 *
 * El menú y este hilo escriben sobre el mismo objeto Terreno; para no pisarse,
 * ambos lo hacen dentro de synchronized (terreno).
 */
public class ClienteEsp32 implements Runnable {

    public static final String HOST_POR_DEFECTO = "esp32-humedad.local";
    public static final int MINUTOS_POR_DEFECTO = 1;

    private static final long INTERVALO_CONSULTA_MS = 5000;
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final Pattern PAR_JSON = Pattern.compile("\"(\\w+)\"\\s*:\\s*(\"[^\"]*\"|[-0-9.eE]+|true|false|null)");

    private final String url;          // tal como lo escribió el usuario (con el nombre .local)
    private volatile String urlIp;     // misma URL pero con la IP ya resuelta
    private int fallosSeguidos;
    private final Terreno terreno;
    private final CsvDatabase database;
    private final long intervaloGuardadoMs;
    private final HttpClient http;

    private Thread hilo;
    private volatile boolean activo;

    // Estado visible desde el menú (opción 10)
    private volatile boolean conectado;
    private volatile String ultimoError;
    private volatile Map<String, String> ultimaLectura;
    private volatile long momentoUltimaLectura;
    private volatile int lecturasAcumuladas;
    private volatile long proximoGuardado;
    private volatile int totalGuardadas;
    private volatile String ultimoGuardado;

    // Acumulador (solo lo usa el hilo)
    private long ultimoSeq = -1;
    private double sumaHumedad;

    public ClienteEsp32(String host, Terreno terreno, CsvDatabase database, int minutosEntreGuardados) {
        this.url = construirUrl(host);
        this.terreno = terreno;
        this.database = database;
        this.intervaloGuardadoMs = minutosEntreGuardados * 60_000L;
        this.http = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)   // el servidor del ESP32 solo habla HTTP/1.1
                .connectTimeout(Duration.ofSeconds(3))
                .build();
    }

    /** Acepta "esp32-humedad.local", "172.20.10.5" o una URL completa. */
    private static String construirUrl(String host) {
        String h = host.trim();
        if (h.startsWith("http://") || h.startsWith("https://")) {
            return h.endsWith("/lectura") ? h : h.replaceAll("/+$", "") + "/lectura";
        }
        return "http://" + h + "/lectura";
    }

    /**
     * Busca la IP del nombre (ej. esp32-humedad.local) UNA vez y la guarda.
     * En macOS buscar un nombre .local tarda ~5 s (primero pregunta por IPv6, que el
     * ESP32 no tiene), así que no conviene hacerlo en cada consulta.
     */
    private void resolverIp() throws Exception {
        URI uri = URI.create(this.url);
        InetAddress elegida = null;
        for (InetAddress dir : InetAddress.getAllByName(uri.getHost())) {
            if (dir instanceof Inet4Address) {
                elegida = dir;
                break;
            }
        }
        if (elegida == null) {
            elegida = InetAddress.getByName(uri.getHost());
        }
        this.urlIp = new URI(uri.getScheme(), null, elegida.getHostAddress(), uri.getPort(),
                uri.getPath(), null, null).toString();
    }

    /** Hace una consulta inmediata. Devuelve la lectura o lanza excepción con el motivo. */
    public Map<String, String> probarConexion() throws Exception {
        resolverIp();
        Map<String, String> datos = consultar();
        registrarConsultaExitosa(datos);
        return datos;
    }

    public void iniciar() {
        if (this.activo) {
            return;
        }
        this.activo = true;
        this.hilo = new Thread(this, "ClienteEsp32");
        this.hilo.setDaemon(true);   // no impide cerrar la app
        this.hilo.start();
    }

    public void detener() {
        this.activo = false;
        if (this.hilo != null) {
            this.hilo.interrupt();
        }
    }

    @Override
    public void run() {
        while (this.activo) {
            try {
                Map<String, String> datos = consultar();
                registrarConsultaExitosa(datos);
                acumular(datos);
            } catch (InterruptedException e) {
                break;
            } catch (Exception e) {
                this.conectado = false;
                this.ultimoError = describirError(e);
                // Tras 3 fallos seguidos, volver a buscar la IP (el ESP32 pudo cambiar de IP)
                if (++this.fallosSeguidos % 3 == 0) {
                    try {
                        resolverIp();
                    } catch (Exception ignorada) {
                        // se reintentará en el próximo ciclo
                    }
                }
            }

            if (this.lecturasAcumuladas > 0 && System.currentTimeMillis() >= this.proximoGuardado) {
                guardarPromedio();
            }

            try {
                Thread.sleep(INTERVALO_CONSULTA_MS);
            } catch (InterruptedException e) {
                break;
            }
        }
        this.activo = false;
        this.conectado = false;
    }

    private Map<String, String> consultar() throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create(this.urlIp != null ? this.urlIp : this.url))
                .timeout(Duration.ofSeconds(4))
                .GET()
                .build();
        HttpResponse<String> respuesta = this.http.send(request, HttpResponse.BodyHandlers.ofString());
        if (respuesta.statusCode() != 200) {
            throw new IllegalStateException("El ESP32 respondió " + respuesta.statusCode() + ": " + respuesta.body());
        }
        Map<String, String> datos = parsearJson(respuesta.body());
        if (!datos.containsKey("humedad") || !datos.containsKey("sensor")) {
            throw new IllegalStateException("Respuesta sin humedad/sensor: " + respuesta.body());
        }
        return datos;
    }

    private void registrarConsultaExitosa(Map<String, String> datos) {
        this.fallosSeguidos = 0;
        this.conectado = true;
        this.ultimoError = null;
        this.ultimaLectura = datos;
        this.momentoUltimaLectura = System.currentTimeMillis();
    }

    private void acumular(Map<String, String> datos) {
        long seq = Long.parseLong(datos.getOrDefault("seq", "-1"));
        if (seq == this.ultimoSeq) {
            return;   // misma lectura que la consulta anterior: no contarla dos veces
        }
        this.ultimoSeq = seq;
        this.sumaHumedad += Double.parseDouble(datos.get("humedad"));
        if (this.lecturasAcumuladas == 0) {
            this.proximoGuardado = System.currentTimeMillis() + this.intervaloGuardadoMs;
        }
        this.lecturasAcumuladas++;
    }

    private void guardarPromedio() {
        Map<String, String> datos = this.ultimaLectura;
        double promedio = Math.round(this.sumaHumedad / this.lecturasAcumuladas * 100.0) / 100.0;
        String idSensor = datos.get("sensor").toUpperCase();
        String fecha = LocalDateTime.now().format(FORMATO_FECHA);

        synchronized (this.terreno) {
            Sensor sensor = this.terreno.buscarSensor(idSensor);
            if (sensor == null) {
                // Sensor nuevo: se registra con su calibración real (mV -> V)
                double vSeco = Double.parseDouble(datos.getOrDefault("seco_mv", "2800")) / 1000.0;
                double vSat = Double.parseDouble(datos.getOrDefault("mojado_mv", "1200")) / 1000.0;
                sensor = new Sensor(idSensor, "Parcela-" + idSensor, vSeco, vSat);
                this.terreno.agregarSensor(sensor);
            }

            String idLectura = this.database.generarSiguienteId(this.terreno);
            Lectura lectura = new Lectura(idLectura, fecha, promedio);
            if (sensor.registrarLectura(lectura) && this.database.guardarTerrenoEnCsv(this.terreno)) {
                this.totalGuardadas++;
                this.ultimoGuardado = String.format("%s | %s | %.2f%% (promedio de %d lecturas)",
                        idLectura, fecha, promedio, this.lecturasAcumuladas);
            } else {
                this.ultimoError = "No se pudo guardar la lectura en el CSV.";
            }
        }

        this.sumaHumedad = 0;
        this.lecturasAcumuladas = 0;
    }

    /** Parser mínimo para el JSON plano que envía el ESP32 ({"clave":valor,...}). */
    static Map<String, String> parsearJson(String json) {
        Map<String, String> datos = new HashMap<>();
        Matcher m = PAR_JSON.matcher(json);
        while (m.find()) {
            String valor = m.group(2);
            if (valor.startsWith("\"")) {
                valor = valor.substring(1, valor.length() - 1);
            }
            datos.put(m.group(1), valor);
        }
        return datos;
    }

    private static String describirError(Exception e) {
        String tipo = e.getClass().getSimpleName();
        if (tipo.contains("UnknownHost") || (e.getCause() != null
                && e.getCause().getClass().getSimpleName().contains("UnknownHost"))) {
            return "No se encuentra el ESP32 en la red (¿mismo hotspot? ¿encendido?).";
        }
        if (tipo.contains("Timeout") || tipo.contains("Connect")) {
            return "El ESP32 no responde (" + tipo + ").";
        }
        return e.getMessage() != null ? e.getMessage() : tipo;
    }

    // ---------------- Getters para el menú ----------------

    public boolean isActivo() {
        return this.activo;
    }

    public boolean isConectado() {
        return this.conectado;
    }

    public String getUrl() {
        if (this.urlIp == null || this.urlIp.equals(this.url)) {
            return this.url;
        }
        return this.url + "  (IP: " + URI.create(this.urlIp).getHost() + ")";
    }

    public Terreno getTerreno() {
        return this.terreno;
    }

    public String getUltimoError() {
        return this.ultimoError;
    }

    public Map<String, String> getUltimaLectura() {
        return this.ultimaLectura;
    }

    public long getSegundosDesdeUltimaLectura() {
        return this.momentoUltimaLectura == 0 ? -1 : (System.currentTimeMillis() - this.momentoUltimaLectura) / 1000;
    }

    public int getLecturasAcumuladas() {
        return this.lecturasAcumuladas;
    }

    public long getSegundosParaGuardar() {
        if (this.lecturasAcumuladas == 0) {
            return -1;
        }
        return Math.max(0, (this.proximoGuardado - System.currentTimeMillis()) / 1000);
    }

    public int getTotalGuardadas() {
        return this.totalGuardadas;
    }

    public String getUltimoGuardado() {
        return this.ultimoGuardado;
    }

    public long getMinutosEntreGuardados() {
        return this.intervaloGuardadoMs / 60_000L;
    }
}
