package yurykorzun.art.universe.music.data.raw.lastfm.collectable.album.service.impl;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import yurykorzun.art.universe.common.CodedRegistry;
import yurykorzun.art.universe.common.data.raw.entity.ApprovalStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.album.dto.AlbumSearchParams;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.album.dto.LastfmAlbumResponseDto;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.album.entity.LastfmAlbum;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.album.repository.LastfmAlbumRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.album.service.LastfmAlbumService;

import java.util.List;
import java.util.Optional;

@Service
public class LastfmAlbumServiceImpl implements LastfmAlbumService {

    private final LastfmAlbumRepository albumRepository;

    public LastfmAlbumServiceImpl(LastfmAlbumRepository albumRepository) {
        this.albumRepository = albumRepository;
    }

    @Override
    public List<LastfmAlbum> findAllByUrls(List<String> urls) {
        return albumRepository.findAllByUrlIn(urls);
    }

    @Override
    public Optional<LastfmAlbum> findById(Long entityId) {
        return albumRepository.findById(entityId);
    }

    @Override
    public LastfmAlbumResponseDto findDtoById(Long id) {
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
    public LastfmAlbumResponseDto updateApprovalStatus(Long id, Integer approvalStatusCode) {
        ApprovalStatus approvalStatus = CodedRegistry.getByCode(approvalStatusCode, ApprovalStatus.class)
            .orElseThrow(() -> new IllegalArgumentException(String.format("ApprovalStatus with code %s not found", approvalStatusCode)));
        
        LastfmAlbum album = albumRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Album not found with id: " + id));
        
        album.updateApprovalStatus(approvalStatus);
        LastfmAlbum updated = albumRepository.save(album);
        return LastfmAlbumResponseDto.from(updated);
    }

    @Override
    public List<LastfmAlbum> findEntitiesByUniqueKeys(List<String> uniqueKeys) {
        return findAllByUrls(uniqueKeys);
    }

    @Override
    public List<LastfmAlbum> saveAll(List<LastfmAlbum> lastfmAlbums) {
        return albumRepository.saveAll(lastfmAlbums);
    }
    
    @Override
    public List<LastfmAlbum> findAlbumsForGetInfo() {
        return albumRepository.findAlbumsForGetInfo();
    }

    private static List<ApprovalStatus> getApprovalStatusesFromCodes(AlbumSearchParams params) {
        return CodedRegistry.getByCodes(params.approvalStatuses(), ApprovalStatus.class);
    }
}
