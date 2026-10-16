package com.simuladorso.devices.services;

import com.simuladorso.devices.model.WindowsDriverInfo;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class WindowsDriverInfoService {

    private static final Pattern USB_ID = Pattern.compile(
            "(?i)(?:USB:0x([0-9a-f]{4}):0x([0-9a-f]{4})|VID_([0-9a-f]{4})&PID_([0-9a-f]{4}))"
    );

    public Optional<WindowsDriverInfo> consultar(String id, String nombreDispositivo) {
        System.out.println("[DRIVER] ID recibido: " + id);
        System.out.println("[DRIVER] Nombre recibido: " + nombreDispositivo);
        if (!System.getProperty("os.name", "")
                .toLowerCase(Locale.ROOT)
                .contains("win")) {
            return Optional.empty();
        }

        Matcher matcher = USB_ID.matcher(id == null ? "" : id);
        if (!matcher.find()) {
            return Optional.empty();
        }

        String vid = (matcher.group(1) != null ? matcher.group(1) : matcher.group(3))
                .toUpperCase(Locale.ROOT);
        String pid = (matcher.group(2) != null ? matcher.group(2) : matcher.group(4))
                .toUpperCase(Locale.ROOT);

        String script = """
                $ErrorActionPreference = 'Stop'
                $pattern = 'VID_%s&PID_%s'
                function Enc([object]$v) {
                    if ($null -eq $v) { $v = '' }
                    return [Convert]::ToBase64String([Text.Encoding]::UTF8.GetBytes([string]$v))
                }
                $items = @(Get-CimInstance Win32_PnPSignedDriver | Where-Object {
                    $_.DeviceID -and $_.DeviceID.ToUpperInvariant().Contains($pattern)
                })
                foreach ($d in $items) {
                    $date = ''
                    if ($d.DriverDate) {
                        try { $date = ([datetime]$d.DriverDate).ToString('dd/MM/yyyy') }
                        catch { $date = [string]$d.DriverDate }
                    }
                    $values = @(
                        (Enc $d.DeviceName),
                        (Enc $d.DriverProviderName),
                        (Enc $d.DriverVersion),
                        (Enc $date),
                        (Enc $d.DeviceID)
                    )
                    Write-Output ($values -join '|')
                }
                """.formatted(vid, pid);

        String encoded = Base64.getEncoder().encodeToString(
                script.getBytes(StandardCharsets.UTF_16LE)
        );

        Process process = null;
        ExecutorService reader = Executors.newSingleThreadExecutor(r -> {
            Thread thread = new Thread(r, "Driver-PowerShell-Reader");
            thread.setDaemon(true);
            return thread;
        });

        try {
            process = new ProcessBuilder(
                    "powershell.exe", "-NoProfile", "-NonInteractive",
                    "-EncodedCommand", encoded
            ).redirectErrorStream(true).start();

            final Process running = process;
            Future<String> output = reader.submit(() -> {
                try (InputStream stream = running.getInputStream()) {
                    return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
                }
            });

            if (!process.waitFor(25, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                return Optional.empty();
            }

            String result = output.get(3, TimeUnit.SECONDS);
            if (process.exitValue() != 0) {
                return Optional.empty();
            }

            List<String[]> candidates = new ArrayList<>();
            for (String line : result.split("\\R")) {
                String[] fields = line.trim().split("\\|", -1);
                if (fields.length != 5) {
                    continue;
                }

                try {
                    for (int i = 0; i < fields.length; i++) {
                        fields[i] = new String(
                                Base64.getDecoder().decode(fields[i]),
                                StandardCharsets.UTF_8
                        );
                    }
                    candidates.add(fields);
                } catch (IllegalArgumentException ignored) {
                    // Ignorar registros que no tengan el formato esperado.
                }
            }

            if (candidates.isEmpty()) {
                return Optional.empty();
            }

            String expected = nombreDispositivo == null
                    ? ""
                    : nombreDispositivo.toLowerCase(Locale.ROOT);

            candidates.sort((a, b) -> Integer.compare(
                    score(b, expected), score(a, expected)
            ));

            String[] best = candidates.get(0);
            return Optional.of(new WindowsDriverInfo(
                    display(best[0]),
                    display(best[1]),
                    display(best[2]),
                    display(best[3])
            ));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Optional.empty();
        } catch (Exception e) {
            return Optional.empty();
        } finally {
            if (process != null && process.isAlive()) {
                process.destroyForcibly();
            }
            reader.shutdownNow();
        }
    }

    private static int score(String[] candidate, String expected) {
        String name = candidate[0].toLowerCase(Locale.ROOT);
        String instance = candidate[4].toUpperCase(Locale.ROOT);
        int value = 0;

        if (!expected.isBlank() && name.equals(expected)) {
            value += 200;
        } else if (!expected.isBlank() && !name.isBlank()
                && (name.contains(expected) || expected.contains(name))) {
            value += 90;
        }

        if ((expected.contains("camera") || expected.contains("cámara")
                || expected.contains("vision"))
                && (name.contains("video") || name.contains("camera"))) {
            value += 100;
        }
        if (expected.contains("mouse") && name.contains("mouse")) {
            value += 100;
        }
        if (expected.contains("keyboard") && name.contains("keyboard")) {
            value += 100;
        }
        if (expected.contains("bluetooth") && name.contains("bluetooth")) {
            value += 100;
        }
        if (name.contains("composite") || name.contains("compuesto")) {
            value -= 80;
        }
        if (name.contains("winusb")) {
            value -= 30;
        }
        if (instance.contains("&MI_")) {
            value += 20;
        }

        return value;
    }

    private static String display(String value) {
        return value == null || value.isBlank()
                ? "No disponible"
                : value.trim();
    }
}
