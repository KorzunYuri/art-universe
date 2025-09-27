package yurykorzun.art.universe.music.data.raw.lastfm.collectable.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.service.EntityService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.dto.AlbumSearchParams;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.dto.LastfmAlbumResponseDto;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmAlbum;

import java.util.List;
import java.util.Optional;

public interface LastfmAlbumService extends EntityService<LastfmAlbum> {

    List<LastfmAlbum> findAllByUrls(List<String> urls);

    Optional<LastfmAlbum> findById(Long entityId);
    
    LastfmAlbumResponseDto findDtoById(Long id);
    
    Page<LastfmAlbumResponseDto> findAll(AlbumSearchParams params, Pageable pageable);
    
    LastfmAlbumResponseDto updateApprovalStatus(Long id, Integer approvalStatusCode);
    
    /**
     * Find albums for album.getInfo API call with the following priority:
     * 1. Albums with missing playCount and listenersCount
     * 2. Prioritize albums from popular artists (join by artist_id, sort by listenersCount)
     * 
     * @return List of albums to process with album.getInfo API call
     */
    List<LastfmAlbum> findAlbumsForGetInfo();
}
