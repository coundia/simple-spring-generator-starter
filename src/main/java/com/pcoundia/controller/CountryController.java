package com.pcoundia.controller;

import com.pcoundia.dto.ApiCollection;
import com.pcoundia.helper.app.ResponseHandler;
import com.pcoundia.model.entity.Country;
import com.pcoundia.repository.CountryRepository;
import com.pcoundia.service.ICrudService;
import com.pcoundia.view.CountryView;
import com.pcoundia.service.CountryService;

import lombok.RequiredArgsConstructor;
import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin
@RequestMapping("/api/countries")
@SecurityRequirement(name = "jwtToken")
@RequiredArgsConstructor
public class CountryController {

     public final CountryService service;

    @JsonView(CountryView.CyRead.class)
    @GetMapping
    //@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'applic_country_list')")
    public ResponseEntity<ApiCollection<List<Country>>> index(@RequestParam(defaultValue = "false") boolean pageable,
                                                                @RequestParam(defaultValue = "1") int page,
                                                                @RequestParam(defaultValue = "10") int size,
                                                                @RequestParam(defaultValue = "false") boolean isDeleted,
                                                                @RequestParam(value = "search", required = false) String search) {
        Map<String, Object> result = service.findAll(pageable, page, size, isDeleted, search);
        if (result.containsKey("totalItems"))
            return (ResponseEntity<ApiCollection<List<Country>>>) ResponseHandler.generateResponse("Liste recuperée", HttpStatus.OK, (Object) result.get("data"), (long) result.get("totalItems"), (int) result.get("totalPages"));
        else
            return (ResponseEntity<ApiCollection<List<Country>>>) ResponseHandler.generateResponse("Liste recuperée", HttpStatus.OK, (Object) result.get("data"));
    }

    @JsonView(CountryView.CyReadDetail.class)
    @PostMapping
    //@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'applic_country_add')")
    public ResponseEntity<Country> store(@Valid @RequestBody @JsonView(CountryView.CyWrite.class) Country element) {
        return ResponseEntity.ok(service.create(element));
    }

    @JsonView(CountryView.CyReadDetail.class)
    @GetMapping(value = "/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'applic_country_list')")
    public ResponseEntity<Country> show(@PathVariable() long id) {
        return ResponseEntity.ok(service.findOne(id));
    }

    @JsonView(CountryView.CyReadDetail.class)
    @PatchMapping(value = "/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'applic_country_update')")
    public ResponseEntity<Country> update(
            @PathVariable() long id,
            @Valid
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Country to Update.",
                    required=true,
                    content=@Content(schema = @Schema(implementation = Country.class)))
            @RequestBody
            HashMap<String, Object> patch) throws IOException {
        return ResponseEntity.ok(service.patch(id, patch));
    }

    @DeleteMapping(value = "/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'applic_country_delete')")
    public ResponseEntity<Country> delete(@PathVariable long id) throws Exception {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

}
