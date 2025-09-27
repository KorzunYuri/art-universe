package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.utils.dedup;

import lombok.extern.slf4j.Slf4j;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.common.dto.ArtistDto;

import java.util.*;

@Slf4j
public class ArtistDeduplicationUtils {

    private ArtistDeduplicationUtils() {
        // Utility class
    }

    public static <D extends ArtistDto> List<D> deduplicateArtistDtos(Collection<D> artistDtos) {
        if (artistDtos == null || artistDtos.isEmpty()) {
            return new ArrayList<>();
        }

        Map<String, D> deduplicatedArtists = new LinkedHashMap<>();

        for (D artistDto : artistDtos) {
            String artistName = artistDto.getName();
            
            if (artistName == null || artistName.trim().isEmpty()) {
                log.warn("Skipping artist DTO with null or empty name: {}", artistDto);
                continue;
            }

            D existingArtist = deduplicatedArtists.get(artistName);

            if (existingArtist == null) {
                deduplicatedArtists.put(artistName, artistDto);
            } else {
                D betterArtist = selectBetterArtist(existingArtist, artistDto);
                deduplicatedArtists.put(artistName, betterArtist);
            }
        }

        int originalCount = artistDtos.size();
        int deduplicatedCount = deduplicatedArtists.size();
        if (originalCount > deduplicatedCount) {
            log.info("Deduplicated {} artists to {} (removed {} duplicates)",
                originalCount, deduplicatedCount, originalCount - deduplicatedCount);
        }

        return new ArrayList<>(deduplicatedArtists.values());
    }

    private static <D extends ArtistDto> D selectBetterArtist(D existing, D candidate) {
        int existingScore = calculateCompletenessScore(existing);
        int candidateScore = calculateCompletenessScore(candidate);

        if (candidateScore > existingScore) {
            return candidate;
        } else {
            return existing;
        }
    }

    private static <D extends ArtistDto> int calculateCompletenessScore(D artistDto) {
        int score = 0;

        if (isValidString(artistDto.getName())) score += 100;
        if (isValidString(artistDto.getMbid())) score += 10;
        if (isValidString(artistDto.getUrl()))  score += 1;

        return score;
    }

    private static boolean isValidString(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
