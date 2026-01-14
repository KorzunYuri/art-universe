package yurykorzun.art.universe.music.data.raw.lastfm.domain.service.impl;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import yurykorzun.art.universe.common.CodedRegistry;
import yurykorzun.art.universe.data.raw.common.domain.entity.ApprovalStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.dto.ArtistSearchParams;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.dto.LastfmArtistResponseDto;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.repository.LastfmArtistRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.service.LastfmArtistService;

import java.util.List;

@Service
public class LastfmArtistServiceImpl implements LastfmArtistService {

    private final LastfmArtistRepository artistRepository;

    public LastfmArtistServiceImpl(LastfmArtistRepository artistRepository) {
        this.artistRepository = artistRepository;
    }

    @Override
    public LastfmArtistResponseDto findById(Long id) {
        return artistRepository.findById(id)
            .map(LastfmArtistResponseDto::from)
            .orElseThrow(() -> new EntityNotFoundException("Artist not found with id: " + id));
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

    private static List<ApprovalStatus> getApprovalStatusesFromCodes(ArtistSearchParams params) {
        return CodedRegistry.getByCodes(params.approvalStatuses(), ApprovalStatus.class);
    }
}
