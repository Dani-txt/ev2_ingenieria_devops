package com.devops.saludo;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "Saludo", description = "Endpoints del microservicio de saludos")
public class SaludoController {

    @Operation(summary = "Bienvenida", description = "Retorna mensaje de bienvenida con metadata del servicio")
    @GetMapping("/")
    public ResponseEntity<Map<String, String>> holaMundo() {
        Map<String, String> response = new LinkedHashMap<>();
        response.put("mensaje", "Hola Mundo. Bienvenido a la evaluación 2");
        response.put("descripcion", "Microservicio de saludos para evaluacion DevOps");
        response.put("timestamp", Instant.now().toString());
        response.put("version", "1.0.0");
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Saludo personalizado", description = "Retorna saludo con el nombre indicado")
    @GetMapping("/saludo")
    public ResponseEntity<Map<String, String>> saludar(
            @Parameter(description = "Nombre de la persona a saludar", example = "Juan")
            @RequestParam(defaultValue = "Mundo") String nombre) {
        Map<String, String> response = new LinkedHashMap<>();
        response.put("mensaje", "Hola, " + nombre + "!");
        response.put("timestamp", Instant.now().toString());
        return ResponseEntity.ok(response);
    }
}