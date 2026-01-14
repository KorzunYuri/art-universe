package yurykorzun.art.universe.music.data.raw.lastfm.domain.service.impl;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import yurykorzun.art.universe.common.CodedRegistry;
import yurykorzun.art.universe.data.raw.common.domain.entity.ApprovalStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.dto.LastfmArtistResponseDto;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.common.LastfmEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.event.EntityStatusChangedEvent;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.repository.LastfmArtistRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.service.LastfmArtistService;

@Service
public class LastfmArtistServiceImpl implements LastfmArtistService {

    private final LastfmArtistRepository artistRepository;
    private final ApplicationEventPublisher eventPublisher;

    public LastfmArtistServiceImpl(LastfmArtistRepository artistRepository, ApplicationEventPublisher eventPublisher) {
        this.artistRepository = artistRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public LastfmArtistResponseDto updateApprovalStatus(Long id, Integer approvalStatusCode) {
        ApprovalStatus approvalStatus = CodedRegistry.getByCode(approvalStatusCode, ApprovalStatus.class)
            .orElseThrow(() -> new IllegalArgumentException(String.format("ApprovalStatus with code %s not found", approvalStatusCode)));
        LastfmArtist artist = artistRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Artist not found"));

        artist.updateApprovalStatus(approvalStatus);
        LastfmArtist updated = artistRepository.save(artist);
        
        eventPublisher.publishEvent(new EntityStatusChangedEvent(LastfmEntityType.ARTIST, id, approvalStatus));
        
        return LastfmArtistResponseDto.from(updated);
    }
}
