package com.simuladorso.filesystem;

import java.util.List;

public class SistemaArchivos {
    private final List<String> directoriosBase = List.of("home", "system", "temp");

    public List<String> getDirectoriosBase() {
        return directoriosBase;
    }
}
