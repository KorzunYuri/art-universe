package yurykorzun.art.universe.music.data.raw.lastfm.domain.service.impl;

import org.springframework.stereotype.Service;
import yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.album.AlbumDto;
import yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.EntityDto;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.LastfmAlbum;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.repository.LastfmAlbumRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.service.LastfmAlbumService;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class LastfmAlbumServiceImpl implements LastfmAlbumService {

    private final LastfmAlbumRepository albumRepository;

    public LastfmAlbumServiceImpl(LastfmAlbumRepository albumRepository) {
        this.albumRepository = albumRepository;
    }

    @Override
    public Optional<LastfmAlbum> findById(Long entityId) {
        return albumRepository.findById(entityId);
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
                    result.put(albumDto, albums.getFirst());
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
                .map(AlbumDto::getUrl)
                .filter(Objects::nonNull)
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
        
        // Add null entries for unmatched DTOs
        for (D dto : dtos) {
            AlbumDto albumDto = (AlbumDto) dto;
            if (!result.containsKey(albumDto)) {
                result.put(albumDto, null);
            }
        }

        return (Map<D, LastfmAlbum>) result;
    }
}
