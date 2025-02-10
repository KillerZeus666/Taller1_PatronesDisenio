package com.patrones.barberia; 


import java.time.LocalTime;
import java.util.Comparator;

public class ComparadorCita implements Comparator<Cita> {
    @Override
    public int compare(Cita c1, Cita c2) {
        LocalTime t1 = LocalTime.parse(c1.getHora());
        LocalTime t2 = LocalTime.parse(c2.getHora());
        return t1.compareTo(t2);
    }
}
