package yurykorzun.art.universe.music.data.raw.spotify.etl.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import yurykorzun.art.universe.data.raw.common.etl.entity.ApiCall;
import yurykorzun.art.universe.data.raw.common.etl.entity.ApiCallTypeConverter;
import yurykorzun.art.universe.music.data.raw.spotify.common.SpotifyConstants;
import yurykorzun.art.universe.music.data.raw.spotify.enums.SpotifyEntityType;
import yurykorzun.art.universe.music.data.raw.spotify.enums.SpotifyEntityTypeConverter;

@Entity(name = "api_call")
@SuperBuilder
@NoArgsConstructor
@Getter
public class SpotifyApiCall extends ApiCall {

    @Id
    @SequenceGenerator(name = "api_call_seq_gen", sequenceName = "api_call_seq", allocationSize = SpotifyConstants.HIBERNATE_BATCH_SIZE)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "api_call_seq_gen")
    private long id;

    @NonNull
    @Column(name = "type")
    @Convert(converter = ApiCallTypeConverter.class)
    private SpotifyApiCallType type;

    @Column(name = "spotify_id")
    private String spotifyId;

    @Column(name = "entity_type")
    @Convert(converter = SpotifyEntityTypeConverter.class)
    private SpotifyEntityType entityType;

    @Column(name = "entity_id")
    private Long entityId;

    @Setter
    @Column(name = "priority")
    private Short priority;

    @Setter
    @Column(name = "kafka_produced")
    private Boolean kafkaProduced;

    @Setter
    @Column(name = "kafka_topic")
    private String kafkaTopic;
}
