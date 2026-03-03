package yurykorzun.art.universe.art.data.master.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yurykorzun.art.universe.art.data.models.dto.PersonDto;
import yurykorzun.art.universe.art.data.models.dto.PersonSaveRequestDto;
import yurykorzun.art.universe.art.data.models.entity.Person;
import yurykorzun.art.universe.common.exception.CustomEntityNotFoundException;
import yurykorzun.art.universe.art.data.master.repository.PersonRepository;

@Service
public class PersonServiceImpl implements PersonService {

    private final PersonRepository personRepository;

    public PersonServiceImpl(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PersonDto> findPersons(String search, Pageable pageable) {
        return personRepository.findPersons(search, pageable)
                .map(this::mapToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public PersonDto getPerson(Long id) {
        Person person = personRepository.findById(id)
                .orElseThrow(() -> new CustomEntityNotFoundException("Person", id));
        return mapToDto(person);
    }

    @Override
    @Transactional
    public PersonDto savePerson(PersonSaveRequestDto request) {
        Person person;
        if (request.getId() != null) {
            person = personRepository.findById(request.getId())
                    .orElseThrow(() -> new CustomEntityNotFoundException("Person", request.getId()));
            person.setName(request.getName());
        } else {
            person = Person.builder()
                    .name(request.getName())
                    .build();
        }

        Person savedPerson = personRepository.save(person);
        return mapToDto(savedPerson);
    }

    @Override
    @Transactional
    public boolean deletePerson(Long id) {
        if (personRepository.existsById(id)) {
            personRepository.deleteById(id);
            return true;
        }
        return false;
    }

    private PersonDto mapToDto(Person person) {
        return PersonDto.builder()
                .id(person.getId())
                .name(person.getName())
                .build();
    }
}
