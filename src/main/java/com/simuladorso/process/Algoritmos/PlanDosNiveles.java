package com.simuladorso.process.Algoritmos;
import java.util.*;

/** Batch model: medium-term rotation every 4 ticks; resident CPU round robin, quantum 2. */
public class PlanDosNiveles {
    public static final int QUANTUM=2, PERIODO=4;
    public record ProcesoPlanificado(int pid,String nombre,int memoriaMb,int rafagaCpu) {}
    public record Paso(ProcesoPlanificado proceso,int inicio,int fin,List<Integer> residentes) {}
    public record Resultado(int memoriaTotalMb,List<ProcesoPlanificado> memoriaInicial,
        List<ProcesoPlanificado> secundariaInicial,List<ProcesoPlanificado> ordenEjecucion,
        List<String> eventos,List<Paso> pasos) {}
    public Resultado simular(int total,List<ProcesoPlanificado> procesos){
        if(total<=0 || procesos==null || procesos.isEmpty())throw new IllegalArgumentException("Memoria y procesos requeridos.");
        Set<Integer> ids=new HashSet<>();
        for(var p:procesos)if(p.memoriaMb()<=0 || p.memoriaMb()>total || p.rafagaCpu()<=0 || !ids.add(p.pid()))
            throw new IllegalArgumentException("Cada proceso debe caber en los "+total+" MB, tener ráfaga positiva y PID único.");
        Map<Integer,Integer> restante=new HashMap<>();procesos.forEach(p->restante.put(p.pid(),p.rafagaCpu()));
        LinkedList<ProcesoPlanificado> disco=new LinkedList<>(procesos),ram=new LinkedList<>();
        List<ProcesoPlanificado> inicial=new ArrayList<>(),secundaria=new ArrayList<>(),terminados=new ArrayList<>();
        List<Paso> pasos=new ArrayList<>();List<String> eventos=new ArrayList<>();int t=0,epoca=0;
        while(!ram.isEmpty() || !disco.isEmpty()){
            if(epoca==0 && !ram.isEmpty() && !disco.isEmpty()){
                for(var p:ram)eventos.add("t="+t+" Swap-out: "+p.nombre());
                disco.addAll(ram);ram.clear();
            }
            int disponible=total-ram.stream().mapToInt(ProcesoPlanificado::memoriaMb).sum();
            for(var it=disco.iterator();it.hasNext();){var p=it.next();if(p.memoriaMb()<=disponible){ram.add(p);disponible-=p.memoriaMb();it.remove();eventos.add("t="+t+" Swap-in: "+p.nombre());}}
            if(t==0){inicial.addAll(ram);secundaria.addAll(disco);}
            var residentes=ram.stream().map(ProcesoPlanificado::pid).toList();
            var actual=ram.removeFirst();int duracion=Math.min(Math.min(QUANTUM,PERIODO-epoca),restante.get(actual.pid()));
            pasos.add(new Paso(actual,t,t+duracion,residentes));t+=duracion;epoca=(epoca+duracion)%PERIODO;
            int falta=restante.get(actual.pid())-duracion;restante.put(actual.pid(),falta);
            if(falta>0)ram.addLast(actual);else{terminados.add(actual);eventos.add("t="+t+" Termina: "+actual.nombre());}
        }
        return new Resultado(total,List.copyOf(inicial),List.copyOf(secundaria),List.copyOf(terminados),List.copyOf(eventos),List.copyOf(pasos));
    }
}
