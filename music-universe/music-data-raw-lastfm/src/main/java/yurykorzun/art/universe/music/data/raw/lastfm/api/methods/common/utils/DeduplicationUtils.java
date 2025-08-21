package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.utils;

import lombok.extern.slf4j.Slf4j;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.common.dto.ArtistDto;

import java.util.*;

/**
 * Utility class for working with artist DTOs across different API methods.
 */
@Slf4j
public class DeduplicationUtils {

    private DeduplicationUtils() {
        // Utility class
    }

    /**
     * Deduplicates artist DTOs by name, keeping the instance with the most complete data.
     * Priority for selecting the best instance: name > mbid > url (non-null values preferred).
     * 
     * @param artistDtos Collection of artist DTOs to deduplicate
     * @param <D> Type of artist DTO
     * @return List of deduplicated artist DTOs with the most complete data for each unique name
     */
    public static <D extends ArtistDto> List<D> deduplicateArtistDtos(Collection<D> artistDtos) {

        if (artistDtos == null || artistDtos.isEmpty()) {
            return new ArrayList<>();
        }

        Map<String, D> deduplicatedArtists = new LinkedHashMap<>();

        for (D artistDto : artistDtos) {
            String artistName = artistDto.getName();
            
            // Skip artists without name (invalid data)
            if (artistName == null || artistName.trim().isEmpty()) {
                log.warn("Skipping artist DTO with null or empty name: {}", artistDto);
                continue;
            }

            D existingArtist = deduplicatedArtists.get(artistName);

            if (existingArtist == null) {
                // First occurrence of this artist name
                deduplicatedArtists.put(artistName, artistDto);
            } else {
                // Choose the artist with more complete data
                D betterArtist = selectBetterArtist(existingArtist, artistDto);
                deduplicatedArtists.put(artistName, betterArtist);
            }
        }

        return new ArrayList<>(deduplicatedArtists.values());
    }

    /**
     * Selects the artist DTO with more complete data based on priority: name > mbid > url.
     * Non-null and non-empty values are preferred.
     * 
     * @param existing Currently stored artist DTO
     * @param candidate New candidate artist DTO
     * @param <D> Type of artist DTO
     * @return The artist DTO with more complete data
     */
    private static <D extends ArtistDto> D selectBetterArtist(D existing, D candidate) {

        // Calculate completeness scores
        int existingScore = calculateCompletenessScore(existing);
        int candidateScore = calculateCompletenessScore(candidate);

        if (candidateScore > existingScore) {
            return candidate;
        } else {
            return existing;
        }
    }

    /**
     * Calculates completeness score for an artist DTO based on field priority and presence.
     * Priority weights: name=100, mbid=10, url=1
     * 
     * @param artistDto Artist DTO to score
     * @param <D> Type of artist DTO
     * @return Completeness score (higher = more complete)
     */
    private static <D extends ArtistDto> int calculateCompletenessScore(D artistDto) {

        int score = 0;

        // Name (highest priority - weight 100)
        String name = artistDto.getName();
        if (isValidString(name)) {
            score += 100;
        }

        // MBID (medium priority - weight 10)
        String mbid = artistDto.getMbid();
        if (isValidString(mbid)) {
            score += 10;
        }

        // URL (lowest priority - weight 1)
        String url = artistDto.getUrl();
        if (isValidString(url)) {
            score += 1;
        }

        return score;
    }

    /**
     * Checks if a string is valid (not null and not empty after trimming).
     * 
     * @param value String to check
     * @return true if string is valid, false otherwise
     */
    private static boolean isValidString(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
