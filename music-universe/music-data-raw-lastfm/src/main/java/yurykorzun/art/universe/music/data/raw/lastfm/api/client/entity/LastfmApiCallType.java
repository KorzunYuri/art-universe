package yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity;

import lombok.Getter;
import yurykorzun.art.universe.common.CodedRegistry;
import yurykorzun.art.universe.common.data.raw.api.client.entity.ApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.common.dto.RootDto;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.topartists.dto.TopArtistsDtoRoot;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.toptags.dto.TopTagsDtoRoot;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.BaseLastfmEntity;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.entity.LastfmTag;
import yurykorzun.art.universe.music.data.raw.lastfm.common.LastfmSpecific;

import javax.annotation.Nullable;
import java.util.*;

import static yurykorzun.art.universe.music.data.raw.lastfm.api.LastfmApiConstants.*;

@Getter
public enum LastfmApiCallType implements ApiCallType, LastfmSpecific {

        TAG_TOP_TAGS(
            1,
            "tag.getTopTags",
            Set.of(PARAM_NAME_API_KEY),
            Set.of(PARAM_NAME_OFFSET),
            TopTagsDtoRoot.class,
            null)
    ,   TAG_TOP_ARTISTS(
            2,
            "tag.getTopArtists",
            Set.of(PARAM_NAME_API_KEY, PARAM_NAME_TAG),
            Set.of(PARAM_NAME_LIMIT, PARAM_NAME_PAGE),
            TopArtistsDtoRoot.class,
            LastfmTag.class)
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
    @Nullable
    private final Class<? extends BaseLastfmEntity> scopeEntityType;

    LastfmApiCallType(
            int code,
            String method,
            Collection<String> mandatoryParameters,
            Collection<String> optionalParameters,
            Class<? extends RootDto> responseDtoClass,
            Class<? extends BaseLastfmEntity> scopeEntityType
    ) {
        this.code = code;
        this.method = method;
        this.optionalParameters = optionalParameters;
        this.mandatoryParameters = Set.copyOf(mandatoryParameters);
        this.responseDtoClass = responseDtoClass;
        this.scopeEntityType = scopeEntityType;

        this.defaultParameterValues.put(PARAM_NAME_METHOD, method);
        this.defaultParameterValues.put(PARAM_NAME_FORMAT, PARAM_DEFAULT_FORMAT);
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
