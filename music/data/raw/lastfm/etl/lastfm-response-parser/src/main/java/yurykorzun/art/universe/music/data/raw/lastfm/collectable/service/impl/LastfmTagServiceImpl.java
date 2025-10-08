package yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.impl;

import org.springframework.stereotype.Service;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.dto.EntityDto;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmTag;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.repository.LastfmTagRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.LastfmTagService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class LastfmTagServiceImpl implements LastfmTagService {

    private final LastfmTagRepository tagRepository;

    public LastfmTagServiceImpl(LastfmTagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    @Override
    public List<LastfmTag> saveAll(List<LastfmTag> tags) {
        return tagRepository.saveAll(tags);
    }

    @Override
    public <D extends EntityDto<LastfmTag>> Map<D, LastfmTag> mapDtoToExistingEntities(List<D> dtos) {
        Map<D, LastfmTag> result = new HashMap<>();

        Map<String, D> nameToDto = new HashMap<>(); // helper map
        List<String> names = dtos.stream()
            .peek(dto -> nameToDto.put(dto.getName(), dto))
            .peek(dto -> result.put(dto, null))
            .map(EntityDto::getName)
            .toList();

        List<LastfmTag> existingTags = tagRepository.findAllByNameIn(names);
        existingTags.forEach(tag -> result.put(nameToDto.get(tag.getName()), tag));

        return result;
    }

    @Override
    public Optional<LastfmTag> findById(Long id) {
        return tagRepository.findById(id);
    }

}
