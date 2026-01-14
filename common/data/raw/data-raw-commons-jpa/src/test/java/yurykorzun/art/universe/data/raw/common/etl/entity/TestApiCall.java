package yurykorzun.art.universe.data.raw.common.etl.entity;

import lombok.experimental.SuperBuilder;

@SuperBuilder
public class TestApiCall extends ApiCall {

    @Override
    public ApiCallType getType() {
        return TestApiCallType.TEST_TYPE;
    }
}
