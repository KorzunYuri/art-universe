package yurykorzun.art.universe.common.data.raw.apiclient.service;

import reactor.core.publisher.Mono;
import yurykorzun.art.universe.common.data.raw.apiclient.entity.ApiCall;


public interface ApiClient {

    Mono<String> makeApiCall(ApiCall callDetails);

}
