
package com.simuladorso.devices.services;

import com.simuladorso.devices.model.DispositivoEntradaSalida;
import oshi.SystemInfo;
import oshi.hardware.UsbDevice;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

public class DeviceScannerService {

    private final SystemInfo systemInfo = new SystemInfo();

    /**
     * Consulta los dispositivos presentes en el equipo.
     * En Windows utiliza Plug and Play; OSHI queda como alternativa.
     */
    public List<DispositivoEntradaSalida> escanearDispositivosReales() {
        List<DispositivoEntradaSalida> windows = escanearConWindows();

        if (windows != null) {
            return windows;
        }

        return escanearConOshi();
    }

    /**
     * null significa que la consulta de Windows falló.
     * Una lista vacía significa que la consulta funcionó,
     * pero no encontró dispositivos relevantes.
     */
    private List<DispositivoEntradaSalida> escanearConWindows() {
        String script =
                "$ErrorActionPreference='Stop'; " +
                        "$OutputEncoding=[Console]::OutputEncoding=[System.Text.UTF8Encoding]::new(); " +
                        "$clases=@('Keyboard','Mouse','Monitor','USB','DiskDrive'," +
                        "'Camera','Image','HIDClass'); " +
                        "Get-PnpDevice -PresentOnly | " +
                        "Where-Object { $_.Status -eq 'OK' -and $_.Class -in $clases } | " +
                        "ForEach-Object { " +
                        "$n=[string]$_.FriendlyName; " +
                        "$c=[string]$_.Class; " +
                        "$m=[string]$_.Manufacturer; " +
                        "$i=[string]$_.InstanceId; " +
                        "if ($n -and $n.Trim()) { " +
                        "$n=$n.Replace('|','/').Replace(\"`t\",' '); " +
                        "$m=$m.Replace('|','/').Replace(\"`t\",' '); " +
                        "$i=$i.Replace('|','/').Replace(\"`t\",' '); " +
                        "Write-Output ($n+'|'+$c+'|'+$m+'|'+$i) " +
                        "} }";

        Process proceso = null;

        try {
            proceso = new ProcessBuilder(
                    "powershell.exe",
                    "-NoProfile",
                    "-NonInteractive",
                    "-ExecutionPolicy", "Bypass",
                    "-Command", script
            )
                    .redirectErrorStream(true)
                    .start();

            // El resultado se lee en el hilo de escaneo, nunca en JavaFX.
            List<String> lineas = new ArrayList<>();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(
                            proceso.getInputStream(),
                            StandardCharsets.UTF_8))) {

                String linea;
                while ((linea = reader.readLine()) != null) {
                    lineas.add(linea);
                }
            }

            if (!proceso.waitFor(2, TimeUnit.SECONDS)
                    || proceso.exitValue() != 0) {
                return null;
            }

            Map<String, DispositivoEntradaSalida> unicos =
                    new TreeMap<>();

            for (String linea : lineas) {
                String[] datos = linea.split("\\|", 4);

                if (datos.length != 4) {
                    continue;
                }

                String nombre = datos[0].trim();
                String clase = datos[1].trim();
                String fabricante = datos[2].trim();
                String idHardware = datos[3].trim();

                if (nombre.isBlank() || !esRelevante(nombre, clase, idHardware)) {
                    continue;
                }

                String conexion = determinarConexion(clase, idHardware, nombre);

                String detalle = traducirClase(clase);

                if (!fabricante.isBlank()
                        && !fabricante.equalsIgnoreCase("Unknown")) {
                    detalle += " | Fabricante: " + fabricante;
                }

                // El ID ayuda a distinguir dispositivos con el mismo nombre.
                // Se conserva en la columna Driver para no cambiar el modelo.
                if (!idHardware.isBlank()) {
                    detalle += " | ID: " + idHardware;
                }

                DispositivoEntradaSalida dispositivo =
                        new DispositivoEntradaSalida(nombre, conexion, detalle);

                String clave = (clase + "|" + idHardware + "|" + nombre)
                        .toLowerCase(Locale.ROOT);

                unicos.put(clave, dispositivo);
            }

