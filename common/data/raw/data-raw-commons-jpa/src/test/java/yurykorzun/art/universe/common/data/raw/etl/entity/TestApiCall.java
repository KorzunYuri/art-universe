package yurykorzun.art.universe.common.data.raw.etl.entity;

import lombok.experimental.SuperBuilder;

@SuperBuilder
public class TestApiCall extends ApiCall {

    @Override
    public ApiCallType getType() {
        return TestApiCallType.TEST_TYPE;
    }
}
