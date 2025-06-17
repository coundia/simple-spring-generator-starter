package com.pcoundia.controller;

import com.pcoundia.model.entity.Country;
import com.pcoundia.repository.CountryRepository;
import com.pcoundia.service.ICrudService;
import com.pcoundia.view.CountryView;
import com.pcoundia.AbstractControllerTest;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CountryController.class)
class CountryControllerTest extends AbstractControllerTest<ICrudService<Country, CountryRepository>, CountryRepository> {

    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN", "applic_country_list"})
    void it_should_return_countries_list() throws Exception {
        Country e = new Country();
        e.setId(1L);
    String codeValue = UUID.randomUUID().toString();
        e.setCode(codeValue);
    String nameValue = UUID.randomUUID().toString();
        e.setName(nameValue);
    String alpha2Value = UUID.randomUUID().toString();
        e.setAlpha2(alpha2Value);

        Map<String, Object> result = new HashMap<>();
        result.put("data", List.of(e));
        result.put("totalItems", 1L);
        result.put("totalPages", 1);

        Mockito.when(service.findAll(false, 1, 10, false, null)).thenReturn(result);

        mockMvc.perform(get("/api/countries"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].code").value(codeValue))
            .andExpect(jsonPath("$.data[0].name").value(nameValue))
            .andExpect(jsonPath("$.data[0].alpha2").value(alpha2Value))
            .andReturn();
    }

    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN", "applic_country_add"})
    void it_should_create_country() throws Exception {
        Country e = new Country();
        e.setId(2L);
    String codeValue = UUID.randomUUID().toString();
        e.setCode(codeValue);
    String nameValue = UUID.randomUUID().toString();
        e.setName(nameValue);
    String alpha2Value = UUID.randomUUID().toString();
        e.setAlpha2(alpha2Value);

        Mockito.when(service.create(any(Country.class))).thenReturn(e);

        mockMvc.perform(post("/api/countries").contentType(MediaType.APPLICATION_JSON).with(csrf())
                .content(objectMapper.writerWithView(CountryView.CyWrite.class).writeValueAsString(e)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(codeValue))
            .andExpect(jsonPath("$.name").value(nameValue))
            .andExpect(jsonPath("$.alpha2").value(alpha2Value))
;
    }

    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN", "applic_country_list"})
    void it_should_get_country_by_id() throws Exception {
        Country e = new Country();
        e.setId(1L);
    String codeValue = UUID.randomUUID().toString();
        e.setCode(codeValue);
    String nameValue = UUID.randomUUID().toString();
        e.setName(nameValue);
    String alpha2Value = UUID.randomUUID().toString();
        e.setAlpha2(alpha2Value);

        Mockito.when(service.findOne(1L)).thenReturn(e);

        mockMvc.perform(get("/api/countries/1")).andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(codeValue))
            .andExpect(jsonPath("$.name").value(nameValue))
            .andExpect(jsonPath("$.alpha2").value(alpha2Value))
;
    }

    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN", "applic_country_update"})
    void it_should_patch_country() throws Exception {
        Country e = new Country();
        e.setId(1L);
    String codeValue = UUID.randomUUID().toString();
        e.setCode(codeValue);
    String nameValue = UUID.randomUUID().toString();
        e.setName(nameValue);
    String alpha2Value = UUID.randomUUID().toString();
        e.setAlpha2(alpha2Value);

        Map<String, Object> patch = new HashMap<>();
        patch.put("code", codeValue);
        patch.put("name", nameValue);
        patch.put("alpha2", alpha2Value);

        Mockito.when(service.patch(eq(1L), anyMap())).thenReturn(e);

        mockMvc.perform(patch("/api/countries/1").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(patch))).andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(codeValue))
            .andExpect(jsonPath("$.name").value(nameValue))
            .andExpect(jsonPath("$.alpha2").value(alpha2Value))
;
    }

    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN", "applic_country_delete"})
    void it_should_delete_country() throws Exception {
        mockMvc.perform(delete("/api/countries/1").with(csrf()))
            .andExpect(status().isNoContent());
    }
}
