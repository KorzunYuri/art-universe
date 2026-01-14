package yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.utils.dedup;

import lombok.extern.slf4j.Slf4j;
import yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.album.AlbumDto;

import java.util.*;

@Slf4j
public class AlbumDeduplicationUtils {

    private AlbumDeduplicationUtils() {
        // Utility class
    }

    public static <D extends AlbumDto> List<D> deduplicateAlbumDtos(Collection<D> albumDtos) {
        if (albumDtos == null || albumDtos.isEmpty()) {
            return new ArrayList<>();
        }

        // First pass: deduplicate by artistName + name
        List<D> firstPassResult = deduplicateByArtistAndName(albumDtos);
        
        // Second pass: deduplicate by URL
        List<D> finalResult = deduplicateByUrl(firstPassResult);

        int originalCount = albumDtos.size();
        int finalCount = finalResult.size();
        if (originalCount > finalCount) {
            log.info("Deduplicated {} albums to {} (removed {} duplicates)", 
                originalCount, finalCount, originalCount - finalCount);
        }

        return finalResult;
    }

    private static <D extends AlbumDto> List<D> deduplicateByArtistAndName(Collection<D> albumDtos) {
        Map<String, D> deduplicatedAlbums = new LinkedHashMap<>();

        for (D albumDto : albumDtos) {
            String artistName = albumDto.getArtistName();
            String albumName = albumDto.getName();
            
            if (!isValidString(albumName) || !isValidString(artistName)) {
                log.warn("Skipping album DTO with null or empty name/artistName: albumName={}, artistName={}, dto={}", 
                    albumName, artistName, albumDto);
                continue;
            }

            String compositeKey = artistName + "|" + albumName;
            
            D existingAlbum = deduplicatedAlbums.get(compositeKey);

            if (existingAlbum == null) {
                deduplicatedAlbums.put(compositeKey, albumDto);
            } else {
                D betterAlbum = selectBetterAlbum(existingAlbum, albumDto);
                deduplicatedAlbums.put(compositeKey, betterAlbum);
            }
        }

        return new ArrayList<>(deduplicatedAlbums.values());
    }

    private static <D extends AlbumDto> List<D> deduplicateByUrl(Collection<D> albumDtos) {
        Map<String, D> deduplicatedAlbums = new LinkedHashMap<>();

        for (D albumDto : albumDtos) {
            String url = albumDto.getUrl();
            
            if (!isValidString(url)) {
                log.warn("Skipping album DTO with null or empty URL: {}", albumDto);
                continue;
            }

            D existingAlbum = deduplicatedAlbums.get(url);

            if (existingAlbum == null) {
                deduplicatedAlbums.put(url, albumDto);
            } else {
                D betterAlbum = selectBetterAlbum(existingAlbum, albumDto);
                deduplicatedAlbums.put(url, betterAlbum);
            }
        }

        return new ArrayList<>(deduplicatedAlbums.values());
    }

    private static <D extends AlbumDto> D selectBetterAlbum(D existing, D candidate) {
        int existingScore = calculateAlbumCompletenessScore(existing);
        int candidateScore = calculateAlbumCompletenessScore(candidate);

        if (candidateScore > existingScore) {
            return candidate;
        } else {
            return existing;
        }
    }

    private static <D extends AlbumDto> int calculateAlbumCompletenessScore(D albumDto) {
        int score = 0;

        if (isValidString(albumDto.getArtistName()))    score += 1000;
        if (isValidString(albumDto.getName()))          score += 100;
        if (isValidString(albumDto.getMbid()))          score += 10;
        if (isValidString(albumDto.getUrl()))           score += 1;

        return score;
    }

    private static boolean isValidString(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
