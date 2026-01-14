package yurykorzun.art.universe.music.data.raw.lastfm.domain.service.impl;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import yurykorzun.art.universe.common.CodedRegistry;
import yurykorzun.art.universe.common.data.raw.domain.entity.ApprovalStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.dto.LastfmAlbumResponseDto;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.LastfmAlbum;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.common.LastfmEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.event.EntityStatusChangedEvent;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.repository.LastfmAlbumRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.service.LastfmAlbumService;

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
