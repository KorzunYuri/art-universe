package yurykorzun.art.universe.art.data.master.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import yurykorzun.art.universe.art.data.master.service.PersonService;
import yurykorzun.art.universe.art.data.models.dto.PersonDto;
import yurykorzun.art.universe.art.data.models.dto.PersonSaveRequestDto;
import yurykorzun.art.universe.common.exception.CustomEntityNotFoundException;
import yurykorzun.art.universe.common.test.archetypes.BaseMvcTest;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PersonController.class)
class PersonControllerMvcTest extends BaseMvcTest {

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PersonService personService;

    private PersonDto personDto;

    @BeforeEach
    void setUp() {
        personDto = PersonDto.builder()
            .id(1L)
            .name("Alice")
            .build();
    }

    // ─── GET /api/v1/persons ──────────────────────────────────────────────────────

    @Test
    void GET_persons_shouldReturnPage() throws Exception {
        Page<PersonDto> page = new PageImpl<>(List.of(personDto), PageRequest.of(0, 20), 1);
        when(personService.findPersons(isNull(), any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/persons")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.content[0].id").value(1))
            .andExpect(jsonPath("$.content[0].name").value("Alice"))
            .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void GET_persons_withSearch_shouldPassSearchParamToService() throws Exception {
        Page<PersonDto> page = new PageImpl<>(List.of(personDto), PageRequest.of(0, 20), 1);
        when(personService.findPersons(eq("Alice"), any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/persons")
                .param("search", "Alice")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].name").value("Alice"));
    }

    // ─── GET /api/v1/persons/{id} ─────────────────────────────────────────────────

    @Test
    void GET_personById_shouldReturnPerson_whenFound() throws Exception {
        when(personService.getPerson(1L)).thenReturn(personDto);

        mockMvc.perform(get("/api/v1/persons/1")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("Alice"));
    }

    @Test
    void GET_personById_shouldReturn404_whenNotFound() throws Exception {
        when(personService.getPerson(99L))
            .thenThrow(new CustomEntityNotFoundException("Person", 99L));

        mockMvc.perform(get("/api/v1/persons/99")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    // ─── POST /api/v1/persons ─────────────────────────────────────────────────────

    @Test
    void POST_persons_shouldCreateAndReturnPerson() throws Exception {
        PersonSaveRequestDto request = PersonSaveRequestDto.builder()
            .name("Alice")
            .build();

        when(personService.savePerson(any())).thenReturn(personDto);

        mockMvc.perform(post("/api/v1/persons")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("Alice"));
    }

    @Test
    void POST_persons_shouldReturn400_whenNameIsBlank() throws Exception {
        PersonSaveRequestDto request = PersonSaveRequestDto.builder()
            .name("")
            .build();

        mockMvc.perform(post("/api/v1/persons")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    // ─── DELETE /api/v1/persons/{id} ──────────────────────────────────────────────

    @Test
    void DELETE_personById_shouldReturnTrue_whenDeleted() throws Exception {
        when(personService.deletePerson(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/v1/persons/1")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string("true"));
    }

    @Test
    void DELETE_personById_shouldReturn404_whenNotFound() throws Exception {
        when(personService.deletePerson(99L)).thenReturn(false);

        mockMvc.perform(delete("/api/v1/persons/99")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }
}
