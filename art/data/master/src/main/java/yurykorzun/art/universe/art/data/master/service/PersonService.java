package yurykorzun.art.universe.art.data.master.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import yurykorzun.art.universe.art.data.models.dto.PersonDto;
import yurykorzun.art.universe.art.data.models.dto.PersonSaveRequestDto;

public interface PersonService {

    Page<PersonDto> findPersons(String search, Pageable pageable);

    PersonDto getPerson(Long id);

    PersonDto savePerson(PersonSaveRequestDto request);

    boolean deletePerson(Long id);
}
