package yurykorzun.art.universe.art.data.master.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import yurykorzun.art.universe.art.data.master.repository.PersonRepository;
import yurykorzun.art.universe.art.data.models.dto.PersonDto;
import yurykorzun.art.universe.art.data.models.dto.PersonSaveRequestDto;
import yurykorzun.art.universe.art.data.models.entity.Person;
import yurykorzun.art.universe.common.exception.CustomEntityNotFoundException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PersonServiceImplTest {

    @Mock
    private PersonRepository personRepository;

    @InjectMocks
    private PersonServiceImpl personService;

    // ─── findPersons ────────────────────────────────────────────────────────────

    @Test
    void findPersons_shouldReturnMappedPage() {
        Person person = Person.builder().name("John").build();
        Pageable pageable = PageRequest.of(0, 10);
        Page<Person> repoPage = new PageImpl<>(List.of(person), pageable, 1);

        when(personRepository.findPersons("John", pageable)).thenReturn(repoPage);

        Page<PersonDto> result = personService.findPersons("John", pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("John");
        verify(personRepository).findPersons("John", pageable);
    }

    @Test
    void findPersons_withNullSearch_shouldDelegateNullToRepository() {
        Pageable pageable = PageRequest.of(0, 10);
        when(personRepository.findPersons(null, pageable)).thenReturn(Page.empty(pageable));

        Page<PersonDto> result = personService.findPersons(null, pageable);

        assertThat(result).isEmpty();
        verify(personRepository).findPersons(null, pageable);
    }

    // ─── getPerson ───────────────────────────────────────────────────────────────

    @Test
    void getPerson_shouldReturnDto_whenFound() {
        Person person = Person.builder().name("Alice").build();
        when(personRepository.findById(1L)).thenReturn(Optional.of(person));

        PersonDto result = personService.getPerson(1L);

        assertThat(result.getName()).isEqualTo("Alice");
    }

    @Test
    void getPerson_shouldThrowCustomEntityNotFoundException_whenNotFound() {
        when(personRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> personService.getPerson(99L))
            .isInstanceOf(CustomEntityNotFoundException.class)
            .hasMessageContaining("Person")
            .hasMessageContaining("99");
    }

    // ─── savePerson ──────────────────────────────────────────────────────────────

    @Test
    void savePerson_shouldCreateNewPerson_whenIdIsNull() {
        PersonSaveRequestDto request = PersonSaveRequestDto.builder()
            .name("Bob")
            .build();

        Person savedPerson = Person.builder().name("Bob").build();
        when(personRepository.save(any(Person.class))).thenReturn(savedPerson);

        PersonDto result = personService.savePerson(request);

        assertThat(result.getName()).isEqualTo("Bob");

        ArgumentCaptor<Person> captor = ArgumentCaptor.forClass(Person.class);
        verify(personRepository).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("Bob");
    }

    @Test
    void savePerson_shouldUpdateExistingPerson_whenIdIsProvided() {
        PersonSaveRequestDto request = PersonSaveRequestDto.builder()
            .id(1L)
            .name("Updated Name")
            .build();

        Person existing = Person.builder().name("Old Name").build();
        Person saved = Person.builder().name("Updated Name").build();

        when(personRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(personRepository.save(existing)).thenReturn(saved);

        PersonDto result = personService.savePerson(request);

        assertThat(result.getName()).isEqualTo("Updated Name");
        assertThat(existing.getName()).isEqualTo("Updated Name");
        verify(personRepository, never()).save(argThat(p -> p != existing));
    }

    @Test
    void savePerson_shouldThrowCustomEntityNotFoundException_whenIdProvidedButNotFound() {
        PersonSaveRequestDto request = PersonSaveRequestDto.builder()
            .id(99L)
            .name("Ghost")
            .build();

        when(personRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> personService.savePerson(request))
            .isInstanceOf(CustomEntityNotFoundException.class)
            .hasMessageContaining("Person")
            .hasMessageContaining("99");

        verify(personRepository, never()).save(any());
    }

    // ─── deletePerson ────────────────────────────────────────────────────────────

    @Test
    void deletePerson_shouldReturnTrue_whenPersonExists() {
        when(personRepository.existsById(1L)).thenReturn(true);

        boolean result = personService.deletePerson(1L);

        assertThat(result).isTrue();
        verify(personRepository).deleteById(1L);
    }

    @Test
    void deletePerson_shouldReturnFalse_whenPersonDoesNotExist() {
        when(personRepository.existsById(99L)).thenReturn(false);

        boolean result = personService.deletePerson(99L);

        assertThat(result).isFalse();
        verify(personRepository, never()).deleteById(any());
    }
}
