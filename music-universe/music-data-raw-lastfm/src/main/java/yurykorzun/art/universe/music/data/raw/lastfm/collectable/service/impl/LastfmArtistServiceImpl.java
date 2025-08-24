package yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.impl;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import yurykorzun.art.universe.common.CodedRegistry;
import yurykorzun.art.universe.common.data.raw.entity.ApprovalStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.dto.EntityDto;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.dto.ArtistSearchParams;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.dto.LastfmArtistResponseDto;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.repository.LastfmArtistRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.LastfmArtistService;

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
    public LastfmArtist save(LastfmArtist artist) {
        return artistRepository.save(artist);
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
    public LastfmArtistResponseDto findDtoById(Long id) {
        return artistRepository.findById(id)
            .map(LastfmArtistResponseDto::from)
            .orElseThrow(() -> new EntityNotFoundException("Artist not found with id: " + id));
    }

    @Override
    public Optional<LastfmArtist> findByName(String name) {
        return artistRepository.findByName(name);
    }

    @Override
    public Page<LastfmArtistResponseDto> findAll(ArtistSearchParams params, Pageable pageable) {
        List<ApprovalStatus> approvalStatuses = getApprovalStatusesFromCodes(params);
        Page<LastfmArtist> artistsPage = artistRepository.findArtists(
            params.search(),
            params.minPlayCount(),
            params.minListenersCount(),
            approvalStatuses,
            params.tagId(),
            pageable);
        return artistsPage.map(LastfmArtistResponseDto::from);
    }

    @Override
    public List<LastfmArtist> findAllByNames(List<String> names) {
        return artistRepository.findAllByNameIn(names);
    }

    @Override
    public List<LastfmArtist> findArtistsForGetInfo() {
        return artistRepository.findAllToGetInfoFor();
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

    @Override
    public LastfmArtistResponseDto updateApprovalStatus(Long id, Integer approvalStatusCode) {
        ApprovalStatus approvalStatus = CodedRegistry.getByCode(approvalStatusCode, ApprovalStatus.class)
            .orElseThrow(() -> new IllegalArgumentException(String.format("ApprovalStatus with code %s not found", approvalStatusCode)));
        LastfmArtist artist = artistRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Artist not found"));

        artist.updateApprovalStatus(approvalStatus);
        LastfmArtist updated = artistRepository.save(artist);
        return LastfmArtistResponseDto.from(updated);
    }

    private static List<ApprovalStatus> getApprovalStatusesFromCodes(ArtistSearchParams params) {
        return CodedRegistry.getByCodes(params.approvalStatuses(), ApprovalStatus.class);
    }
}
