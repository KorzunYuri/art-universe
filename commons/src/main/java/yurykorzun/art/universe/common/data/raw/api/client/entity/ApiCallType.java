package yurykorzun.art.universe.common.data.raw.api.client.entity;

import yurykorzun.art.universe.common.Coded;

import java.util.Collection;
import java.util.Map;

public interface ApiCallType extends Coded {
    String getMethod();
    String getPath();
    Map<String, String> getDefaultParamValues();
    Collection<String> getMandatoryParams();
    Collection<String> getOptionalParams();
}