            return new ArrayList<>(unicos.values());

        } catch (Exception e) {
            return null;
        } finally {
            if (proceso != null && proceso.isAlive()) {
                proceso.destroyForcibly();
            }
        }
    }

    /**
     * Alternativa multiplataforma: OSHI puede exponer menos detalles
     * que Plug and Play en Windows.
     */
    private List<DispositivoEntradaSalida> escanearConOshi() {
        Map<String, DispositivoEntradaSalida> unicos = new TreeMap<>();

        for (UsbDevice usb : systemInfo.getHardware().getUsbDevices(true)) {
            procesarUsb(usb, unicos);
        }

        return new ArrayList<>(unicos.values());
    }

    private void procesarUsb(
            UsbDevice usb,
            Map<String, DispositivoEntradaSalida> unicos) {

        if (usb == null) {
            return;
        }

        String nombre = usb.getName() == null
                ? ""
                : usb.getName().trim();

        String lower = nombre.toLowerCase(Locale.ROOT);

        if (!nombre.isBlank() && !esTerminoInterno(lower)) {
            String tipo = clasificarNombre(lower);

            if (tipo != null) {
                String conexion = determinarConexion(tipo, "", nombre);

                String detalle = tipo;

                String vendorId = usb.getVendorId();
                String productId = usb.getProductId();
                String serial = usb.getSerialNumber();

                if (vendorId != null && !vendorId.isBlank()) {
                    detalle += " | Vendor ID: " + vendorId;
                }

                if (productId != null && !productId.isBlank()) {
                    detalle += " | Product ID: " + productId;
                }

                if (serial != null && !serial.isBlank()) {
                    detalle += " | Serie: " + serial;
                }

                DispositivoEntradaSalida dispositivo =
                        new DispositivoEntradaSalida(nombre, conexion, detalle);

                String clave = (nombre + "|" + vendorId + "|" + productId)
                        .toLowerCase(Locale.ROOT);

                unicos.put(clave, dispositivo);
            }
        }

        if (usb.getConnectedDevices() != null) {
            for (UsbDevice hijo : usb.getConnectedDevices()) {
                procesarUsb(hijo, unicos);
            }
        }
    }

    private boolean esRelevante(
            String nombre, String clase, String idHardware) {

        String n = nombre.toLowerCase(Locale.ROOT);
        String c = clase.toLowerCase(Locale.ROOT);
        String id = idHardware.toLowerCase(Locale.ROOT);

        if (esTerminoInterno(n)) {
            return false;
        }

        // Filtra dispositivos virtuales y entradas que no son periféricos.
        if (n.contains("virtual")
                || n.contains("loopback")
                || n.contains("bluetooth enumerator")
                || n.contains("software device")
                || n.contains("root enumerator")) {
            return false;
        }

        if (c.equals("monitor")) {
            return true;
        }

        if (c.equals("mouse") || c.equals("keyboard")
                || c.equals("camera") || c.equals("image")
                || c.equals("diskdrive")) {
            return true;
        }

        if (c.equals("usb") || c.equals("hidclass")) {
            return clasificarNombre(n) != null
                    || id.startsWith("usb\\")
                    || id.startsWith("usbstor\\");
        }

        return false;
    }

    private boolean esTerminoInterno(String nombre) {
        return nombre.contains("host controller")
                || nombre.contains("root hub")
                || nombre.contains("generic hub")
                || nombre.contains("usb hub")
                || nombre.contains("hub raíz")
                || nombre.contains("controlador host")
                || nombre.contains("virtual")
                || nombre.contains("root enumerator")
                || nombre.contains("software device");
    }

    private String clasificarNombre(String nombre) {
        String n = nombre.toLowerCase(Locale.ROOT);

        if (n.contains("mouse") || n.contains("ratón")
                || n.contains("raton") || n.contains("trackball")) {
            return "Ratón";
        }

        if (n.contains("keyboard") || n.contains("teclado")) {
            return "Teclado";
        }

        if (n.contains("monitor") || n.contains("display")) {
            return "Monitor";
        }

        if (n.contains("webcam") || n.contains("camera")
                || n.contains("cámara") || n.contains("camara")) {
            return "Cámara";
        }

        if (n.contains("flash") || n.contains("mass storage")
                || n.contains("storage") || n.contains("thumb drive")
                || n.contains("pendrive") || n.contains("kingston")
                || n.contains("sandisk") || n.contains("cruzer")) {
            return "Almacenamiento USB";
        }

        if (n.contains("gamepad") || n.contains("controller")
                || n.contains("joystick")) {
            return "Control de videojuegos";
        }

        if (n.contains("headset") || n.contains("auricular")
                || n.contains("headphone")) {
            return "Auriculares";
        }

        if (n.contains("printer") || n.contains("impresora")) {
            return "Impresora";
        }

        if (n.contains("scanner") || n.contains("escáner")
                || n.contains("escaner")) {
            return "Escáner";
        }

        return null;
    }

    private String traducirClase(String clase) {
        switch (clase.toLowerCase(Locale.ROOT)) {
            case "mouse":
                return "Ratón";
            case "keyboard":
                return "Teclado";
            case "monitor":
                return "Monitor";
            case "diskdrive":
                return "Unidad de almacenamiento";
            case "camera":
            case "image":
                return "Cámara / Imagen";
            case "usb":
                return "Dispositivo USB";
            case "hidclass":
                return "Periférico HID";
            default:
                return clase;
        }
    }

    private String determinarConexion(
            String clase, String idHardware, String nombre) {

        String id = idHardware.toLowerCase(Locale.ROOT);
        String n = nombre.toLowerCase(Locale.ROOT);
        String c = clase.toLowerCase(Locale.ROOT);

        if (c.equals("monitor")) {
            return "Monitor / Vídeo";
        }

        if (id.startsWith("usbstor\\")
                || id.startsWith("usb\\")
                || n.contains("usb")) {
            return "USB";
        }

        if (c.equals("mouse") || c.equals("keyboard")
                || c.equals("hidclass")) {
            return "HID / USB u otra interfaz";
        }

        return "Plug and Play";
    }
}
