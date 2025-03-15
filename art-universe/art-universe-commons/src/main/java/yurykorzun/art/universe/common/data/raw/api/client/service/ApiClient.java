package yurykorzun.art.universe.common.data.raw.api.client.service;

import reactor.core.publisher.Mono;
import yurykorzun.art.universe.common.data.raw.api.client.entity.ApiCall;


public interface ApiClient {

    Mono<String> makeApiCall(ApiCall callDetails);

}
