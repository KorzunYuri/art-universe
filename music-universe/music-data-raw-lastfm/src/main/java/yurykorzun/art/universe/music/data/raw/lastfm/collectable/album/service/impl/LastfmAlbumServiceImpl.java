package yurykorzun.art.universe.music.data.raw.lastfm.collectable.album.service.impl;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import yurykorzun.art.universe.common.CodedRegistry;
import yurykorzun.art.universe.common.data.raw.entity.ApprovalStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.album.common.dto.AlbumDto;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.dto.EntityDto;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.album.dto.AlbumSearchParams;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.album.dto.LastfmAlbumResponseDto;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.album.entity.LastfmAlbum;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.album.repository.LastfmAlbumRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.album.service.LastfmAlbumService;

import java.util.*;
import java.util.stream.Collectors;

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
    public List<LastfmAlbum> saveAll(List<LastfmAlbum> lastfmAlbums) {
        return albumRepository.saveAll(lastfmAlbums);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <D extends EntityDto<LastfmAlbum>> Map<D, LastfmAlbum> mapDtoToExistingEntities(List<D> dtos) {
        Map<AlbumDto, LastfmAlbum> result = new HashMap<>();
        
        // Step 1: Try to find albums by name + artist (individual queries)
        List<AlbumDto> unmatchedDtos = new ArrayList<>();
        for (D dto : dtos) {
            AlbumDto albumDto = (AlbumDto) dto;
            
            if (albumDto.getArtistName() != null) {
                List<LastfmAlbum> albums = albumRepository.findByNameAndArtistName(albumDto.getName(), albumDto.getArtistName());
                if (albums.size() == 1) {
                    result.put(albumDto, albums.get(0));
                } else if (albums.size() > 1) {
                    throw new IllegalStateException(String.format("Multiple albums found for album %s - %s with url %s and mbid %s",
                        albumDto.getArtistName(), albumDto.getName(), albumDto.getUrl(), albumDto.getMbid()));
                } else {
                    unmatchedDtos.add(albumDto);
                }
            } else {
                unmatchedDtos.add(albumDto);
            }
        }
        
        // Step 2: Batch search by URLs for remaining DTOs
        if (!unmatchedDtos.isEmpty()) {
            List<String> urls = unmatchedDtos.stream()
                .filter(dto -> dto.getUrl() != null)
                .map(AlbumDto::getUrl)
                .distinct()
                .collect(Collectors.toList());
                
            if (!urls.isEmpty()) {
                Map<String, LastfmAlbum> albumsByUrl = albumRepository.findAllByUrlIn(urls).stream()
                    .collect(Collectors.toMap(
                        LastfmAlbum::getUrl,
                        album -> album,
                        (existing, replacement) -> {
                            throw new IllegalStateException("Multiple albums found for URL: " + existing.getUrl());
                        }
                    ));
                
                // Match DTOs with found albums by URL
                Iterator<AlbumDto> iterator = unmatchedDtos.iterator();
                while (iterator.hasNext()) {
                    AlbumDto dto = iterator.next();
                    if (dto.getUrl() != null) {
                        LastfmAlbum album = albumsByUrl.get(dto.getUrl());
                        if (album != null) {
                            result.put(dto, album);
                            iterator.remove();
                        }
                    }
                }
            }
        }
        
        // Step 3: Batch search by MBIDs for remaining DTOs
        if (!unmatchedDtos.isEmpty()) {
            List<String> mbids = unmatchedDtos.stream()
                .filter(dto -> dto.getMbid() != null && !dto.getMbid().isEmpty())
                .map(AlbumDto::getMbid)
                .distinct()
                .collect(Collectors.toList());
                
            if (!mbids.isEmpty()) {
                Map<String, LastfmAlbum> albumsByMbid = albumRepository.findAllByMbidIn(mbids).stream()
                    .collect(Collectors.toMap(
                        LastfmAlbum::getMbid,
                        album -> album,
                        (existing, replacement) -> {
                            throw new IllegalStateException("Multiple albums found for MBID: " + existing.getMbid());
                        }
                    ));
                
                // Match DTOs with found albums by MBID
                for (AlbumDto dto : unmatchedDtos) {
                    if (dto.getMbid() != null && !dto.getMbid().isEmpty()) {
                        LastfmAlbum album = albumsByMbid.get(dto.getMbid());
                        if (album != null) {
                            result.put(dto, album);
                        }
                    }
                }
            }
        }
        
        // Add null entries for unmatched DTOs
        for (D dto : dtos) {
            AlbumDto albumDto = (AlbumDto) dto;
            if (!result.containsKey(albumDto)) {
                result.put(albumDto, null);
            }
        }

        return (Map<D, LastfmAlbum>) result;
    }

    @Override
    public List<LastfmAlbum> findAlbumsForGetInfo() {
        return albumRepository.findAlbumsForGetInfo();
    }

    private static List<ApprovalStatus> getApprovalStatusesFromCodes(AlbumSearchParams params) {
        return CodedRegistry.getByCodes(params.approvalStatuses(), ApprovalStatus.class);
    }
}
