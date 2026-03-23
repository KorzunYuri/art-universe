package yurykorzun.art.universe.art.data.master.controller;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import yurykorzun.art.universe.art.data.models.dto.PersonDto;
import yurykorzun.art.universe.art.data.models.dto.PersonSaveRequestDto;
import yurykorzun.art.universe.common.exception.CustomEntityNotFoundException;
import yurykorzun.art.universe.art.data.master.service.PersonService;

@RestController
@RequestMapping("/api/v1/persons")
public class PersonController {

    private final PersonService personService;

    public PersonController(PersonService personService) {
        this.personService = personService;
    }

    @GetMapping
    public Page<PersonDto> findPersons(
        @RequestParam(required = false) String search,
        Pageable pageable
    ) {
        return personService.findPersons(search, pageable);
    }

    @GetMapping("/{id}")
    public PersonDto getPerson(@PathVariable Long id) {
        return personService.getPerson(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('MASTER_CURATOR')")
    public PersonDto savePerson(@Valid @RequestBody PersonSaveRequestDto request) {
        return personService.savePerson(request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MASTER_CURATOR')")
    public boolean deletePerson(@PathVariable Long id) {
        boolean deleted = personService.deletePerson(id);
        if (!deleted) {
            throw new CustomEntityNotFoundException("Person", id);
        }
        return true;
    }
}
