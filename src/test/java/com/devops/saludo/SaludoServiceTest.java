package com.devops.saludo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SaludoService - Pruebas Unitarias")
class SaludoServiceTest {

    private SaludoService saludoService;

    @BeforeEach
    void setUp() {
        saludoService = new SaludoService();
    }

    @Test
    @DisplayName("Debe saludar con nombre válido")
    void debeGenerarSaludoConNombre() {
        String resultado = saludoService.generarSaludo("Juan");
        assertThat(resultado).isEqualTo("Hola, Juan!");
    }

    @Test
    @DisplayName("Debe saludar al Mundo cuando el nombre es null")
    void debeGenerarSaludoConNombreNull() {
        String resultado = saludoService.generarSaludo(null);
        assertThat(resultado).isEqualTo("Hola, Mundo!");
    }

    @Test
    @DisplayName("Debe saludar al Mundo cuando el nombre está vacío")
    void debeGenerarSaludoConNombreVacio() {
        String resultado = saludoService.generarSaludo("");
        assertThat(resultado).isEqualTo("Hola, Mundo!");
    }

    @Test
    @DisplayName("Debe saludar al Mundo cuando el nombre es solo espacios")
    void debeGenerarSaludoConNombreSoloEspacios() {
        String resultado = saludoService.generarSaludo("   ");
        assertThat(resultado).isEqualTo("Hola, Mundo!");
    }

    @Test
    @DisplayName("Debe recortar espacios en el nombre")
    void debeRecortarEspaciosEnNombre() {
        String resultado = saludoService.generarSaludo("  Maria  ");
        assertThat(resultado).isEqualTo("Hola, Maria!");
    }

    @Test
    @DisplayName("Debe retornar el nombre del servicio correcto")
    void debeRetornarNombreServicio() {
        assertThat(saludoService.getNombreServicio()).isEqualTo("saludo-service");
    }
}
