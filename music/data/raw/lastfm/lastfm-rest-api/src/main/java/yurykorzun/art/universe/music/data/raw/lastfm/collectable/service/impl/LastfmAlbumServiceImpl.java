package yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.impl;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import yurykorzun.art.universe.common.CodedRegistry;
import yurykorzun.art.universe.common.data.raw.entity.ApprovalStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.dto.AlbumSearchParams;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.dto.LastfmAlbumResponseDto;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmAlbum;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.repository.LastfmAlbumRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.LastfmAlbumService;

import java.util.List;

@Service
public class LastfmAlbumServiceImpl implements LastfmAlbumService {

    private final LastfmAlbumRepository albumRepository;

    public LastfmAlbumServiceImpl(LastfmAlbumRepository albumRepository) {
        this.albumRepository = albumRepository;
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

    private static List<ApprovalStatus> getApprovalStatusesFromCodes(AlbumSearchParams params) {
        return CodedRegistry.getByCodes(params.approvalStatuses(), ApprovalStatus.class);
    }
}
