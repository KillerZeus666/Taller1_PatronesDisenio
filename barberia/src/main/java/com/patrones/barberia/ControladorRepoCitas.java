package com.patrones.barberia; 

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class ControladorRepoCitas {
    public static void crearDirBarberia() {
        Path ruta = Paths.get("C:/Barberia_T1");
        try {
            Files.createDirectories(ruta);
        } catch (IOException e) {
            System.out.println("Error al crear directorio barberia.");
            e.printStackTrace();
        }
    }
    public static void crearCitaJSON(Cita cita_ingresada) {
        String ruta_dir_dia = "C:/Barberia_T1" + "/" + cita_ingresada.getFecha();
        try {
            Files.createDirectories(Paths.get(ruta_dir_dia));
            ObjectMapper mapper = new ObjectMapper();
            File archivo_json = new File(ruta_dir_dia, cita_ingresada.getCedula() + ".json");
            mapper.writeValue(archivo_json, cita_ingresada);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String buscarDirDia(String fecha_cita) {
        Path comienzo = Paths.get("C:/Barberia_T1");
        try (Stream<Path> stream = Files.list(comienzo)) {
            return stream
                    .filter(Files::isDirectory) // Solo directorios
                    .filter(dir -> dir.getFileName().toString().equals(fecha_cita))
                    .map(Path::toAbsolutePath) // Obtiene la ruta absoluta
                    .map(Path::toString) // Convierte a String
                    .findFirst() // Obtiene el primer resultado
                    .orElse(null); // Devuelve null si no se encuentra
        } catch (IOException e) {
            System.out.println("Error al crear el directorio correspondiente al dia");
            e.printStackTrace();
        }
        return null;
    }

    public static List<Cita> leerCitasPorDia(String ruta_carpeta) {
        List<Cita> citas = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        File dir_dia = new File(ruta_carpeta);

        if (dir_dia.exists() && dir_dia.isDirectory()) {
            File [] citas_json = dir_dia.listFiles((dir, name) -> name.endsWith(".json"));

            if (citas_json != null) {
                for (File archivo : citas_json) {
                    try {
                        Cita cita = mapper.readValue(archivo, Cita.class);
                        citas.add(cita);
                    } catch (IOException e) {
                        System.err.println("Error al leer el archivo " + archivo.getName() + ": " + e.getMessage());
                    }
                }
            }
        } else {
            System.err.println("La carpeta no existe o no es un directorio: " + ruta_carpeta);
        }
        return citas;
    }

    public static boolean esCitaRepetida(List<Cita> citas, Cita cita_ingresada) {
        LocalDate fecha_in = LocalDate.parse(cita_ingresada.getFecha(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        LocalTime hora_in = LocalTime.parse(cita_ingresada.getHora(), DateTimeFormatter.ofPattern("HH:mm"));
        LocalDateTime fecha_hora_in = LocalDateTime.of(fecha_in, hora_in);

        for (Cita cita : citas) {
            LocalDate fecha = LocalDate.parse(cita.getFecha(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            LocalTime hora = LocalTime.parse(cita.getHora(), DateTimeFormatter.ofPattern("HH:mm"));
            LocalDateTime fecha_hora = LocalDateTime.of(fecha, hora);

            long dif_min = Math.abs(ChronoUnit.MINUTES.between(fecha_hora,fecha_hora_in));
            if(dif_min < 60) {
                System.out.println("DIFF = " + dif_min);
                return true;
            }
        }
        return false;
    }

    public static boolean crearCita(Cita cita_ingresada) {
        crearDirBarberia();
        String dir_dia = buscarDirDia(cita_ingresada.getFecha());
        if (dir_dia == null) {
            crearCitaJSON(cita_ingresada);
            return true;
        } else {
            //Revisar si tiene la misma hora
            List<Cita> citas = new ArrayList<>(leerCitasPorDia(dir_dia));
            if (esCitaRepetida(citas, cita_ingresada)) {
                System.err.println("Error: Cita repetida");
            } else {
                crearCitaJSON(cita_ingresada);
                return true;
            }
        }
        return false;
    }

    public static List<String> obtenerCitasDisponibles() {
        File dir_principal = new File("C:/Barberia_T1");
        List<String> citas = new ArrayList<>();
        if (dir_principal.exists() && dir_principal.isDirectory()) {
            File [] dias_disponibles = dir_principal.listFiles();
            if (dias_disponibles != null) {
                for (File archivo : dias_disponibles) {
                    citas.add(archivo.getName());
                }
            }
        } else {
            System.err.println("Error al obtener el archivo principal");
        }
        return citas;
    }
}
