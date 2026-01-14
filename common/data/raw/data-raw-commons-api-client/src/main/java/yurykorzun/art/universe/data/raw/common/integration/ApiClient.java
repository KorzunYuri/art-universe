package yurykorzun.art.universe.data.raw.common.integration;

import yurykorzun.art.universe.data.raw.common.etl.entity.ApiCall;


public interface ApiClient {

    String makeApiCall(ApiCall callDetails);

}
