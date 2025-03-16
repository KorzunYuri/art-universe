package yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity;

import lombok.Getter;
import yurykorzun.art.universe.common.CodedRegistry;
import yurykorzun.art.universe.common.data.raw.api.client.entity.ApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.common.dto.RootDto;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.toptags.dto.TopTagsDtoRoot;
import yurykorzun.art.universe.music.data.raw.lastfm.common.LastfmSpecific;

import java.util.*;

@Getter
public enum LastfmApiCallType implements ApiCallType, LastfmSpecific {

        TAG_TOP_TAGS(
            1,
            "tag.getTopTags",
            Set.of("api_key"),
            Collections.emptySet(),
            TopTagsDtoRoot.class)
    ;

    static {
        CodedRegistry.register(Arrays.asList(values()), ApiCallType.class);
    }

    private final int code;
    private final String method;
    private final Map<String, String> defaultParameterValues = new HashMap<>();
    private final Collection<String> mandatoryParameters;
    private final Collection<String> optionalParameters;
    private final Class<? extends RootDto> responseDtoClass;

    LastfmApiCallType(
            int code,
            String method,
            Collection<String> mandatoryParameters,
            Collection<String> optionalParameters, Class<? extends RootDto> responseDtoClass
    ) {
        this.code = code;
        this.method = method;
        this.optionalParameters = optionalParameters;
        this.mandatoryParameters = Set.copyOf(mandatoryParameters);
        this.responseDtoClass = responseDtoClass;
        this.defaultParameterValues.put("method", method);
        this.defaultParameterValues.put("format", "json");
    }

    @Override
    public String getPath() {
        return "";
    }

    @Override
    public Map<String, String> getDefaultParamValues() {
        return Collections.unmodifiableMap(this.defaultParameterValues);
    }

    @Override
    public Collection<String> getMandatoryParams() {
        return Set.copyOf(this.mandatoryParameters);
    }

    @Override
    public Collection<String> getOptionalParams() {
        return Set.copyOf(this.optionalParameters);
    }

    @Override
    public String getTypeName() {
        return "api_call";
    }

    @Override
    public Integer getCode() {
        return this.code;
    }
}
