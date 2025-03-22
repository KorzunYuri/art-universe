package yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.hibernate.type.SqlTypes;
import yurykorzun.art.universe.common.data.raw.api.client.entity.ApiResponse;
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
    @Type(JsonBinaryType.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "response_body", columnDefinition = "jsonb")
    private String responseBody;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        LastfmApiResponse other = (LastfmApiResponse) o;
        if (Objects.equals(getApiCallType(), other.getApiCallType())) {
            if (this.getId() != 0 && other.getId() != 0) {
                return this.getId() == other.getId();
            } else {
                return Objects.equals(getApiCallId(), other.getApiCallId());
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        if (this.getId() != 0) {
            return Objects.hash(id, getApiCallType());
        } else {
            return Objects.hash(getApiCallId(), getApiCallType());
        }
    }

}
