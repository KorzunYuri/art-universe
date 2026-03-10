package yurykorzun.art.universe.music.data.raw.spotify.etl.entity;

import jakarta.persistence.*;
import lombok.*;
import yurykorzun.art.universe.music.data.raw.spotify.enums.SpotifyEntityType;
import yurykorzun.art.universe.music.data.raw.spotify.enums.SpotifyEntityTypeConverter;

import java.time.Instant;

@Entity(name = "search_attempt")
@Table(name = "search_attempt")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class SpotifySearchAttempt {

    @Id
    @SequenceGenerator(name = "search_attempt_seq_gen", sequenceName = "search_attempt_seq", allocationSize = 50)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "search_attempt_seq_gen")
    @Setter(AccessLevel.NONE)
    private long id;

    @Column(name = "entity_type", nullable = false)
    @Convert(converter = SpotifyEntityTypeConverter.class)
    private SpotifyEntityType entityType;

    @Column(name = "master_entity_id", nullable = false)
    private long masterEntityId;

    @Column(name = "search_string", nullable = false, length = 1024)
    private String searchString;

    @Column(name = "status", nullable = false)
    @Convert(converter = SearchAttemptStatusConverter.class)
    @Builder.Default
    private SearchAttemptStatus status = SearchAttemptStatus.PENDING;

    @Column(name = "best_match_score")
    private Double bestMatchScore;

    @Column(name = "matched_spotify_id", length = 64)
    private String matchedSpotifyId;

    @Column(name = "api_call_id")
    private Long apiCallId;

    @Column(name = "searched_at")
    private Instant searchedAt;

    @Column(name = "next_retry_after")
    private Instant nextRetryAfter;

    @Column(name = "created_at", updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    @Builder.Default
    private Instant updatedAt = Instant.now();
}
