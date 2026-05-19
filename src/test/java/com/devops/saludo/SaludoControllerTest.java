package com.devops.saludo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SaludoController.class)
@DisplayName("SaludoController - Pruebas de Integración Web")
class SaludoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET / debe retornar Hola Mundo con status 200")
    void getHolaMundoDebeRetornar200() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Hola Mundo"));
    }

    @Test
    @DisplayName("GET /saludo sin parámetro debe saludar al Mundo")
    void getSaludoSinParametroDebeSaludarAlMundo() throws Exception {
        mockMvc.perform(get("/saludo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Hola, Mundo!"));
    }

    @Test
    @DisplayName("GET /saludo?nombre=Pedro debe saludar a Pedro")
    void getSaludoConNombreDebeSaludarConNombre() throws Exception {
        mockMvc.perform(get("/saludo").param("nombre", "Pedro"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Hola, Pedro!"));
    }

    @Test
    @DisplayName("GET /info debe retornar datos del servicio")
    void getInfoDebeRetornarDatosDelServicio() throws Exception {
        mockMvc.perform(get("/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.servicio").value("saludo-service"));
    }
}
