package yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.impl;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import yurykorzun.art.universe.common.CodedRegistry;
import yurykorzun.art.universe.common.data.raw.entity.ApprovalStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.dto.LastfmAlbumResponseDto;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmAlbum;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.common.LastfmEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.event.EntityStatusChangedEvent;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.repository.LastfmAlbumRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.LastfmAlbumService;

@Service
public class LastfmAlbumServiceImpl implements LastfmAlbumService {

    private final LastfmAlbumRepository albumRepository;
    private final ApplicationEventPublisher eventPublisher;

    public LastfmAlbumServiceImpl(LastfmAlbumRepository albumRepository, ApplicationEventPublisher eventPublisher) {
        this.albumRepository = albumRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public LastfmAlbumResponseDto updateApprovalStatus(Long id, Integer approvalStatusCode) {
        ApprovalStatus approvalStatus = CodedRegistry.getByCode(approvalStatusCode, ApprovalStatus.class)
            .orElseThrow(() -> new IllegalArgumentException(String.format("ApprovalStatus with code %s not found", approvalStatusCode)));
        
        LastfmAlbum album = albumRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Album not found with id: " + id));
        
        album.updateApprovalStatus(approvalStatus);
        LastfmAlbum updated = albumRepository.save(album);
        
        eventPublisher.publishEvent(new EntityStatusChangedEvent(LastfmEntityType.ALBUM, id, approvalStatus));
        
        return LastfmAlbumResponseDto.from(updated);
    }

}
