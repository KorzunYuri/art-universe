package yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.service.impl;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import yurykorzun.art.universe.common.CodedRegistry;
import yurykorzun.art.universe.common.data.raw.entity.ApprovalStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.dto.ArtistSearchParams;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.dto.LastfmArtistResponseDto;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.repository.LastfmArtistRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.service.LastfmArtistService;

import java.util.List;
import java.util.Optional;

@Service
public class LastfmArtistServiceImpl implements LastfmArtistService {

    private final LastfmArtistRepository artistRepository;

    public LastfmArtistServiceImpl(LastfmArtistRepository artistRepository) {
        this.artistRepository = artistRepository;
    }

    @Override
    public LastfmArtist saveArtist(LastfmArtist artist) {
        return artistRepository.save(artist);
    }

    @Override
    public List<LastfmArtist> saveArtists(List<LastfmArtist> artists) {
        return artistRepository.saveAll(artists);
    }

    @Override
    public Optional<LastfmArtist> findById(long id) {
        return artistRepository.findById(id);
    }

    @Override
    public Optional<LastfmArtist> findByName(String name) {
        return artistRepository.findByName(name);
    }

    @Override
    public Page<LastfmArtistResponseDto> findArtists(ArtistSearchParams params, Pageable pageable) {
        List<ApprovalStatus> approvalStatuses = getApprovalStatusesFromCodes(params);
        Page<LastfmArtist> artistsPage = artistRepository.findArtists(
            params.search(),
            params.minPlayCount(),
            params.minListenersCount(),
            approvalStatuses,
            pageable);
        return artistsPage.map(LastfmArtistResponseDto::from);
    }

    private static List<ApprovalStatus> getApprovalStatusesFromCodes(ArtistSearchParams params) {
        return CodedRegistry.getByCodes(params.approvalStatuses(), ApprovalStatus.class);
    }

    @Override
    public List<LastfmArtist> findAllByNames(List<String> names) {
        return artistRepository.findAllByNameIn(names);
    }

    @Override
    public List<LastfmArtist> findAllToGetInfoFor() {
        return artistRepository.findAllToGetInfoFor();
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
}
