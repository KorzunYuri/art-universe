package yurykorzun.art.universe.music.data.raw.lastfm.domain.service.impl;

import org.springframework.stereotype.Service;
import yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.EntityDto;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.repository.LastfmArtistRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.service.LastfmArtistService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class LastfmArtistServiceImpl implements LastfmArtistService {

    private final LastfmArtistRepository artistRepository;

    public LastfmArtistServiceImpl(LastfmArtistRepository artistRepository) {
        this.artistRepository = artistRepository;
    }

    @Override
    public List<LastfmArtist> saveAll(List<LastfmArtist> artists) {
        return artistRepository.saveAll(artists);
    }

    @Override
    public Optional<LastfmArtist> findById(Long id) {
        return artistRepository.findById(id);
    }

    @Override
    public <D extends EntityDto<LastfmArtist>> Map<D, LastfmArtist> mapDtoToExistingEntities(List<D> dtos) {
        Map<D, LastfmArtist> result = new HashMap<>();

        Map<String, D> nameToDto = new HashMap<>(); // helper map
        List<String> names = dtos.stream()
            .peek(dto -> nameToDto.put(dto.getName(), dto))
            .peek(dto -> result.put(dto, null))
            .map(EntityDto::getName)
            .toList();

        List<LastfmArtist> existingArtists = artistRepository.findAllByNameIn(names);
        existingArtists.forEach(artist -> result.put(nameToDto.get(artist.getName()), artist));

        return result;

    }
}
