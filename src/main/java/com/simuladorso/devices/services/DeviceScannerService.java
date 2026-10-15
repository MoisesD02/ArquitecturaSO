package com.simuladorso.devices.services;

import com.simuladorso.devices.model.DispositivoEntradaSalida;
import oshi.SystemInfo;
import oshi.hardware.Display;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.NetworkIF;
import oshi.hardware.SoundCard;
import oshi.hardware.UsbDevice;

import java.util.ArrayList;
import java.util.List;

public class DeviceScannerService {

    private final SystemInfo systemInfo = new SystemInfo();

    public List<DispositivoEntradaSalida> escanearDispositivosReales() {
        List<DispositivoEntradaSalida> listaActual = new ArrayList<>();
        HardwareAbstractionLayer hal = systemInfo.getHardware();

        // 1. Pantallas y Monitores Físicos Conectados (HDMI, DisplayPort, Pantalla Integrada)
        int contadorMonitores = 1;
        for (Display display : hal.getDisplays()) {
            // Cada objeto Display en la lista representa una pantalla física detectada por el SO
            String nombre = "Monitor / Pantalla " + contadorMonitores;

            // Si la implementación de OSHI en tu versión expone toString() o información textual:
            String infoDisplay = display.toString();
            if (infoDisplay != null && !infoDisplay.isBlank() && !infoDisplay.contains("Display")) {
                nombre = infoDisplay;
            }

            agregarSiNoExiste(listaActual, new DispositivoEntradaSalida(nombre, "HDMI / DisplayPort / Bus de Video", "Controlador de Pantalla PnP"));
            contadorMonitores++;
        }

        // 2. Interfaces de Red Activas (Wi-Fi, Ethernet)
        for (NetworkIF net : hal.getNetworkIFs()) {
            net.updateAttributes();
            if (net.getIfOperStatus() == NetworkIF.IfOperStatus.UP
                    && !net.getDisplayName().toLowerCase().contains("virtual")
                    && !net.getDisplayName().toLowerCase().contains("loopback")
                    && !net.getDisplayName().toLowerCase().contains("pseudo")
                    && net.getBytesRecv() > 0) {

                String nombre = net.getDisplayName();
                String conexion = (nombre.toLowerCase().contains("wi-fi") || nombre.toLowerCase().contains("wireless")) ? "Wi-Fi" : "Ethernet / LAN";
                agregarSiNoExiste(listaActual, new DispositivoEntradaSalida(nombre, conexion, "Controlador de Red"));
            }
        }

        // 3. Tarjetas de Sonido / Audio Activas
        for (SoundCard audio : hal.getSoundCards()) {
            String name = audio.getName();
            if (name != null && !name.toLowerCase().contains("virtual") && !name.isBlank()) {
                agregarSiNoExiste(listaActual, new DispositivoEntradaSalida(name, "Audio Analógico / Digital", audio.getCodec()));
            }
        }

        // 4. Árbol USB (Teclados, Mouse, Almacenamiento USB)
        for (UsbDevice usb : hal.getUsbDevices(true)) {
            procesarDispositivoUsb(usb, listaActual);
        }

        return listaActual;
    }

    private void procesarDispositivoUsb(UsbDevice device, List<DispositivoEntradaSalida> lista) {
        String name = device.getName() != null ? device.getName().trim() : "";
        String lowerName = name.toLowerCase();

        // Filtro estricto para ignorar hubs internos, controladores y caché Bluetooth
        boolean esBasuraInterna = lowerName.contains("hub")
                || lowerName.contains("host controller")
                || lowerName.contains("raíz")
                || lowerName.contains("root")
                || lowerName.contains("composite")
                || lowerName.contains("compuesto")
                || lowerName.contains("virtual")
                || lowerName.contains("pci")
                || lowerName.contains("bluetooth")
                || lowerName.contains("bth")
                || name.isBlank();

        if (!esBasuraInterna) {
            String conexion = "USB / Thunderbolt";
            String driver = "Controlador USB";

            if (lowerName.contains("mouse") || lowerName.contains("keyboard") || lowerName.contains("teclado") || lowerName.contains("input") || lowerName.contains("hid")) {
                driver = "Dispositivo de Interfaz Humana (HID)";
            } else if (lowerName.contains("storage") || lowerName.contains("flash") || lowerName.contains("mass")) {
                driver = "Almacenamiento Masivo";
            } else if (lowerName.contains("camera") || lowerName.contains("video")) {
                driver = "Dispositivo de Captura de Video";
            }

            agregarSiNoExiste(lista, new DispositivoEntradaSalida(name, conexion, driver));
        }

        for (UsbDevice child : device.getConnectedDevices()) {
            procesarDispositivoUsb(child, lista);
        }
    }

    private void agregarSiNoExiste(List<DispositivoEntradaSalida> lista, DispositivoEntradaSalida nuevo) {
        if (!lista.contains(nuevo)) {
            lista.add(nuevo);
        }
    }
}