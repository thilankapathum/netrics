package dev.thilanka.beam.common.exception;

import dev.thilanka.beam.dto.AntennaDto;
import dev.thilanka.beam.entity.Antenna;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

    @RestController
    static class StubController {
        @PostMapping("/entity-invalid")
        void entityInvalid() {
            Antenna antenna = Antenna.builder().antennaIndex((byte) 0).build();
            var violations = Validation.buildDefaultValidatorFactory().getValidator().validate(antenna);
            throw new ConstraintViolationException(violations);
        }

        @PostMapping("/duplicate")
        void duplicate() {
            throw new DataIntegrityViolationException("ERROR: duplicate key value violates unique constraint \"uq_antennas_sector_antenna_index\"");
        }

        @PostMapping("/body")
        void body(@RequestBody AntennaDto dto) {
        }
    }

    private final MockMvc mvc = MockMvcBuilders.standaloneSetup(new StubController())
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();

    @Test
    void entityConstraintViolationIs400WithFieldErrors() throws Exception {
        mvc.perform(post("/entity-invalid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("antennaIndex"));
    }

    @Test
    void knownUniqueConstraintGetsSpecificMessage() throws Exception {
        mvc.perform(post("/duplicate"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("An antenna with this index already exists in the sector"));
    }

    @Test
    void byteOverflowInBodyIs400() throws Exception {
        mvc.perform(post("/body").contentType(MediaType.APPLICATION_JSON).content("{\"antennaIndex\": 300, \"azimuth\": 0, \"mechanicalTilt\": 0, \"antennaHeight\": 0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("BAD_REQUEST_BODY"));
    }
}
