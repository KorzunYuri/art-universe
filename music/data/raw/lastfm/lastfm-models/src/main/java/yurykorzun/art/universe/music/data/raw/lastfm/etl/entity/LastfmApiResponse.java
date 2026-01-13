package yurykorzun.art.universe.music.data.raw.lastfm.etl.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import yurykorzun.art.universe.common.data.raw.etl.entity.ApiResponse;
import yurykorzun.art.universe.common.domain.converter.GzipBase64StringConverter;
import yurykorzun.art.universe.music.data.raw.lastfm.common.LastfmConstants;

import java.util.Objects;

@Entity(name = "api_response")
@SuperBuilder
@NoArgsConstructor
@Getter
public class LastfmApiResponse extends ApiResponse {

    @Id
    @SequenceGenerator(
            name = "api_response_seq_gen",
            sequenceName = "api_response_seq",
            allocationSize = LastfmConstants.HIBERNATE_BATCH_SIZE
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "api_response_seq")
    private long id;

    @NonNull
    @JoinColumn(name = "api_call_id")
    @ManyToOne(fetch = FetchType.EAGER)
    private LastfmApiCall apiCall;

    @NonNull
    @Convert(converter = GzipBase64StringConverter.class)
    @Column(name = "response_body")
    private String responseBody;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        LastfmApiResponse other = (LastfmApiResponse) o;
        if (this.getId() != 0 && other.getId() != 0) {
            return this.getId() == other.getId();
        } else {
            return Objects.equals(getApiCall(), other.getApiCall());
        }
    }

    @Override
    public int hashCode() {
        if (this.getId() != 0) {
            return Objects.hash(id);
        } else {
            return Objects.hash(getApiCall());
        }
    }

}
