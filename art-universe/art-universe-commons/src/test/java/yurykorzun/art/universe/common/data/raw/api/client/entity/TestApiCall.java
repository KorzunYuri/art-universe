package yurykorzun.art.universe.common.data.raw.api.client.entity;

import lombok.experimental.SuperBuilder;

@SuperBuilder
public class TestApiCall extends ApiCall{

    @Override
    public ApiCallType getType() {
        return TestApiCallType.TEST_TYPE;
    }
}
