package yurykorzun.art.universe.common.data.raw.api.client.service;

import yurykorzun.art.universe.common.data.raw.api.client.entity.ApiCall;


public interface ApiClient {

    String makeApiCall(ApiCall callDetails);

}
