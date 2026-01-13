package yurykorzun.art.universe.common.data.raw.integration;

import yurykorzun.art.universe.common.data.raw.etl.entity.ApiCall;


public interface ApiClient {

    String makeApiCall(ApiCall callDetails);

}
