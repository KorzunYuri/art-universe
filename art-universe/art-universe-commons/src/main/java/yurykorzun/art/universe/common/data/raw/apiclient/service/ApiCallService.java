package yurykorzun.art.universe.common.data.raw.apiclient.service;

import yurykorzun.art.universe.common.data.raw.apiclient.dto.ApiCallCreateRequest;
import yurykorzun.art.universe.common.data.raw.apiclient.dto.ApiCallCreateResponse;

public interface ApiCallService {

    ApiCallCreateResponse create(ApiCallCreateRequest request);

}
