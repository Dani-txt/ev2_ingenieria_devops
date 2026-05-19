package com.devops.saludo;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SaludoController {
    /*
        Endpoint principal. Metodo GET que responde Hola Mundo y saludo
     */
    @GetMapping("/")
    public ResponseEntity<Map<String, String>> holaMundo() {
        return ResponseEntity.ok(Map.of(
                "mensaje", "Hola Mundo. Bienvenido a la evaluación 2",
                "descripcion", "Microservicio de saludos para evaluacion DevOps"
        ));
    }

    /**
     * BONUS
        Endpoint personalizado con nombre. Metodo GET /saludo?nombre=Juan
     */
    @GetMapping("/saludo")
    public ResponseEntity<Map<String, String>> saludar(
            @RequestParam(defaultValue = "Mundo") String nombre) {
        return ResponseEntity.ok(Map.of(
                "mensaje", "Hola, " + nombre + "!"
        ));
    }

}
