package com.simuladorso.memory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Contiguous allocation over 1024 fixed-size allocation units. */
public final class BitmapMemory {
    public static final int CELLS = 1024;
    public record Allocation(int id, String name, int requestedKB, int start, int units) {
        @Override public String toString() {
            return name + " · " + requestedKB + " KB · celdas " + start + "–" + (start + units - 1);
        }
    }
    private final int[] owners = new int[CELLS];
    private final List<Allocation> allocations = new ArrayList<>();
    private int unitKB = 4;
    private int nextId = 1;
    public int unitKB() { return unitKB; }
    public int owner(int index) { return owners[index]; }
    public List<Allocation> allocations() { return List.copyOf(allocations); }
    public int freeUnits() { return (int) Arrays.stream(owners).filter(v -> v == 0).count(); }
    public int largestGap() {
        int largest = 0, run = 0;
        for (int owner : owners) { run = owner == 0 ? run + 1 : 0; largest = Math.max(largest, run); }
        return largest;
    }
    public long wasteKB() {
        return allocations.stream().mapToLong(a -> (long) a.units() * unitKB - a.requestedKB()).sum();
    }
    public void configure(int size) {
        if (!allocations.isEmpty()) throw new IllegalArgumentException("Reinicia antes de cambiar la unidad.");
        if (size < 1 || size > 1048576 || (size & (size - 1)) != 0)
            throw new IllegalArgumentException("La unidad debe ser una potencia de 2 entre 1 y 1048576 KB.");
        unitKB = size;
    }
    public Allocation allocate(String name, int requestedKB) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Escribe el nombre del proceso.");
        if (requestedKB <= 0) throw new IllegalArgumentException("El tamaño debe ser mayor que cero.");
        int needed = (int) (((long) requestedKB + unitKB - 1) / unitKB);
        int run = 0;
        for (int i = 0; i < CELLS; i++) {
            run = owners[i] == 0 ? run + 1 : 0;
            if (run == needed) {
                Allocation a = new Allocation(nextId++, name.trim(), requestedKB, i - needed + 1, needed);
                Arrays.fill(owners, a.start(), i + 1, a.id());
                allocations.add(a);
                return a;
            }
        }
        throw new IllegalArgumentException("Se necesitan " + needed + " unidades consecutivas. Libres: "
                + freeUnits() + ". Mayor hueco: " + largestGap() + ".");
    }
    public void release(int id) {
        Allocation a = allocations.stream().filter(v -> v.id() == id).findFirst().orElseThrow();
        Arrays.fill(owners, a.start(), a.start() + a.units(), 0);
        allocations.remove(a);
    }
    public void reset() { Arrays.fill(owners, 0); allocations.clear(); nextId = 1; }
}
