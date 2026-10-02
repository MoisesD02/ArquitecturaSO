package com.simuladorso.memory;

import java.util.*;

/** Variable partitions, represented by an actual singly linked list. Addresses are in KB. */
public final class LinkedMemory {
    private static final class Node {
        int start, size, id; String name; Node next;
        Node(int start, int size) { this.start=start; this.size=size; }
    }
    public record Block(int start, int size, int id, String name) {
        public boolean free() { return id==0; }
    }
    private Node head;
    private int capacity, nextId, cursor;
    private String explanation="";
    public LinkedMemory(int capacity) { reset(capacity); }
    public void reset(int capacity) {
        if(capacity<1 || capacity>1048576) throw new IllegalArgumentException("Usa una capacidad entre 1 y 1048576 KB.");
        this.capacity=capacity; head=new Node(0,capacity); nextId=1; cursor=0;
        explanation="Memoria reiniciada: un único bloque libre.";
    }
    public int capacity(){return capacity;}
    public int cursor(){return cursor;}
    public String explanation(){return explanation;}
    public List<Block> blocks(){
        List<Block> result=new ArrayList<>();
        for(Node n=head;n!=null;n=n.next) result.add(new Block(n.start,n.size,n.id,n.name));
        return List.copyOf(result);
    }
    public int freeKB(){return blocks().stream().filter(Block::free).mapToInt(Block::size).sum();}
    public int largestGap(){return blocks().stream().filter(Block::free).mapToInt(Block::size).max().orElse(0);}
    public Block allocate(String name,int size,String strategy){
        if(name==null || name.isBlank()) throw new IllegalArgumentException("Escribe el nombre del proceso.");
        if(size<=0) throw new IllegalArgumentException("El tamaño debe ser mayor que cero.");
        Node selected=null; int address=-1, visited=0;
        if("Next Fit".equals(strategy)) {
            // Search from the saved address, wrap once, then consider the full containing hole.
            for(int pass=0;pass<2 && selected==null;pass++) {
                for(Node n=head;n!=null;n=n.next) {
                    if(pass==0 && n.start+n.size<=cursor || pass==1 && n.start>=cursor) continue;
                    visited++;
                    int begin=pass==0 ? Math.max(n.start,cursor):n.start;
                    if(n.id==0 && n.start+n.size-begin>=size){selected=n;address=begin;break;}
                }
            }
        } else {
            if(!List.of("First Fit","Best Fit","Worst Fit").contains(strategy)) throw new IllegalArgumentException("Estrategia desconocida.");
            for(Node n=head;n!=null;n=n.next){
                visited++;
                if(n.id!=0 || n.size<size) continue;
                if(selected==null || "Best Fit".equals(strategy)&&n.size<selected.size || "Worst Fit".equals(strategy)&&n.size>selected.size) selected=n;
                if("First Fit".equals(strategy)) break;
            }
            if(selected!=null) address=selected.start;
        }
        if(selected==null) throw new IllegalArgumentException("No hay un hueco continuo de "+size+" KB. Libres: "+freeKB()+" KB; mayor hueco: "+largestGap()+" KB.");
        int originalSize=selected.size;
        if(address>selected.start){
            Node remainder=new Node(address,selected.start+selected.size-address);
            remainder.next=selected.next; selected.next=remainder; selected.size=address-selected.start;selected=remainder;
        }
        if(selected.size>size){
            Node tail=new Node(address+size,selected.size-size);tail.next=selected.next;selected.next=tail;
        }
        selected.size=size;selected.id=nextId++;selected.name=name.trim();cursor=(address+size)%capacity;
        explanation=strategy+": "+visited+" bloques examinados. Hueco de "+originalSize+" KB; asignados "+size+" KB en ["+address+", "+(address+size)+").";
        return new Block(address,size,selected.id,selected.name);
    }
    public void release(int id){
        Node target=head;while(target!=null && target.id!=id) target=target.next;
        if(id==0 || target==null) throw new IllegalArgumentException("Selecciona un proceso ocupado.");
        String name=target.name;target.id=0;target.name=null;int merges=0;
        for(Node n=head;n!=null && n.next!=null;){
            if(n.id==0 && n.next.id==0){n.size+=n.next.size;n.next=n.next.next;merges++;}
            else n=n.next;
        }
        explanation=name+" liberado. Fusiones de huecos vecinos: "+merges+". Los demás procesos conservan sus direcciones.";
    }
    public void example(){
        reset(1024);
        allocate("A",100,"First Fit");var b=allocate("B",250,"First Fit");
        allocate("C",100,"First Fit");var d=allocate("D",150,"First Fit");
        allocate("E",100,"First Fit");release(b.id());release(d.id());
        explanation="Ejemplo: huecos de 250, 150 y 324 KB. Prueba un proceso de 120 KB: First Fit → 100; Next Fit → 700; Best Fit → 450; Worst Fit → 700. Recarga el ejemplo para comparar.";
    }
}
