package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.utils;

import lombok.extern.slf4j.Slf4j;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.common.dto.ArtistDto;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.track.common.dto.TrackDto;

import java.util.*;

/**
 * Utility class for working with artist and track DTOs across different API methods.
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

        int originalCount = artistDtos.size();
        int deduplicatedCount = deduplicatedArtists.size();
        if (originalCount > deduplicatedCount) {
            log.info("Deduplicated {} artists to {} (removed {} duplicates)",
                originalCount, deduplicatedCount, originalCount - deduplicatedCount);
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
        if (isValidString(artistDto.getName())) score += 100;
        if (isValidString(artistDto.getMbid())) score += 10;
        if (isValidString(artistDto.getUrl()))  score += 1;

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

    /**
     * Deduplicates track DTOs by artistName + name combination, keeping the instance with the most complete data.
     * Priority for selecting the best instance: artistName > name > mbid > url (non-null values preferred).
     * 
     * @param trackDtos Collection of track DTOs to deduplicate
     * @param <D> Type of track DTO
     * @return List of deduplicated track DTOs with the most complete data for each unique artistName + name combination
     */
    public static <D extends TrackDto> List<D> deduplicateTrackDtos(Collection<D> trackDtos) {

        if (trackDtos == null || trackDtos.isEmpty()) {
            return new ArrayList<>();
        }

        Map<String, D> deduplicatedTracks = new LinkedHashMap<>();

        for (D trackDto : trackDtos) {
            String artistName = trackDto.getArtistName();
            String trackName = trackDto.getName();
            
            // Skip tracks without name or artist name (invalid data)
            if (!isValidString(trackName) || !isValidString(artistName)) {
                log.warn("Skipping track DTO with null or empty name/artistName: trackName={}, artistName={}, dto={}", 
                    trackName, artistName, trackDto);
                continue;
            }

            // Create composite key: artistName + trackName
            String compositeKey = artistName + "|" + trackName;
            
            D existingTrack = deduplicatedTracks.get(compositeKey);

            if (existingTrack == null) {
                // First occurrence of this artist + track combination
                deduplicatedTracks.put(compositeKey, trackDto);
            } else {
                // Choose the track with more complete data
                D betterTrack = selectBetterTrack(existingTrack, trackDto);
                deduplicatedTracks.put(compositeKey, betterTrack);
            }
        }

        int originalCount = trackDtos.size();
        int deduplicatedCount = deduplicatedTracks.size();
        if (originalCount > deduplicatedCount) {
            log.info("Deduplicated {} tracks to {} (removed {} duplicates)", 
                originalCount, deduplicatedCount, originalCount - deduplicatedCount);
        }

        return new ArrayList<>(deduplicatedTracks.values());
    }

    /**
     * Selects the track DTO with more complete data based on priority: artistName > name > mbid > url.
     * Non-null and non-empty values are preferred.
     * 
     * @param existing Currently stored track DTO
     * @param candidate New candidate track DTO
     * @param <D> Type of track DTO
     * @return The track DTO with more complete data
     */
    private static <D extends TrackDto> D selectBetterTrack(D existing, D candidate) {

        // Calculate completeness scores
        int existingScore = calculateTrackCompletenessScore(existing);
        int candidateScore = calculateTrackCompletenessScore(candidate);

        if (candidateScore > existingScore) {
            return candidate;
        } else {
            return existing;
        }
    }

    /**
     * Calculates completeness score for a track DTO based on field priority and presence.
     * Priority weights: artistName=1000, name=100, mbid=10, url=1
     * 
     * @param trackDto Track DTO to score
     * @param <D> Type of track DTO
     * @return Completeness score (higher = more complete)
     */
    private static <D extends TrackDto> int calculateTrackCompletenessScore(D trackDto) {
        int score = 0;

        if (isValidString(trackDto.getArtistName()))    score += 1000;
        if (isValidString(trackDto.getName()))          score += 100;
        if (isValidString(trackDto.getMbid()))          score += 10;
        if (isValidString(trackDto.getUrl()))           score += 1;

        return score;
    }
}
