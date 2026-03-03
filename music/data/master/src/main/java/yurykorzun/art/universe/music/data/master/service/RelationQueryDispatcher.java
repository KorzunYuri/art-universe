package yurykorzun.art.universe.music.data.master.service;

import jakarta.annotation.Nullable;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.common.domain.entity.EntityType;
import yurykorzun.art.universe.music.data.master.dto.relation.RelatedEntityProjection;
import yurykorzun.art.universe.common.domain.entity.MasterEntityType;
import yurykorzun.art.universe.music.data.master.repository.AlbumAlbumRepository;
import yurykorzun.art.universe.music.data.master.repository.AlbumCategoryRepository;
import yurykorzun.art.universe.music.data.master.repository.AlbumPersonRepository;
import yurykorzun.art.universe.music.data.master.repository.AlbumTrackRepository;
import yurykorzun.art.universe.music.data.master.repository.ArtistAlbumRepository;
import yurykorzun.art.universe.music.data.master.repository.ArtistArtistRepository;
import yurykorzun.art.universe.music.data.master.repository.ArtistCategoryRepository;
import yurykorzun.art.universe.music.data.master.repository.ArtistPersonRepository;
import yurykorzun.art.universe.music.data.master.repository.ArtistTrackRepository;
import yurykorzun.art.universe.music.data.master.repository.TrackCategoryRepository;
import yurykorzun.art.universe.music.data.master.repository.TrackPersonRepository;
import yurykorzun.art.universe.music.data.master.repository.TrackTrackRepository;

import java.util.List;

/**
 * Dispatches relation queries to the correct typed repository
 * based on source and target entity types.
 */
@Component
public class RelationQueryDispatcher {

    private final ArtistTrackRepository artistTrackRepository;
    private final ArtistAlbumRepository artistAlbumRepository;
    private final AlbumTrackRepository albumTrackRepository;
    private final ArtistArtistRepository artistArtistRepository;
    private final AlbumAlbumRepository albumAlbumRepository;
    private final TrackTrackRepository trackTrackRepository;
    private final ArtistCategoryRepository artistCategoryRepository;
    private final AlbumCategoryRepository albumCategoryRepository;
    private final TrackCategoryRepository trackCategoryRepository;
    private final ArtistPersonRepository artistPersonRepository;
    private final AlbumPersonRepository albumPersonRepository;
    private final TrackPersonRepository trackPersonRepository;

    public RelationQueryDispatcher(
            ArtistTrackRepository artistTrackRepository,
            ArtistAlbumRepository artistAlbumRepository,
            AlbumTrackRepository albumTrackRepository,
            ArtistArtistRepository artistArtistRepository,
            AlbumAlbumRepository albumAlbumRepository,
            TrackTrackRepository trackTrackRepository,
            ArtistCategoryRepository artistCategoryRepository,
            AlbumCategoryRepository albumCategoryRepository,
            TrackCategoryRepository trackCategoryRepository,
            ArtistPersonRepository artistPersonRepository,
            AlbumPersonRepository albumPersonRepository,
            TrackPersonRepository trackPersonRepository
    ) {
        this.artistTrackRepository = artistTrackRepository;
        this.artistAlbumRepository = artistAlbumRepository;
        this.albumTrackRepository = albumTrackRepository;
        this.artistArtistRepository = artistArtistRepository;
        this.albumAlbumRepository = albumAlbumRepository;
        this.trackTrackRepository = trackTrackRepository;
        this.artistCategoryRepository = artistCategoryRepository;
        this.albumCategoryRepository = albumCategoryRepository;
        this.trackCategoryRepository = trackCategoryRepository;
        this.artistPersonRepository = artistPersonRepository;
        this.albumPersonRepository = albumPersonRepository;
        this.trackPersonRepository = trackPersonRepository;
    }

    /**
     * Finds related entities using typed repository queries.
     * Automatically selects the correct repository and query direction
     * (forward vs reverse) based on source and target entity types.
     */
    public List<RelatedEntityProjection> findRelatedEntities(
            EntityType sourceType,
            Long sourceId,
            EntityType targetType,
            @Nullable Long relationTypeId
    ) {
        if (sourceType == targetType && sourceType instanceof MasterEntityType masterType) {
            return findSameEntityRelations(masterType, sourceId, relationTypeId);
        }
        return findCrossEntityRelations(sourceType, sourceId, targetType, relationTypeId);
    }

    private List<RelatedEntityProjection> findSameEntityRelations(
            MasterEntityType entityType, Long entityId, @Nullable Long relationTypeId) {
        return switch (entityType) {
            case ARTIST -> artistArtistRepository.findRelatedArtists(entityId, relationTypeId);
            case ALBUM -> albumAlbumRepository.findRelatedAlbums(entityId, relationTypeId);
            case TRACK -> trackTrackRepository.findRelatedTracks(entityId, relationTypeId);
            default -> throw new IllegalArgumentException(
                    "Same-entity relations not supported for type: " + entityType.getName());
        };
    }

    private List<RelatedEntityProjection> findCrossEntityRelations(
            EntityType sourceType, Long sourceId,
            EntityType targetType, @Nullable Long relationTypeId) {
        String key = sourceType.getName() + "_" + targetType.getName();
        return switch (key) {
            case "artist_track" -> artistTrackRepository.findRelatedTracksByArtistId(sourceId, relationTypeId);
            case "track_artist" -> artistTrackRepository.findRelatedArtistsByTrackId(sourceId, relationTypeId);
            case "artist_album" -> artistAlbumRepository.findRelatedAlbumsByArtistId(sourceId, relationTypeId);
            case "album_artist" -> artistAlbumRepository.findRelatedArtistsByAlbumId(sourceId, relationTypeId);
            case "album_track" -> albumTrackRepository.findRelatedTracksByAlbumId(sourceId, relationTypeId);
            case "track_album" -> albumTrackRepository.findRelatedAlbumsByTrackId(sourceId, relationTypeId);
            case "artist_category" -> artistCategoryRepository.findRelatedCategoriesByArtistId(sourceId);
            case "album_category" -> albumCategoryRepository.findRelatedCategoriesByAlbumId(sourceId);
            case "track_category" -> trackCategoryRepository.findRelatedCategoriesByTrackId(sourceId);
            case "artist_person" -> artistPersonRepository.findRelatedPersonsByArtistId(sourceId, relationTypeId);
            case "person_artist" -> artistPersonRepository.findRelatedArtistsByPersonId(sourceId, relationTypeId);
            case "album_person" -> albumPersonRepository.findRelatedPersonsByAlbumId(sourceId, relationTypeId);
            case "person_album" -> albumPersonRepository.findRelatedAlbumsByPersonId(sourceId, relationTypeId);
            case "track_person" -> trackPersonRepository.findRelatedPersonsByTrackId(sourceId, relationTypeId);
            case "person_track" -> trackPersonRepository.findRelatedTracksByPersonId(sourceId, relationTypeId);
            default -> throw new IllegalArgumentException(
                    String.format("Cross-entity relations not supported for %s -> %s",
                            sourceType.getName(), targetType.getName()));
        };
    }
}
