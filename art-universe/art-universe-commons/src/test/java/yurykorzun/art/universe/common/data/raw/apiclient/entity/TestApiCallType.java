package yurykorzun.art.universe.common.data.raw.apiclient.entity;

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
    public String getCode() {
        return "";
    }

    @Override
    public String getDataSourceCode() {
        return "";
    }

    @Override
    public String getDomainCode() {
        return "";
    }

    @Override
    public String getTypeName() {
        return "";
    }
}
