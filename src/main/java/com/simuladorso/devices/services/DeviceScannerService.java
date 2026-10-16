package com.simuladorso.devices.services;

import com.simuladorso.devices.model.DispositivoEntradaSalida;
import oshi.SystemInfo;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.UsbDevice;

import java.util.*;

public class DeviceScannerService {

    private final SystemInfo systemInfo = new SystemInfo();

    public List<DispositivoEntradaSalida>
    escanearDispositivosReales() {

        HardwareAbstractionLayer hardware =
                systemInfo.getHardware();

        List<UsbDevice> dispositivos =
                hardware.getUsbDevices(false);

        Map<String, GrupoUsb> grupos =
                new LinkedHashMap<>();

        for (UsbDevice usb : dispositivos) {

            String idUnico = textoSeguro(
                    usb.getUniqueDeviceId()
            );

            String vid = textoSeguro(
                    usb.getVendorId()
            ).toLowerCase(Locale.ROOT);

            String pid = textoSeguro(
                    usb.getProductId()
            ).toLowerCase(Locale.ROOT);

            String serial = textoSeguro(
                    usb.getSerialNumber()
            );

            String clave;

            if (!vid.isBlank() && !pid.isBlank()) {

                clave = "USB:" + vid + ":" + pid;

            } else if (!idUnico.isBlank()) {

                clave = idUnico.toLowerCase(Locale.ROOT);

            } else {

                clave = "DESCONOCIDO:"
                        + textoSeguro(usb.getName())
                        + ":"
                        + serial;
            }

            GrupoUsb grupo = grupos.computeIfAbsent(
                    clave,
                    GrupoUsb::new
            );

            grupo.agregar(usb);
        }

        List<DispositivoEntradaSalida> resultado =
                new ArrayList<>();

        for (GrupoUsb grupo : grupos.values()) {

            resultado.add(grupo.convertir());
        }

        resultado.sort(
                Comparator.comparing(
                        DispositivoEntradaSalida::getNombre,
                        String.CASE_INSENSITIVE_ORDER
                )
        );

        return resultado;
    }

    private static String textoSeguro(String texto) {

        return texto == null
                ? ""
                : texto.trim();
    }

    private static int puntuacionNombre(String nombre) {

        String n = textoSeguro(nombre)
                .toLowerCase(Locale.ROOT);

        int puntos = 0;

        // Prioridad a nombres de dispositivos reconocibles.

        if (n.contains("mouse")
                || n.contains("ratón")) {
            puntos += 100;
        }

        if (n.contains("camera")
                || n.contains("cámara")
                || n.contains("webcam")) {
            puntos += 90;
        }

        if (n.contains("bluetooth")) {
            puntos += 85;
        }

        if (n.contains("keyboard")
                || n.contains("teclado")) {
            puntos += 80;
        }

        if (n.contains("printer")
                || n.contains("impresora")) {
            puntos += 75;
        }

        if (n.contains("storage")
                || n.contains("disk")
                || n.contains("almacenamiento")) {
            puntos += 75;
        }

        if (n.contains("microphone")
                || n.contains("micrófono")) {
            puntos += 70;
        }

        if (n.contains("concentrador raíz")
                || n.contains("root hub")) {
            puntos += 60;
        }

        // Penalizar nombres genéricos de interfaces.

        if (n.contains("dispositivo compuesto")) {
            puntos -= 60;
        }

        if (n.contains("dispositivo de entrada usb")) {
            puntos -= 60;
        }

        if (n.contains("definido por el proveedor")) {
            puntos -= 60;
        }

        if (n.contains("controlador del sistema")) {
            puntos -= 50;
        }

        if (n.contains("control del consumidor")) {
            puntos -= 40;
        }

        if (n.contains("dfu")) {
            puntos -= 30;
        }

        if (n.contains("dispositivo de teclado hid")) {
            puntos -= 25;
        }

        return puntos;
    }


    private static boolean fabricanteValido(String fabricante) {

        String f = textoSeguro(fabricante)
                .toLowerCase(Locale.ROOT);

        if (f.isBlank()
                || f.equals("desconocido")
                || f.equals("unknown")
                || f.equals("no disponible")) {
            return false;
        }

        // Descripciones genéricas de Windows.
        if (f.contains("controladora de host usb")
                || f.contains("controlador de host usb")
                || f.contains("usb host controller")
                || f.contains("teclados estándar")
                || f.contains("standard keyboards")
                || f.contains("mouse estándar")
                || f.contains("standard mice")
                || f.contains("dispositivos de entrada")
                || f.contains("standard system devices")) {
            return false;
        }

        return true;
    }


    private static class GrupoUsb {

        private final String id;

        private String nombre = "Dispositivo USB";
        private String fabricante = "No disponible";

        private int mejorPuntuacion = Integer.MIN_VALUE;
        private int cantidadInterfaces = 0;

        private GrupoUsb(String id) {
            this.id = id;
        }

        private void agregar(UsbDevice usb) {

            cantidadInterfaces++;

            String nombreActual =
                    textoSeguro(usb.getName());

            String fabricanteActual =
                    textoSeguro(usb.getVendor());

            if (!nombreActual.isBlank()) {

                int puntuacion =
                        puntuacionNombre(nombreActual);

                if (puntuacion > mejorPuntuacion) {

                    mejorPuntuacion = puntuacion;
                    nombre = nombreActual;
                }
            }

            if (fabricanteValido(fabricanteActual)) {

                if ("No disponible".equals(fabricante)) {
                    fabricante = fabricanteActual;
                }
            }
        }

        private DispositivoEntradaSalida convertir() {

            return new DispositivoEntradaSalida(
                    id,
                    nombre,
                    "USB",
                    fabricante,
                    "Conectado",
                    cantidadInterfaces
            );
        }
    }
}
