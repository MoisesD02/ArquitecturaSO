package com.simuladorso.memory.algoritmos.buddy;

import java.util.ArrayList;
import java.util.List;

public class BuddyTree {

    public enum EstadoBloque { LIBRE, OCUPADO, DIVIDIDO }

    public static class NodoBloque {
        private final int startAddress;
        private final int sizeKB;
        private EstadoBloque estado;
        private String processName;
        private int requestedKB;

        private NodoBloque leftChild;
        private NodoBloque rightChild;
        private NodoBloque parent;

        public NodoBloque(int startAddress, int sizeKB, NodoBloque parent) {
            this.startAddress = startAddress;
            this.sizeKB = sizeKB;
            this.estado = EstadoBloque.LIBRE;
            this.parent = parent;
        }

        public boolean isLeaf() {
            return estado != EstadoBloque.DIVIDIDO;
        }

        // Getters
        public int getStartAddress() { return startAddress; }
        public int getSizeKB() { return sizeKB; }
        public EstadoBloque getEstado() { return estado; }
        public String getProcessName() { return processName; }
        public int getRequestedKB() { return requestedKB; }
        public NodoBloque getLeftChild() { return leftChild; }
        public NodoBloque getRightChild() { return rightChild; }
    }

    private final int totalMemoryKB;
    private final NodoBloque root;

    public BuddyTree(int totalMemoryKB) {
        this.totalMemoryKB = totalMemoryKB;
        this.root = new NodoBloque(0, totalMemoryKB, null);
    }

    /**
     * Intenta asignar un proceso buscando el bloque libre más pequeño posible (potencia de 2)
     */
    public boolean allocate(String processName, int requestedKB) {
        if (requestedKB <= 0 || requestedKB > totalMemoryKB) return false;

        // Calcular el bloque mínimo (potencia de 2) capaz de albergar el proceso
        int targetBlockSize = getNextPowerOfTwo(requestedKB);

        NodoBloque bestNode = findBestFitNode(root, targetBlockSize);
        if (bestNode == null) return false; // Memoria insuficiente/fragmentada

        // Subdividir sucesivamente si el bloque encontrado es más grande de lo necesario
        while (bestNode.sizeKB > targetBlockSize) {
            splitNode(bestNode);
            bestNode = bestNode.leftChild; // Tomar el compañero izquierdo para asignar
        }

        // Asignar el proceso en el nodo hoja
        bestNode.estado = EstadoBloque.OCUPADO;
        bestNode.processName = processName;
        bestNode.requestedKB = requestedKB;
        return true;
    }

    /**
     * Libera un proceso y fusiona los bloques compañeros (buddies) iterativamente
     */
    public boolean deallocate(String processName) {
        NodoBloque targetNode = findNodeByProcess(root, processName);
        if (targetNode == null) return false;

        targetNode.estado = EstadoBloque.LIBRE;
        targetNode.processName = null;
        targetNode.requestedKB = 0;

        // Proceso de coalescencia (fusión de buddies libres)
        tryCoalesce(targetNode);
        return true;
    }

    private void splitNode(NodoBloque node) {
        int halfSize = node.sizeKB / 2;
        node.estado = EstadoBloque.DIVIDIDO;
        node.leftChild = new NodoBloque(node.startAddress, halfSize, node);
        node.rightChild = new NodoBloque(node.startAddress + halfSize, halfSize, node);
    }

    private void tryCoalesce(NodoBloque node) {
        NodoBloque current = node;
        while (current.parent != null) {
            NodoBloque parent = current.parent;
            NodoBloque left = parent.leftChild;
            NodoBloque right = parent.rightChild;

            // Si ambos hermanos son hojas y están LIBRES, los fusionamos en el padre
            if (left.estado == EstadoBloque.LIBRE && right.estado == EstadoBloque.LIBRE) {
                parent.leftChild = null;
                parent.rightChild = null;
                parent.estado = EstadoBloque.LIBRE;
                current = parent; // Continuar intentando fusionar hacia arriba
            } else {
                break;
            }
        }
    }

    private NodoBloque findBestFitNode(NodoBloque node, int targetSize) {
        if (node.estado == EstadoBloque.OCUPADO) return null;

        if (node.isLeaf()) {
            return (node.estado == EstadoBloque.LIBRE && node.sizeKB >= targetSize) ? node : null;
        }

        // Buscar primero en el lado izquierdo
        NodoBloque leftResult = findBestFitNode(node.leftChild, targetSize);
        if (leftResult != null && leftResult.sizeKB == targetSize) return leftResult;

        // Si no está en el izquierdo o hay uno mejor, buscar en el derecho
        NodoBloque rightResult = findBestFitNode(node.rightChild, targetSize);

        if (leftResult == null) return rightResult;
        if (rightResult == null) return leftResult;

        return (leftResult.sizeKB <= rightResult.sizeKB) ? leftResult : rightResult;
    }

    private NodoBloque findNodeByProcess(NodoBloque node, String processName) {
        if (node == null) return null;
        if (node.estado == EstadoBloque.OCUPADO && processName.equals(node.processName)) {
            return node;
        }
        if (node.isLeaf()) return null;

        NodoBloque left = findNodeByProcess(node.leftChild, processName);
        if (left != null) return left;

        return findNodeByProcess(node.rightChild, processName);
    }

    /**
     * Retorna todas las hojas activas (bloques resultantes) en orden cronológico de memoria
     */
    public List<NodoBloque> getLeafBlocks() {
        List<NodoBloque> leaves = new ArrayList<>();
        collectLeaves(root, leaves);
        return leaves;
    }

    private void collectLeaves(NodoBloque node, List<NodoBloque> leaves) {
        if (node == null) return;
        if (node.isLeaf()) {
            leaves.add(node);
        } else {
            collectLeaves(node.leftChild, leaves);
            collectLeaves(node.rightChild, leaves);
        }
    }

    public static int getNextPowerOfTwo(int value) {
        if (value <= 1) return 1;
        return 1 << (32 - Integer.numberOfLeadingZeros(value - 1));
    }

    public int getTotalMemoryKB() { return totalMemoryKB; }
}