package yurykorzun.art.universe.common.data.raw.apiclient.messaging;

import yurykorzun.art.universe.common.data.raw.apiclient.dto.ApiCallRunRequest;

public interface ApiCallMessageProducer {

    void send(ApiCallRunRequest message);

}
