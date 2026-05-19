package com.devops.saludo;

import org.springframework.stereotype.Service;

@Service
public class SaludoService {

    public String generarSaludo(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return "Hola, Mundo!";
        }
        return "Hola, " + nombre.trim() + "!";
    }

    public String getNombreServicio() {
        return "saludo-service";
    }
}
