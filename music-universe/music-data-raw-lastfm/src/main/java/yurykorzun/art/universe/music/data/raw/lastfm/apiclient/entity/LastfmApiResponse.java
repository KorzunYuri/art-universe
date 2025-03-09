package yurykorzun.art.universe.music.data.raw.lastfm.apiclient.entity;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.Column;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.hibernate.type.SqlTypes;
import yurykorzun.art.universe.common.data.raw.apiclient.entity.ApiResponse;

import jakarta.persistence.Entity;

@Entity(name = "api_response")
@SuperBuilder
@NoArgsConstructor
@Getter
public class LastfmApiResponse extends ApiResponse {

    @NonNull
    @Type(JsonBinaryType.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "response_body", columnDefinition = "jsonb")
    private String responseBody;

}
