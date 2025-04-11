package yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.service.impl;

import org.springframework.stereotype.Service;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.entity.LastfmTag;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.repository.LastfmTagRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.service.LastfmTagService;

import java.util.List;

@Service
public class LastfmTagServiceImpl implements LastfmTagService {

    private final LastfmTagRepository tagRepository;

    public LastfmTagServiceImpl(LastfmTagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    @Override
    public List<LastfmTag> findAllByNameIn(List<String> tagNames) {
        return tagRepository.findAllByNameIn(tagNames);
    }

    @Override
    public List<LastfmTag> saveTags(List<LastfmTag> tags) {
        return tagRepository.saveAll(tags);
    }
}
