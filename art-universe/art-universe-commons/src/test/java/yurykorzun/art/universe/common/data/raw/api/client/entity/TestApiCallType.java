package yurykorzun.art.universe.common.data.raw.api.client.entity;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public enum TestApiCallType implements ApiCallType {
    TEST_TYPE;

    @Override
    public String getMethod() {
        return "";
    }

    @Override
    public String getPath() {
        return "";
    }

    @Override
    public Map<String, String> getDefaultParamValues() {
        return Map.of();
    }

    @Override
    public Collection<String> getMandatoryParams() {
        return List.of();
    }

    @Override
    public Collection<String> getOptionalParams() {
        return List.of();
    }

    @Override
    public Integer getCode() {
        return 1;
    }
}
