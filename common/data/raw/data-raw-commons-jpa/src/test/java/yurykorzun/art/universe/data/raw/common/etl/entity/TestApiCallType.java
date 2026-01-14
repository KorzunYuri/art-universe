package yurykorzun.art.universe.data.raw.common.etl.entity;

import yurykorzun.art.universe.data.raw.common.etl.entity.ApiCallType;

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

    @Override
    public String getName() {
        return name();
    }
}
