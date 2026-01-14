package yurykorzun.art.universe.music.data.raw.lastfm.domain.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import yurykorzun.art.universe.data.raw.common.domain.entity.ApprovalStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.common.LastfmEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.repository.LastfmAlbumRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.repository.LastfmTrackRepository;

@Component
@Slf4j
public class EntityStatusEventHandler {
    
    private final LastfmTrackRepository trackRepository;
    private final LastfmAlbumRepository albumRepository;
    
    public EntityStatusEventHandler(LastfmTrackRepository trackRepository, LastfmAlbumRepository albumRepository) {
        this.trackRepository = trackRepository;
        this.albumRepository = albumRepository;
    }
    
    @Async
    @EventListener
    @Transactional
    public void handleEntityStatusChanged(EntityStatusChangedEvent event) {
        if (event.newStatus() != ApprovalStatus.DECLINED && event.newStatus() != ApprovalStatus.IGNORED) {
            return;
        }
        
        if (event.entityType() == LastfmEntityType.ARTIST) {
            int tracksUpdated = trackRepository.updateTrackStatusByArtistId(event.entityId(), event.newStatus());
            int albumsUpdated = albumRepository.updateAlbumStatusByArtistId(event.entityId(), event.newStatus());
            log.debug("Updated {} tracks and {} albums for artist {}", tracksUpdated, albumsUpdated, event.entityId());
        } else if (event.entityType() == LastfmEntityType.ALBUM) {
            int tracksUpdated = trackRepository.updateTrackStatusByAlbumId(event.entityId(), event.newStatus());
            log.debug("Updated {} tracks for album {}", tracksUpdated, event.entityId());
        }
    }
}
