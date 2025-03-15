package yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity;

import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import yurykorzun.art.universe.common.data.raw.api.client.entity.ApiCall;

import jakarta.persistence.Entity;

@Entity(name = "api_call")
@SuperBuilder
@NoArgsConstructor
public class LastfmApiCall extends ApiCall {
}
