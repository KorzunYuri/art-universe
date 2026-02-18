package yurykorzun.art.universe.music.data.raw.lastfm.domain.service.impl;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import yurykorzun.art.universe.common.CodedRegistry;
import yurykorzun.art.universe.data.raw.common.domain.entity.ApprovalStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.dto.AlbumSearchParams;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.dto.LastfmAlbumResponseDto;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.dto.LastfmAlbumTrackResponseDto;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.LastfmAlbum;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.repository.LastfmAlbumRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.repository.LastfmAlbumTrackRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.service.LastfmAlbumService;

import java.util.List;

@Service
public class LastfmAlbumServiceImpl implements LastfmAlbumService {

    private final LastfmAlbumRepository albumRepository;
    private final LastfmAlbumTrackRepository albumTrackRepository;

    public LastfmAlbumServiceImpl(LastfmAlbumRepository albumRepository,
                                  LastfmAlbumTrackRepository albumTrackRepository) {
        this.albumRepository = albumRepository;
        this.albumTrackRepository = albumTrackRepository;
    }

    @Override
    public LastfmAlbumResponseDto findById(Long id) {
        return albumRepository.findById(id)
            .map(LastfmAlbumResponseDto::from)
            .orElseThrow(() -> new EntityNotFoundException("Album not found with id: " + id));
    }

    @Override
    public Page<LastfmAlbumResponseDto> findAll(AlbumSearchParams params, Pageable pageable) {
        List<ApprovalStatus> approvalStatuses = getApprovalStatusesFromCodes(params);
        Page<LastfmAlbum> albumsPage = albumRepository.findAlbums(
            params.search(),
            params.minPlayCount(),
            params.minListenersCount(),
            params.artistId(),
            approvalStatuses,
            params.tagId(),
            pageable);
        return albumsPage.map(LastfmAlbumResponseDto::from);
    }

    @Override
    public List<LastfmAlbumTrackResponseDto> findAlbumTracks(Long albumId) {
        return albumTrackRepository.findByAlbumIdWithTracks(albumId)
            .stream()
            .map(LastfmAlbumTrackResponseDto::from)
            .toList();
    }

    private static List<ApprovalStatus> getApprovalStatusesFromCodes(AlbumSearchParams params) {
        return CodedRegistry.getByCodes(params.approvalStatuses(), ApprovalStatus.class);
    }
}
