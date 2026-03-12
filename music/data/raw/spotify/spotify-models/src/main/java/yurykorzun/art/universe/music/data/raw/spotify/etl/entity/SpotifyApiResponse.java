package yurykorzun.art.universe.music.data.raw.spotify.etl.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import yurykorzun.art.universe.common.persistence.converter.GzipBase64StringConverter;
import yurykorzun.art.universe.data.raw.common.etl.entity.ApiResponse;
import yurykorzun.art.universe.music.data.raw.spotify.common.SpotifyConstants;

@Entity(name = "api_response")
@SuperBuilder
@NoArgsConstructor
@Getter
public class SpotifyApiResponse extends ApiResponse {

    @Id
    @SequenceGenerator(name = "api_response_seq_gen", sequenceName = "api_response_seq", allocationSize = SpotifyConstants.HIBERNATE_BATCH_SIZE)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "api_response_seq_gen")
    private long id;

    @NonNull
    @JoinColumn(name = "api_call_id")
    @ManyToOne(fetch = FetchType.EAGER)
    private SpotifyApiCall apiCall;

    @NonNull
    @Convert(converter = GzipBase64StringConverter.class)
    @Column(name = "response_body")
    private String responseBody;
}
