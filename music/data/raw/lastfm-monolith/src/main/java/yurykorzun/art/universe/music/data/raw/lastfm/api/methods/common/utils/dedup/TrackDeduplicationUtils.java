package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.utils.dedup;

import lombok.extern.slf4j.Slf4j;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.track.common.dto.TrackDto;

import java.util.*;

@Slf4j
public class TrackDeduplicationUtils {

    private TrackDeduplicationUtils() {
        // Utility class
    }

    public static <D extends TrackDto> List<D> deduplicateTrackDtos(Collection<D> trackDtos) {
        if (trackDtos == null || trackDtos.isEmpty()) {
            return new ArrayList<>();
        }

        // First pass: deduplicate by artistName + name
        List<D> firstPassResult = deduplicateByArtistAndName(trackDtos);
        
        // Second pass: deduplicate by URL
        List<D> finalResult = deduplicateByUrl(firstPassResult);

        int originalCount = trackDtos.size();
        int finalCount = finalResult.size();
        if (originalCount > finalCount) {
            log.info("Deduplicated {} tracks to {} (removed {} duplicates)", 
                originalCount, finalCount, originalCount - finalCount);
        }

        return finalResult;
    }

    private static <D extends TrackDto> List<D> deduplicateByArtistAndName(Collection<D> trackDtos) {
        Map<String, D> deduplicatedTracks = new LinkedHashMap<>();

        for (D trackDto : trackDtos) {
            String artistName = trackDto.getArtistName();
            String trackName = trackDto.getName();
            
            if (!isValidString(trackName) || !isValidString(artistName)) {
                log.warn("Skipping track DTO with null or empty name/artistName: trackName={}, artistName={}, dto={}", 
                    trackName, artistName, trackDto);
                continue;
            }

            String compositeKey = artistName + "|" + trackName;
            
            D existingTrack = deduplicatedTracks.get(compositeKey);

            if (existingTrack == null) {
                deduplicatedTracks.put(compositeKey, trackDto);
            } else {
                D betterTrack = selectBetterTrack(existingTrack, trackDto);
                deduplicatedTracks.put(compositeKey, betterTrack);
            }
        }

        return new ArrayList<>(deduplicatedTracks.values());
    }

    private static <D extends TrackDto> List<D> deduplicateByUrl(Collection<D> trackDtos) {
        Map<String, D> deduplicatedTracks = new LinkedHashMap<>();

        for (D trackDto : trackDtos) {
            String url = trackDto.getUrl();
            
            if (!isValidString(url)) {
                log.warn("Skipping track DTO with null or empty URL: {}", trackDto);
                continue;
            }

            D existingTrack = deduplicatedTracks.get(url);

            if (existingTrack == null) {
                deduplicatedTracks.put(url, trackDto);
            } else {
                D betterTrack = selectBetterTrack(existingTrack, trackDto);
                deduplicatedTracks.put(url, betterTrack);
            }
        }

        return new ArrayList<>(deduplicatedTracks.values());
    }

    private static <D extends TrackDto> D selectBetterTrack(D existing, D candidate) {
        int existingScore = calculateTrackCompletenessScore(existing);
        int candidateScore = calculateTrackCompletenessScore(candidate);

        if (candidateScore > existingScore) {
            return candidate;
        } else {
            return existing;
        }
    }

    private static <D extends TrackDto> int calculateTrackCompletenessScore(D trackDto) {
        int score = 0;

        if (isValidString(trackDto.getArtistName()))    score += 1000;
        if (isValidString(trackDto.getName()))          score += 100;
        if (isValidString(trackDto.getMbid()))          score += 10;
        if (isValidString(trackDto.getUrl()))           score += 1;

        return score;
    }

    private static boolean isValidString(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
